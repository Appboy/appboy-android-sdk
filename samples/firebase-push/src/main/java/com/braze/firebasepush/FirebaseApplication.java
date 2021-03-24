package com.braze.firebasepush;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.support.AppboyLogger;
import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseApplication extends Application {
  private static final String TAG = FirebaseApplication.class.getName();

  @Override
  public void onCreate() {
    super.onCreate();

    AppboyConfig.Builder appboyConfig = new AppboyConfig.Builder()
        .setDefaultNotificationChannelName("Braze Push")
        .setDefaultNotificationChannelDescription("Braze related push")
        .setPushDeepLinkBackStackActivityEnabled(true)
        .setPushDeepLinkBackStackActivityClass(MainActivity.class)
        .setInAppMessageTestPushEagerDisplayEnabled(true)
        .setHandlePushDeepLinksAutomatically(true);
    Appboy.configure(this, appboyConfig.build());

    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
    AppboyLogger.setLogLevel(Log.VERBOSE);

    // Example of how to register for Firebase Cloud Messaging manually.
    final Context applicationContext = this;
    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
        return;
      }

      final String token = task.getResult();
      Log.i(TAG, "================");
      Log.i(TAG, "================");
      Log.i(TAG, "Registering firebase token in Application class: " + token);
      Log.i(TAG, "================");
      Log.i(TAG, "================");
      Appboy.getInstance(applicationContext).registerAppboyPushMessages(token);
    });
  }
}
