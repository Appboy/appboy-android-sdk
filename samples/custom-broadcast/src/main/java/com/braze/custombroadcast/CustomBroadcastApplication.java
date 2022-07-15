package com.braze.custombroadcast;

import android.app.Application;
import android.util.Log;

import com.braze.Braze;
import com.braze.BrazeActivityLifecycleCallbackListener;
import com.braze.configuration.BrazeConfig;
import com.braze.support.BrazeLogger;

public class CustomBroadcastApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    BrazeLogger.setLogLevel(Log.VERBOSE);

    BrazeConfig.Builder appboyConfig = new BrazeConfig.Builder()
        .setDefaultNotificationChannelName("Braze Push")
        .setDefaultNotificationChannelDescription("Braze related push");
    Braze.configure(this, appboyConfig.build());

    registerActivityLifecycleCallbacks(new BrazeActivityLifecycleCallbackListener());
  }
}
