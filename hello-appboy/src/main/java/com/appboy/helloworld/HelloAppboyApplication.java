package com.appboy.helloworld;

import android.app.Application;
import android.util.Log;

import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.support.AppboyLogger;

public class HelloAppboyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    AppboyLogger.LogLevel = Log.VERBOSE;
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
  }
}
