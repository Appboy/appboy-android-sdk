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
import android.webkit.WebView;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.appboy.sample.util.EmulatorDetectionUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.PackageUtils;
import com.appboy.support.StringUtils;

import java.util.Arrays;

public class DroidboyApplication extends Application {
  private static final String TAG = AppboyLogger.getAppboyLogTag(DroidboyApplication.class);
  private static String sOverrideApiKeyInUse;

  public static final String OVERRIDE_API_KEY_PREF_KEY = "override_api_key";
  public static final String OVERRIDE_ENDPOINT_PREF_KEY = "override_endpoint_url";

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      activateStrictMode();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        WebView.setWebContentsDebuggingEnabled(true);
      }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }

    int logLevel = getApplicationContext().getSharedPreferences(getString(R.string.log_level_dialog_title), Context.MODE_PRIVATE)
        .getInt(getString(R.string.current_log_level), Log.VERBOSE);
    AppboyLogger.setLogLevel(logLevel);

    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs_location), MODE_PRIVATE);
    disableNetworkRequestsIfConfigured(sharedPreferences);

    AppboyConfig.Builder appboyConfigBuilder = new AppboyConfig.Builder();
    setOverrideApiKeyIfConfigured(sharedPreferences, appboyConfigBuilder);
    Appboy.configure(this, appboyConfigBuilder.build());

    String overrideEndpointUrl = sharedPreferences.getString(OVERRIDE_ENDPOINT_PREF_KEY, null);
    if (!StringUtils.isNullOrBlank(overrideEndpointUrl)) {
      Appboy.setAppboyEndpointProvider(new DroidboyEndpointProvider(overrideEndpointUrl));
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

  @SuppressLint("NewApi")
  private void activateStrictMode() {
    StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog();

    // We are explicitly not detecting detectLeakedClosableObjects(), detectLeakedSqlLiteObjects(), and detectUntaggedSockets()
    // The okhttp library used on most https calls trips the detectUntaggedSockets() check
    // com.google.android.gms.internal trips both the detectLeakedClosableObjects() and detectLeakedSqlLiteObjects() checks
    StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
        .detectActivityLeaks()
        .penaltyLog();

    // Note that some detections require a specific sdk version or higher to enable.
    vmPolicyBuilder.detectLeakedRegistrationObjects();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      vmPolicyBuilder.detectFileUriExposure();
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      vmPolicyBuilder.detectCleartextNetwork();
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vmPolicyBuilder.detectContentUriWithoutPermission();
      vmPolicyBuilder.detectUntaggedSockets();
    }
    StrictMode.setThreadPolicy(threadPolicyBuilder.build());
    StrictMode.setVmPolicy(vmPolicyBuilder.build());

    StrictMode.allowThreadDiskReads();
    StrictMode.allowThreadDiskWrites();
  }

  // Disable Braze network requests if the preference has been set and the current device model matches a list of emulators
  // we don't want to run Appboy on in certain scenarios.
  private void disableNetworkRequestsIfConfigured(SharedPreferences sharedPreferences) {
    boolean disableAppboyNetworkRequestsBooleanString = sharedPreferences.getBoolean(getString(R.string.mock_appboy_network_requests), false);
    if (disableAppboyNetworkRequestsBooleanString && Arrays.asList(EmulatorDetectionUtils.getEmulatorModelsForAppboyDeactivation()).contains(Build.MODEL)) {
      Appboy.enableMockAppboyNetworkRequestsAndDropEventsMode();
      Log.i(TAG, String.format("Mocking Braze network requests because preference was set and model was %s", Build.MODEL));
    }
  }

  private void setOverrideApiKeyIfConfigured(SharedPreferences sharedPreferences, AppboyConfig.Builder appboyConfigBuilder) {
    String overrideApiKey = sharedPreferences.getString(OVERRIDE_API_KEY_PREF_KEY, null);
    if (!StringUtils.isNullOrBlank(overrideApiKey)) {
      Log.i(TAG, String.format("Override API key found, configuring Braze with override key %s.", overrideApiKey));
      appboyConfigBuilder.setApiKey(overrideApiKey);
      sOverrideApiKeyInUse = overrideApiKey;
    }
  }

  public static String getApiKeyInUse(Context context) {
    if (!StringUtils.isNullOrBlank(sOverrideApiKeyInUse)) {
      return sOverrideApiKeyInUse;
    } else {
      // Check if the api key is in resources
      return readStringResourceValue(context, "com_appboy_api_key", "NO-API-KEY-SET");
    }
  }

  private static String readStringResourceValue(Context context, String key, String defaultValue) {
    try {
      if (key == null) {
        return defaultValue;
      }

      int resId = context.getResources().getIdentifier(key, "string", PackageUtils.getResourcePackageName(context));
      if (resId == 0) {
        AppboyLogger.d(TAG, "Unable to find the xml string value with key " + key + ". "
            + "Using default value '" + defaultValue + "'.");
        return defaultValue;
      } else {
        return context.getResources().getString(resId);
      }
    } catch (Exception e) {
      AppboyLogger.d(TAG, "Unexpected exception retrieving the xml string configuration"
          + " value with key " + key + ". Using default value " + defaultValue + "'.");
      return defaultValue;
    }
  }
}
