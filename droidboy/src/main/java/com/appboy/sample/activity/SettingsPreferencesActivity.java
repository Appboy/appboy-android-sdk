package com.appboy.sample.activity;

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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.appboy.Appboy;
import com.appboy.AppboyInternal;
import com.appboy.Constants;
import com.appboy.IAppboyImageLoader;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.lrucache.AppboyLruImageLoader;
import com.appboy.models.cards.Card;
import com.appboy.models.outgoing.AttributionData;
import com.appboy.sample.BuildConfig;
import com.appboy.sample.CustomFeedClickActionListener;
import com.appboy.sample.DroidboyApplication;
import com.appboy.sample.MainFragment;
import com.appboy.sample.R;
import com.appboy.sample.imageloading.GlideAppboyImageLoader;
import com.appboy.sample.util.ContentCardsTestingUtil;
import com.appboy.sample.util.LifecycleUtils;
import com.appboy.sample.util.LogcatExportUtil;
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
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;

@SuppressLint({"ApplySharedPref", "ExportedPreferenceActivity"})
@SuppressWarnings("deprecation")
public class SettingsPreferencesActivity extends PreferenceActivity {
  private static final String TAG = AppboyLogger.getAppboyLogTag(SettingsPreferencesActivity.class);
  private static final Map<String, String> API_KEY_TO_APP_MAP;
  private static final int REQUEST_IMAGE_CAPTURE = 271;
  private static final String BRAZE_ENVIRONMENT_DEEPLINK_SCHEME_PATH = "braze://environment";
  private static final String BRAZE_ENVIRONMENT_DEEPLINK_ENDPOINT = "endpoint";
  private static final String BRAZE_ENVIRONMENT_DEEPLINK_API_KEY = "api_key";
  private static final String DEV_DROIDBOY_API_KEY = "da8f263e-1483-4e9f-ac0c-7b40030c8f40";
  private static final String DEV_FIREOS_DROIDBOY_API_KEY = "ecb81855-149f-465c-bab0-0254d6512133";
  private static final String DEV_SDK_ENDPOINT = "https://elsa.braze.com/";

  static {
    Map<String, String> keyToAppMap = new HashMap<>();
    keyToAppMap.put("da8f263e-1483-4e9f-ac0c-7b40030c8f40","App: Droidboy, App group: Stopwatch & Droidboy, Company: Braze");
    keyToAppMap.put("ecb81855-149f-465c-bab0-0254d6512133","App: Fire OS Droidboy, App group: Stopwatch & Droidboy, Company: Braze");
    API_KEY_TO_APP_MAP = Collections.unmodifiableMap(keyToAppMap);
  }

  private SharedPreferences mSharedPreferences;
  private int mAttributionUniqueInt = 0;
  private IAppboyImageLoader mGlideAppboyImageLoader;
  private IAppboyImageLoader mAppboyLruImageLoader;
  private AppboyConfigurationProvider mAppboyConfigurationProvider;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mGlideAppboyImageLoader = new GlideAppboyImageLoader();
    mAppboyLruImageLoader = new AppboyLruImageLoader(getApplicationContext());
    mSharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_location), Context.MODE_PRIVATE);
    mAppboyConfigurationProvider = new AppboyConfigurationProvider(getApplicationContext());

    addPreferencesFromResource(R.xml.preferences);
    setContentView(R.layout.preference_wrapper_view);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle(getString(R.string.settings));

    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_button_droidboy));
    toolbar.setNavigationOnClickListener(view -> onBackPressed());

    setAboutSectionInfo();
    final Preference dataFlushPreference = findPreference("data_flush");
    final Preference setManualLocationPreference = findPreference("set_manual_location");
    final Preference locationRuntimePermissionDialogPreference = findPreference("location_runtime_permission_dialog");
    final Preference openSessionPreference = findPreference("open_session");
    final Preference closeSessionPreference = findPreference("close_session");
    final Preference anonymousUserRevertPreference = findPreference("anonymous_revert");
    final Preference toggleDisableAppboyNetworkRequestsPreference = findPreference("toggle_disable_appboy_network_requests_for_filtered_emulators");
    final Preference logAttributionPreference = findPreference("log_attribution");
    final Preference enableAutomaticNetworkRequestsPreference = findPreference("enable_outbound_network_requests");
    final Preference disableAutomaticNetworkRequestsPreference = findPreference("disable_outbound_network_requests");
    final CheckBoxPreference sortNewsFeed = (CheckBoxPreference) findPreference("sort_feed");
    final SharedPreferences sharedPrefSort = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
    sortNewsFeed.setChecked(sharedPrefSort.getBoolean(getString(R.string.sort_feed), false));
    final CheckBoxPreference setCustomNewsFeedClickActionListener = (CheckBoxPreference) findPreference("set_custom_news_feed_card_click_action_listener");

    final Preference enableSdkPreference = findPreference("enable_sdk_key");
    final Preference disableSdkPreference = findPreference("disable_sdk_key");
    final Preference wipeSdkDataPreference = findPreference("wipe_data_preference_key");

    final Preference enableGlideLibraryPreference = findPreference("glide_image_loader_enable_setting_key");
    final Preference disableGlideLibraryPreference = findPreference("glide_image_loader_disable_setting_key");

    final CheckBoxPreference displayInCutoutPreference = (CheckBoxPreference) findPreference("display_in_full_cutout_setting_key");
    final CheckBoxPreference displayNoLimitsPreference = (CheckBoxPreference) findPreference("display_no_limits_setting_key");

    setManualLocationPreference.setOnPreferenceClickListener(preference -> {
      Appboy.getInstance(this).getCurrentUser().setLastKnownLocation(1.0, 2.0, 3.0, 4.0);
      showToast("Manually set location to latitude 1.0d, longitude 2.0d, altitude 3.0m, accuracy 4.0m.");
      return true;
    });
    locationRuntimePermissionDialogPreference.setOnPreferenceClickListener(preference -> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION);
      } else {
        Toast.makeText(this, "Below Android M there is no need to check for runtime permissions.", Toast.LENGTH_SHORT).show();
      }
      return true;
    });

    disableAutomaticNetworkRequestsPreference.setOnPreferenceClickListener(preference -> {
      Appboy.setOutboundNetworkRequestsOffline(true);
      showToast(getString(R.string.disabled_outbound_network_requests_toast));
      return true;
    });
    enableAutomaticNetworkRequestsPreference.setOnPreferenceClickListener(preference -> {
      Appboy.setOutboundNetworkRequestsOffline(false);
      showToast(getString(R.string.enabled_outbound_network_requests_toast));
      return true;
    });

    dataFlushPreference.setOnPreferenceClickListener(preference -> {
      Appboy.getInstance(this).requestImmediateDataFlush();
      showToast(getString(R.string.data_flush_toast));
      return true;
    });
    openSessionPreference.setOnPreferenceClickListener(preference -> {
      Appboy.getInstance(this).openSession(this);
      return true;
    });
    closeSessionPreference.setOnPreferenceClickListener(preference -> {
      Appboy.getInstance(this).closeSession(this);
      showToast(getString(R.string.close_session_toast));
      return true;
    });
    anonymousUserRevertPreference.setOnPreferenceClickListener(preference -> {
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
    });
    toggleDisableAppboyNetworkRequestsPreference.setOnPreferenceClickListener(preference -> {
      boolean newDisableAppboyNetworkRequestsPreference = !Boolean
          .parseBoolean(getApplicationContext().getSharedPreferences(
              getString(R.string.shared_prefs_location), Context.MODE_PRIVATE)
              .getString(getString(R.string.mock_appboy_network_requests), null));
      SharedPreferences.Editor sharedPreferencesEditor = getApplicationContext().getSharedPreferences(getString(R.string.shared_prefs_location), Context.MODE_PRIVATE).edit();
      sharedPreferencesEditor.putBoolean(getString(R.string.mock_appboy_network_requests), newDisableAppboyNetworkRequestsPreference);
      sharedPreferencesEditor.apply();
      if (newDisableAppboyNetworkRequestsPreference) {
        Toast.makeText(this, "Disabling Braze network requests for selected emulators in the next app run", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, "Enabling Braze network requests for the next app run for all devices", Toast.LENGTH_LONG).show();
      }
      return true;
    });

    logAttributionPreference.setOnPreferenceClickListener(preference -> {
      Appboy.getInstance(getApplicationContext()).getCurrentUser().setAttributionData(new AttributionData("network_val_" + mAttributionUniqueInt,
          "campaign_val_" + mAttributionUniqueInt,
          "adgroup_val_" + mAttributionUniqueInt,
          "creative_val_" + mAttributionUniqueInt));
      mAttributionUniqueInt++;
      showToast("Attribution data sent to server");
      return true;
    });
    sortNewsFeed.setOnPreferenceChangeListener((preference, newValue) -> {
      SharedPreferences sharedPref = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sharedPref.edit();
      editor.putBoolean(getString(R.string.sort_feed), (boolean) newValue);
      editor.apply();
      return true;
    });
    setCustomNewsFeedClickActionListener.setOnPreferenceChangeListener((preference, newValue) -> {
      AppboyFeedManager.getInstance().setFeedCardClickActionListener((boolean) newValue ? new CustomFeedClickActionListener() : null);
      return true;
    });
    wipeSdkDataPreference.setOnPreferenceClickListener(preference -> {
      Appboy.wipeData(this);
      return true;
    });
    enableSdkPreference.setOnPreferenceClickListener(preference -> {
      Appboy.enableSdk(this);
      return true;
    });
    disableSdkPreference.setOnPreferenceClickListener(preference -> {
      Appboy.disableSdk(this);
      return true;
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

    findPreference("environment_barcode_picture_intent_key").setOnPreferenceClickListener((Preference preference) -> {
      // Take a picture via intent
      Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      try {
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
      } catch (Exception e) {
        AppboyLogger.e(TAG, "Failed to handle image capture intent", e);
      }
      return true;
    });
    findPreference("environment_reset_key").setOnPreferenceClickListener((Preference preference) -> {
      SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();
      sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY);
      sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY);

      sharedPreferencesEditor.commit();
      LifecycleUtils.restartApp(this);
      return true;
    });
    findPreference("environment_switch_dev").setOnPreferenceClickListener((Preference preference) -> {
      changeEndpointToDevelopment();
      return true;
    });

    findPreference("content_card_populate_random_cards_setting_key").setOnPreferenceClickListener((Preference preference) -> {
      final List<Card> randomCards = ContentCardsTestingUtil.Companion.createRandomCards(getApplicationContext(), 3);
      for (Card card : randomCards) {
        final String userId = Appboy.getInstance(getApplicationContext()).getCurrentUser().getUserId();
        AppboyInternal.addSerializedContentCardToStorage(getApplicationContext(), card.forJsonPut().toString(), userId);
      }
      return true;
    });
    findPreference("content_card_dismiss_all_cards_setting_key").setOnPreferenceClickListener((Preference preference) -> {
      final List<Card> cachedContentCards = Appboy.getInstance(getApplicationContext()).getCachedContentCards();
      if (cachedContentCards != null) {
        for (Card card : cachedContentCards) {
          card.setIsDismissed(true);
        }
      }
      return true;
    });
    findPreference("logcat_export_file_key").setOnPreferenceClickListener((Preference preference) -> {
      final Uri logcatFileUri = LogcatExportUtil.Companion.exportLogcatToFile(getApplicationContext());
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_STREAM, logcatFileUri);

      // Grant temporary read permission to the content URI
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      startActivity(Intent.createChooser(shareIntent, "Export logcat as a big text file"));
      return true;
    });
  }

  // Displays a toast to the user
  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  private String getApiKeyBackendString() {
    String apiKey = DroidboyApplication.getApiKeyInUse(this);
    String apiKeyTarget = API_KEY_TO_APP_MAP.get(apiKey);
    if (StringUtils.isNullOrBlank(apiKeyTarget)) {
      return "Unknown";
    }
    return apiKeyTarget;
  }

  private String getUserId() {
    String userId = Appboy.getInstance(this).getCurrentUser().getUserId();
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
    RuntimePermissionUtils.handleOnRequestPermissionsResult(this, requestCode, grantResults);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      Bitmap bitmap = (Bitmap) extras.get("data");
      analyzeBitmapForEnvironmentBarcode(bitmap);
    }
  }

  private void setAboutSectionInfo() {
    Preference sdkPreference = findPreference("sdk_version");
    Preference apiKeyPreference = findPreference("api_key");
    Preference pushTokenPreference = findPreference("push_token");
    Preference buildTypePreference = findPreference("build_type");
    Preference versionCodePreference = findPreference("version_code");
    Preference buildNamePreference = findPreference("build_name");
    Preference currentUserIdPreference = findPreference("current_user_id");
    Preference apiKeyBackendPreference = findPreference("api_key_backend");
    Preference branchNamePreference = findPreference("branch_name");
    Preference commitHashPreference = findPreference("commit_hash");
    Preference installTimePreference = findPreference("install_time");
    Preference deviceIdPreference = findPreference("device_id");
    Preference endpointPreference = findPreference("current_endpoint");

    sdkPreference.setSummary(Constants.APPBOY_SDK_VERSION);
    apiKeyPreference.setSummary(DroidboyApplication.getApiKeyInUse(getApplicationContext()));
    String pushToken = Appboy.getInstance(this).getAppboyPushMessageRegistrationId();
    if (StringUtils.isNullOrBlank(pushToken)) {
      pushToken = "None";
    }
    pushTokenPreference.setSummary(pushToken);
    buildTypePreference.setSummary(BuildConfig.BUILD_TYPE);
    versionCodePreference.setSummary(String.valueOf(BuildConfig.VERSION_CODE));
    buildNamePreference.setSummary(BuildConfig.VERSION_NAME);
    currentUserIdPreference.setSummary(getUserId());
    String apiKeyBackendString = getApiKeyBackendString();
    apiKeyBackendPreference.setSummary(apiKeyBackendString);
    commitHashPreference.setSummary(BuildConfig.COMMIT_HASH);
    branchNamePreference.setSummary(BuildConfig.CURRENT_BRANCH);
    installTimePreference.setSummary(BuildConfig.BUILD_TIME);
    deviceIdPreference.setSummary(Appboy.getInstance(getApplicationContext()).getDeviceId());
    endpointPreference.setSummary(Appboy.getAppboyApiEndpoint(Uri.parse(mAppboyConfigurationProvider.getBaseUrlForRequests())).toString());
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

  private void changeEndpointToDevelopment() {
    SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();
    if (Constants.IS_AMAZON) {
      sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, DEV_FIREOS_DROIDBOY_API_KEY);
    } else {
      sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, DEV_DROIDBOY_API_KEY);
    }
    sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, DEV_SDK_ENDPOINT);

    sharedPreferencesEditor.commit();
    LifecycleUtils.restartApp(getApplicationContext());
  }
}
