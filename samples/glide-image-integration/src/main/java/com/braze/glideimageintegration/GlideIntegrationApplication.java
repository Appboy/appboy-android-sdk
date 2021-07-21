package com.braze.glideimageintegration;

import android.app.Application;
import android.util.Log;

import com.braze.Braze;
import com.braze.BrazeActivityLifecycleCallbackListener;
import com.braze.support.BrazeLogger;

public class GlideIntegrationApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    BrazeLogger.setLogLevel(Log.VERBOSE);
    registerActivityLifecycleCallbacks(new BrazeActivityLifecycleCallbackListener());
    Braze.getInstance(this).setImageLoader(new GlideAppboyImageLoader());
  }
}
