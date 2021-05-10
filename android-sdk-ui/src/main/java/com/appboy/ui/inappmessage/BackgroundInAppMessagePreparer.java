package com.appboy.ui.inappmessage;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.appboy.enums.inappmessage.InAppMessageFailureType;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageWithImage;
import com.appboy.models.IInAppMessageZippedAssetHtml;
import com.appboy.models.InAppMessageFull;
import com.appboy.models.InAppMessageHtml;
import com.appboy.support.StringUtils;
import com.braze.Braze;
import com.braze.enums.BrazeViewBounds;
import com.braze.images.IBrazeImageLoader;
import com.braze.support.BrazeLogger;
import com.braze.support.WebContentUtils;

import java.io.File;

public class BackgroundInAppMessagePreparer {
  private static final String TAG = BrazeLogger.getBrazeLogTag(BackgroundInAppMessagePreparer.class);

  /**
   * @param mainLooperHandler A {@link Handler} of the main thread that will be used to
   *                          call {@link AppboyInAppMessageManager#displayInAppMessage(IInAppMessage, boolean)}
   *                          on successful preparation of the message.
   * @param inAppMessageToPrepare The message to prepare for eventual display.
   */
  public static void prepareInAppMessageForDisplay(@NonNull Handler mainLooperHandler, @NonNull IInAppMessage inAppMessageToPrepare) {
    new Thread(new BackgroundPreparationRunnable(mainLooperHandler, inAppMessageToPrepare)).start();
  }

  private static class BackgroundPreparationRunnable implements Runnable {
    @NonNull
    private final Handler mMainLooperHandler;
    @NonNull
    private final IInAppMessage mInAppMessageToPrepare;

    private BackgroundPreparationRunnable(@NonNull Handler mainLooperHandler, @NonNull IInAppMessage inAppMessageToPrepare) {
      mMainLooperHandler = mainLooperHandler;
      mInAppMessageToPrepare = inAppMessageToPrepare;
    }

    @Override
    public void run() {
      IInAppMessage preparedInAppMessage;
      try {
        preparedInAppMessage = prepareInAppMessage(mInAppMessageToPrepare);
      } catch (Exception e) {
        BrazeLogger.e(TAG, "Caught error while preparing in app message in background", e);
        return;
      }

      if (preparedInAppMessage == null) {
        BrazeLogger.w(TAG, "Cannot display the in-app message because the in-app message was null.");
        return;
      }

      displayPreparedInAppMessage(preparedInAppMessage);
    }

    private void displayPreparedInAppMessage(@NonNull final IInAppMessage inAppMessage) {
      mMainLooperHandler.post(() -> {
        BrazeLogger.d(TAG, "Displaying in-app message.");
        AppboyInAppMessageManager.getInstance().displayInAppMessage(inAppMessage, false);
      });
    }
  }

  @Nullable
  private static IInAppMessage prepareInAppMessage(IInAppMessage inAppMessageToPrepare) {
    if (inAppMessageToPrepare.isControl()) {
      BrazeLogger.d(TAG, "Skipping in-app message preparation for control in-app message.");
      return inAppMessageToPrepare;
    }
    BrazeLogger.d(TAG, "Starting asynchronous in-app message preparation for message.");
    switch (inAppMessageToPrepare.getMessageType()) {
      case HTML_FULL:
        if (!prepareInAppMessageWithZippedAssetHtml((IInAppMessageZippedAssetHtml) inAppMessageToPrepare)) {
          BrazeLogger.w(TAG, "Logging html in-app message zip asset download failure");
          inAppMessageToPrepare.logDisplayFailure(InAppMessageFailureType.ZIP_ASSET_DOWNLOAD);
          return null;
        }
        break;
      case HTML:
        prepareInAppMessageWithHtml((InAppMessageHtml) inAppMessageToPrepare);
        break;
      default:
        boolean imageDownloadSucceeded = prepareInAppMessageWithBitmapDownload(inAppMessageToPrepare);
        if (!imageDownloadSucceeded) {
          BrazeLogger.w(TAG, "Logging in-app message image download failure");
          inAppMessageToPrepare.logDisplayFailure(InAppMessageFailureType.IMAGE_DOWNLOAD);
          return null;
        }
    }
    return inAppMessageToPrepare;
  }

  /**
   * Prepares the in-app message for displaying Html content.
   *
   * @param inAppMessage the in-app message to be prepared
   * @return The success of the asset download.
   */
  @VisibleForTesting
  static boolean prepareInAppMessageWithZippedAssetHtml(IInAppMessageZippedAssetHtml inAppMessageHtml) {
    // If the local assets exist already, return right away.
    String localAssets = inAppMessageHtml.getLocalAssetsDirectoryUrl();
    if (!StringUtils.isNullOrBlank(localAssets) && new File(localAssets).exists()) {
      BrazeLogger.i(TAG, "Local assets for html in-app message are already populated. Not downloading assets.");
      return true;
    }
    // Otherwise, return if no remote asset zip location is specified.
    if (StringUtils.isNullOrBlank(inAppMessageHtml.getAssetsZipRemoteUrl())) {
      BrazeLogger.i(TAG, "Html in-app message has no remote asset zip. Continuing with in-app message preparation.");
      return true;
    }
    // Otherwise, download the asset zip.
    Context applicationContext = AppboyInAppMessageManager.getInstance().getApplicationContext();
    File internalStorageCacheDirectory = WebContentUtils.getHtmlInAppMessageAssetCacheDirectory(applicationContext);
    String localWebContentUrl = WebContentUtils.getLocalHtmlUrlFromRemoteUrl(internalStorageCacheDirectory, inAppMessageHtml.getAssetsZipRemoteUrl());
    if (!StringUtils.isNullOrBlank(localWebContentUrl)) {
      BrazeLogger.d(TAG, "Local url for html in-app message assets is " + localWebContentUrl);
      inAppMessageHtml.setLocalAssetsDirectoryUrl(localWebContentUrl);
      return true;
    } else {
      BrazeLogger.w(TAG, "Download of html content to local directory failed for remote url: "
          + inAppMessageHtml.getAssetsZipRemoteUrl() + " . Returned local url is: " + localWebContentUrl);
      return false;
    }
  }

  /**
   * Prepares the in-app message for displaying images using a bitmap downloader. The in-app
   * message must have a valid image url.
   *
   * @param inAppMessageWithImage the in-app message to be prepared
   * @return whether or not the asset download succeeded
   */
  @VisibleForTesting
  static boolean prepareInAppMessageWithBitmapDownload(IInAppMessage inAppMessage) {
    if (!(inAppMessage instanceof IInAppMessageWithImage)) {
      BrazeLogger.d(TAG, "Cannot prepare non IInAppMessageWithImage object with bitmap download.");
      return false;
    }

    IInAppMessageWithImage inAppMessageWithImage = (IInAppMessageWithImage) inAppMessage;
    if (inAppMessageWithImage.getBitmap() != null) {
      BrazeLogger.i(TAG, "In-app message already contains image bitmap. Not downloading image from URL.");
      inAppMessageWithImage.setImageDownloadSuccessful(true);
      return true;
    }

    BrazeViewBounds viewBounds = getViewBoundsByType(inAppMessage);
    Context applicationContext = AppboyInAppMessageManager.getInstance().getApplicationContext();
    IBrazeImageLoader imageLoader = Braze.getInstance(applicationContext).getImageLoader();

    // Try to load the local url first
    String localImageUrl = inAppMessageWithImage.getLocalImageUrl();
    if (!StringUtils.isNullOrBlank(localImageUrl)) {
      BrazeLogger.i(TAG, "Passing in-app message local image url to image loader: " + localImageUrl);
      inAppMessageWithImage.setBitmap(imageLoader.getInAppMessageBitmapFromUrl(applicationContext, inAppMessage, localImageUrl, viewBounds));
      if (inAppMessageWithImage.getBitmap() != null) {
        // Got the image, we're done
        inAppMessageWithImage.setImageDownloadSuccessful(true);
        return true;
      }
      // The local uri didn't work, unset it from the IAM
      BrazeLogger.d(TAG, "Removing local image url from IAM since it could not be loaded. URL: " + localImageUrl);
      inAppMessageWithImage.setLocalImageUrl(null);
    }

    // Try to load the remote url next
    String remoteImageUrl = inAppMessageWithImage.getRemoteImageUrl();
    if (!StringUtils.isNullOrBlank(remoteImageUrl)) {
      BrazeLogger.i(TAG, "In-app message has remote image url. Downloading image at url: " + remoteImageUrl);
      inAppMessageWithImage.setBitmap(imageLoader.getInAppMessageBitmapFromUrl(applicationContext, inAppMessage, remoteImageUrl, viewBounds));
    } else {
      BrazeLogger.w(TAG, "In-app message has no remote image url. Not downloading image.");
      if (inAppMessageWithImage instanceof InAppMessageFull) {
        BrazeLogger.w(TAG, "In-app message full has no remote image url yet is required to have an image. Failing message display.");
        return false;
      }
      return true;
    }
    if (inAppMessageWithImage.getBitmap() != null) {
      inAppMessageWithImage.setImageDownloadSuccessful(true);
      return true;
    }
    return false;
  }

  /**
   * Prepares the in-app message for display by substituting locally
   * cached assets into the message of the {@link InAppMessageHtml}
   */
  @VisibleForTesting
  static void prepareInAppMessageWithHtml(InAppMessageHtml inAppMessage) {
    if (inAppMessage.getLocalPrefetchedAssetPaths().isEmpty()) {
      BrazeLogger.d(TAG, "HTML in-app message does not have prefetched assets. "
          + "Not performing any substitutions.");
      return;
    }

    String transformedHtml = WebContentUtils.replacePrefetchedUrlsWithLocalAssets(inAppMessage.getMessage(), inAppMessage.getLocalPrefetchedAssetPaths());
    inAppMessage.setMessage(transformedHtml);
  }

  private static BrazeViewBounds getViewBoundsByType(IInAppMessage inAppMessage) {
    switch (inAppMessage.getMessageType()) {
      case SLIDEUP:
        return BrazeViewBounds.IN_APP_MESSAGE_SLIDEUP;
      case MODAL:
        return BrazeViewBounds.IN_APP_MESSAGE_MODAL;
      default:
        return BrazeViewBounds.NO_BOUNDS;
    }
  }
}
