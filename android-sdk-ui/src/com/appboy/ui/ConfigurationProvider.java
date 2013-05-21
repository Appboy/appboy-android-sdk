package com.appboy.ui;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class ConfigurationProvider {
  private static final String TAG = String.format("%s.%s", com.appboy.Constants.APPBOY, ConfigurationProvider.class.getName());
  private static final String SMALL_NOTIFICATION_ICON = "com.appboy.SMALL_NOTIFICATION_ICON";

  private final Context mContext;
  private final Bundle mMetadata;

  public ConfigurationProvider(Context context) {
    mContext = context;
    mMetadata = getPackageMetadata();
  }

  public int getSmallNotificationIconResourceId() {
    int resourceId = mMetadata.getInt(SMALL_NOTIFICATION_ICON, 0);
    if (resourceId == 0) {
      Log.d(TAG, String.format("No resource with metadata name %s was found. Will use the app icon " +
        "when displaying notifications.", SMALL_NOTIFICATION_ICON));
    }
    return resourceId;
  }

  private Bundle getPackageMetadata() {
    try {
      String packageName = mContext.getPackageName();
      ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(packageName,
        PackageManager.GET_META_DATA);
      return applicationInfo.metaData;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Unable to read application metadata");
      return null;
    }
  }
}
