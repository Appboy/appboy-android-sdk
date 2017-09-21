package com.appboy.push;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;
import com.appboy.support.PackageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AppboyNotificationRemoteViewsUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyNotificationRemoteViewsUtils.class);
  public static final String APPBOY_NOTIFICATION_ID = "com_appboy_notification";
  public static final String APPBOY_NOTIFICATION_ID_NO_ICON = "com_appboy_notification_no_icon";
  public static final String APPBOY_NOTIFICATION_TITLE_ID = "com_appboy_notification_title";
  public static final String APPBOY_NOTIFICATION_CONTENT_ID = "com_appboy_notification_content";
  public static final String APPBOY_NOTIFICATION_ICON_ID = "com_appboy_notification_icon";
  public static final String APPBOY_NOTIFICATION_TIME_ID = "com_appboy_notification_time";
  public static final String APPBOY_NOTIFICATION_TWENTY_FOUR_HOUR_FORMAT_ID = "com_appboy_notification_time_twenty_four_hour_format";
  public static final String APPBOY_NOTIFICATION_TWELVE_HOUR_FORTMAT_ID = "com_appboy_notification_time_twelve_hour_format";

  /**
   * Returns a custom multi-line push notification view. For devices running Jelly Bean+, you should use
   * the native BigTextStyle to get this functionality.
   *
   * @param context
   * @param notificationExtras
   * @param smallIconResourceId the resource id of the small icon defined in appboy.xml as com_appboy_push_small_notification_icon
   * @param showSmallIcon       whether or not to custom display the small icon in the remote view. If an icon outside of the remote view
   *                            itself will be displayed, pass in false to avoid redundant icon display.
   * @return a RemoteViews instance representing the notification or null if the view cannot be created.
   */
  public static RemoteViews createMultiLineContentNotificationView(Context context, Bundle notificationExtras, int smallIconResourceId, boolean showSmallIcon) {
    if (notificationExtras != null) {
      String title = notificationExtras.getString(Constants.APPBOY_PUSH_TITLE_KEY);
      String contentText = notificationExtras.getString(Constants.APPBOY_PUSH_CONTENT_KEY);
      Resources resources = context.getResources();

      String resourcePackageName = PackageUtils.getResourcePackageName(context);
      int layoutResourceId;
      if (showSmallIcon) {
        layoutResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_ID, "layout", resourcePackageName);
      } else {
        // If we are using a large icon or do not want to show the small notification icon for some other reason,
        // choose the layout at "com_appboy_notification_no_icon", which contains no small icon image view
        // as the layout resource id.
        layoutResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_ID_NO_ICON, "layout", resourcePackageName);
      }
      int titleResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TITLE_ID, "id", resourcePackageName);
      int contentResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_CONTENT_ID, "id", resourcePackageName);
      int iconResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_ICON_ID, "id", resourcePackageName);
      int timeViewResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TIME_ID, "id", resourcePackageName);
      int twentyFourHourFormatResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TWENTY_FOUR_HOUR_FORMAT_ID, "string", resourcePackageName);
      int twelveHourFormatResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TWELVE_HOUR_FORTMAT_ID, "string", resourcePackageName);

      String twentyFourHourTimeFormat = AppboyNotificationUtils.getOptionalStringResource(resources,
          twentyFourHourFormatResourceId, Constants.DEFAULT_TWENTY_FOUR_HOUR_TIME_FORMAT);
      String twelveHourTimeFormat = AppboyNotificationUtils.getOptionalStringResource(resources,
          twelveHourFormatResourceId, Constants.DEFAULT_TWELVE_HOUR_TIME_FORMAT);

      if (layoutResourceId == 0 || titleResourceId == 0 || contentResourceId == 0 || iconResourceId == 0
          || timeViewResourceId == 0) {
        AppboyLogger.w(TAG, "Couldn't find all resource IDs for custom notification view, extended view will "
            + "not be used for push notifications. Received " + layoutResourceId + " for layout, " + titleResourceId
            + " for title, " + contentResourceId + " for content, " + iconResourceId + " for icon, "
            + "and " + timeViewResourceId + " for time.");
      } else {
        AppboyLogger.d(TAG, "Using RemoteViews for rendering of push notification.");

        RemoteViews remoteViews;
        try {
          remoteViews = new RemoteViews(PackageUtils.getResourcePackageName(context), layoutResourceId);
        } catch (Exception e) {
          AppboyLogger.e(TAG, "Failed to initialize remote views with package " + PackageUtils.getResourcePackageName(context), e);
          try {
            remoteViews = new RemoteViews(context.getPackageName(), layoutResourceId);
          } catch (Exception e2) {
            AppboyLogger.e(TAG, "Failed to initialize remote views with package " + context.getPackageName(), e2);
            return null;
          }
        }

        remoteViews.setTextViewText(titleResourceId, title);
        remoteViews.setTextViewText(contentResourceId, contentText);

        if (showSmallIcon) {
          remoteViews.setImageViewResource(iconResourceId, smallIconResourceId);
        }

        // Custom views cannot be used as part of a RemoteViews so we're using a TextView widget instead. This
        // view will always display the time without date information (even after the day has changed).
        // Using the device's default locale since we're displaying a notification to the user
        SimpleDateFormat timeFormat = new SimpleDateFormat(
            android.text.format.DateFormat.is24HourFormat(context) ? twentyFourHourTimeFormat : twelveHourTimeFormat, Locale.getDefault());
        String notificationTime = timeFormat.format(new Date());
        remoteViews.setTextViewText(timeViewResourceId, notificationTime);
        return remoteViews;
      }
    }

    return null;
  }
}
