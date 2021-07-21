package com.appboy.sample.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.appboy.sample.activity.DroidBoyActivity;
import com.braze.support.BrazeLogger;
import com.braze.support.IntentUtils;

@SuppressWarnings("PMD.DoNotCallSystemExit")
public class LifecycleUtils {
  private static final String TAG = BrazeLogger.getBrazeLogTag(LifecycleUtils.class);

  public static void restartApp(Context context) {
    new Handler(context.getMainLooper()).postDelayed(() -> {
      Intent startActivity = new Intent(context, DroidBoyActivity.class);
      int pendingIntentId = 109829837;
      final int flags = PendingIntent.FLAG_CANCEL_CURRENT | IntentUtils.getDefaultPendingIntentFlags();
      PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, startActivity, flags);
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
      BrazeLogger.i(TAG, "Restarting application to apply new environment values");
      System.exit(0);
    }, 500);
  }
}
