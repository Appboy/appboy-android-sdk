package com.braze.googletagmanager;

import android.app.Application;
import android.util.Log;

import com.appboy.AppboyLifecycleCallbackListener;
import com.braze.Braze;
import com.braze.configuration.BrazeConfig;
import com.braze.support.BrazeLogger;

public class GtmApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    BrazeLogger.setLogLevel(Log.VERBOSE);
    BrazeGtmTagProvider.setApplicationContext(this.getApplicationContext());
    Braze.configure(this.getApplicationContext(), new BrazeConfig.Builder()
        .setApiKey("4149fcbf-ee7a-45a8-8e89-e17e9fec1306")
        .setFirebaseCloudMessagingSenderIdKey("901477453852")
        .setIsFirebaseCloudMessagingRegistrationEnabled(true)
        .setHandlePushDeepLinksAutomatically(true)
        .build()
    );
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
  }
}
