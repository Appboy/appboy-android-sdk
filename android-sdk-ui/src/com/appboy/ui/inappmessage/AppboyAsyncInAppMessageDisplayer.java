package com.appboy.ui.inappmessage;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.appboy.Constants;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageHtmlBase;
import com.appboy.models.InAppMessageHtmlFull;
import com.appboy.support.AppboyFileUtils;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.support.WebContentUtils;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;

public class AppboyAsyncInAppMessageDisplayer extends AsyncTask<IInAppMessage, Integer, IInAppMessage> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyAsyncInAppMessageDisplayer.class.getName());
  @Override
  protected IInAppMessage doInBackground(IInAppMessage... inAppMessages) {
    try {
      AppboyLogger.d(TAG, "Starting asynchronous in-app message preparation.");
      Activity activity = getInAppMessageManagerActivity();
      if (activity == null) {
        AppboyLogger.e(TAG, "No activity is currently registered to receive in-app messages. Doing nothing.");
        return null;
      }
      boolean assetDownloadSucceeded;
      IInAppMessage inAppMessage = inAppMessages[0];
      if (inAppMessage instanceof InAppMessageHtmlFull) {
        // Note, this will clear the IAM cache, which is OK because no other IAM is currently displaying
        // and AsyncTasks are are executed on a single thread, which guarantees no other IAM is
        // relying on the cache dir right now.
        // See http://developer.android.com/reference/android/os/AsyncTask.html#execute(Params...)
        assetDownloadSucceeded = prepareInAppMessageWithHtml(inAppMessage);
      } else {
        String imageUrl = inAppMessage.getImageUrl();
        if (StringUtils.isNullOrBlank(imageUrl)) {
          AppboyLogger.w(TAG, "In-app message has no image URL. Not downloading image from URL.");
          return inAppMessage;
        }

        if (FrescoLibraryUtils.canUseFresco(activity.getApplicationContext())) {
          assetDownloadSucceeded = prepareInAppMessageWithFresco(inAppMessage);
        } else {
          assetDownloadSucceeded = prepareInAppMessageWithBitmapDownload(inAppMessage);
        }
      }
      if (!assetDownloadSucceeded) {
        return null;
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
      Activity activity = getInAppMessageManagerActivity();
      if (activity == null) {
        AppboyLogger.e(TAG, "No activity is currently registered to receive in-app messages. Not displaying" +
            "in-app message.");
        return;
      }
      AppboyLogger.d(TAG, "Finished asynchronous in-app message preparation. Attempting to display in-app message.");

      if (inAppMessage != null) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            AppboyLogger.d(TAG, "Displaying in-app message.");
            AppboyInAppMessageManager.getInstance().displayInAppMessage(inAppMessage);
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
   * Prepares the In-App Message for displaying Html content.
   *
   * @param inAppMessage the In-App Message to be prepared
   *
   * @return whether or not asset download succeeded
   */
  boolean prepareInAppMessageWithHtml(IInAppMessage inAppMessage) {
    Activity activity = getInAppMessageManagerActivity();
    if (activity == null) {
      AppboyLogger.e(TAG, "Can't store HTML in-app message assets because activity is null.");
      return false;
    }
    // Get the local URL directory for the html display
    InAppMessageHtmlBase inAppMessageHtml = (InAppMessageHtmlBase) inAppMessage;
    if (StringUtils.isNullOrBlank(inAppMessageHtml.getAssetsZipRemoteUrl())) {
      AppboyLogger.i(TAG, "Html in-app message has no remote asset zip. Continuing with in-app message preparation.");
      return true;
    }
    if (!StringUtils.isNullOrBlank(inAppMessageHtml.getLocalAssetsDirectoryUrl())) {
      AppboyLogger.i(TAG, "Local assets for html in-app message are already populated. Not downloading assets.");
      return true;
    }
    File internalStorageCacheDirectory = activity.getCacheDir();
    String localWebContentUrl = WebContentUtils.getLocalHtmlUrlFromRemoteUrl(internalStorageCacheDirectory, inAppMessageHtml.getAssetsZipRemoteUrl(), true);
    if (!StringUtils.isNullOrBlank(localWebContentUrl)) {
      AppboyLogger.d(TAG, "Local url for html in-app message assets is " + localWebContentUrl);
      inAppMessageHtml.setLocalAssetsDirectoryUrl(localWebContentUrl);
      return true;
    } else {
      AppboyLogger.w(TAG, String.format("Download of html content to local directory failed for remote url: %s . Returned local url is: %s",
          inAppMessageHtml.getAssetsZipRemoteUrl(), localWebContentUrl));
      return false;
    }
  }

  /**
   * Prepares the In-App Message for displaying images using the Fresco library. The in-app
   * message must have a valid image url.
   *
   * @param inAppMessage the In-App Message to be prepared
   * @return whether or not asset download succeeded
   */
  boolean prepareInAppMessageWithFresco(IInAppMessage inAppMessage) {
    String imageUrl = inAppMessage.getImageUrl();
    // If the image already has a local Uri, it will be loaded into the SimpleDrawee view when the in-app
    // message view is instantiated.
    if (AppboyFileUtils.isLocalUri(Uri.parse(imageUrl))) {
      AppboyLogger.i(TAG, "In-app message has local image Uri for Fresco display. Not downloading image.");
      inAppMessage.setImageDownloadSuccessful(true);
      return true;
    }
    // Prefetch the image content via http://frescolib.org/docs/using-image-pipeline.html#prefetching
    ImagePipeline imagePipeline = Fresco.getImagePipeline();
    // Create a request for the image
    ImageRequest imageRequest = ImageRequest.fromUri(imageUrl);
    DataSource dataSource = imagePipeline.prefetchToDiskCache(imageRequest, new Object());

    // Since we're in an asyncTask, we can wait for the also asynchronous prefetch by Fresco
    // to finish.
    while (!dataSource.isFinished()) {
      // Wait for the prefetch to finish
    }

    boolean downloadSucceeded = !dataSource.hasFailed();
    if (downloadSucceeded) {
      inAppMessage.setImageDownloadSuccessful(true);
    } else {
      if (dataSource.getFailureCause() == null) {
        AppboyLogger.w(TAG, "Fresco disk prefetch failed with null cause for image url:" + imageUrl);
      } else {
        AppboyLogger.w(TAG, "Fresco disk prefetch failed with cause: " + dataSource.getFailureCause().getMessage() + " with image url: " + imageUrl);
      }
    }
    // Release the resource reference
    dataSource.close();
    return downloadSucceeded;
  }

  /**
   * Prepares the In-App Message for displaying images using a bitmap downloader. The in-app
   * message must have a valid image url.
   *
   * @param inAppMessage the In-App Message to be prepared
   * @return whether or not the asset download succeeded
   */
  boolean prepareInAppMessageWithBitmapDownload(IInAppMessage inAppMessage) {
    if (inAppMessage.getBitmap() != null) {
      AppboyLogger.i(TAG, "In-app message already contains image bitmap. Not downloading image from URL.");
      inAppMessage.setImageDownloadSuccessful(true);
      return true;
    }
    inAppMessage.setBitmap(AppboyImageUtils.getBitmap(Uri.parse(inAppMessage.getImageUrl())));
    if (inAppMessage.getBitmap() != null) {
      inAppMessage.setImageDownloadSuccessful(true);
      return true;
    }
    return false;
  }

  private Activity getInAppMessageManagerActivity() {
    return AppboyInAppMessageManager.getInstance().getActivity();
  }
}