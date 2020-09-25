package com.appboy.sample;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.models.push.BrazeNotificationPayload;
import com.appboy.push.AppboyNotificationFactory;

public class DroidboyNotificationFactory implements IAppboyNotificationFactory {

  @Override
  public Notification createNotification(BrazeNotificationPayload brazeNotificationPayload) {
    NotificationCompat.Builder notificationBuilder = AppboyNotificationFactory.getInstance().populateNotificationBuilder(brazeNotificationPayload);
    notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    return notificationBuilder.build();
  }

  @Override
  public Notification createNotification(AppboyConfigurationProvider appConfigurationProvider,
                                         Context context,
                                         Bundle notificationExtras,
                                         Bundle appboyExtras) {
    NotificationCompat.Builder notificationBuilder = AppboyNotificationFactory.getInstance()
        .populateNotificationBuilder(appConfigurationProvider, context, notificationExtras, appboyExtras);
    notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    return notificationBuilder.build();
  }
}
