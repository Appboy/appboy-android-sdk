package com.appboy.sample.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.appboy.Constants;
import com.appboy.services.AppboyLocationService;

public class RuntimePermissionUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, RuntimePermissionUtils.class.getName());
  public static final int DROIDBOY_PERMISSION_LOCATION = 40;
  public static final int DROIDBOY_PERMISSION_WRITE_EXTERNAL_STORAGE = 100;

  public static void handleOnRequestPermissionsResult(Context context, int requestCode, int[] grantResults) {
    switch (requestCode) {
      case DROIDBOY_PERMISSION_LOCATION:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Log.i(TAG, "Location permission granted.");
          Toast.makeText(context, "Location permission granted.", Toast.LENGTH_SHORT).show();
          AppboyLocationService.requestInitialization(context);
        } else {
          Log.i(TAG, "Location permission NOT granted.");
          Toast.makeText(context, "Location permission NOT granted.", Toast.LENGTH_SHORT).show();
        }
        break;
      case DROIDBOY_PERMISSION_WRITE_EXTERNAL_STORAGE:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Log.i(TAG, "Write external storage permission granted.");
          Toast.makeText(context, "Write external storage permission granted.", Toast.LENGTH_SHORT).show();
        } else {
          Log.i(TAG, "Write external storage permission NOT granted.");
          Toast.makeText(context, "Write external storage permission NOT granted.", Toast.LENGTH_SHORT).show();
        }
        break;
      default:
        break;
    }
  }
}
