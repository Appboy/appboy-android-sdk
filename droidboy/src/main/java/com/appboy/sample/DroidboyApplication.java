package com.appboy.sample;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.sample.util.EmulatorDetectionUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.Arrays;

public class DroidboyApplication extends Application {
  private static final String TAG = AppboyLogger.getAppboyLogTag(DroidboyApplication.class);
  protected static final String OVERRIDE_API_KEY_PREF_KEY = "override_api_key";
  protected static final String OVERRIDE_ENDPOINT_PREF_KEY = "override_endpoint_url";
  private static String sOverrideApiKeyInUse;

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      activateStrictMode();
    }

    int logLevel = getApplicationContext().getSharedPreferences(getString(R.string.log_level_dialog_title), Context.MODE_PRIVATE)
        .getInt(getString(R.string.current_log_level), Log.VERBOSE);
    AppboyLogger.setLogLevel(logLevel);

    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs_location), MODE_PRIVATE);
    disableNetworkRequestsIfConfigured(sharedPreferences);

    // Clear the configuration cache with null
    Appboy.configure(this, null);
    AppboyConfig.Builder appboyConfigBuilder = new AppboyConfig.Builder();
    setOverrideApiKeyIfConfigured(sharedPreferences, appboyConfigBuilder);
    Appboy.configure(this, appboyConfigBuilder.build());

    String overrideEndpointUrl = sharedPreferences.getString(OVERRIDE_ENDPOINT_PREF_KEY, null);
    Appboy.setAppboyEndpointProvider(new DroidboyEndpointProvider(overrideEndpointUrl));

    if (FrescoLibraryUtils.canUseFresco(getApplicationContext())) {
      Fresco.initialize(getApplicationContext());
    }

    registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener());

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    createNotificationGroup(notificationManager, R.string.droidboy_notification_group_01_id, R.string.droidboy_notification_group_01_name);
    createNotificationChannel(notificationManager, R.string.droidboy_notification_channel_01_id, R.string.droidboy_notification_channel_messages_name,
        R.string.droidboy_notification_channel_messages_desc, R.string.droidboy_notification_group_01_id);
    createNotificationChannel(notificationManager, R.string.droidboy_notification_channel_02_id, R.string.droidboy_notification_channel_matches_name,
        R.string.droidboy_notification_channel_matches_desc, R.string.droidboy_notification_group_01_id);
    createNotificationChannel(notificationManager, R.string.droidboy_notification_channel_03_id, R.string.droidboy_notification_channel_offers_name,
        R.string.droidboy_notification_channel_offers_desc, R.string.droidboy_notification_group_01_id);
    createNotificationChannel(notificationManager, R.string.droidboy_notification_channel_04_id, R.string.droidboy_notification_channel_recommendations_name,
        R.string.droidboy_notification_channel_recommendations_desc, R.string.droidboy_notification_group_01_id);
  }

  @SuppressLint("NewApi")
  private void createNotificationGroup(NotificationManager notificationManager, int idResource, int nameResource) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(
          getString(idResource),
          getString(nameResource)
      ));
    }
  }

  @SuppressLint("NewApi")
  private void createNotificationChannel(NotificationManager notificationManager, int idResource, int nameResource, int descResource, int groupResource) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(
          getString(idResource),
          getString(nameResource),
          NotificationManager.IMPORTANCE_LOW);
      channel.setDescription(getString(descResource));
      channel.enableLights(true);
      channel.setLightColor(Color.RED);
      channel.setGroup(getString(groupResource));
      channel.enableVibration(true);
      channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
      notificationManager.createNotificationChannel(channel);
    }
  }

  private void activateStrictMode() {
    StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog();
    StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
        .detectAll()
        .detectLeakedClosableObjects()
        .penaltyLog();
    StrictMode.setThreadPolicy(threadPolicyBuilder.build());
    StrictMode.setVmPolicy(vmPolicyBuilder.build());
  }

  // Disable Appboy network requests if the preference has been set and the current device model matches a list of emulators
  // we don't want to run Appboy on in certain scenarios.
  private void disableNetworkRequestsIfConfigured(SharedPreferences sharedPreferences) {
    boolean disableAppboyNetworkRequestsBooleanString = sharedPreferences.getBoolean(getString(R.string.mock_appboy_network_requests), false);
    if (disableAppboyNetworkRequestsBooleanString && Arrays.asList(EmulatorDetectionUtils.getEmulatorModelsForAppboyDeactivation()).contains(Build.MODEL)) {
      Appboy.enableMockAppboyNetworkRequestsAndDropEventsMode();
      Log.i(TAG, String.format("Mocking Appboy network requests because preference was set and model was %s", Build.MODEL));
    }
  }

  private void setOverrideApiKeyIfConfigured(SharedPreferences sharedPreferences, AppboyConfig.Builder appboyConfigBuilder) {
    String overrideApiKey = sharedPreferences.getString(OVERRIDE_API_KEY_PREF_KEY, null);
    if (!StringUtils.isNullOrBlank(overrideApiKey)) {
      Log.i(TAG, String.format("Override API key found, configuring Appboy with override key %s.", overrideApiKey));
      appboyConfigBuilder.setApiKey(overrideApiKey);
      sOverrideApiKeyInUse = overrideApiKey;
    }
  }

  protected static String getApiKeyInUse(Context context) {
    if (!StringUtils.isNullOrBlank(sOverrideApiKeyInUse)) {
      return sOverrideApiKeyInUse;
    } else {
      return context.getResources().getString(R.string.com_appboy_api_key);
    }
  }
}
