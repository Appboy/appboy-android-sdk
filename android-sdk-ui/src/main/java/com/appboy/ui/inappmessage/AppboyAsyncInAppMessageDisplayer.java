package com.appboy.ui.inappmessage;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import com.appboy.Appboy;
import com.appboy.IAppboyImageLoader;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.enums.inappmessage.InAppMessageFailureType;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageFull;
import com.appboy.models.InAppMessageHtmlBase;
import com.appboy.models.InAppMessageHtmlFull;
import com.appboy.models.InAppMessageModal;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.support.WebContentUtils;

import java.io.File;

public class AppboyAsyncInAppMessageDisplayer extends AsyncTask<IInAppMessage, Integer, IInAppMessage> {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyAsyncInAppMessageDisplayer.class);

  @Override
  protected IInAppMessage doInBackground(IInAppMessage... inAppMessages) {
    try {
      IInAppMessage inAppMessage = inAppMessages[0];
      if (inAppMessage.isControl()) {
        AppboyLogger.d(TAG, "Skipping in-app message preparation for control in-app message.");
        return inAppMessage;
      }
      AppboyLogger.d(TAG, "Starting asynchronous in-app message preparation.");
      if (inAppMessage instanceof InAppMessageHtmlFull) {
        // Note, this will clear the in-app message cache, which is OK because no other in-app message is currently displaying
        // and AsyncTasks are are executed on a single thread, which guarantees no other in-app message is
        // relying on the cache dir right now.
        // See http://developer.android.com/reference/android/os/AsyncTask.html#execute(Params...)
        if (!prepareInAppMessageWithHtml(inAppMessage)) {
          inAppMessage.logDisplayFailure(InAppMessageFailureType.ZIP_ASSET_DOWNLOAD);
          return null;
        }
      } else {
        boolean imageDownloadSucceeded = prepareInAppMessageWithBitmapDownload(inAppMessage);
        if (!imageDownloadSucceeded) {
          inAppMessage.logDisplayFailure(InAppMessageFailureType.IMAGE_DOWNLOAD);
          return null;
        }
      }
      return inAppMessage;
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Error running AsyncInAppMessageDisplayer", e);
      return null;
    }
  }

  @Override
  protected void onPostExecute(final IInAppMessage inAppMessage) {
    try {
      if (inAppMessage != null) {
        AppboyLogger.d(TAG, "Finished asynchronous in-app message preparation. Attempting to display in-app message.");
        Context applicationContext = AppboyInAppMessageManager.getInstance().getApplicationContext();
        Handler mainLooperHandler = new Handler(applicationContext.getMainLooper());
        mainLooperHandler.post(new Runnable() {
          @Override
          public void run() {
            AppboyLogger.d(TAG, "Displaying in-app message.");
            AppboyInAppMessageManager.getInstance().displayInAppMessage(inAppMessage, false);
          }
        });
      } else {
        AppboyLogger.e(TAG, "Cannot display the in-app message because the in-app message was null.");
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Error running onPostExecute", e);
    }
  }

  /**
   * Prepares the in-app message for displaying Html content.
   *
   * @param inAppMessage the in-app message to be prepared
   *
   * @return whether or not asset download succeeded
   */
  boolean prepareInAppMessageWithHtml(IInAppMessage inAppMessage) {
    InAppMessageHtmlBase inAppMessageHtml = (InAppMessageHtmlBase) inAppMessage;
    // If the local assets exist already, return right away.
    String localAssets = inAppMessageHtml.getLocalAssetsDirectoryUrl();
    if (!StringUtils.isNullOrBlank(localAssets) && new File(localAssets).exists()) {
      AppboyLogger.i(TAG, "Local assets for html in-app message are already populated. Not downloading assets.");
      return true;
    }
    // Otherwise, return if no remote asset zip location is specified.
    if (StringUtils.isNullOrBlank(inAppMessageHtml.getAssetsZipRemoteUrl())) {
      AppboyLogger.i(TAG, "Html in-app message has no remote asset zip. Continuing with in-app message preparation.");
      return true;
    }
    // Otherwise, download the asset zip.
    Context applicationContext = AppboyInAppMessageManager.getInstance().getApplicationContext();
    File internalStorageCacheDirectory = WebContentUtils.getHtmlInAppMessageAssetCacheDirectory(applicationContext);
    String localWebContentUrl = WebContentUtils.getLocalHtmlUrlFromRemoteUrl(internalStorageCacheDirectory, inAppMessageHtml.getAssetsZipRemoteUrl());
    if (!StringUtils.isNullOrBlank(localWebContentUrl)) {
      AppboyLogger.d(TAG, "Local url for html in-app message assets is " + localWebContentUrl);
      inAppMessageHtml.setLocalAssetsDirectoryUrl(localWebContentUrl);
      return true;
    } else {
      AppboyLogger.w(TAG, "Download of html content to local directory failed for remote url: "
          + inAppMessageHtml.getAssetsZipRemoteUrl() + " . Returned local url is: " + localWebContentUrl);
      return false;
    }
  }

  /**
   * Prepares the in-app message for displaying images using a bitmap downloader. The in-app
   * message must have a valid image url.
   *
   * @param inAppMessage the in-app message to be prepared
   * @return whether or not the asset download succeeded
   */
  boolean prepareInAppMessageWithBitmapDownload(IInAppMessage inAppMessage) {
    if (inAppMessage.getBitmap() != null) {
      AppboyLogger.i(TAG, "In-app message already contains image bitmap. Not downloading image from URL.");
      inAppMessage.setImageDownloadSuccessful(true);
      return true;
    }
    // If the image already has a local Uri, attempt to load it
    String localImageUrl = inAppMessage.getLocalImageUrl();
    if (!StringUtils.isNullOrBlank(localImageUrl) && new File(localImageUrl).exists()) {
      AppboyLogger.i(TAG, "In-app message has local image url.");
      inAppMessage.setBitmap(AppboyImageUtils.getBitmap(Uri.parse(localImageUrl)));
    }
    // If loading fails or no local image is specified, download from the remote url.
    // Return if no remote uri is specified.
    if (inAppMessage.getBitmap() == null) {
      String remoteImageUrl = inAppMessage.getRemoteImageUrl();
      if (!StringUtils.isNullOrBlank(remoteImageUrl)) {
        AppboyLogger.i(TAG, "In-app message has remote image url. Downloading.");

        // Try to sample the image for slideup and modal in-app messages
        // By default, the image won't be sampled
        AppboyViewBounds viewBounds = AppboyViewBounds.NO_BOUNDS;

        if (inAppMessage instanceof InAppMessageSlideup) {
          viewBounds = AppboyViewBounds.IN_APP_MESSAGE_SLIDEUP;
        } else if (inAppMessage instanceof InAppMessageModal) {
          viewBounds = AppboyViewBounds.IN_APP_MESSAGE_MODAL;
        }

        Context applicationContext = AppboyInAppMessageManager.getInstance().getApplicationContext();
        IAppboyImageLoader appboyImageLoader = Appboy.getInstance(applicationContext).getAppboyImageLoader();
        inAppMessage.setBitmap(appboyImageLoader.getInAppMessageBitmapFromUrl(applicationContext, inAppMessage, remoteImageUrl, viewBounds));
      } else {
        AppboyLogger.w(TAG, "In-app message has no remote image url. Not downloading image.");
        if (inAppMessage instanceof InAppMessageFull) {
          AppboyLogger.w(TAG, "In-app message full has no remote image url yet is required to have an image. Failing download.");
          return false;
        }
        return true;
      }
    }
    if (inAppMessage.getBitmap() != null) {
      inAppMessage.setImageDownloadSuccessful(true);
      return true;
    }
    return false;
  }
}
