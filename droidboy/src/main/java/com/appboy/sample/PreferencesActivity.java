package com.appboy.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.IAppboyImageLoader;
import com.appboy.lrucache.AppboyLruImageLoader;
import com.appboy.models.outgoing.AttributionData;
import com.appboy.sample.imageloading.GlideAppboyImageLoader;
import com.appboy.sample.util.LifecycleUtils;
import com.appboy.sample.util.RuntimePermissionUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.feed.AppboyFeedManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.branch.referral.Branch;

public class PreferencesActivity extends PreferenceActivity {
  private static final String TAG = AppboyLogger.getAppboyLogTag(PreferencesActivity.class);
  private static final Map<String, String> API_KEY_TO_APP_MAP;
  private static final int REQUEST_IMAGE_CAPTURE = 271;
  private static final String BRAZE_ENVIRONMENT_DEEPLINK_SCHEME_PATH = "braze://environment";
  private static final String BRAZE_ENVIRONMENT_DEEPLINK_ENDPOINT = "endpoint";
  private static final String BRAZE_ENVIRONMENT_DEEPLINK_API_KEY = "api_key";

  static {
    Map<String, String> keyToAppMap = new HashMap<>();
    keyToAppMap.put("1d502a81-f92f-48d4-96a7-1cbafc42b425","App:Droidboy, App group:Droidboy, Company:Appboy, Environment:Staging");
    keyToAppMap.put("b9514ba7-993b-4e81-b339-8447dde48547","App:Fireos, App group:Droidboy, Company:Appboy, Environment:Staging");
    API_KEY_TO_APP_MAP = Collections.unmodifiableMap(keyToAppMap);
  }

  private String mEnvironmentBarcodePhotoPath;
  private SharedPreferences mSharedPreferences;
  private int mAttributionUniqueInt = 0;
  private IAppboyImageLoader mGlideAppboyImageLoader;
  private IAppboyImageLoader mAppboyLruImageLoader;

  // This lint suppression is for the shared prefs commits done for app restarts. You can't apply
  // annotations on lambdas so this has to go here
  @SuppressLint("ApplySharedPref")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mGlideAppboyImageLoader = new GlideAppboyImageLoader();
    mAppboyLruImageLoader = new AppboyLruImageLoader(getApplicationContext());
    mSharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_location), Context.MODE_PRIVATE);

    addPreferencesFromResource(R.xml.preferences);
    setContentView(R.layout.preference_wrapper_view);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(getString(R.string.settings));

    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_button_droidboy));
    toolbar.setNavigationOnClickListener(view -> onBackPressed());

    Preference dataFlushPreference = findPreference("data_flush");
    Preference setManualLocationPreference = findPreference("set_manual_location");
    Preference locationRuntimePermissionDialogPreference = findPreference("location_runtime_permission_dialog");
    Preference openSessionPreference = findPreference("open_session");
    Preference closeSessionPreference = findPreference("close_session");
    Preference anonymousUserRevertPreference = findPreference("anonymous_revert");
    Preference sdkPreference = findPreference("sdk_version");
    Preference apiKeyPreference = findPreference("api_key");
    Preference pushTokenPreference = findPreference("push_token");
    Preference buildTypePreference = findPreference("build_type");
    Preference flavorPreference = findPreference("flavor");
    Preference versionCodePreference = findPreference("version_code");
    Preference buildNamePreference = findPreference("build_name");
    Preference currentUserIdPreference = findPreference("current_user_id");
    Preference apiKeyBackendPreference = findPreference("api_key_backend");
    Preference branchNamePreference = findPreference("branch_name");
    Preference commitHashPreference = findPreference("commit_hash");
    Preference installTimePreference = findPreference("install_time");
    Preference deviceIdPreference = findPreference("device_id");
    Preference toggleDisableAppboyNetworkRequestsPreference = findPreference("toggle_disable_appboy_network_requests_for_filtered_emulators");
    Preference logAttributionPreference = findPreference("log_attribution");
    Preference enableAutomaticNetworkRequestsPreference = findPreference("enable_outbound_network_requests");
    Preference disableAutomaticNetworkRequestsPreference = findPreference("disable_outbound_network_requests");
    Preference brazeEnvironmentBarcodePreference = findPreference("environment_barcode_picture_intent_key");
    Preference brazeEnvironmentResetPreference = findPreference("environment_reset_key");
    CheckBoxPreference sortNewsFeed = (CheckBoxPreference) findPreference("sort_feed");
    SharedPreferences sharedPrefSort = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
    sortNewsFeed.setChecked(sharedPrefSort.getBoolean(getString(R.string.sort_feed), false));
    CheckBoxPreference setCustomNewsFeedClickActionListener = (CheckBoxPreference) findPreference("set_custom_news_feed_card_click_action_listener");

    Preference enableSdkPreference = findPreference("enable_sdk_key");
    Preference disableSdkPreference = findPreference("disable_sdk_key");
    Preference wipeSdkDataPreference = findPreference("wipe_data_preference_key");

    Preference enableGlideLibraryPreference = findPreference("glide_image_loader_enable_setting_key");
    Preference disableGlideLibraryPreference = findPreference("glide_image_loader_disable_setting_key");

    CheckBoxPreference displayInCutoutPreference = (CheckBoxPreference) findPreference("display_in_full_cutout_setting_key");
    CheckBoxPreference displayNoLimitsPreference = (CheckBoxPreference) findPreference("display_no_limits_setting_key");

    sdkPreference.setSummary(Constants.APPBOY_SDK_VERSION);
    apiKeyPreference.setSummary(DroidboyApplication.getApiKeyInUse(getApplicationContext()));
    String pushToken = Appboy.getInstance(PreferencesActivity.this).getAppboyPushMessageRegistrationId();
    if (StringUtils.isNullOrBlank(pushToken)) {
      pushToken = "None";
    }
    pushTokenPreference.setSummary(pushToken);
    buildTypePreference.setSummary(BuildConfig.BUILD_TYPE);
    flavorPreference.setSummary(BuildConfig.FLAVOR);
    versionCodePreference.setSummary(String.valueOf(BuildConfig.VERSION_CODE));
    buildNamePreference.setSummary(BuildConfig.VERSION_NAME);
    currentUserIdPreference.setSummary(getUserId());
    String apiKeyBackendString = getApiKeyBackendString();
    apiKeyBackendPreference.setSummary(apiKeyBackendString);
    commitHashPreference.setSummary(BuildConfig.COMMIT_HASH);
    branchNamePreference.setSummary(BuildConfig.CURRENT_BRANCH);
    installTimePreference.setSummary(BuildConfig.BUILD_TIME);
    deviceIdPreference.setSummary(Appboy.getInstance(getApplicationContext()).getDeviceId());
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

    disableAutomaticNetworkRequestsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.setOutboundNetworkRequestsOffline(true);
        showToast(getString(R.string.disabled_outbound_network_requests_toast));
        return true;
      }
    });
    enableAutomaticNetworkRequestsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.setOutboundNetworkRequestsOffline(false);
        showToast(getString(R.string.enabled_outbound_network_requests_toast));
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
    openSessionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).openSession(PreferencesActivity.this);
        return true;
      }
    });
    closeSessionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).closeSession(PreferencesActivity.this);
        showToast(getString(R.string.close_session_toast));
        return true;
      }
    });
    anonymousUserRevertPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      @SuppressLint("ApplySharedPref")
      public boolean onPreferenceClick(Preference preference) {
        // Note that .commit() is used here since we're restarting the process and thus need to immediately flush all shared prefs changes to disk
        SharedPreferences userSharedPreferences = getSharedPreferences("com.appboy.offline.storagemap", Context.MODE_PRIVATE);
        userSharedPreferences
            .edit()
            .clear()
            .commit();
        SharedPreferences droidboySharedPrefs = getSharedPreferences("droidboy", Context.MODE_PRIVATE);
        droidboySharedPrefs
            .edit()
            .remove(MainFragment.USER_ID_KEY)
            .commit();
        LifecycleUtils.restartApp(getApplicationContext());
        return true;
      }
    });
    toggleDisableAppboyNetworkRequestsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        boolean newDisableAppboyNetworkRequestsPreference = !Boolean
            .parseBoolean(getApplicationContext().getSharedPreferences(
                getString(R.string.shared_prefs_location), Context.MODE_PRIVATE)
                .getString(getString(R.string.mock_appboy_network_requests), null));
        SharedPreferences.Editor sharedPreferencesEditor = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs_location), Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean(getString(R.string.mock_appboy_network_requests), newDisableAppboyNetworkRequestsPreference);
        sharedPreferencesEditor.apply();
        if (newDisableAppboyNetworkRequestsPreference) {
          Toast.makeText(PreferencesActivity.this, "Disabling Braze network requests for selected emulators in the next app run", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(PreferencesActivity.this, "Enabling Braze network requests for the next app run for all devices", Toast.LENGTH_LONG).show();
        }
        return true;
      }
    });

    logAttributionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(getApplicationContext()).getCurrentUser().setAttributionData(new AttributionData("network_val_" + mAttributionUniqueInt,
            "campaign_val_" + mAttributionUniqueInt,
            "adgroup_val_" + mAttributionUniqueInt,
            "creative_val_" + mAttributionUniqueInt));
        mAttributionUniqueInt++;
        showToast("Attribution data sent to server");
        return true;
      }
    });
    sortNewsFeed.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.sort_feed), (boolean) newValue);
        editor.apply();
        return true;
      }
    });
    setCustomNewsFeedClickActionListener.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        AppboyFeedManager.getInstance().setFeedCardClickActionListener((boolean) newValue ? new CustomFeedClickActionListener() : null);
        return true;
      }
    });
    wipeSdkDataPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.wipeData(PreferencesActivity.this);
        return true;
      }
    });
    enableSdkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.enableSdk(PreferencesActivity.this);
        return true;
      }
    });
    disableSdkPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.disableSdk(PreferencesActivity.this);
        return true;
      }
    });

    enableGlideLibraryPreference.setOnPreferenceClickListener(preference -> {
      Appboy.getInstance(getApplicationContext()).setAppboyImageLoader(mGlideAppboyImageLoader);
      showToast("Glide enabled");
      return true;
    });
    disableGlideLibraryPreference.setOnPreferenceClickListener(preference -> {
      Appboy.getInstance(getApplicationContext()).setAppboyImageLoader(mAppboyLruImageLoader);
      showToast("Glide disabled. Default Image loader in use.");
      return true;
    });

    displayInCutoutPreference.setOnPreferenceClickListener(preference -> {
      // Restart the app to force onCreate() to re-run
      // Note that an app restart won't commit prefs changes so we have to do it manually
      PreferenceManager.getDefaultSharedPreferences(this).edit()
          .putBoolean("display_in_full_cutout_setting_key", ((CheckBoxPreference) preference).isChecked())
          .commit();
      LifecycleUtils.restartApp(getApplicationContext());
      return true;
    });
    displayNoLimitsPreference.setOnPreferenceClickListener(preference -> {
      // Restart the app to force onCreate() to re-run
      // Note that an app restart won't commit prefs changes so we have to do it manually
      PreferenceManager.getDefaultSharedPreferences(this).edit()
          .putBoolean("display_no_limits_setting_key", ((CheckBoxPreference) preference).isChecked())
          .commit();
      LifecycleUtils.restartApp(getApplicationContext());
      return true;
    });

    brazeEnvironmentBarcodePreference.setOnPreferenceClickListener((Preference preference) -> {
      // Take a picture via intent
      Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
      }
      return true;
    });
    brazeEnvironmentResetPreference.setOnPreferenceClickListener((Preference preference) -> {
      SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();
      sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY);
      sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY);

      sharedPreferencesEditor.commit();
      LifecycleUtils.restartApp(this);
      return true;
    });
  }

  // Displays a toast to the user
  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  private String getApiKeyBackendString() {
    String apiKey = PreferencesActivity.this.getResources().getString(R.string.com_appboy_api_key);
    String apiKeyTarget = API_KEY_TO_APP_MAP.get(apiKey);
    if (StringUtils.isNullOrBlank(apiKeyTarget)) {
      return "Unknown";
    }
    return apiKeyTarget;
  }

  private String getUserId() {
    String userId = Appboy.getInstance(PreferencesActivity.this).getCurrentUser().getUserId();
    if (StringUtils.isNullOrBlank(userId)) {
      userId = "Anonymous User";
    }
    return userId;
  }

  @Override
  public void onStart() {
    super.onStart();
    try {
      Branch branch = Branch.getInstance();
      branch.initSession((referringParams, error) -> {
        if (error == null) {
          String param1 = referringParams.optString("$param_1", "");
          String param2 = referringParams.optString("$param_2", "");
          if (param1.equals("hello")) {
            AppboyLogger.i(TAG, "This activity was opened by a Branch deep link with custom param 1.");
          } else if (param2.equals("goodbye")) {
            AppboyLogger.i(TAG, "This activity was opened by a Branch deep link with custom param 2.");
          } else {
            AppboyLogger.i(TAG, "This activity was opened by a Branch deep link with no custom params!");
          }
        } else {
          Log.i(TAG, error.getMessage());
        }
      }, this.getIntent().getData(), this);
    } catch (Exception e) {
      Log.e(TAG, "Exception occurred while initializing Branch", e);
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    this.setIntent(intent);
  }

  @Override
  public void onResume() {
    super.onResume();

    // Shows a toast if the activity detects that it was opened via a deep link.
    Bundle extras = getIntent().getExtras();
    if (extras != null && Constants.APPBOY.equals(extras.getString(getResources().getString(R.string.source_key)))) {
      showToast("This activity was opened by a deep link!");
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    Branch.getInstance(getApplicationContext()).closeSession();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    RuntimePermissionUtils.handleOnRequestPermissionsResult(PreferencesActivity.this, requestCode, grantResults);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      Bitmap bitmap = (Bitmap) extras.get("data");
      analyzeBitmapForEnvironmentBarcode(bitmap);
    }
  }

  private void analyzeBitmapForEnvironmentBarcode(final Bitmap bitmap) {
    // Build the barcode detector
    FirebaseVisionBarcodeDetectorOptions options =
        new FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build();

    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
    FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
        .getVisionBarcodeDetector(options);

    detector.detectInImage(image)
        .addOnSuccessListener(barcodes -> {
          if (barcodes.isEmpty()) {
            showToast("Couldn't find barcode. Please try again!");
          } else {
            for (FirebaseVisionBarcode barcode : barcodes) {
              final String rawValue = barcode.getRawValue();
              if (rawValue.startsWith(BRAZE_ENVIRONMENT_DEEPLINK_SCHEME_PATH)) {
                showToast("Found barcode: " + rawValue);
                setEnvironmentViaDeepLink(rawValue);
              }
            }
          }
        })
        .addOnFailureListener(e -> AppboyLogger.e(TAG, "Failed to parse barcode bitmap", e))
        .addOnCompleteListener(e -> bitmap.recycle());
  }

  /**
   * Braze deep link in the form
   * braze://environment?endpoint=ENDPOINT_HERE&api_key=API_KEY_HERE
   */
  @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
  private void setEnvironmentViaDeepLink(String environmentText) {
    Uri uri = Uri.parse(environmentText);
    String endpoint = uri.getQueryParameter(BRAZE_ENVIRONMENT_DEEPLINK_ENDPOINT);
    String apiKey = uri.getQueryParameter(BRAZE_ENVIRONMENT_DEEPLINK_API_KEY);

    AppboyLogger.i(TAG, "Using environment endpoint: " + endpoint);
    AppboyLogger.i(TAG, "Using environment api key: " + apiKey);

    StringBuilder message = new StringBuilder()
        .append("Looks correct? 👌")
        .append("\n\n")
        .append("New environment endpoint: ")
        .append("\n")
        .append(endpoint)
        .append("\n\n")
        .append("New environment api key: ")
        .append("\n")
        .append(apiKey);

    new AlertDialog.Builder(this)
        .setTitle("Changing Droidboy environment")
        .setMessage(message.toString())

        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
          SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();
          sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, apiKey);
          sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, endpoint);

          sharedPreferencesEditor.commit();
          LifecycleUtils.restartApp(getApplicationContext());
        })

        // A null listener allows the button to dismiss the dialog and take no further action.
        .setNegativeButton(android.R.string.no, null)
        .setIcon(android.R.drawable.ic_dialog_info)
        .show();
  }
}
