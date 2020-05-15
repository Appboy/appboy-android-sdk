package com.appboy.googletagmanager;

import android.app.Application;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.support.AppboyLogger;

public class GtmApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AppboyLogger.setLogLevel(Log.VERBOSE);
    BrazeGtmTagProvider.setApplicationContext(this.getApplicationContext());
    Appboy.configure(this.getApplicationContext(), new AppboyConfig.Builder()
        .setApiKey("4149fcbf-ee7a-45a8-8e89-e17e9fec1306")
        .setFirebaseCloudMessagingSenderIdKey("901477453852")
        .setIsFirebaseCloudMessagingRegistrationEnabled(true)
        .setHandlePushDeepLinksAutomatically(true)
        .build()
    );
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
  }
}
