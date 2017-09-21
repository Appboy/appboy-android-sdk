package com.appboy.firebasepush;

import android.app.Application;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.support.AppboyLogger;

public class FirebaseApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    AppboyConfig.Builder appboyConfig = new AppboyConfig.Builder()
        .setGcmMessagingRegistrationEnabled(false)
        .setDefaultNotificationChannelName("Appboy Push")
        .setDefaultNotificationChannelDescription("Appboy related push")
        .setPushDeepLinkBackStackActivityEnabled(true)
        .setPushDeepLinkBackStackActivityClass(MainActivity.class)
        .setHandlePushDeepLinksAutomatically(true);
    Appboy.configure(this, appboyConfig.build());

    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
    AppboyLogger.setLogLevel(Log.VERBOSE);
  }
}
