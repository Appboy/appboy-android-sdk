package com.appboy.sample;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.sample.util.EmulatorDetectionUtils;
import com.appboy.sample.util.SharedPrefsUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;

import java.util.Arrays;
import java.util.Locale;

public class DroidboyApplication extends Application
{
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DroidboyApplication.class.getName());
  private static final String QA_FLAVOR = "QA";
  private static final String OVERRIDE_API_KEY = "f9622241-8e26-4366-8183-1c9e310af6b0";
  private static final Locale OVERRIDE_LOCALE = Locale.CHINA;

  @Override
  public void onCreate()
  {
    super.onCreate();

    activateStrictMode();

    // Disable Appboy network requests if the preference has been set and the current device model matches a list of emulators
    // we don't want to run Appboy on in certain scenarios.
    String disableAppboyNetworkRequestsBooleanString = getApplicationContext().getSharedPreferences(SharedPrefsUtil.SharedPrefsFilename, Context.MODE_PRIVATE).getString(SharedPrefsUtil.DISABLE_APPBOY_NETWORK_REQUESTS_KEY, null);
    if (Boolean.parseBoolean(disableAppboyNetworkRequestsBooleanString) && Arrays.asList(EmulatorDetectionUtils.getEmulatorModelsForAppboyDeactivation()).contains(android.os.Build.MODEL)) {
      Appboy.disableAllAppboyNetworkRequests();
      Log.i(TAG, String.format("Mocking Appboy network requests because preference was set and model was %s", android.os.Build.MODEL));
    }
    if (BuildConfig.FLAVOR.equals(QA_FLAVOR)) {
      Log.i(TAG, "QA build detected, configuring Appboy to clear any existing override key regardless of locale.");
      Appboy.configure(getApplicationContext(), null);
    } else if (Locale.getDefault().equals(OVERRIDE_LOCALE)) {
      Log.i(TAG, String.format("Matched %s locale, configuring Appboy with override key.", OVERRIDE_LOCALE));
      Appboy.configure(getApplicationContext(), OVERRIDE_API_KEY);
    } else {
      Log.i(TAG, String.format("Did not match %s locale, configuring Appboy to clear any existing override key.", OVERRIDE_LOCALE));
      Appboy.configure(getApplicationContext(), null);
    }
    Appboy.setAppboyEndpointProvider(new DummyEndpointProvider());
    Appboy.setCustomAppboyNotificationFactory(new DroidboyNotificationFactory());

    Fresco.initialize(getApplicationContext());

    Stetho.initialize(
            Stetho.newInitializerBuilder(this)
                    .enableDumpapp(
                            Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(
                    Stetho.defaultInspectorModulesProvider(this))
                    .build());
  }

  private void activateStrictMode() {
    // Set the activity to Strict mode so that we get LogCat warnings when code misbehaves on the main thread.
    if (BuildConfig.DEBUG) {
      StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog();
      StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .penaltyLog()
        .penaltyDeath();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        // Add detectLeakedClosableObjects (available from API 11) if it's available
        addDetectLeakedClosableObjects(vmPolicyBuilder);
      }
      StrictMode.setThreadPolicy(threadPolicyBuilder.build());
      StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }
  }

  @TargetApi(11)
  private void addDetectLeakedClosableObjects(StrictMode.VmPolicy.Builder vmPolicyBuilder) {
    vmPolicyBuilder.detectLeakedClosableObjects();
  }
}