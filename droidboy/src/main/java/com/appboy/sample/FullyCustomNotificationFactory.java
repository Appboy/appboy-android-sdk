package com.appboy.sample;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.appboy.Constants;
import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.push.AppboyNotificationUtils;

public class FullyCustomNotificationFactory implements IAppboyNotificationFactory {

  @Override
  public Notification createNotification(AppboyConfigurationProvider appConfigurationProvider,
                                         Context context, Bundle notificationExtras, Bundle appboyExtras) {
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
    notificationBuilder.setContentTitle(notificationExtras.getString(Constants.APPBOY_PUSH_TITLE_KEY));
    notificationBuilder.setSmallIcon(R.drawable.com_appboy_push_small_notification_icon);
    AppboyNotificationUtils.setAccentColorIfPresentAndSupported(appConfigurationProvider, notificationBuilder, notificationExtras);
    String contentString = parseContentsFromExtras(appboyExtras);
    notificationBuilder.setContentText(contentString);
    return notificationBuilder.build();
  }

  private String parseContentsFromExtras(Bundle appboyExtras) {
    String buildString = "Your order: ";
    buildString += appboyExtras.getString(PushTesterFragment.EXAMPLE_APPBOY_EXTRA_KEY_1);
    buildString += ", ";
    buildString += appboyExtras.getString(PushTesterFragment.EXAMPLE_APPBOY_EXTRA_KEY_2);
    buildString += ", and ";
    buildString += appboyExtras.getString(PushTesterFragment.EXAMPLE_APPBOY_EXTRA_KEY_3);
    buildString += ".";
    return buildString;
  }
}
