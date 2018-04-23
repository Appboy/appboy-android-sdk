package com.appboy.firebasepush;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.support.AppboyLogger;
import com.google.firebase.iid.FirebaseInstanceId;

public class FirebaseApplication extends Application {
  private static final String TAG = FirebaseApplication.class.getName();

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

    // Example of how to register for Firebase Cloud Messaging manually.
    final Context applicationContext = this;
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          String token = FirebaseInstanceId.getInstance().getToken(getString(R.string.sender_id), getString(R.string.firebase_scope));
          Log.i(TAG, "================");
          Log.i(TAG, "================");
          Log.i(TAG, "Registering firebase token in Application class: " + token);
          Log.i(TAG, "================");
          Log.i(TAG, "================");
          Appboy.getInstance(applicationContext).registerAppboyPushMessages(token);
        } catch (Exception e) {
          Log.e(TAG, "Exception while registering Firebase token with Braze.", e);
        }
      }
    }).start();
  }
}
