package com.appboy.push;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.appboy.support.AppboyLogger;

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
    sendIntent.putExtras(receivedIntent.getExtras());
    context.sendBroadcast(sendIntent);
    finish();
  }
}
