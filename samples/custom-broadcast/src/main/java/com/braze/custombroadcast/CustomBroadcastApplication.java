package com.braze.custombroadcast;

import android.app.Application;
import android.util.Log;

import com.appboy.AppboyLifecycleCallbackListener;
import com.braze.Braze;
import com.braze.configuration.BrazeConfig;
import com.braze.support.BrazeLogger;

public class CustomBroadcastApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    BrazeLogger.setLogLevel(Log.VERBOSE);

    BrazeConfig.Builder appboyConfig = new BrazeConfig.Builder()
        .setDefaultNotificationChannelName("Appboy Push")
        .setDefaultNotificationChannelDescription("Appboy related push");
    Braze.configure(this, appboyConfig.build());

    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
  }
}
