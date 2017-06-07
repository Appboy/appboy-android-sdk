package com.appboy.sample.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.appboy.Constants;
import com.appboy.sample.DroidBoyActivity;
import com.appboy.support.AppboyLogger;

public class LifecycleUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, LifecycleUtils.class.getName());

  public static void restartApp(Context context) {
    Intent startActivity = new Intent(context, DroidBoyActivity.class);
    int pendingIntentId = 109829837;
    PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
    AppboyLogger.i(TAG, "Restarting application to apply new environment values");
    System.exit(0);
  }
}
