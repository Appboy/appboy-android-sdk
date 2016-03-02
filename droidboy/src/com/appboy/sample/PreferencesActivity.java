package com.appboy.sample;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.outgoing.AttributionData;
import com.appboy.sample.util.RuntimePermissionUtils;
import com.appboy.sample.util.SharedPrefsUtil;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

public class PreferencesActivity extends PreferenceActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, PreferencesActivity.class.getName());
  private int attributionUniqueInt = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    Preference dataFlushPreference = findPreference("data_flush");
    Preference requestInAppMessagePreference = findPreference("request_inappmessage");
    Preference setManualLocationPreference = findPreference("set_manual_location");
    Preference locationRuntimePermissionDialogPreference = findPreference("location_runtime_permission_dialog");
    Preference openSessionPreference = findPreference("open_session");
    Preference closeSessionPreference = findPreference("close_session");
    Preference aboutPreference = findPreference("about");
    Preference externalStorageRuntimePermissionDialogPreference = findPreference("external_storage_runtime_permission_dialog");
    Preference toggleDisableAppboyNetworkRequestsPreference = findPreference("toggle_disable_appboy_network_requests_for_filtered_emulators");
    Preference toggleDisableAppboyLoggingPreference = findPreference("toggle_disable_appboy_logging");
    Preference getRegistrationIdPreference = findPreference("get_registration_id");
    Preference logAttributionPreference = findPreference("log_attribution");

    aboutPreference.setSummary(String.format(getResources().getString(R.string.about_summary), Constants.APPBOY_SDK_VERSION));

    setManualLocationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).getCurrentUser().setLastKnownLocation(1.0, 2.0, 3.0, 4.0);
        showToast("Manually set location to latitude 1.0d, longitude 2.0d, altitude 3.0m, accuracy 4.0m.");
        return true;
      }
    });
    locationRuntimePermissionDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION);
        } else {
          Toast.makeText(PreferencesActivity.this, "Below Android M there is no need to check for runtime permissions.", Toast.LENGTH_SHORT).show();
        }
        return true;
      }
    });

    dataFlushPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).requestImmediateDataFlush();
        showToast(getString(R.string.data_flush_toast));
        return true;
      }
    });
    requestInAppMessagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).requestInAppMessageRefresh();
        showToast(getString(R.string.requested_inappmessage_toast));
        return true;
      }
    });
    openSessionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (Appboy.getInstance(PreferencesActivity.this).openSession(PreferencesActivity.this)) {
          showToast(getString(R.string.open_session_toast));
        } else {
          showToast(getString(R.string.resume_session_toast));
        }
        return true;
      }
    });
    closeSessionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (Appboy.getInstance(PreferencesActivity.this).closeSession(PreferencesActivity.this)) {
          showToast(getString(R.string.close_session_toast));
        } else {
          showToast(getString(R.string.no_session_toast));
        }
        return true;
      }
    });
    externalStorageRuntimePermissionDialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RuntimePermissionUtils.DROIDBOY_PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
          Toast.makeText(PreferencesActivity.this, "Below Android M there is no need to check for runtime permissions.", Toast.LENGTH_SHORT).show();
        }
        return true;
      }
    });
    toggleDisableAppboyNetworkRequestsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        boolean newDisableAppboyNetworkRequestsPreference = !Boolean
            .parseBoolean(getApplicationContext().getSharedPreferences(
                SharedPrefsUtil.SharedPrefsFilename, Context.MODE_PRIVATE)
                .getString(SharedPrefsUtil.DISABLE_APPBOY_NETWORK_REQUESTS_KEY, null));
        SharedPreferences.Editor sharedPreferencesEditor = getApplicationContext().getSharedPreferences(SharedPrefsUtil.SharedPrefsFilename, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString(SharedPrefsUtil.DISABLE_APPBOY_NETWORK_REQUESTS_KEY, String.valueOf(newDisableAppboyNetworkRequestsPreference));
        sharedPreferencesEditor.apply();
        if (newDisableAppboyNetworkRequestsPreference) {
          Toast.makeText(PreferencesActivity.this, "Disabling Appboy network requests for selected emulators in the next app run", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(PreferencesActivity.this, "Enabling Appboy network requests for the next app run for all devices", Toast.LENGTH_LONG).show();
        }
        return true;
      }
    });

    toggleDisableAppboyLoggingPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (AppboyLogger.LogLevel != Log.VERBOSE) {
          AppboyLogger.LogLevel = Log.VERBOSE;
          showToast("Set log level back to VERBOSE to show all Appboy messages.");
        } else {
          AppboyLogger.LogLevel = AppboyLogger.SUPPRESS;
          showToast("Disabled Appboy Logging.");
        }
        return true;
      }
    });

    getRegistrationIdPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        showToast("Registration Id: " + Appboy.getInstance(PreferencesActivity.this).getAppboyPushMessageRegistrationId());
        return true;
      }
    });
    logAttributionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(getApplicationContext()).getCurrentUser().setAttributionData(new AttributionData("network_val_" + attributionUniqueInt,
            "campaign_val_" + attributionUniqueInt,
            "adgroup_val_" + attributionUniqueInt,
            "creative_val_" + attributionUniqueInt));
        attributionUniqueInt++;
        showToast("Attribution data sent to server");
        return true;
      }
    });
  }

  // Displays a toast to the user
  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session. You can now start logging custom events.
    Appboy.getInstance(this).openSession(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers the AppboyInAppMessageManager for the current Activity. This Activity will now listen for
    // in-app messages from Appboy.
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(this);

    // Shows a toast if the activity detects that it was opened via a deep link.
    Bundle extras = getIntent().getExtras();
    if (extras != null && Constants.APPBOY.equals(extras.getString(AppboyBroadcastReceiver.SOURCE_KEY))) {
      showToast("This activity was opened by a deep link!");
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    // Unregisters the AppboyInAppMessageManager.
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession(this);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    RuntimePermissionUtils.handleOnRequestPermissionsResult(PreferencesActivity.this, requestCode, grantResults);
  }
}
