package com.appboy.push;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.models.push.BrazeNotificationPayload;
import com.appboy.support.AppboyLogger;

public class AppboyNotificationFactory implements IAppboyNotificationFactory {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyNotificationFactory.class);
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

  @Override
  @Deprecated
  public Notification createNotification(AppboyConfigurationProvider appConfigurationProvider,
                                         Context context,
                                         Bundle notificationExtras,
                                         Bundle appboyExtras) {
    BrazeNotificationPayload payload = new BrazeNotificationPayload(context,
        appConfigurationProvider,
        notificationExtras,
        appboyExtras);
    return createNotification(payload);
  }

  /**
   * Creates the rich notification. The notification content varies based on the Android version on the
   * device, but each notification can contain an icon, image, title, and content.
   *
   * Opening a notification from the notification center triggers a broadcast message to be sent.
   * The broadcast message action is <host-app-package-name>.intent.APPBOY_NOTIFICATION_OPENED.
   */
  @Override
  public Notification createNotification(BrazeNotificationPayload payload) {
    final NotificationCompat.Builder builder = populateNotificationBuilder(payload);
    if (builder != null) {
      return builder.build();
    } else {
      AppboyLogger.i(TAG, "Notification could not be built. Returning null as created notification");
      return null;
    }
  }

  /**
   * Equivalent to {@link #createNotification(BrazeNotificationPayload)}
   */
  @Nullable
  public NotificationCompat.Builder populateNotificationBuilder(AppboyConfigurationProvider appboyConfigurationProvider,
                                                                Context context,
                                                                Bundle notificationExtras,
                                                                Bundle appboyExtras) {
    BrazeNotificationPayload payload = new BrazeNotificationPayload(context,
        appboyConfigurationProvider,
        notificationExtras,
        appboyExtras);
    return populateNotificationBuilder(payload);
  }

  /**
   * Returns a notification builder populated with all fields from the notification extras and
   * Braze extras.
   *
   * To create a notification object, call `build()` on the returned builder instance.
   */
  @Nullable
  public static NotificationCompat.Builder populateNotificationBuilder(BrazeNotificationPayload payload) {
    AppboyLogger.v(TAG, "Using BrazeNotificationPayload: " + payload);
    final Context context = payload.getContext();

    if (context == null) {
      AppboyLogger.d(TAG, "BrazeNotificationPayload has null context. Not creating notification");
      return null;
    }
    final AppboyConfigurationProvider appboyConfigurationProvider = payload.getAppboyConfigurationProvider();
    if (appboyConfigurationProvider == null) {
      AppboyLogger.d(TAG, "BrazeNotificationPayload has null app configuration provider. Not creating notification");
      return null;
    }
    final Bundle notificationExtras = payload.getNotificationExtras();

    // We build up the notification by setting values if they are present in the extras and supported
    // on the device. The notification building is currently order/combination independent, but
    // the addition of new RemoteViews options could mean that some methods conflict/overwrite. For clarity
    // we build the notification up in the order that each feature was supported.

    // If this notification is a push story,
    // make a best effort to preload bitmap images into the cache.
    AppboyNotificationUtils.prefetchBitmapsIfNewlyReceivedStoryPush(context, notificationExtras, payload.getAppboyExtras());

    String notificationChannelId = AppboyNotificationUtils.getOrCreateNotificationChannelId(payload);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, notificationChannelId)
        .setAutoCancel(true);

    AppboyNotificationUtils.setTitleIfPresent(notificationBuilder, payload);
    AppboyNotificationUtils.setContentIfPresent(notificationBuilder, payload);
    AppboyNotificationUtils.setTickerIfPresent(notificationBuilder, payload);
    AppboyNotificationUtils.setSetShowWhen(notificationBuilder, payload);

    // Add intent to fire when the notification is opened or deleted.
    AppboyNotificationUtils.setContentIntentIfPresent(context, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setDeleteIntent(context, notificationBuilder, notificationExtras);
    AppboyNotificationUtils.setSmallIcon(appboyConfigurationProvider, notificationBuilder);

    AppboyNotificationUtils.setLargeIconIfPresentAndSupported(notificationBuilder, payload);
    AppboyNotificationUtils.setSoundIfPresentAndSupported(notificationBuilder, payload);

    // Subtext, priority, notification actions, and styles were added in JellyBean.
    AppboyNotificationUtils.setSummaryTextIfPresentAndSupported(notificationBuilder, payload);
    AppboyNotificationUtils.setPriorityIfPresentAndSupported(notificationBuilder, notificationExtras);
    AppboyNotificationStyleFactory.setStyleIfSupported(notificationBuilder, payload);
    AppboyNotificationActionUtils.addNotificationActions(context, notificationBuilder, notificationExtras);

    // Accent color, category, visibility, and public notification were added in Lollipop.
    AppboyNotificationUtils.setAccentColorIfPresentAndSupported(notificationBuilder, payload);
    AppboyNotificationUtils.setCategoryIfPresentAndSupported(notificationBuilder, payload);
    AppboyNotificationUtils.setVisibilityIfPresentAndSupported(notificationBuilder, payload);
    AppboyNotificationUtils.setPublicVersionIfPresentAndSupported(notificationBuilder, payload);

    // Notification priority and sound were deprecated in Android O
    AppboyNotificationUtils.setNotificationBadgeNumberIfPresent(notificationBuilder, payload);

    return notificationBuilder;
  }
}
