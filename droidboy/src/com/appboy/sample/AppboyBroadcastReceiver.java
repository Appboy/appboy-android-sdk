package com.appboy.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.appboy.AppboyGcmReceiver;
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
    } else if (notificationOpenedAction.equals(action)) {
      Bundle extras = getPushExtrasBundle(intent);

      // If a custom URI is defined, start an ACTION_VIEW intent pointing at the custom URI.
      // The intent returned from getStartActivityIntent() is placed on the back stack.
      // Otherwise, start the intent defined in getStartActivityIntent().
      if (intent.getStringExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY) != null) {
        Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY)))
            .putExtras(extras);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(getStartActivityIntent(context, extras));
        stackBuilder.addNextIntent(uriIntent);
        stackBuilder.startActivities(extras);
      } else {
        context.startActivity(getStartActivityIntent(context, extras));
      }
    } else {
      Log.d(TAG, String.format("Ignoring intent with unsupported action %s", action));
    }
  }

  private Intent getStartActivityIntent(Context context, Bundle extras) {
    Intent startActivityIntent = new Intent(context, DroidBoyActivity.class);
    startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    if (extras != null) {
      startActivityIntent.putExtras(extras);
    }
    return startActivityIntent;
  }

  private Bundle getPushExtrasBundle(Intent intent) {
    Bundle extras = intent.getBundleExtra(Constants.APPBOY_PUSH_EXTRAS_KEY);
    if (extras == null) {
      extras = new Bundle();
    }
    extras.putString(DESTINATION_VIEW, FEED);
    extras.putString(AppboyGcmReceiver.CAMPAIGN_ID_KEY, intent.getStringExtra(AppboyGcmReceiver.CAMPAIGN_ID_KEY));
    extras.putString(SOURCE_KEY, Constants.APPBOY);
    return extras;
  }
}
