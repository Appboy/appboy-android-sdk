package com.appboy.sample.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.appboy.services.AppboyLocationService;
import com.appboy.support.AppboyLogger;

public class RuntimePermissionUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(RuntimePermissionUtils.class);
  public static final int DROIDBOY_PERMISSION_LOCATION = 40;

  public static void handleOnRequestPermissionsResult(Context context, int requestCode, int[] grantResults) {
    if (requestCode == DROIDBOY_PERMISSION_LOCATION) {
      // In Android Q, we require both FINE and BACKGROUND location permissions. Both
      // are requested simultaneously.
      if (areAllPermissionsGranted(grantResults)) {
        Log.i(TAG, "Required location permissions granted.");
        Toast.makeText(context, "Required location permissions granted.", Toast.LENGTH_SHORT).show();
        AppboyLocationService.requestInitialization(context);
      } else {
        Log.i(TAG, "Required location permissions NOT granted.");
        Toast.makeText(context, "Required location permissions NOT granted.", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private static boolean areAllPermissionsGranted(int[] grantResults) {
    for (int grantResult : grantResults) {
      if (grantResult != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }
}
