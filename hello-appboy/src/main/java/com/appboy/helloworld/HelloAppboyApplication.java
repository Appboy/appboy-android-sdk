package com.appboy.helloworld;

import android.app.Application;

import com.appboy.AppboyLifecycleCallbackListener;

public class HelloAppboyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
  }
}
