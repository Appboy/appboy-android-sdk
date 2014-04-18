package com.appboy.ui.configuration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.appboy.Constants;
import com.appboy.configuration.CachedConfigurationProvider;

public class XmlUIConfigurationProvider extends CachedConfigurationProvider {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, XmlUIConfigurationProvider.class.getName());
  private static final String APPLICATION_ICON_KEY = "application_icon";

  private final Context mContext;

  public XmlUIConfigurationProvider(Context context) {
    super(context);
    mContext = context;
  }

  public int getApplicationIconResourceId() {
    if (mConfigurationCache.containsKey(APPLICATION_ICON_KEY)) {
      return (Integer) mConfigurationCache.get(APPLICATION_ICON_KEY);
    } else {
      String packageName = mContext.getPackageName();
      int resourceId = 0;
      try {
        ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(packageName, 0);
        resourceId = applicationInfo.icon;
      } catch (PackageManager.NameNotFoundException e) {
        Log.e(TAG, String.format("Cannot find package named %s", packageName));
      }
      mConfigurationCache.put(APPLICATION_ICON_KEY, resourceId);
      return resourceId;
    }
  }
}
