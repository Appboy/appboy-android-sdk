package com.braze.custombroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appboy.Constants;
import com.appboy.push.AppboyNotificationUtils;
import com.braze.support.BrazeLogger;

import java.util.concurrent.TimeUnit;

public class CustomBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = BrazeLogger.getBrazeLogTag(CustomBroadcastReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    String packageName = context.getPackageName();
    String pushReceivedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_RECEIVED_SUFFIX;
    String notificationOpenedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX;
    String notificationDeletedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_DELETED_SUFFIX;

    String action = intent.getAction();
    Log.d(TAG, String.format("Received intent with action %s", action));

    logNotificationDuration(intent);

    if (pushReceivedAction.equals(action)) {
      Log.d(TAG, "Received push notification.");
    } else if (notificationOpenedAction.equals(action)) {
      AppboyNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
    } else if (notificationDeletedAction.equals(action)) {
      Log.d(TAG, "Received push notification deleted intent.");
    } else {
      Log.d(TAG, String.format("Ignoring intent with unsupported action %s", action));
    }
  }

  /**
   * Logs the length of time elapsed since the notification's creation time.
   */
  private void logNotificationDuration(Intent intent) {
    // Log the duration of the push notification
    Bundle extras = intent.getExtras();
    if (extras != null && extras.containsKey(Constants.APPBOY_PUSH_RECEIVED_TIMESTAMP_MILLIS)) {
      long createdAt = extras.getLong(Constants.APPBOY_PUSH_RECEIVED_TIMESTAMP_MILLIS);
      long durationMillis = System.currentTimeMillis() - createdAt;
      long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis);
      Log.i(TAG, "Notification active for " + durationSeconds + " seconds.");
    }
  }
}
