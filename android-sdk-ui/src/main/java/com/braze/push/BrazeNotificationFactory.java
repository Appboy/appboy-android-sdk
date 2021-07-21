package com.braze.push;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.appboy.models.push.BrazeNotificationPayload;
import com.braze.IBrazeNotificationFactory;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;

public class BrazeNotificationFactory implements IBrazeNotificationFactory {
  private static final String TAG = BrazeLogger.getBrazeLogTag(BrazeNotificationFactory.class);
  private static volatile BrazeNotificationFactory sInstance = null;

  /**
   * Returns the singleton {@link BrazeNotificationFactory} instance.
   */
  public static BrazeNotificationFactory getInstance() {
    if (sInstance == null) {
      synchronized (BrazeNotificationFactory.class) {
        if (sInstance == null) {
          sInstance = new BrazeNotificationFactory();
        }
      }
    }
    return sInstance;
  }

  /**
   * @deprecated Deprecated since 8/25/20
   */
  @Override
  @Deprecated
  @SuppressWarnings("deprecation")
  public Notification createNotification(com.appboy.configuration.AppboyConfigurationProvider appConfigurationProvider,
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
      BrazeLogger.i(TAG, "Notification could not be built. Returning null as created notification");
      return null;
    }
  }

  /**
   * @deprecated Please use {@link #populateNotificationBuilder(BrazeConfigurationProvider, Context, Bundle, Bundle)}
   * instead. Deprecated since 3/26/21
   */
  @Deprecated
  @Nullable
  @SuppressWarnings("deprecation")
  public NotificationCompat.Builder populateNotificationBuilder(com.appboy.configuration.AppboyConfigurationProvider appboyConfigurationProvider,
                                                                Context context,
                                                                Bundle notificationExtras,
                                                                Bundle appboyExtras) {
    BrazeNotificationPayload payload = new BrazeNotificationPayload(context,
        (BrazeConfigurationProvider) appboyConfigurationProvider,
        notificationExtras,
        appboyExtras);
    return populateNotificationBuilder(payload);
  }

  /**
   * Equivalent to {@link #createNotification(BrazeNotificationPayload)}
   */
  @Nullable
  public NotificationCompat.Builder populateNotificationBuilder(BrazeConfigurationProvider configurationProvider,
                                                                Context context,
                                                                Bundle notificationExtras,
                                                                Bundle appboyExtras) {
    BrazeNotificationPayload payload = new BrazeNotificationPayload(context,
        (BrazeConfigurationProvider) configurationProvider,
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
    BrazeLogger.v(TAG, "Using BrazeNotificationPayload: " + payload);
    final Context context = payload.getContext();

    if (context == null) {
      BrazeLogger.d(TAG, "BrazeNotificationPayload has null context. Not creating notification");
      return null;
    }
    final BrazeConfigurationProvider appboyConfigurationProvider = payload.getConfigurationProvider();
    if (appboyConfigurationProvider == null) {
      BrazeLogger.d(TAG, "BrazeNotificationPayload has null app configuration provider. Not creating notification");
      return null;
    }
    final Bundle notificationExtras = payload.getNotificationExtras();

    // We build up the notification by setting values if they are present in the extras and supported
    // on the device. The notification building is currently order/combination independent, but
    // the addition of new RemoteViews options could mean that some methods conflict/overwrite. For clarity
    // we build the notification up in the order that each feature was supported.

    // If this notification is a push story,
    // make a best effort to preload bitmap images into the cache.
    BrazeNotificationUtils.prefetchBitmapsIfNewlyReceivedStoryPush(context, notificationExtras, payload.getAppboyExtras());

    String notificationChannelId = BrazeNotificationUtils.getOrCreateNotificationChannelId(payload);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, notificationChannelId)
        .setAutoCancel(true);

    BrazeNotificationUtils.setTitleIfPresent(notificationBuilder, payload);
    BrazeNotificationUtils.setContentIfPresent(notificationBuilder, payload);
    BrazeNotificationUtils.setTickerIfPresent(notificationBuilder, payload);
    BrazeNotificationUtils.setSetShowWhen(notificationBuilder, payload);

    // Add intent to fire when the notification is opened or deleted.
    BrazeNotificationUtils.setContentIntentIfPresent(context, notificationBuilder, notificationExtras);
    BrazeNotificationUtils.setDeleteIntent(context, notificationBuilder, notificationExtras);
    BrazeNotificationUtils.setSmallIcon(appboyConfigurationProvider, notificationBuilder);

    BrazeNotificationUtils.setLargeIconIfPresentAndSupported(notificationBuilder, payload);
    BrazeNotificationUtils.setSoundIfPresentAndSupported(notificationBuilder, payload);

    // Subtext, priority, notification actions, and styles were added in JellyBean.
    BrazeNotificationUtils.setSummaryTextIfPresentAndSupported(notificationBuilder, payload);
    BrazeNotificationUtils.setPriorityIfPresentAndSupported(notificationBuilder, notificationExtras);
    BrazeNotificationStyleFactory.setStyleIfSupported(notificationBuilder, payload);
    BrazeNotificationActionUtils.addNotificationActions(notificationBuilder, payload);

    // Accent color, category, visibility, and public notification were added in Lollipop.
    BrazeNotificationUtils.setAccentColorIfPresentAndSupported(notificationBuilder, payload);
    BrazeNotificationUtils.setCategoryIfPresentAndSupported(notificationBuilder, payload);
    BrazeNotificationUtils.setVisibilityIfPresentAndSupported(notificationBuilder, payload);
    BrazeNotificationUtils.setPublicVersionIfPresentAndSupported(notificationBuilder, payload);

    // Notification priority and sound were deprecated in Android O
    BrazeNotificationUtils.setNotificationBadgeNumberIfPresent(notificationBuilder, payload);

    return notificationBuilder;
  }
}
