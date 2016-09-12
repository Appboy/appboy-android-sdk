package com.appboy.push;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.appboy.Constants;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;

public class AppboyNotificationStyleFactory {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyNotificationStyleFactory.class.getName());
  public static final int BIG_PICTURE_STYLE_IMAGE_HEIGHT = 192;

  /**
   * Returns a big style NotificationCompat.Style.  If an image is present, this will be a BigPictureStyle,
   * otherwise it will be a BigTextStyle.
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static NotificationCompat.Style getBigNotificationStyle(Context context, Bundle notificationExtras, Bundle appboyExtras) {
    NotificationCompat.Style style = null;

    if (appboyExtras != null && appboyExtras.containsKey(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY)) {
      style = AppboyNotificationStyleFactory.getBigPictureNotificationStyle(context, notificationExtras, appboyExtras);
    }

    // Default style is BigTextStyle.
    if (style == null) {
      AppboyLogger.d(TAG, "Rendering push notification with BigTextStyle");
      style = AppboyNotificationStyleFactory.getBigTextNotificationStyle(notificationExtras);
    }

    return style;
  }

  /**
   * Returns a BigTextStyle notification style initialized with the content, big title, and big summary
   * specified in the notificationExtras and appboyExtras bundles.
   * <p/>
   * If summary text exists, it will be shown in the expanded notification view.
   * If a title exists, it will override the default in expanded notification view.
   */
  public static NotificationCompat.BigTextStyle getBigTextNotificationStyle(Bundle notificationExtras) {
    if (notificationExtras != null) {
      NotificationCompat.BigTextStyle bigTextNotificationStyle = new NotificationCompat.BigTextStyle();
      bigTextNotificationStyle.bigText(notificationExtras.getString(Constants.APPBOY_PUSH_CONTENT_KEY));

      String bigSummary = null;
      String bigTitle = null;

      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY)) {
        bigSummary = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY);
      }
      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY)) {
        bigTitle = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY);
      }
      if (bigSummary != null) {
        bigTextNotificationStyle.setSummaryText(bigSummary);
      }
      if (bigTitle != null) {
        bigTextNotificationStyle.setBigContentTitle(bigTitle);
      }

      return bigTextNotificationStyle;
    } else {
      return null;
    }
  }

  /**
   * Returns a BigPictureStyle notification style initialized with the bitmap, big title, and big summary
   * specified in the notificationExtras and appboyExtras bundles.
   * <p/>
   * If summary text exists, it will be shown in the expanded notification view.
   * If a title exists, it will override the default in expanded notification view.
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static NotificationCompat.BigPictureStyle getBigPictureNotificationStyle(Context context, Bundle notificationExtras, Bundle appboyExtras) {
    if (appboyExtras == null || !appboyExtras.containsKey(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY)) {
      return null;
    }

    String imageUrl = appboyExtras.getString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY);
    if (StringUtils.isNullOrBlank(imageUrl)) {
      return null;
    }

    Bitmap imageBitmap = AppboyImageUtils.getBitmap(Uri.parse(imageUrl));
    if (imageBitmap == null) {
      return null;
    }

    try {
      // Images get cropped differently across different screen sizes
      // Here we grab the current screen size and scale the image to fit correctly
      // Note: if the height is greater than the width it's going to look poor, so we might
      // as well let the system modify it and not complicate things by trying to smoosh it here.
      if (imageBitmap.getWidth() > imageBitmap.getHeight()) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        // BigPictureHeight is set in https://android.googlesource.com/platform/frameworks/base/+/6387d2f6dae27ba6e8481883325adad96d3010f4/core/res/res/layout/notification_template_big_picture.xml.
        int bigPictureHeightPixels = AppboyImageUtils.getPixelsFromDensityAndDp(displayMetrics.densityDpi, BIG_PICTURE_STYLE_IMAGE_HEIGHT);
        // 2:1 aspect ratio
        int bigPictureWidthPixels = 2 * bigPictureHeightPixels;
        if (bigPictureWidthPixels > displayMetrics.widthPixels) {
          bigPictureWidthPixels = displayMetrics.widthPixels;
        }

        try {
          imageBitmap = Bitmap.createScaledBitmap(imageBitmap, bigPictureWidthPixels, bigPictureHeightPixels, true);
        } catch (Exception e) {
          AppboyLogger.e(TAG, "Failed to scale image bitmap, using original.", e);
        }
      }
      if (imageBitmap == null) {
        AppboyLogger.i(TAG, "Bitmap download failed for push notification. No image will be included with the notification.");
        return null;
      }

      NotificationCompat.BigPictureStyle bigPictureNotificationStyle = new NotificationCompat.BigPictureStyle();
      bigPictureNotificationStyle.bigPicture(imageBitmap);
      setBigPictureSummaryAndTitle(bigPictureNotificationStyle, notificationExtras);

      return bigPictureNotificationStyle;
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to create Big Picture Style.", e);
      return null;
    }
  }

  static void setBigPictureSummaryAndTitle(NotificationCompat.BigPictureStyle bigPictureNotificationStyle, Bundle notificationExtras) {
    String bigSummary = null;
    String bigTitle = null;

    if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY)) {
      bigSummary = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY);
    }
    if (notificationExtras.containsKey(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY)) {
      bigTitle = notificationExtras.getString(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY);
    }

    if (bigSummary != null) {
      bigPictureNotificationStyle.setSummaryText(bigSummary);
    }
    if (bigTitle != null) {
      bigPictureNotificationStyle.setBigContentTitle(bigTitle);
    }

    // If summary is null (which we set to the subtext in setSummaryTextIfPresentAndSupported in AppboyNotificationUtils)
    // and bigSummary is null, set the summary to the message.  Without this, the message would be blank in expanded mode.
    String summaryText = notificationExtras.getString(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY);
    if (summaryText == null && bigSummary == null) {
      bigPictureNotificationStyle.setSummaryText(notificationExtras.getString(Constants.APPBOY_PUSH_CONTENT_KEY));
    }
  }
}