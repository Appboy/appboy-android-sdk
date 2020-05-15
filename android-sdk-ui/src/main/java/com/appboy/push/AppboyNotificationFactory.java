package com.appboy.push;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

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
   * Returns a notification builder populated with all fields from the notification extras and
   * Braze extras.
   *
   * To create a notification object, call `build()` on the returned builder instance.
   */
  public NotificationCompat.Builder populateNotificationBuilder(AppboyConfigurationProvider appboyConfigurationProvider,
                                                                Context context, Bundle notificationExtras, Bundle appboyExtras) {
    // We build up the notification by setting values if they are present in the extras and supported
    // on the device. The notification building is currently order/combination independent, but
    // the addition of new RemoteViews options could mean that some methods conflict/overwrite. For clarity
    // we build the notification up in the order that each feature was supported.

    // If this notification is a push story,
    // make a best effort to preload bitmap images into the cache.
    AppboyNotificationUtils.prefetchBitmapsIfNewlyReceivedStoryPush(context, notificationExtras, appboyExtras);

    String notificationChannelId = AppboyNotificationUtils.getOrCreateNotificationChannelId(context, appboyConfigurationProvider, notificationExtras);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, notificationChannelId)
        .setAutoCancel(true);

    AppboyNotificationUtils.setTitleIfPresent(appboyConfigurationProvider, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setContentIfPresent(appboyConfigurationProvider, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setTickerIfPresent(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setSetShowWhen(notificationBuilder, notificationExtras);

    // Add intent to fire when the notification is opened or deleted.
    AppboyNotificationUtils.setContentIntentIfPresent(context, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setDeleteIntent(context, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setSmallIcon(appboyConfigurationProvider, notificationBuilder);

    AppboyNotificationUtils.setLargeIconIfPresentAndSupported(context, appboyConfigurationProvider, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setSoundIfPresentAndSupported(notificationBuilder, notificationExtras);

    // Subtext, priority, notification actions, and styles were added in JellyBean.
    AppboyNotificationUtils.setSummaryTextIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setPriorityIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setStyleIfSupported(context, notificationBuilder, notificationExtras, appboyExtras);
    AppboyNotificationActionUtils.addNotificationActions(context, notificationBuilder, notificationExtras);

    // Accent color, category, visibility, and public notification were added in Lollipop.
    AppboyNotificationUtils.setAccentColorIfPresentAndSupported(appboyConfigurationProvider, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setCategoryIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setVisibilityIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setPublicVersionIfPresentAndSupported(context, appboyConfigurationProvider, notificationBuilder, notificationExtras);

    // Notification priority and sound were deprecated in Android O
    AppboyNotificationUtils.setNotificationBadgeNumberIfPresent(notificationBuilder, notificationExtras);

    return notificationBuilder;
  }

  /**
   * Creates the rich notification. The notification content varies based on the Android version on the
   * device, but each notification can contain an icon, image, title, and content.
   *
   * Opening a notification from the notification center triggers a broadcast message to be sent.
   * The broadcast message action is <host-app-package-name>.intent.APPBOY_NOTIFICATION_OPENED.
   */
  @Override
  public Notification createNotification(AppboyConfigurationProvider appConfigurationProvider,
                                         Context context,
                                         Bundle notificationExtras,
                                         Bundle appboyExtras) {
    return populateNotificationBuilder(appConfigurationProvider, context, notificationExtras, appboyExtras).build();
  }
}
