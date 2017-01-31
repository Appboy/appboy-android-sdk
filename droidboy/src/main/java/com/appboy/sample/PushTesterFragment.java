package com.appboy.sample;

import android.Manifest;
import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.appboy.Constants;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.push.AppboyNotificationUtils;
import com.appboy.sample.util.RuntimePermissionUtils;
import com.appboy.sample.util.SpinnerUtils;
import com.appboy.support.PermissionUtils;
import com.appboy.support.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PushTesterFragment extends Fragment implements AdapterView.OnItemSelectedListener {
  protected static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, PushTesterFragment.class.getName());
  private static final String[] WEAR_EXTRA_PAGE_TITLES = new String[]{"Extra page (title)", "So Many Pages! (title)", "Last Page (title)"};
  private static final String[] WEAR_EXTRA_PAGE_CONTENTS = new String[]{"Space (content)",
      "The Final Frontier (content)",
      "There's a lot of text here. There's so much text here! So many lines! This is also the last extra page (content)"};
  private static final String TITLE = "Title";
  private static final String CONTENT = "Content";
  private static final String BIG_TITLE = "Big Title";
  private static final String BIG_SUMMARY = "Big Summary";
  private static final String SUMMARY_TEXT = "Summary Text";
  private AppboyConfigurationProvider mAppConfigurationProvider;
  private NotificationManagerCompat mNotificationManager;
  private String mPriority = String.valueOf(Notification.PRIORITY_DEFAULT);
  private String mImage;
  private String mClickActionUrl;
  private String mCategory;
  private String mVisibility;
  private String mWearBackgroundImageUrl;
  private String mActionType;
  private String mAccentColorString;
  private String mLargeIconString;
  private boolean mUseSummary = false;
  private boolean mUseBigSummary = false;
  private boolean mUseImage = false;
  private boolean mShouldOverflowText = false;
  private boolean mUseBigTitle = false;
  private boolean mUseClickAction = false;
  private boolean mUseCategory = false;
  private boolean mUseVisibility = false;
  private boolean mSetPublicVersion = false;
  private boolean mSetAccentColor = false;
  private boolean mSetLargeIcon = false;
  private boolean mWearHideAppIcon = false;
  private boolean mWearUseBackgroundImage = false;
  private boolean mWearAddExtraPages = false;
  private boolean mTestTriggerFetch = false;
  private View mView;

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    mView = layoutInflater.inflate(R.layout.push_tester, container, false);

    mNotificationManager = NotificationManagerCompat.from(getContext());

    ((CheckBox) mView.findViewById(R.id.push_tester_big_title)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mUseBigTitle = isChecked;
      }
    });
    ((CheckBox) mView.findViewById(R.id.push_tester_summary)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mUseSummary = isChecked;
      }
    });
    ((CheckBox) mView.findViewById(R.id.push_tester_big_summary)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mUseBigSummary = isChecked;
      }
    });
    ((CheckBox) mView.findViewById(R.id.push_tester_overflow_text)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mShouldOverflowText = isChecked;
      }
    });
    ((CheckBox) mView.findViewById(R.id.push_tester_set_public_version)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mSetPublicVersion = isChecked;
      }
    });
    ((CheckBox) mView.findViewById(R.id.push_tester_wear_hide_app_icon)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mWearHideAppIcon = isChecked;
      }
    });
    ((CheckBox) mView.findViewById(R.id.push_tester_wear_add_extra_pages)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mWearAddExtraPages = isChecked;
      }
    });
    ((CheckBox) mView.findViewById(R.id.push_tester_test_triggers)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mTestTriggerFetch = isChecked;
      }
    });

    // Creates the push image spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_image_spinner), this, R.array.push_image_options);

    // Creates the push priority spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_priority_spinner), this, R.array.push_priority_options);

    // Creates the push click action spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_click_action_spinner), this, R.array.push_click_action_options);

    // Creates the notification category spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_category_spinner), this, R.array.push_category_options);

    // Creates the visibility spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_visibility_spinner), this, R.array.push_visibility_options);

    // Creates the Wear background image spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_wear_background_image_spinner), this, R.array.push_wear_background_image_options);

    // Creates the push image spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_image_spinner), this, R.array.push_image_options);

    // Creates the push action spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_action_spinner), this, R.array.push_action_options);

    // Creates the push accent color spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_accent_color_spinner), this, R.array.push_accent_color_options);

    // Creates the large icon spinner.
    SpinnerUtils.setUpSpinner((Spinner) mView.findViewById(R.id.push_large_icon_spinner), this, R.array.push_large_icon_options);

    mAppConfigurationProvider = new AppboyConfigurationProvider(getContext());
    Button pushTestButton = (Button) mView.findViewById(R.id.test_push_button);
    pushTestButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View clickedView) {
        (new Thread(new Runnable() {
          public void run() {
            Bundle notificationExtras = new Bundle();
            notificationExtras.putString(Constants.APPBOY_PUSH_TITLE_KEY, generateDisplayValue(TITLE));
            notificationExtras.putString(Constants.APPBOY_PUSH_CONTENT_KEY, generateDisplayValue(CONTENT));
            notificationExtras.putString(Constants.APPBOY_PUSH_WEAR_HIDE_APP_ICON_KEY, String.valueOf(mWearHideAppIcon));

            int notificationId = AppboyNotificationUtils.getNotificationId(notificationExtras);
            notificationExtras.putInt(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);
            notificationExtras = addActionButtons(notificationExtras);

            if (mUseSummary) {
              notificationExtras.putString(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY, generateDisplayValue(SUMMARY_TEXT));
            }
            if (mUseClickAction) {
              notificationExtras.putString(Constants.APPBOY_PUSH_DEEP_LINK_KEY, mClickActionUrl);
            }
            notificationExtras.putString(Constants.APPBOY_PUSH_PRIORITY_KEY, mPriority);
            if (mUseBigTitle) {
              notificationExtras.putString(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY, generateDisplayValue(BIG_TITLE));
            }
            if (mUseBigSummary) {
              notificationExtras.putString(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY, generateDisplayValue(BIG_SUMMARY));
            }
            if (mUseCategory) {
              notificationExtras.putString(Constants.APPBOY_PUSH_CATEGORY_KEY, mCategory);
            }
            if (mUseVisibility) {
              notificationExtras.putString(Constants.APPBOY_PUSH_VISIBILITY_KEY, mVisibility);
            }
            if (mSetPublicVersion) {
              try {
                notificationExtras.putString(Constants.APPBOY_PUSH_PUBLIC_NOTIFICATION_KEY, getPublicVersionNotificationString());
              } catch (JSONException jsonException) {
                Log.e(TAG, "Failed to created public version notification JSON string", jsonException);
              }
            }
            if (mWearUseBackgroundImage) {
              notificationExtras.putString(Constants.APPBOY_PUSH_WEAR_BACKGROUND_IMAGE_URL_KEY, mWearBackgroundImageUrl);
            }
            if (mWearAddExtraPages) {
              addWearExtraPagesToNotificationBuilder(notificationExtras);
            }
            if (mTestTriggerFetch) {
              notificationExtras.putString(Constants.APPBOY_PUSH_FETCH_TEST_TRIGGERS_KEY, "true");
            }
            if (mSetAccentColor) {
              notificationExtras.putString(Constants.APPBOY_PUSH_ACCENT_KEY, mAccentColorString);
            }
            if (mSetLargeIcon) {
              notificationExtras.putString(Constants.APPBOY_PUSH_LARGE_ICON_KEY, mLargeIconString);
            }

            // Manually build the appboy extras bundle.
            Bundle appboyExtras = new Bundle();
            if (mUseImage) {
              if (Constants.IS_AMAZON) {
                // Amazon flattens the extras bundle so we have to put it in the regular notification
                // extras to imitate that functionality.
                notificationExtras.putString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY, mImage.replaceAll("&amp;", "&"));
                appboyExtras = new Bundle(notificationExtras);
              } else {
                appboyExtras.putString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY, mImage.replaceAll("&amp;", "&"));
              }
            }

            notificationExtras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, appboyExtras);
            Notification notification = AppboyNotificationUtils.getActiveNotificationFactory().createNotification(
                mAppConfigurationProvider,
                getContext(),
                notificationExtras,
                appboyExtras);

            if (notification != null) {
              mNotificationManager.notify(Constants.APPBOY_PUSH_NOTIFICATION_TAG, AppboyNotificationUtils.getNotificationId(notificationExtras), notification);
            }
          }
        })).start();
      }
    });
    return mView;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    switch (parent.getId()) {
      case R.id.push_image_spinner:
        String pushImageUriString = getResources().getStringArray(R.array.push_image_values)[parent.getSelectedItemPosition()];
        if (!StringUtils.isNullOrBlank(pushImageUriString)) {
          mUseImage = true;
          mImage = pushImageUriString;
          if (mActionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_SHARE)) {
            requestLocalStoragePermission();
          }
        } else {
          mUseImage = false;
        }
        break;
      case R.id.push_wear_background_image_spinner:
        String pushWearBackgroundImageUriString = getResources().getStringArray(R.array.push_wear_background_image_values)[parent.getSelectedItemPosition()];
        if (!StringUtils.isNullOrBlank(pushWearBackgroundImageUriString)) {
          mWearUseBackgroundImage = true;
          mWearBackgroundImageUrl = pushWearBackgroundImageUriString;
          if (mActionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_SHARE)) {
            requestLocalStoragePermission();
          }
        } else {
          mWearUseBackgroundImage = false;
        }
        break;
      case R.id.push_priority_spinner:
        mPriority = getResources().getStringArray(R.array.push_priority_values)[parent.getSelectedItemPosition()];
        break;
      case R.id.push_click_action_spinner:
        String pushClickActionUriString = getResources().getStringArray(R.array.push_click_action_values)[parent.getSelectedItemPosition()];
        if (!StringUtils.isNullOrBlank(pushClickActionUriString)) {
          mUseClickAction = true;
          mClickActionUrl = pushClickActionUriString;
        } else {
          mUseClickAction = false;
        }
        break;
      case R.id.push_category_spinner:
        mCategory = getResources().getStringArray(R.array.push_category_values)[parent.getSelectedItemPosition()];
        if (!StringUtils.isNullOrBlank(mCategory)) {
          mUseCategory = true;
        } else {
          mUseCategory = false;
        }
        break;
      case R.id.push_visibility_spinner:
        mVisibility = getResources().getStringArray(R.array.push_visibility_values)[parent.getSelectedItemPosition()];
        if (!StringUtils.isNullOrBlank(mVisibility)) {
          mUseVisibility = true;
        } else {
          mUseVisibility = false;
        }
        break;
      case R.id.push_action_spinner:
        mActionType = getResources().getStringArray(R.array.push_action_values)[parent.getSelectedItemPosition()];
        if (mActionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_SHARE) && (mUseImage || mWearUseBackgroundImage)) {
          requestLocalStoragePermission();
        }
        break;
      case R.id.push_accent_color_spinner:
        String pushAccentColorString = getResources().getStringArray(R.array.push_accent_color_values)[parent.getSelectedItemPosition()];
        if (!StringUtils.isNullOrBlank(pushAccentColorString)) {
          mSetAccentColor = true;
          // Convert our hexadecimal string to the decimal expected by Appboy
          mAccentColorString = Long.decode(pushAccentColorString).toString();
        } else {
          mSetAccentColor = false;
        }
        break;
      case R.id.push_large_icon_spinner:
        String largeIconString = getResources().getStringArray(R.array.push_large_icon_values)[parent.getSelectedItemPosition()];
        if (!StringUtils.isNullOrBlank(largeIconString)) {
          mSetLargeIcon = true;
          mLargeIconString = largeIconString;
        } else {
          mSetLargeIcon = false;
        }
        break;
      default:
        Log.e(TAG, "Item selected for unknown spinner");
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    RuntimePermissionUtils.handleOnRequestPermissionsResult(getContext(), requestCode, grantResults);
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing
  }

  private String getPublicVersionNotificationString() throws JSONException {
    JSONObject publicVersionJSON = new JSONObject();
    publicVersionJSON.put(Constants.APPBOY_PUSH_TITLE_KEY, "Don't open in public (title)");
    publicVersionJSON.put(Constants.APPBOY_PUSH_CONTENT_KEY, "Please (content)");
    publicVersionJSON.put(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY, "Summary");
    return publicVersionJSON.toString();
  }

  private Bundle addActionButtons(Bundle notificationExtras) {
    if (StringUtils.isNullOrBlank(mActionType)) {
      return notificationExtras;
    }
    if (mActionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_OPEN)) {
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), Constants.APPBOY_PUSH_ACTION_TYPE_OPEN);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Open app");
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), getString(R.string.droidboy_close_button_text));
    } else if (mActionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_URI)) {
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Appboy");
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "0"), getString(R.string.appboy_homepage_url));
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), "Google");
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "1"), getString(R.string.google_url));
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "2"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "2"), getString(R.string.droidboy_close_button_text));
    } else if (mActionType.equals("deep_link")) {
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Preferences");
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "0"), getString(R.string.droidboy_deep_link));
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), "Telephone");
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "1"), getString(R.string.telephone_uri));
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "2"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "2"), getString(R.string.droidboy_close_button_text));
    } else if (mActionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_SHARE)) {
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), Constants.APPBOY_PUSH_ACTION_TYPE_SHARE);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Share");
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), getString(R.string.droidboy_close_button_text));
    } else if (mActionType.equals(getString(R.string.droidboy_custom_action))) {
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), getString(R.string.droidboy_custom_action));
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Droidboy!");
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
      notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), getString(R.string.droidboy_close_button_text));
    }
    return notificationExtras;
  }

  private void requestLocalStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && !PermissionUtils.hasPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, RuntimePermissionUtils.DROIDBOY_PERMISSION_WRITE_EXTERNAL_STORAGE);
    }
  }

  // If shouldOverflowText is specified we concatenate an append string
  // This is to test big text and ellipsis cutoff in varying screen sizes
  private String generateDisplayValue(String field) {
    if (mShouldOverflowText) {
      return field + getString(R.string.overflow_string);
    }
    return field;
  }

  private void addWearExtraPagesToNotificationBuilder(Bundle notificationExtras) {
    for (int i = 0; i < WEAR_EXTRA_PAGE_TITLES.length; i++) {
      String titleKey = Constants.APPBOY_PUSH_WEAR_EXTRA_PAGE_TITLE_KEY_PREFIX + i;
      notificationExtras.putString(titleKey, WEAR_EXTRA_PAGE_TITLES[i]);
    }
    for (int i = 0; i < WEAR_EXTRA_PAGE_CONTENTS.length; i++) {
      String contentKey = Constants.APPBOY_PUSH_WEAR_EXTRA_PAGE_CONTENT_KEY_PREFIX + i;
      notificationExtras.putString(contentKey, WEAR_EXTRA_PAGE_CONTENTS[i]);
    }
  }
}
