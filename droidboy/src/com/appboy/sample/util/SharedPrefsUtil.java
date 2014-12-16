package com.appboy.sample.util;

import android.annotation.TargetApi;
import android.content.SharedPreferences;

public class SharedPrefsUtil {
  public static final String SharedPrefsFilename = "com.appboy.sample.sharedpreferences";

  public static final String API_KEY_STATUS_KEY = "com_appboy_api_key_status";

  // A simple method to commit a sharedPreference file.
  @TargetApi(9)
  public static void persist(SharedPreferences.Editor editor) {
    if (android.os.Build.VERSION.SDK_INT < 9) {
      editor.commit();
    } else {
      editor.apply();
    }
  }
}
