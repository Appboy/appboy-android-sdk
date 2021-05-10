package com.braze.glideimageintegration;

import android.app.Application;
import android.util.Log;

import com.appboy.AppboyLifecycleCallbackListener;
import com.braze.Braze;
import com.braze.support.BrazeLogger;

public class GlideIntegrationApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    BrazeLogger.setLogLevel(Log.VERBOSE);
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
    Braze.getInstance(this).setImageLoader(new GlideAppboyImageLoader());
  }
}
