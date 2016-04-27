package com.appboy.unity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.appboy.Constants;
import com.appboy.push.AppboyNotificationUtils;
import com.appboy.support.StringUtils;

// This BroadcastReceiver is not compatible with Prime31 plugins. If you are using any Prime31 plugins, you
// must use the AppboyUnityGcmReceiver BroadcastReceiver in the com.appboy.unity.prime31compatible package instead.

public class AppboyUnityGcmReceiver extends BroadcastReceiver {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyUnityGcmReceiver.class.getName());
  public static final String CONTAINS_GCM_MESSAGE_KEY = "contains_gcm_message";

  @Override
  public void onReceive(Context context, Intent intent) {
    String packageName = context.getPackageName();
    String appboyNotificationOpenedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX;
    String pushReceivedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_RECEIVED_SUFFIX;

    String action = intent.getAction();
    if (pushReceivedAction.equals(action)) {
      Log.d(TAG, "Received push notification.");
      if (AppboyNotificationUtils.isUninstallTrackingPush(intent.getExtras())) {
        Log.d(TAG, "Got uninstall tracking push");
      }
    } else if (appboyNotificationOpenedAction.equals(action)) {
      Log.d(TAG, "Received a GCM message opened broadcast message. Opening up the Unity Player.");

      Intent startActivityIntent = getStartActivityIntent(context, intent);
      if (startActivityIntent == null) {
        Log.w(TAG, "Could not get start activity intent for this Unity player. Doing nothing.");
        return;
      }

      // If a deep link exists, start an ACTION_VIEW intent pointing at the deep link.
      // The intent returned from getStartActivityIntent() is placed on the back stack.
      // Otherwise, start the intent defined in getStartActivityIntent().
      String deepLink = intent.getStringExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY);
      if (!StringUtils.isNullOrBlank(deepLink)) {
        Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
            .putExtras(intent.getExtras());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(startActivityIntent);
        stackBuilder.addNextIntent(uriIntent);
        try {
          stackBuilder.startActivities(intent.getExtras());
        } catch (ActivityNotFoundException e) {
          Log.w(TAG, String.format("Could not find appropriate activity to open for deep link %s.", deepLink));
        }
      } else {
        context.startActivity(startActivityIntent);
      }
    } else {
      Log.e(TAG, String.format("The GCM Message broadcast receiver received an intent with an "
          + "unsupported action (%s)", action));
    }
  }

  /**
   * @param context
   * @param intent the incoming intent from GCM
   * @return a start activity intent, or null if one could not be found.
   * Unlike the start activity intent in Droidboy, the Unity start activity intent contains
   * the intent extras directly as they came from GCM.
   */
  private Intent getStartActivityIntent(Context context, Intent intent) {
    // Determine the correct Activity class to send the message to.
    Intent startUnityIntent = getMainActivityIntent(context);
    if (startUnityIntent == null) {
      return null;
    }
    startUnityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startUnityIntent.putExtras(intent.getExtras());
    startUnityIntent.putExtra(CONTAINS_GCM_MESSAGE_KEY, true);
    return startUnityIntent;
  }

  /**
   * Returns the main Activity registered in the manifest. If there are multiple main Activity classes
   * registered in the manifest, it will only return the first one found.
   *
   * See {@link PackageManager#getLaunchIntentForPackage(String)}
   *
   * @param context
   * @return A fully-qualified {@link Intent} that can be used to launch the
   * main activity in the package, or null otherwise.
   */
  private Intent getMainActivityIntent(Context context) {
    return context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
  }
}
