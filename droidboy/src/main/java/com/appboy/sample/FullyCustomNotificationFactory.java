package com.appboy.sample;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.appboy.IAppboyNotificationFactory;
import com.appboy.models.push.BrazeNotificationPayload;
import com.appboy.push.AppboyNotificationUtils;

public class FullyCustomNotificationFactory implements IAppboyNotificationFactory {

  @Override
  public Notification createNotification(BrazeNotificationPayload payload) {
    String notificationChannelId = AppboyNotificationUtils.getOrCreateNotificationChannelId(payload);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(payload.getContext(), notificationChannelId);
    notificationBuilder.setContentTitle(payload.getTitleText());
    notificationBuilder.setSmallIcon(R.drawable.com_appboy_push_small_notification_icon);
    AppboyNotificationUtils.setAccentColorIfPresentAndSupported(notificationBuilder, payload);
    String contentString = parseContentsFromExtras(payload.getAppboyExtras());
    notificationBuilder.setContentText(contentString);
    return notificationBuilder.build();
  }

  @Override
  public Notification createNotification(com.appboy.configuration.AppboyConfigurationProvider appConfigurationProvider,
                                         Context context,
                                         Bundle notificationExtras,
                                         Bundle appboyExtras) {
    BrazeNotificationPayload brazeNotificationPayload = new BrazeNotificationPayload(context, appConfigurationProvider, notificationExtras);
    return createNotification(brazeNotificationPayload);
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
