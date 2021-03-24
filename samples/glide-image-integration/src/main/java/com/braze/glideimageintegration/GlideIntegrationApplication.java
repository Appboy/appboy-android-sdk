package com.braze.glideimageintegration;

import android.app.Application;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.support.AppboyLogger;

public class GlideIntegrationApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AppboyLogger.setLogLevel(Log.VERBOSE);
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
    Appboy.getInstance(this).setAppboyImageLoader(new GlideAppboyImageLoader());
  }
}
