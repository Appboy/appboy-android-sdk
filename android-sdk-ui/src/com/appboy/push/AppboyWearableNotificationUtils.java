package com.appboy.push;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.appboy.Constants;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;

public class AppboyWearableNotificationUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyWearableNotificationUtils.class.getName());

  /**
   * Optionally set the optional parameters specifically for Android Wear notifications. Since notificationCompat
   * can only be extended once, all Wear Notification extensions must be applied here.
   * See http://developer.android.com/training/wearables/notifications/creating.html#AddWearableFeatures
   */
  public static void setWearableNotificationFeaturesIfPresentAndSupported(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (notificationExtras != null) {
      // Hide the app icon if present
      NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_WEAR_HIDE_APP_ICON_KEY)) {
        Boolean wearableHideIcon = Boolean.parseBoolean(notificationExtras.getString(Constants.APPBOY_PUSH_WEAR_HIDE_APP_ICON_KEY));
        wearableExtender.setHintHideIcon(wearableHideIcon);
      }

      // Set the background image if present
      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_WEAR_BACKGROUND_IMAGE_URL_KEY)) {
        String uriString = notificationExtras.getString(Constants.APPBOY_PUSH_WEAR_BACKGROUND_IMAGE_URL_KEY);
        Bitmap wearNotificationBackgroundBitmap = AppboyImageUtils.getBitmap(Uri.parse(uriString));
        if (wearNotificationBackgroundBitmap != null) {
          wearableExtender.setBackground(wearNotificationBackgroundBitmap);
        }
      }

      // Set any extra pages that may be present. Since we don't know how many pages are present in the
      // bundle, we'll iterate over the keys until we can't find anymore extra pages.
      int currentPageIndex = 0;
      while (isWearExtraPagePresentInBundle(notificationExtras, currentPageIndex)) {
        String extraPageTitle = notificationExtras.getString(Constants.APPBOY_PUSH_WEAR_EXTRA_PAGE_TITLE_KEY_PREFIX + currentPageIndex);
        String extraPageText = notificationExtras.getString(Constants.APPBOY_PUSH_WEAR_EXTRA_PAGE_CONTENT_KEY_PREFIX + currentPageIndex);

        if (extraPageText == null || extraPageTitle == null) {
          AppboyLogger.d(TAG, String.format("The title or content of extra page %s was null. Adding no further extra pages.", currentPageIndex));
          break;
        }

        // When setting the big text on the style, only the bigText appears. To set the content title, we'll set the title on the extra page
        // builder. The docs example at https://developer.android.com/training/wearables/notifications/pages.html appears to be broken.
        NotificationCompat.BigTextStyle extraPageStyle = new NotificationCompat.BigTextStyle();
        extraPageStyle.bigText(extraPageText);

        Notification wearExtraPageNotification = new NotificationCompat.Builder(context).setContentTitle(extraPageTitle).setStyle(extraPageStyle).build();
        wearableExtender.addPage(wearExtraPageNotification);
        currentPageIndex++;
      }

      // Apply the extender
      notificationBuilder.extend(wearableExtender);
    }
  }

  /**
   * For an extra page to be present, both the title and text keys must appear in the bundle.
   * @param appboyExtras the bundle to check
   * @param pageNumber the page number of the extra page
   * @return true if wearable extra page at pageNumber is presnt in the extra bundle.
   */
  private static boolean isWearExtraPagePresentInBundle(Bundle appboyExtras, int pageNumber) {
    // Pages are keyed in the bundle in the form "PREFIX{PAGE_NUMBER}"
    String titleKey = Constants.APPBOY_PUSH_WEAR_EXTRA_PAGE_TITLE_KEY_PREFIX + pageNumber;
    String textKey = Constants.APPBOY_PUSH_WEAR_EXTRA_PAGE_CONTENT_KEY_PREFIX + pageNumber;
    return appboyExtras.containsKey(titleKey) && appboyExtras.containsKey(textKey);
  }
}