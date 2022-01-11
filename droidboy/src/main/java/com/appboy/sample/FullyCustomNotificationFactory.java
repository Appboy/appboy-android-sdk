package com.appboy.sample;

import android.app.Notification;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.appboy.models.push.BrazeNotificationPayload;
import com.braze.IBrazeNotificationFactory;
import com.braze.push.BrazeNotificationUtils;

public class FullyCustomNotificationFactory implements IBrazeNotificationFactory {

  @Override
  public Notification createNotification(BrazeNotificationPayload payload) {
    String notificationChannelId = BrazeNotificationUtils.getOrCreateNotificationChannelId(payload);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(payload.getContext(), notificationChannelId);
    notificationBuilder.setContentTitle(payload.getTitleText());
    notificationBuilder.setSmallIcon(R.drawable.com_braze_push_small_notification_icon);
    BrazeNotificationUtils.setAccentColorIfPresentAndSupported(notificationBuilder, payload);
    String contentString = parseContentsFromExtras(payload.getBrazeExtras());
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
