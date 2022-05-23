package com.braze.ui.inappmessage.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.braze.Braze
import com.braze.coroutine.BrazeCoroutineScope
import com.braze.enums.BrazeViewBounds
import com.braze.enums.inappmessage.InAppMessageFailureType
import com.braze.enums.inappmessage.MessageType
import com.braze.images.IBrazeImageLoader
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageWithImage
import com.braze.models.inappmessage.IInAppMessageZippedAssetHtml
import com.braze.models.inappmessage.InAppMessageFull
import com.braze.models.inappmessage.InAppMessageHtml
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.WebContentUtils.getHtmlInAppMessageAssetCacheDirectory
import com.braze.support.WebContentUtils.getLocalHtmlUrlFromRemoteUrl
import com.braze.support.WebContentUtils.replacePrefetchedUrlsWithLocalAssets
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object BackgroundInAppMessagePreparer {
    /**
     * @param inAppMessageToPrepare The message to prepare for eventual display.
     */
    @JvmStatic
    fun prepareInAppMessageForDisplay(inAppMessageToPrepare: IInAppMessage) {
        BrazeCoroutineScope.launch {
            try {
                val preparedInAppMessage = prepareInAppMessage(inAppMessageToPrepare)
                if (preparedInAppMessage == null) {
                    brazelog(W) {
                        "Cannot display the in-app message because the in-app message was null."
                    }
                } else {
                    displayPreparedInAppMessage(preparedInAppMessage)
                }
            } catch (e: Exception) {
                brazelog(E, e) { "Caught error while preparing in app message in background" }
            }
        }
    }

    private fun prepareInAppMessage(inAppMessageToPrepare: IInAppMessage): IInAppMessage? {
        if (inAppMessageToPrepare.isControl) {
            brazelog { "Skipping in-app message preparation for control in-app message." }
            return inAppMessageToPrepare
        }
        brazelog { "Starting asynchronous in-app message preparation for message." }
        when (inAppMessageToPrepare.messageType) {
            MessageType.HTML_FULL -> if (!prepareInAppMessageWithZippedAssetHtml(
                    inAppMessageToPrepare as IInAppMessageZippedAssetHtml
                )
            ) {
                brazelog(W) { "Logging html in-app message zip asset download failure" }
                inAppMessageToPrepare.logDisplayFailure(InAppMessageFailureType.ZIP_ASSET_DOWNLOAD)
                return null
            }
            MessageType.HTML -> prepareInAppMessageWithHtml(inAppMessageToPrepare as InAppMessageHtml)
            else -> {
                val didImageDownloadSucceed =
                    prepareInAppMessageWithBitmapDownload(inAppMessageToPrepare)
                if (!didImageDownloadSucceed) {
                    brazelog(W) { "Logging in-app message image download failure" }
                    inAppMessageToPrepare.logDisplayFailure(InAppMessageFailureType.IMAGE_DOWNLOAD)
                    return null
                }
            }
        }
        return inAppMessageToPrepare
    }

    /**
     * Prepares the in-app message for displaying Html content.
     *
     * @param inAppMessageHtml the in-app message to be prepared
     * @return The success of the asset download.
     */
    @JvmStatic
    @VisibleForTesting
    fun prepareInAppMessageWithZippedAssetHtml(inAppMessageHtml: IInAppMessageZippedAssetHtml): Boolean {
        // If the local assets exist already, return right away.
        val localAssets = inAppMessageHtml.localAssetsDirectoryUrl
        if (!localAssets.isNullOrBlank() && File(localAssets).exists()) {
            brazelog(I) {
                "Local assets for html in-app message are already populated. " +
                    "Not downloading assets. Location = $localAssets"
            }
            return true
        }

        val assetsZipRemoteUrl = inAppMessageHtml.assetsZipRemoteUrl
        // Otherwise, return if no remote asset zip location is specified.
        if (assetsZipRemoteUrl.isNullOrBlank()) {
            brazelog(I) {
                "Html in-app message has no remote asset zip. " +
                    "Continuing with in-app message preparation."
            }
            return true
        }
        // Otherwise, download the asset zip.
        val applicationContext = BrazeInAppMessageManager.getInstance().applicationContext
        if (applicationContext == null) {
            brazelog(W) { "BrazeInAppMessageManager applicationContext is null. Not downloading image." }
            return false
        }

        val internalStorageCacheDirectory =
            getHtmlInAppMessageAssetCacheDirectory(applicationContext)
        val localWebContentUrl = getLocalHtmlUrlFromRemoteUrl(
            internalStorageCacheDirectory,
            assetsZipRemoteUrl
        )
        return if (!localWebContentUrl.isNullOrBlank()) {
            brazelog { "Local url for html in-app message assets is $localWebContentUrl" }
            inAppMessageHtml.localAssetsDirectoryUrl = localWebContentUrl
            true
        } else {
            brazelog(W) {
                "Download of html content to local directory failed for remote url: " +
                    "${inAppMessageHtml.assetsZipRemoteUrl} . Returned local url is: $localWebContentUrl"
            }
            false
        }
    }

    /**
     * Prepares the in-app message for displaying images using a bitmap downloader. The in-app
     * message must have a valid image url.
     *
     * @param inAppMessage the in-app message to be prepared
     * @return whether or not the asset download succeeded
     */
    @JvmStatic
    @VisibleForTesting
    @Suppress("ReturnCount")
    fun prepareInAppMessageWithBitmapDownload(inAppMessage: IInAppMessage?): Boolean {
        if (inAppMessage !is IInAppMessageWithImage) {
            brazelog { "Cannot prepare non IInAppMessageWithImage object with bitmap download." }
            return false
        }
        val inAppMessageWithImage = inAppMessage as IInAppMessageWithImage
        if (inAppMessageWithImage.bitmap != null) {
            brazelog(I) { "In-app message already contains image bitmap. Not downloading image from URL." }
            inAppMessageWithImage.imageDownloadSuccessful = true
            return true
        }
        val viewBounds = getViewBoundsByType(inAppMessage)
        val applicationContext = BrazeInAppMessageManager.getInstance().applicationContext
        if (applicationContext == null) {
            brazelog(W) { "BrazeInAppMessageManager applicationContext is null. Not downloading image." }
            return false
        }

        val imageLoader = Braze.getInstance(applicationContext).imageLoader
        // Try to load the local url first
        val localImageUrl = inAppMessageWithImage.localImageUrl
        if (!localImageUrl.isNullOrBlank()) {
            if (handleLocalImage(
                    localImageUrl,
                    inAppMessageWithImage,
                    imageLoader,
                    applicationContext,
                    inAppMessage,
                    viewBounds
                )
            ) return true
        }

        // Try to load the remote url next
        val remoteImageUrl = inAppMessageWithImage.remoteImageUrl
        if (!remoteImageUrl.isNullOrBlank()) {
            brazelog(I) { "In-app message has remote image url. Downloading image at url: $remoteImageUrl" }
            inAppMessageWithImage.bitmap = imageLoader.getInAppMessageBitmapFromUrl(
                applicationContext,
                inAppMessage,
                remoteImageUrl,
                viewBounds
            )
        } else {
            brazelog(W) { "In-app message has no remote image url. Not downloading image." }
            if (inAppMessageWithImage is InAppMessageFull) {
                brazelog(W) {
                    "In-app message full has no remote image url yet is required to have an image. " +
                        "Failing message display."
                }
                return false
            }
            return true
        }
        if (inAppMessageWithImage.bitmap != null) {
            inAppMessageWithImage.imageDownloadSuccessful = true
            return true
        }
        return false
    }

    /**
     * Grab the image from a local URI and set the bitmap field of the in-app message.
     *
     * @return Boolean if the bitmap was successfully loaded.
     */
    @Suppress("LongParameterList")
    private fun handleLocalImage(
        localImageUrl: String,
        inAppMessageWithImage: IInAppMessageWithImage,
        imageLoader: IBrazeImageLoader,
        applicationContext: Context,
        inAppMessage: IInAppMessage,
        viewBounds: BrazeViewBounds
    ): Boolean {
        brazelog(I) { "Passing in-app message local image url to image loader: $localImageUrl" }
        inAppMessageWithImage.bitmap = imageLoader.getInAppMessageBitmapFromUrl(
            applicationContext,
            inAppMessage,
            localImageUrl,
            viewBounds
        )
        if (inAppMessageWithImage.bitmap != null) {
            // Got the image, we're done
            inAppMessageWithImage.imageDownloadSuccessful = true
            return true
        }
        // The local uri didn't work, unset it from the IAM
        brazelog {
            "Removing local image url from IAM since it could not be loaded. URL: $localImageUrl"
        }
        inAppMessageWithImage.localImageUrl = null
        return false
    }

    /**
     * Prepares the in-app message for display by substituting locally
     * cached assets into the message of the [InAppMessageHtml]
     */
    @VisibleForTesting
    fun prepareInAppMessageWithHtml(inAppMessage: InAppMessageHtml) {
        if (inAppMessage.getLocalPrefetchedAssetPaths().isEmpty()) {
            brazelog {
                "HTML in-app message does not have prefetched assets. " +
                    "Not performing any substitutions."
            }
            return
        }
        val message = inAppMessage.message
        if (message == null) {
            brazelog {
                "HTML in-app message does not have message. Not performing any substitutions."
            }
            return
        }
        val transformedHtml = replacePrefetchedUrlsWithLocalAssets(
            message,
            inAppMessage.getLocalPrefetchedAssetPaths()
        )
        inAppMessage.message = transformedHtml
    }

    private fun getViewBoundsByType(inAppMessage: IInAppMessage): BrazeViewBounds {
        return when (inAppMessage.messageType) {
            MessageType.SLIDEUP -> BrazeViewBounds.IN_APP_MESSAGE_SLIDEUP
            MessageType.MODAL -> BrazeViewBounds.IN_APP_MESSAGE_MODAL
            else -> BrazeViewBounds.NO_BOUNDS
        }
    }

    private suspend fun displayPreparedInAppMessage(inAppMessage: IInAppMessage) {
        withContext(Dispatchers.Main) {
            this@BackgroundInAppMessagePreparer.brazelog { "Displaying in-app message." }
            BrazeInAppMessageManager.getInstance().displayInAppMessage(inAppMessage, false)
        }
    }
}
