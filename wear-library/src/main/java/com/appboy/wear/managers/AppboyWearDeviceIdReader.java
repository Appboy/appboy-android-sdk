package com.appboy.wear.managers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Manages all static device information
 */
public class AppboyWearDeviceIdReader {
  private static final String DEVICE_ID_KEY = "wear_device_id";

  private final Context mContext;

  public AppboyWearDeviceIdReader(Context context) {
    mContext = context;
  }

  /**
   * Tries to read a cached guid from SharedPreferences and generates one if none is found.
   */
  public String readGeneratedDeviceId() {
    SharedPreferences sharedPreferences = mContext.getSharedPreferences("com.appboy.weardevice", Context.MODE_PRIVATE);
    String storedDeviceId = sharedPreferences.getString(DEVICE_ID_KEY, null);
    if (storedDeviceId == null) {
      String generatedDeviceId = UUID.randomUUID().toString();
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString(DEVICE_ID_KEY, generatedDeviceId);
      editor.apply();
      return generatedDeviceId;
    } else {
      return storedDeviceId;
    }
  }
}
