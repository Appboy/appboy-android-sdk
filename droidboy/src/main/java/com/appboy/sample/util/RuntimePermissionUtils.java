package com.appboy.sample.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.appboy.sample.R;
import com.appboy.services.AppboyLocationService;
import com.appboy.support.AppboyLogger;

public class RuntimePermissionUtils {
  private static final String TAG = AppboyLogger.getBrazeLogTag(RuntimePermissionUtils.class);
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

  /**
   * Shows a "here's why we need location permissions" prompt before
   * actually requesting the location permission(s).
   */
  public static void requestLocationPermissions(final Activity activity, @NonNull String[] permissions, int requestCode) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return;
    }

    new AlertDialog.Builder(activity)
        .setTitle(R.string.droidboy_required_location_prompt_title)
        .setMessage(R.string.droidboy_required_location_prompt_message)
        .setPositiveButton("allow", (dialog, which) -> activity.requestPermissions(permissions, requestCode))
        .setNegativeButton("no", null)
        .setIcon(android.R.drawable.ic_dialog_map)
        .show();
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
