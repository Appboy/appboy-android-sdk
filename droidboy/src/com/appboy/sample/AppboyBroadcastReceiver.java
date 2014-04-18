package com.appboy.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.appboy.AppboyGcmReceiver;
import com.appboy.Constants;

public class AppboyBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyBroadcastReceiver.class.getName());
  public static final String SOURCE_KEY = "source";
  public static final String DESTINATION_VIEW = "destination";
  public static final String HOME = "home";
  public static final String FEED = "feed";
  public static final String FEEDBACK = "feedback";

  @Override
  public void onReceive(Context context, Intent intent) {
    String packageName = context.getPackageName();
    String pushReceivedAction = packageName + ".intent.APPBOY_PUSH_RECEIVED";
    String notificationOpenedAction = packageName + ".intent.APPBOY_NOTIFICATION_OPENED";
    String action = intent.getAction();
    Log.d(TAG, String.format("Received intent with action %s", action));

    if (pushReceivedAction.equals(action)) {
      Log.d(TAG, "Received push notification.");
    } else if (notificationOpenedAction.equals(action)) {
      Bundle extras = new Bundle();
      extras.putString(DESTINATION_VIEW, FEED);
      extras.putString(AppboyGcmReceiver.CAMPAIGN_ID_KEY, intent.getStringExtra(AppboyGcmReceiver.CAMPAIGN_ID_KEY));
      startDroidBoyWithIntent(context, extras);
    } else {
      Log.d(TAG, String.format("Ignoring intent with unsupported action %s", action));
    }
  }

  private void startDroidBoyWithIntent(Context context, Bundle extras) {
    Intent startActivityIntent = new Intent(context, DroidBoyActivity.class);
    startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivityIntent.putExtra(SOURCE_KEY, Constants.APPBOY);
    if (extras != null) {
      startActivityIntent.putExtras(extras);
    }
    context.startActivity(startActivityIntent);
  }
}
