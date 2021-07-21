package com.braze.helloworld;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.appboy.helloworld.R;
import com.braze.Braze;
import com.braze.BrazeActivityLifecycleCallbackListener;
import com.braze.configuration.BrazeConfig;
import com.braze.support.BrazeLogger;

import java.util.ArrayList;
import java.util.List;

public class CustomApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    BrazeLogger.setLogLevel(Log.VERBOSE);
    configureAppboyAtRuntime();
    registerActivityLifecycleCallbacks(new BrazeActivityLifecycleCallbackListener());
  }

  private void configureAppboyAtRuntime() {
    List<String> localeToApiKeyMapping = new ArrayList<>();
    localeToApiKeyMapping.add("customLocale, customApiKeyForThatLocale");
    localeToApiKeyMapping.add("fr_NC, anotherAPIKey");

    Resources resources = getResources();
    BrazeConfig brazeConfig = new BrazeConfig.Builder()
        .setApiKey("dd162bff-b14e-4d87-9bf0-fec609a77ca4")
        .setIsFirebaseCloudMessagingRegistrationEnabled(false)
        .setAdmMessagingRegistrationEnabled(false)
        .setSessionTimeout(11)
        .setHandlePushDeepLinksAutomatically(true)
        .setSmallNotificationIcon(resources.getResourceEntryName(R.drawable.ic_launcher_hello_braze))
        .setLargeNotificationIcon(resources.getResourceEntryName(R.drawable.ic_launcher_hello_braze))
        .setTriggerActionMinimumTimeIntervalSeconds(5)
        .setIsLocationCollectionEnabled(false)
        .setNewsfeedVisualIndicatorOn(true)
        .setDefaultNotificationAccentColor(0xFFf33e3e)
        .setLocaleToApiMapping(localeToApiKeyMapping)
        .setBadNetworkDataFlushInterval(120)
        .setGoodNetworkDataFlushInterval(60)
        .setGreatNetworkDataFlushInterval(10)
        .build();
    Braze.configure(this, brazeConfig);
  }
}
