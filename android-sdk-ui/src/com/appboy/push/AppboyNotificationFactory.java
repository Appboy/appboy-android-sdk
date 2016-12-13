package com.appboy.push;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.AppboyConfigurationProvider;

public class AppboyNotificationFactory implements IAppboyNotificationFactory {
  private static volatile AppboyNotificationFactory sInstance = null;

  /**
   * Returns the singleton AppboyNotificationFactory instance.
   */
  public static AppboyNotificationFactory getInstance() {
    if (sInstance == null) {
      synchronized (AppboyNotificationFactory.class) {
        if (sInstance == null) {
          sInstance = new AppboyNotificationFactory();
        }
      }
    }
    return sInstance;
  }

  /**
   * Creates the rich notification. The notification content varies based on the Android version on the
   * device, but each notification can contain an icon, image, title, and content.
   *
   * Opening a notification from the notification center triggers a broadcast message to be sent.
   * The broadcast message action is <host-app-package-name>.intent.APPBOY_NOTIFICATION_OPENED.
   *
   * Note: Gingerbread notifications are limited to one line of content.
   */
  public Notification createNotification(AppboyConfigurationProvider appConfigurationProvider,
                                                Context context, Bundle notificationExtras, Bundle appboyExtras) {

    // We build up the notification by setting values if they are present in the extras and supported
    // on the device.  The notification building is currently order/combination independent, but
    // the addition of new RemoteViews options could mean that some methods conflict/overwrite.  For clarity
    // we build the notification up in the order that each feature was supported.

    NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(context).setAutoCancel(true);

    AppboyNotificationUtils.setTitleIfPresent(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setContentIfPresent(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setTickerIfPresent(notificationBuilder, notificationExtras);

    // Add intent to fire when the notification is opened.
    AppboyNotificationUtils.setContentIntentIfPresent(context, notificationBuilder, notificationExtras);
    int smallNotificationIconResourceId = AppboyNotificationUtils.setSmallIcon(appConfigurationProvider, notificationBuilder);

    // Honeycomb added large icons and sound
    boolean usingLargeIcon = AppboyNotificationUtils.setLargeIconIfPresentAndSupported(context, appConfigurationProvider, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setSoundIfPresentAndSupported(notificationBuilder, notificationExtras);

    // From Honeycomb to ICS, we can use a custom view for our notifications which will allow them to be taller than
    // the standard one line of text.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      // Pass in !usingLargeIcon because if no large icon is present, we want to display the small icon in its place.
      RemoteViews remoteViews = AppboyNotificationRemoteViewsUtils.createMultiLineContentNotificationView(context, notificationExtras, smallNotificationIconResourceId, !usingLargeIcon);
      if (remoteViews != null) {
        notificationBuilder.setContent(remoteViews);
        return notificationBuilder.build();
      }
    }

    // Subtext, priority, notification actions, and styles were added in JellyBean.
    AppboyNotificationUtils.setSummaryTextIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setPriorityIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setStyleIfSupported(context, notificationBuilder, notificationExtras, appboyExtras);
    AppboyNotificationActionUtils.addNotificationActions(context, notificationBuilder, notificationExtras);

    // Accent color, category, visibility, and public notification were added in Lollipop.
    AppboyNotificationUtils.setAccentColorIfPresentAndSupported(appConfigurationProvider, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setCategoryIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setVisibilityIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setPublicVersionIfPresentAndSupported(context, appConfigurationProvider, notificationBuilder, notificationExtras);

    // Android Wear Notification support
    AppboyWearableNotificationUtils.setWearableNotificationFeaturesIfPresentAndSupported(context, notificationBuilder, notificationExtras);
    return notificationBuilder.build();
  }
}