package com.appboy.custombroadcast;

import android.app.Application;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.support.AppboyLogger;

public class CustomBroadcastApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AppboyLogger.setLogLevel(Log.VERBOSE);

    AppboyConfig.Builder appboyConfig = new AppboyConfig.Builder()
        .setDefaultNotificationChannelName("Appboy Push")
        .setDefaultNotificationChannelDescription("Appboy related push");
    Appboy.configure(this, appboyConfig.build());

    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
  }
}
