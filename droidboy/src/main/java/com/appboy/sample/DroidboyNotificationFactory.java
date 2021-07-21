package com.appboy.sample;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.appboy.models.push.BrazeNotificationPayload;
import com.braze.IBrazeNotificationFactory;
import com.braze.push.BrazeNotificationFactory;

public class DroidboyNotificationFactory implements IBrazeNotificationFactory {

  @Override
  public Notification createNotification(BrazeNotificationPayload brazeNotificationPayload) {
    NotificationCompat.Builder notificationBuilder = BrazeNotificationFactory.getInstance().populateNotificationBuilder(brazeNotificationPayload);
    notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    return notificationBuilder.build();
  }

  @SuppressWarnings("deprecation")
  @Override
  public Notification createNotification(com.appboy.configuration.AppboyConfigurationProvider appConfigurationProvider,
                                         Context context,
                                         Bundle notificationExtras,
                                         Bundle appboyExtras) {
    NotificationCompat.Builder notificationBuilder = BrazeNotificationFactory.getInstance()
        .populateNotificationBuilder(appConfigurationProvider, context, notificationExtras, appboyExtras);
    notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    return notificationBuilder.build();
  }
}
