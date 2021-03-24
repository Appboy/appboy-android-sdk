package com.appboy.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.appboy.support.AppboyLogger;

public class NotificationTrampolineActivity extends Activity {
  private static final String TAG = AppboyLogger.getBrazeLogTag(NotificationTrampolineActivity.class);

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppboyLogger.v(TAG, "NotificationTrampolineActivity created");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Intent receivedIntent = getIntent();
    if (receivedIntent == null) {
      AppboyLogger.d(TAG, "Notification trampoline activity received null intent. Doing nothing.");
      finish();
      return;
    }

    String action = receivedIntent.getAction();
    if (action == null) {
      AppboyLogger.d(TAG, "Notification trampoline activity received intent with null action. Doing nothing.");
      finish();
      return;
    }

    AppboyLogger.i(TAG, "Notification trampoline activity received intent: " + receivedIntent);

    // Route the intent back to the receiver
    Intent sendIntent = new Intent(action).setClass(this, AppboyNotificationUtils.getNotificationReceiverClass());
    if (receivedIntent.getExtras() != null) {
      sendIntent.putExtras(receivedIntent.getExtras());
    }
    this.sendBroadcast(sendIntent);

    // Note that finish() is no longer called here due to "Background Activity" limitations in Android Q
    // See https://developer.android.com/guide/components/activities/background-starts
  }

  @Override
  protected void onPause() {
    super.onPause();
    AppboyLogger.v(TAG, "NotificationTrampolineActivity paused and finishing");
    finish();
  }
}
