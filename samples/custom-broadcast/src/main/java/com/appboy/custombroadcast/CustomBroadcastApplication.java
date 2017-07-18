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

    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
    AppboyLogger.setLogLevel(Log.VERBOSE);
  }
}
