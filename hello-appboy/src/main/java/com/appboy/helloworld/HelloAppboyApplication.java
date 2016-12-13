package com.appboy.helloworld;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.support.AppboyLogger;

import java.util.ArrayList;
import java.util.List;

public class HelloAppboyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    AppboyLogger.LogLevel = Log.VERBOSE;
    configureAppboyAtRuntime();
    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());
  }

  private void configureAppboyAtRuntime() {
    List<String> localeToApiKeyMapping = new ArrayList<>();
    localeToApiKeyMapping.add("customLocale, customApiKeyForThatLocale");
    localeToApiKeyMapping.add("fr_NC, anotherAPIKey");

    Resources resources = getResources();
    AppboyConfig appboyConfig = new AppboyConfig.Builder()
        .setApiKey("2fc268e6-93f9-4176-8b67-42f62f245ad6")
        .setGcmMessagingRegistrationEnabled(false)
        .setAdmMessagingRegistrationEnabled(false)
        .setDisableUilImageCache(true)
        .setFrescoLibraryEnabled(false)
        .setSessionTimeout(11)
        .setHandlePushDeepLinksAutomatically(true)
        .setSmallNotificationIcon(resources.getResourceEntryName(R.drawable.ic_launcher_hello_appboy))
        .setLargeNotificationIcon(resources.getResourceEntryName(R.drawable.ic_launcher_hello_appboy))
        .setTriggerActionMinimumTimeIntervalSeconds(5)
        .setEnableBackgroundLocationCollection(false)
        .setDisableLocationCollection(true)
        .setLocationUpdateDistance(100)
        .setNewsfeedVisualIndicatorOn(true)
        .setDefaultNotificationAccentColor(0xFFf33e3e)
        .setLocaleToApiMapping(localeToApiKeyMapping)
        .setBadNetworkDataFlushInterval(120)
        .setGoodNetworkDataFlushInterval(60)
        .setGreatNetworkDataFlushInterval(10)
        .build();
    Appboy.configure(this, appboyConfig);
  }
}
