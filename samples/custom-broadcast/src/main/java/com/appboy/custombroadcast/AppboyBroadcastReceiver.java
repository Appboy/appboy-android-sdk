package com.appboy.custombroadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appboy.push.AppboyNotificationUtils;
import com.appboy.support.AppboyLogger;

public class AppboyBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyBroadcastReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    String packageName = context.getPackageName();
    String pushReceivedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_RECEIVED_SUFFIX;
    String notificationOpenedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX;
    String action = intent.getAction();
    Log.d(TAG, String.format("Received intent with action %s", action));

    if (pushReceivedAction.equals(action)) {
      Log.d(TAG, "Received push notification.");
      if (AppboyNotificationUtils.isUninstallTrackingPush(intent.getExtras())) {
        Log.d(TAG, "Got uninstall tracking push");
      }
    } else if (notificationOpenedAction.equals(action)) {
      AppboyNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
    } else {
      Log.d(TAG, String.format("Ignoring intent with unsupported action %s", action));
    }
  }
}
