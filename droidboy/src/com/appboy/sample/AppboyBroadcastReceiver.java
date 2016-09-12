package com.appboy.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.appboy.Constants;
import com.appboy.push.AppboyNotificationUtils;

public class AppboyBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyBroadcastReceiver.class.getName());
  public static final String SOURCE_KEY = "source";
  public static final String DESTINATION_VIEW = "destination";
  public static final String HOME = "home";
  public static final String FEED = "feed";
  public static final String FEEDBACK = "feedback";

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
      if (intent.getBooleanExtra(Constants.APPBOY_ACTION_IS_CUSTOM_ACTION_KEY, false)) {
        Toast.makeText(context, "You clicked a Droidboy custom action!", Toast.LENGTH_LONG).show();
      } else {
        AppboyNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
      }
    } else {
      Log.d(TAG, String.format("Ignoring intent with unsupported action %s", action));
    }
  }
}