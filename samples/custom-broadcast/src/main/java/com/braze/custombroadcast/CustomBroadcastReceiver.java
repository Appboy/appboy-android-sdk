package com.braze.custombroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appboy.Constants;
import com.braze.push.BrazeNotificationUtils;
import com.braze.support.BrazeLogger;

import java.util.concurrent.TimeUnit;

public class CustomBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = BrazeLogger.getBrazeLogTag(CustomBroadcastReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action == null) {
      return;
    }
    Log.d(TAG, String.format("Received intent with action %s", action));
    logNotificationDuration(intent);

    switch (action) {
      case Constants.BRAZE_PUSH_INTENT_NOTIFICATION_RECEIVED:
        Log.d(TAG, "Received push notification.");
        break;
      case Constants.BRAZE_PUSH_INTENT_NOTIFICATION_OPENED:
        BrazeNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
        break;
      case Constants.BRAZE_PUSH_INTENT_NOTIFICATION_DELETED:
        Log.d(TAG, "Received push notification deleted intent.");
        break;
      default:
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
