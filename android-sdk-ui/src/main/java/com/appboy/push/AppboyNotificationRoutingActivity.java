package com.appboy.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.appboy.support.AppboyLogger;

/**
 * This class merely acts as a liaison between notification action buttons and opening Activities directly
 * from the lock screen. Having the notification action button deeplink intent go directly to the
 * {@link AppboyNotificationUtils#getNotificationReceiverClass()} does not prompt the user to open
 * the phone from behind a lockscreen. Deeplinking to this liaison solves that.
 * <br>
 * However note that any deeplink to this {@link Activity} will also open the host app. For example,
 * if the origin deeplink is intended to close a notification, deeplinking to this {@link Activity} will
 * also open the app, which is likely not intended.
 */
public class AppboyNotificationRoutingActivity extends Activity {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyNotificationRoutingActivity.class);

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent receivedIntent = getIntent();
    if (receivedIntent == null) {
      AppboyLogger.d(TAG, "Notification routing activity received null intent. Doing nothing.");
      finish();
      return;
    }

    String action = receivedIntent.getAction();
    if (action == null) {
      AppboyLogger.d(TAG, "Notification routing activity received intent with null action. Doing nothing.");
      finish();
      return;
    }

    AppboyLogger.i(TAG, "Notification routing activity received intent: " + receivedIntent.toString());
    final Context context = this.getApplicationContext();

    // Route the intent back to the receiver
    Intent sendIntent = new Intent(action).setClass(context, AppboyNotificationUtils.getNotificationReceiverClass());
    if (receivedIntent.getExtras() != null) {
      sendIntent.putExtras(receivedIntent.getExtras());
    }
    context.sendBroadcast(sendIntent);

    // Note that finish() is no longer called here due to "Background Activity" limitations in Android Q
    // See https://developer.android.com/preview/privacy/background-activity-starts
    // TODO: juliancontreras 3/26/19 Test this on each Developer Preview of Android-Q 
  }
}
