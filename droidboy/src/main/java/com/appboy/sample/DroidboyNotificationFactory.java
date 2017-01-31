package com.appboy.sample;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.push.AppboyNotificationFactory;

public class DroidboyNotificationFactory implements IAppboyNotificationFactory {

  public Notification createNotification(AppboyConfigurationProvider appConfigurationProvider,
                                         Context context, Bundle notificationExtras, Bundle appboyExtras) {
    Notification notification = AppboyNotificationFactory.getInstance().createNotification(appConfigurationProvider, context, notificationExtras, appboyExtras);
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
      if (notification.sound == null) {
        notification.sound = Settings.System.DEFAULT_NOTIFICATION_URI;
      }
    }
    return notification;
  }
}
