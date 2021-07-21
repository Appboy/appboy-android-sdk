package com.braze.push;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.appboy.Constants;
import com.braze.support.BrazeLogger;

public class NotificationTrampolineActivity extends Activity {
  private static final String TAG = BrazeLogger.getBrazeLogTag(NotificationTrampolineActivity.class);

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    BrazeLogger.v(TAG, "NotificationTrampolineActivity created");
  }

  @Override
  protected void onResume() {
    super.onResume();
    try {
      Intent receivedIntent = getIntent();
      if (receivedIntent == null) {
        BrazeLogger.d(TAG, "Notification trampoline activity received null intent. Doing nothing.");
        finish();
        return;
      }

      String action = receivedIntent.getAction();
      if (action == null) {
        BrazeLogger.d(TAG, "Notification trampoline activity received intent with null action. Doing nothing.");
        finish();
        return;
      }

      BrazeLogger.v(TAG, "Notification trampoline activity received intent: " + receivedIntent);
      // Route the intent back to the receiver
      Intent sendIntent = new Intent(action).setClass(this, BrazeNotificationUtils.getNotificationReceiverClass());
      if (receivedIntent.getExtras() != null) {
        sendIntent.putExtras(receivedIntent.getExtras());
      }
      if (Constants.IS_AMAZON) {
        BrazeAmazonDeviceMessagingReceiver.handleReceivedIntent(this, sendIntent);
      } else {
        BrazePushReceiver.handleReceivedIntent(this, sendIntent, false);
      }
    } catch (Exception e) {
      BrazeLogger.e(TAG, "Failed to route intent to notification receiver", e);
    }

    // Now that this Activity has been created, we are safe to finish it in accordance with
    // https://developer.android.com/guide/components/activities/background-starts#exceptions
    new Handler(this.getMainLooper()).postDelayed(() -> finish(), 200);
  }

  @Override
  protected void onPause() {
    super.onPause();
    BrazeLogger.v(TAG, "Notification trampoline activity paused and finishing");
    finish();
  }
}
