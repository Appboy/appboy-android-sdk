package com.braze.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import com.braze.Constants
import com.appboy.models.push.BrazeNotificationPayload
import com.appboy.models.push.BrazeNotificationPayload.PushStoryPage
import com.appboy.ui.R
import com.braze.Braze
import com.braze.IBrazeDeeplinkHandler.IntentFlagPurpose
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.enums.BrazeDateFormat
import com.braze.enums.BrazeViewBounds
import com.braze.push.support.getHtmlSpannedTextIfEnabled
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.IntentUtils.getImmutablePendingIntentFlags
import com.braze.support.IntentUtils.getRequestCode
import com.braze.support.formatDateNow
import com.braze.support.getDensityDpi
import com.braze.support.getDisplayWidthPixels
import com.braze.support.getPixelsFromDensityAndDp
import com.braze.ui.BrazeDeeplinkHandler.Companion.getInstance

open class BrazeNotificationStyleFactory {
    /**
     * A sentinel value used solely to denote that a style
     * should not be set on the notification builder.
     *
     * Example usage would be a fully custom [RemoteViews]
     * that has handled rendering notification view without the
     * use of a system style. Returning null in that scenario
     * would lead to a lack of information as to whether that
     * custom rendering failed.
     */
    private class NoOpSentinelStyle : NotificationCompat.Style()

    companion object {
        /**
         * BigPictureHeight is set in notification_template_big_picture.xml.
         */
        private const val BIG_PICTURE_STYLE_IMAGE_HEIGHT = 192
        private const val STORY_SET_GRAVITY = "setGravity"
        private const val STORY_SET_VISIBILITY = "setVisibility"

        /**
         * Sets the style of the notification if supported.
         *
         * If there is an image url found in the extras payload and the image can be downloaded, then
         * use the android BigPictureStyle as the notification. Else, use the BigTextStyle instead.
         */
        @JvmStatic
        fun setStyleIfSupported(
            notificationBuilder: NotificationCompat.Builder,
            payload: BrazeNotificationPayload
        ) {
            val style = getNotificationStyle(notificationBuilder, payload)
            if (style !is NoOpSentinelStyle) {
                brazelog { "Setting style for notification" }
                notificationBuilder.setStyle(style)
            }
        }

        /**
         * Returns a big style [NotificationCompat.Style]. If an image is present, this will be a [NotificationCompat.BigPictureStyle],
         * otherwise it will be a [NotificationCompat.BigTextStyle].
         */
        fun getNotificationStyle(
            notificationBuilder: NotificationCompat.Builder,
            payload: BrazeNotificationPayload
        ): NotificationCompat.Style {
            var style: NotificationCompat.Style? = null
            if (payload.isPushStory && payload.context != null) {
                brazelog { "Rendering push notification with DecoratedCustomViewStyle (Story)" }
                style = getStoryStyle(notificationBuilder, payload)
            } else if (payload.isConversationalPush && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                brazelog { "Rendering conversational push" }
                style = getConversationalPushStyle(notificationBuilder, payload)
            } else if (payload.bigImageUrl != null) {
                style =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && payload.isInlineImagePush) {
                        brazelog { "Rendering push notification with custom inline image style" }
                        getInlineImageStyle(payload, notificationBuilder)
                    } else {
                        brazelog { "Rendering push notification with BigPictureStyle" }
                        getBigPictureNotificationStyle(payload)
                    }
            }

            // Default style is BigTextStyle.
            if (style == null) {
                brazelog { "Rendering push notification with BigTextStyle" }
                style = getBigTextNotificationStyle(payload)
            }
            return style
        }

        /**
         * Returns a [NotificationCompat.BigTextStyle] notification style initialized with the content, big title, and big summary
         * specified in the notificationExtras and brazeExtras bundles.
         *
         * If summary text exists, it will be shown in the expanded notification view.
         * If a title exists, it will override the default in expanded notification view.
         */
        fun getBigTextNotificationStyle(payload: BrazeNotificationPayload): NotificationCompat.BigTextStyle {
            val bigTextNotificationStyle = NotificationCompat.BigTextStyle()
            val appConfigProvider = payload.configurationProvider ?: return bigTextNotificationStyle

            payload.contentText?.getHtmlSpannedTextIfEnabled(appConfigProvider)?.let {
                bigTextNotificationStyle.bigText(it)
            }

            payload.bigSummaryText?.let {
                bigTextNotificationStyle.setSummaryText(
                    it.getHtmlSpannedTextIfEnabled(
                        appConfigProvider
                    )
                )
            }

            payload.bigTitleText?.let {
                bigTextNotificationStyle.setBigContentTitle(
                    it.getHtmlSpannedTextIfEnabled(
                        appConfigProvider
                    )
                )
            }
            return bigTextNotificationStyle
        }

        /**
         * Returns a [androidx.core.app.NotificationCompat.DecoratedCustomViewStyle] for push story.
         *
         * @param notificationBuilder
         * @param payload
         * @return a [androidx.core.app.NotificationCompat.DecoratedCustomViewStyle] that describes the appearance of the push story.
         */
        fun getStoryStyle(
            notificationBuilder: NotificationCompat.Builder,
            payload: BrazeNotificationPayload
        ): NotificationCompat.DecoratedCustomViewStyle? {
            val context = payload.context
            if (context == null) {
                brazelog { "Push story page cannot render without a context" }
                return null
            }
            val pushStoryPages = payload.pushStoryPages
            val pageIndex = payload.pushStoryPageIndex
            val pushStoryPage = pushStoryPages[pageIndex]
            val storyView =
                RemoteViews(context.packageName, R.layout.com_braze_push_story_one_image)
            if (!populatePushStoryPage(storyView, payload, pushStoryPage)) {
                brazelog(W) { "Push story page was not populated correctly. Not using DecoratedCustomViewStyle." }
                return null
            }
            val notificationExtras = payload.notificationExtras
            val style = NotificationCompat.DecoratedCustomViewStyle()
            val numPages = pushStoryPages.size
            val previousButtonPendingIntent = createStoryTraversedPendingIntent(
                context,
                notificationExtras,
                (pageIndex - 1 + numPages) % numPages
            )
            storyView.setOnClickPendingIntent(
                R.id.com_braze_story_button_previous,
                previousButtonPendingIntent
            )
            val nextButtonPendingIntent = createStoryTraversedPendingIntent(
                context,
                notificationExtras,
                (pageIndex + 1) % numPages
            )
            storyView.setOnClickPendingIntent(
                R.id.com_braze_story_button_next,
                nextButtonPendingIntent
            )
            notificationBuilder.setCustomBigContentView(storyView)

            // Ensure clicks on the story don't vibrate or make noise after the story first appears
            notificationBuilder.setOnlyAlertOnce(true)
            return style
        }

        /**
         * This method sets a fully custom [android.widget.RemoteViews.RemoteView] to render the
         * notification.
         *
         * In the successful case, a [NoOpSentinelStyle] is returned.
         * In the failure case (image bitmap is null, system information not found, etc.), a
         * null style is returned.
         */
        @Suppress("LongMethod", "ReturnCount")
        @RequiresApi(api = Build.VERSION_CODES.M)
        fun getInlineImageStyle(
            payload: BrazeNotificationPayload,
            notificationBuilder: NotificationCompat.Builder
        ): NotificationCompat.Style? {
            val context = payload.context
            if (context == null) {
                brazelog { "Inline Image Push cannot render without a context" }
                return null
            }
            val imageUrl = payload.bigImageUrl
            if (imageUrl.isNullOrBlank()) {
                brazelog { "Inline Image Push image url invalid" }
                return null
            }
            val notificationExtras = payload.notificationExtras

            // Set the image
            val largeNotificationBitmap = Braze.getInstance(context).imageLoader
                .getPushBitmapFromUrl(
                    context,
                    notificationExtras,
                    imageUrl,
                    BrazeViewBounds.NOTIFICATION_INLINE_PUSH_IMAGE
                )
            if (largeNotificationBitmap == null) {
                brazelog { "Inline Image Push failed to get image bitmap" }
                return null
            }
            val isNotificationSpaceConstrained =
                isRemoteViewNotificationAvailableSpaceConstrained(context)
            val remoteView = RemoteViews(
                context.packageName,
                if (isNotificationSpaceConstrained) R.layout.com_braze_push_inline_image_constrained else R.layout.com_braze_notification_inline_image
            )
            val configurationProvider = BrazeConfigurationProvider(context)

            // Set the app icon drawable
            val appIcon = Icon.createWithResource(
                context,
                configurationProvider.smallNotificationIconResourceId
            )

            payload.accentColor?.let { color ->
                appIcon.setTint(color)
            }
            remoteView.setImageViewIcon(R.id.com_braze_inline_image_push_app_icon, appIcon)

            // Set the app name
            val packageManager = context.packageManager
            val applicationInfo: ApplicationInfo = try {
                packageManager.getApplicationInfo(context.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                brazelog(E, e) { "Inline Image Push application info was null" }
                return null
            }
            val applicationName = packageManager.getApplicationLabel(applicationInfo) as String
            val htmlSpannedAppName =
                applicationName.getHtmlSpannedTextIfEnabled(configurationProvider)
            remoteView.setTextViewText(
                R.id.com_braze_inline_image_push_app_name_text,
                htmlSpannedAppName
            )

            // Set the current time
            remoteView.setTextViewText(
                R.id.com_braze_inline_image_push_time_text,
                formatDateNow(BrazeDateFormat.CLOCK_12_HOUR)
            )

            // Set the text area title
            notificationExtras.getString(Constants.BRAZE_PUSH_TITLE_KEY)?.let { title ->
                remoteView.setTextViewText(
                    R.id.com_braze_inline_image_push_title_text,
                    title.getHtmlSpannedTextIfEnabled(configurationProvider)
                )
            }

            // Set the text area content
            notificationExtras.getString(Constants.BRAZE_PUSH_CONTENT_KEY)?.let { content ->
                remoteView.setTextViewText(
                    R.id.com_braze_inline_image_push_content_text,
                    content.getHtmlSpannedTextIfEnabled(configurationProvider)
                )
            }
            notificationBuilder.setCustomContentView(remoteView)
            return if (isNotificationSpaceConstrained) {
                // On Android 12 and above, the custom image view we had
                // just can't render the same way so we'll fake it with a large icon
                notificationBuilder.setLargeIcon(largeNotificationBitmap)
                NotificationCompat.DecoratedCustomViewStyle()
            } else {
                remoteView.setImageViewBitmap(
                    R.id.com_braze_inline_image_push_side_image,
                    largeNotificationBitmap
                )
                // Since this is entirely custom, no decorated
                // style is returned to the system.
                NoOpSentinelStyle()
            }
        }

        /**
         * Returns a [NotificationCompat.BigPictureStyle] notification style initialized with the bitmap, big title, and big summary
         * specified in the notificationExtras and brazeExtras bundles.
         *
         * If summary text exists, it will be shown in the expanded notification view.
         * If a title exists, it will override the default in expanded notification view.
         */
        @Suppress("ReturnCount")
        fun getBigPictureNotificationStyle(payload: BrazeNotificationPayload): NotificationCompat.BigPictureStyle? {
            val context = payload.context ?: return null
            val imageUrl = payload.bigImageUrl
            if (imageUrl.isNullOrBlank()) {
                return null
            }
            val notificationExtras = payload.notificationExtras
            var imageBitmap = Braze.getInstance(context).imageLoader
                .getPushBitmapFromUrl(
                    context,
                    notificationExtras,
                    imageUrl,
                    BrazeViewBounds.NOTIFICATION_EXPANDED_IMAGE
                )
            if (imageBitmap == null) {
                brazelog {
                    "Failed to download image bitmap for big picture notification style. Url: $imageUrl"
                }
                return null
            }
            return try {
                // Images get cropped differently across different screen sizes
                // Here we grab the current screen size and scale the image to fit correctly
                // Note: if the height is greater than the width it's going to look poor, so we might
                // as well let the system modify it and not complicate things by trying to smoosh it here.
                if (imageBitmap.width > imageBitmap.height) {
                    val bigPictureHeightPixels = getPixelsFromDensityAndDp(
                        getDensityDpi(context),
                        BIG_PICTURE_STYLE_IMAGE_HEIGHT
                    )
                    // 2:1 aspect ratio
                    var bigPictureWidthPixels = 2 * bigPictureHeightPixels
                    val displayWidthPixels = getDisplayWidthPixels(context)
                    if (bigPictureWidthPixels > displayWidthPixels) {
                        bigPictureWidthPixels = displayWidthPixels
                    }
                    try {
                        imageBitmap = Bitmap.createScaledBitmap(
                            imageBitmap,
                            bigPictureWidthPixels,
                            bigPictureHeightPixels,
                            true
                        )
                    } catch (e: Exception) {
                        brazelog(E, e) { "Failed to scale image bitmap, using original." }
                    }
                }
                if (imageBitmap == null) {
                    brazelog(I) {
                        "Bitmap download failed for push notification. No image will be included with the notification."
                    }
                    return null
                }
                val bigPictureNotificationStyle = NotificationCompat.BigPictureStyle()
                bigPictureNotificationStyle.bigPicture(imageBitmap)
                setBigPictureSummaryAndTitle(bigPictureNotificationStyle, payload)
                bigPictureNotificationStyle
            } catch (e: Exception) {
                brazelog(E, e) { "Failed to create Big Picture Style." }
                null
            }
        }

        fun getConversationalPushStyle(
            notificationBuilder: NotificationCompat.Builder,
            payload: BrazeNotificationPayload
        ): NotificationCompat.MessagingStyle? {
            return try {
                val conversationPersonMap = payload.conversationPersonMap
                val replyPerson = conversationPersonMap[payload.conversationReplyPersonId]
                if (replyPerson == null) {
                    brazelog { "Reply person does not exist in mapping. Not rendering a style" }
                    return null
                }
                val style = NotificationCompat.MessagingStyle(replyPerson.person)
                for (message in payload.conversationMessages) {
                    val person = conversationPersonMap[message.personId]
                    if (person == null) {
                        brazelog {
                            "Message person does not exist in mapping. Not rendering a style. $message"
                        }
                        return null
                    }
                    style.addMessage(message.message, message.timestamp, person.person)
                }
                style.isGroupConversation = conversationPersonMap.size > 1
                notificationBuilder.setShortcutId(payload.conversationShortcutId)
                style
            } catch (e: Exception) {
                brazelog(E, e) { "Failed to create conversation push style. Returning null." }
                null
            }
        }

        private fun createStoryPageClickedPendingIntent(
            context: Context,
            pushStoryPage: PushStoryPage
        ): PendingIntent {
            val storyClickedIntent = Intent(Constants.BRAZE_STORY_CLICKED_ACTION)
                .setClass(context, NotificationTrampolineActivity::class.java)
            storyClickedIntent.flags =
                storyClickedIntent.flags or getInstance().getIntentFlags(IntentFlagPurpose.NOTIFICATION_PUSH_STORY_PAGE_CLICK)
            storyClickedIntent.putExtra(Constants.BRAZE_ACTION_URI_KEY, pushStoryPage.deeplink)
            storyClickedIntent.putExtra(
                Constants.BRAZE_ACTION_USE_WEBVIEW_KEY,
                pushStoryPage.useWebview
            )
            storyClickedIntent.putExtra(Constants.BRAZE_STORY_PAGE_ID, pushStoryPage.storyPageId)
            storyClickedIntent.putExtra(Constants.BRAZE_CAMPAIGN_ID, pushStoryPage.campaignId)
            return PendingIntent.getActivity(
                context,
                getRequestCode(),
                storyClickedIntent,
                getImmutablePendingIntentFlags()
            )
        }

        private fun createStoryTraversedPendingIntent(
            context: Context,
            notificationExtras: Bundle?,
            pageIndex: Int
        ): PendingIntent {
            val storyNextClickedIntent = Intent(Constants.BRAZE_STORY_TRAVERSE_CLICKED_ACTION)
                .setClass(context, BrazeNotificationUtils.notificationReceiverClass)
            if (notificationExtras != null) {
                notificationExtras.putInt(Constants.BRAZE_STORY_INDEX_KEY, pageIndex)
                storyNextClickedIntent.putExtras(notificationExtras)
            }
            val flags = PendingIntent.FLAG_ONE_SHOT or getImmutablePendingIntentFlags()
            return PendingIntent.getBroadcast(
                context,
                getRequestCode(),
                storyNextClickedIntent,
                flags
            )
        }

        /**
         * Adds the appropriate image, title/subtitle, and PendingIntents to the story page.
         *
         * @param view               The push story remoteView, as instantiated in the getStoryStyle method.
         * @param payload
         * @param pushStoryPage
         * @return True if the push story page was populated correctly.
         */
        @Suppress("LongMethod", "ReturnCount")
        private fun populatePushStoryPage(
            view: RemoteViews,
            payload: BrazeNotificationPayload,
            pushStoryPage: PushStoryPage
        ): Boolean {
            val context = payload.context
            if (context == null) {
                brazelog { "Push story page cannot render without a context" }
                return false
            }
            val configurationProvider = payload.configurationProvider
            if (configurationProvider == null) {
                brazelog { "Push story page cannot render without a configuration provider" }
                return false
            }
            val bitmapUrl = pushStoryPage.bitmapUrl
            if (bitmapUrl.isNullOrBlank()) {
                brazelog { "Push story page image url invalid" }
                return false
            }
            val notificationExtras = payload.notificationExtras

            // Set up bitmap url
            val largeNotificationBitmap = Braze.getInstance(context).imageLoader
                .getPushBitmapFromUrl(
                    context,
                    notificationExtras,
                    bitmapUrl,
                    BrazeViewBounds.NOTIFICATION_ONE_IMAGE_STORY
                )
                ?: return false
            view.setImageViewBitmap(R.id.com_braze_story_image_view, largeNotificationBitmap)

            // Set up title
            val pageTitle = pushStoryPage.title

            // If the title is null or blank, the visibility of the container becomes GONE.
            if (!pageTitle.isNullOrBlank()) {
                val pageTitleText = pageTitle.getHtmlSpannedTextIfEnabled(configurationProvider)
                view.setTextViewText(R.id.com_braze_story_text_view, pageTitleText)
                val titleGravity = pushStoryPage.titleGravity
                view.setInt(
                    R.id.com_braze_story_text_view_container,
                    STORY_SET_GRAVITY,
                    titleGravity
                )
            } else {
                view.setInt(
                    R.id.com_braze_story_text_view_container,
                    STORY_SET_VISIBILITY,
                    View.GONE
                )
            }

            // Set up subtitle
            val pageSubtitle = pushStoryPage.subtitle

            // If the subtitle is null or blank, the visibility of the container becomes GONE.
            if (!pageSubtitle.isNullOrBlank()) {
                val pageSubtitleText =
                    pageSubtitle.getHtmlSpannedTextIfEnabled(configurationProvider)
                view.setTextViewText(R.id.com_braze_story_text_view_small, pageSubtitleText)
                val subtitleGravity = pushStoryPage.subtitleGravity
                view.setInt(
                    R.id.com_braze_story_text_view_small_container,
                    STORY_SET_GRAVITY,
                    subtitleGravity
                )
            } else {
                view.setInt(
                    R.id.com_braze_story_text_view_small_container,
                    STORY_SET_VISIBILITY,
                    View.GONE
                )
            }

            // Set up story clicked intent
            val storyClickedPendingIntent =
                createStoryPageClickedPendingIntent(context, pushStoryPage)
            view.setOnClickPendingIntent(
                R.id.com_braze_story_relative_layout,
                storyClickedPendingIntent
            )
            return true
        }

        @JvmStatic
        @VisibleForTesting
        fun setBigPictureSummaryAndTitle(
            bigPictureNotificationStyle: NotificationCompat.BigPictureStyle,
            payload: BrazeNotificationPayload
        ) {
            val appConfigProvider = payload.configurationProvider ?: return
            val bigSummaryText = payload.bigSummaryText
            val bigTitleText = payload.bigTitleText
            val summaryText = payload.summaryText

            if (bigSummaryText != null) {
                bigPictureNotificationStyle.setSummaryText(
                    bigSummaryText.getHtmlSpannedTextIfEnabled(
                        appConfigProvider
                    )
                )
            }
            if (bigTitleText != null) {
                bigPictureNotificationStyle.setBigContentTitle(
                    bigTitleText.getHtmlSpannedTextIfEnabled(
                        appConfigProvider
                    )
                )
            }

            // If summary is null (which we set to the subtext in setSummaryTextIfPresentAndSupported in BrazeNotificationUtils)
            // and bigSummary is null, set the summary to the message. Without this, the message would be blank in expanded mode.
            if (summaryText == null && bigSummaryText == null) {
                payload.contentText?.let {
                    bigPictureNotificationStyle.setSummaryText(
                        it.getHtmlSpannedTextIfEnabled(
                            appConfigProvider
                        )
                    )
                }
            }
        }

        /**
         * On an Android 12 device and app targeting Android 12, the available space to
         * a [RemoteViews] notification is significantly reduced.
         */
        private fun isRemoteViewNotificationAvailableSpaceConstrained(context: Context): Boolean {
            // Check that the device is on Android 12+ && the app is targeting Android 12+
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                context.applicationContext.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.S
        }
    }
}
