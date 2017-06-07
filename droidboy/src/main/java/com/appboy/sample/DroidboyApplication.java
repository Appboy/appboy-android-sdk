package com.appboy.sample;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.configuration.AppboyConfig;
import com.appboy.sample.util.EmulatorDetectionUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.Arrays;

public class DroidboyApplication extends Application {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DroidboyApplication.class.getName());
  protected static final String OVERRIDE_API_KEY_PREF_KEY = "override_api_key";
  protected static final String OVERRIDE_ENDPOINT_PREF_KEY = "override_endpoint_url";
  private static String sOverrideApiKeyInUse;

  @Override
  public void onCreate() {
    super.onCreate();

    if (BuildConfig.DEBUG) {
      activateStrictMode();
    }

    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs_location), MODE_PRIVATE);
    disableNetworkRequestsIfConfigured(sharedPreferences);

    // Clear the configuration cache with null
    Appboy.configure(this, null);
    AppboyConfig.Builder appboyConfigBuilder = new AppboyConfig.Builder();
    setOverrideApiKeyIfConfigured(sharedPreferences, appboyConfigBuilder);
    Appboy.configure(this, appboyConfigBuilder.build());

    String overrideEndpointUrl = sharedPreferences.getString(OVERRIDE_ENDPOINT_PREF_KEY, null);
    Appboy.setAppboyEndpointProvider(new DroidboyEndpointProvider(overrideEndpointUrl));
    int logLevel = getApplicationContext().getSharedPreferences(getString(R.string.log_level_dialog_title), Context.MODE_PRIVATE)
        .getInt(getString(R.string.current_log_level), Log.VERBOSE);
    AppboyLogger.setLogLevel(logLevel);

    if (FrescoLibraryUtils.canUseFresco(getApplicationContext())) {
      Fresco.initialize(getApplicationContext());
    }
  }

  private void activateStrictMode() {
    StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog();
    StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      addDetectLeakedClosableObjects(vmPolicyBuilder);
    }
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

  @TargetApi(11)
  private void addDetectLeakedClosableObjects(StrictMode.VmPolicy.Builder vmPolicyBuilder) {
    vmPolicyBuilder.detectLeakedClosableObjects();
  }
}
