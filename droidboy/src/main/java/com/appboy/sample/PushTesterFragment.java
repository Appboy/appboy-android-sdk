package com.appboy.sample;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.push.AppboyNotificationUtils;
import com.appboy.sample.util.RuntimePermissionUtils;
import com.appboy.sample.util.SpinnerUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class PushTesterFragment extends Fragment implements AdapterView.OnItemSelectedListener {
  protected static final String TAG = AppboyLogger.getAppboyLogTag(PushTesterFragment.class);
  private static final String TITLE = "Title";
  private static final String CONTENT = "Content";
  private static final String BIG_TITLE = "Big Title";
  private static final String BIG_SUMMARY = "Big Summary";
  private static final String SUMMARY_TEXT = "Summary Text";
  private static final SecureRandom sSecureRandom = new SecureRandom();
  private AppboyConfigurationProvider mAppConfigurationProvider;
  private NotificationManagerCompat mNotificationManager;
  @SuppressLint("InlinedApi")
  private String mPriority = String.valueOf(Notification.PRIORITY_DEFAULT);
  private String mImage;
  private String mClickActionUrl;
  private String mCategory;
  private String mVisibility;
  private String mActionType;
  private String mAccentColorString;
  private String mLargeIconString;
  private String mNotificationFactoryType;
  private String mPushStoryTitleGravity;
  private String mPushStorySubtitleGravity;
  private int mPushStoryType = 0;
  private int mPushStoryNumPages;
  private String mChannel;

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
  private boolean mOpenInWebview = false;
  private boolean mTestTriggerFetch = false;
  private boolean mSetChannel = false;
  private boolean mUseConstantNotificationId = false;
  private boolean mStoryDeepLink = false;
  private boolean mStoryTitles = true;
  private boolean mStorySubtitles = true;
  static final String EXAMPLE_APPBOY_EXTRA_KEY_1 = "Entree";
  static final String EXAMPLE_APPBOY_EXTRA_KEY_2 = "Side";
  static final String EXAMPLE_APPBOY_EXTRA_KEY_3 = "Drink";
  private static final Map<Integer, String[]> PUSH_STORY_PAGE_VALUES;

  static {
    Map<Integer, String[]> pushStoryPageValues = new HashMap<>();
    pushStoryPageValues.put(0, new String[]{"http://appboy.com", "Twenty WWWWWWW WWWW#", "Twenty WWWWWWW WWWW#", "https://i2.wp.com/www.appboy.com/blog/wp-content/uploads/2017/09/AB17_blog_header_REVEAL.png?resize=1200%2C600&ssl=1"});
    pushStoryPageValues.put(1, new String[]{"http://google.com", "Twenty Five WW WWWW WWWW#", "Twenty Five WW WWWW WWWW#", "https://i2.wp.com/www.appboy.com/blog/wp-content/uploads/2017/08/abcnews_blog.png?resize=1200%2C600&ssl=1"});
    pushStoryPageValues.put(2, new String[]{"http://appboy.com", "Thirty WW WWWW WWWW WWWW WWWW#", "Thirty WW WWWW WWWW WWWW WWWW#", "https://i2.wp.com/www.appboy.com/blog/wp-content/uploads/2017/02/Man-Using_Smartphone-While-Watching-TV.jpg?resize=1200%2C600&ssl=1"});
    pushStoryPageValues.put(3, new String[]{"http://appboy.com", "Forty WWW WWWW WWWW WWWW WWWW WWWW WWWW#", "Forty WWW WWWW WWWW WWWW WWWW WWWW WWWW#", "https://i0.wp.com/www.appboy.com/blog/wp-content/uploads/2015/12/Appboy_Web_Push_Notifications_Announcement.png?resize=1200%2C600&ssl=1"});
    pushStoryPageValues.put(4, new String[]{"http://appboy.com", "Forty Five WWW WWWW WWWW WWWW WWWW WWWW WWWW#", "Forty Five WWW WWWW WWWW WWWW WWWW WWWW WWWW#", "https://i0.wp.com/www.appboy.com/blog/wp-content/uploads/2017/04/Workers-Shaking-Hands_Featured-Image.jpg?resize=1200%2C600&ssl=1"});
    pushStoryPageValues.put(5, new String[]{"http://appboy.com", "Fifteen W WWW#", "Fifteen W WWW#", "https://i1.wp.com/www.appboy.com/blog/wp-content/uploads/2017/09/Concerned-Voter_Featured-Image.jpg?resize=1200%2C600&ssl=1"});
    pushStoryPageValues.put(6, new String[]{"http://appboy.com", "Ten  WWWW#", "Ten  WWWW#", "https://i0.wp.com/www.appboy.com/blog/wp-content/uploads/2017/04/Male-and-Female-Coworkers-Laptop_Featured-Image.jpg?resize=1200%2C600&ssl=1"});
    pushStoryPageValues.put(7, new String[]{"http://appboy.com", "Five#", "Five#", "https://i0.wp.com/www.appboy.com/blog/wp-content/uploads/2016/05/Jack.jpg?resize=1200%2C600&ssl=1"});
    PUSH_STORY_PAGE_VALUES = pushStoryPageValues;
  }

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View view = layoutInflater.inflate(R.layout.push_tester, container, false);

    mNotificationManager = NotificationManagerCompat.from(getContext());

    ((CheckBox) view.findViewById(R.id.push_tester_big_title)).setOnCheckedChangeListener((buttonView, isChecked) -> mUseBigTitle = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_summary)).setOnCheckedChangeListener((buttonView, isChecked) -> mUseSummary = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_big_summary)).setOnCheckedChangeListener((buttonView, isChecked) -> mUseBigSummary = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_overflow_text)).setOnCheckedChangeListener((buttonView, isChecked) -> mShouldOverflowText = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_set_public_version)).setOnCheckedChangeListener((buttonView, isChecked) -> mSetPublicVersion = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_test_triggers)).setOnCheckedChangeListener((buttonView, isChecked) -> mTestTriggerFetch = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_constant_nid)).setOnCheckedChangeListener((buttonView, isChecked) -> mUseConstantNotificationId = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_set_open_webview)).setOnCheckedChangeListener((buttonView, isChecked) -> mOpenInWebview = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_story_deep_link)).setOnCheckedChangeListener((buttonView, isChecked) -> mStoryDeepLink = isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_story_title)).setOnCheckedChangeListener((buttonView, isChecked) -> mStoryTitles = !isChecked);
    ((CheckBox) view.findViewById(R.id.push_tester_story_subtitle)).setOnCheckedChangeListener((buttonView, isChecked) -> mStorySubtitles = !isChecked);

    // Creates the push image spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_image_spinner), this, R.array.push_image_options);

    // Creates the push image number spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_image_number_spinner), this, R.array.push_image_number_options);

    // Creates the push priority spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_priority_spinner), this, R.array.push_priority_options);

    // Creates the push click action spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_click_action_spinner), this, R.array.push_click_action_options);

    // Creates the notification category spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_category_spinner), this, R.array.push_category_options);

    // Creates the visibility spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_visibility_spinner), this, R.array.push_visibility_options);

    // Creates the push image spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_image_spinner), this, R.array.push_image_options);

    // Creates the story title align spinner
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_story_title_align_spinner), this, R.array.push_story_title_align_options);

    // Creates the story subtitle align spinner
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_story_subtitle_align_spinner), this, R.array.push_story_subtitle_align_options);

    // Creates the push action spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_action_spinner), this, R.array.push_action_options);

    // Creates the push accent color spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_accent_color_spinner), this, R.array.push_accent_color_options);

    // Creates the large icon spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_large_icon_spinner), this, R.array.push_large_icon_options);

    // Creates the notification factory spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_notification_factory_spinner), this, R.array.push_notification_factory_options);

    // Creates the notification channel spinner.
    SpinnerUtils.setUpSpinner(view.findViewById(R.id.push_channel_spinner), this, R.array.push_channel_options);

    mAppConfigurationProvider = new AppboyConfigurationProvider(getContext());
    Button pushTestButton = view.findViewById(R.id.test_push_button);
    pushTestButton.setOnClickListener(clickedView -> (new Thread(() -> {
      Bundle notificationExtras = new Bundle();
      notificationExtras.putString(Constants.APPBOY_PUSH_TITLE_KEY, generateDisplayValue(TITLE));
      notificationExtras.putString(Constants.APPBOY_PUSH_CONTENT_KEY, generateDisplayValue(CONTENT + sSecureRandom.nextInt()));
      notificationExtras.putString(Constants.APPBOY_PUSH_APPBOY_KEY, "true");

      int notificationId;
      if (mUseConstantNotificationId) {
        notificationId = 100;
      } else {
        notificationId = AppboyNotificationUtils.getNotificationId(notificationExtras);
      }
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
      if (mOpenInWebview) {
        notificationExtras.putString(Constants.APPBOY_PUSH_OPEN_URI_IN_WEBVIEW_KEY, "true");
      }
      if (mSetPublicVersion) {
        try {
          notificationExtras.putString(Constants.APPBOY_PUSH_PUBLIC_NOTIFICATION_KEY, getPublicVersionNotificationString());
        } catch (JSONException jsonException) {
          Log.e(TAG, "Failed to created public version notification JSON string", jsonException);
        }
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
      if (mSetChannel) {
        notificationExtras.putString(Constants.APPBOY_PUSH_NOTIFICATION_CHANNEL_ID_KEY, mChannel);
      }
      setNotificationFactory();

      if (mPushStoryType != 0) {
        addPushStoryPages(notificationExtras);
        notificationExtras.putString(Constants.APPBOY_PUSH_STORY_KEY, Integer.toString(mPushStoryType));
      }

      // Manually build the Braze extras bundle.
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
      appboyExtras.putString(EXAMPLE_APPBOY_EXTRA_KEY_1, "Hamburger");
      appboyExtras.putString(EXAMPLE_APPBOY_EXTRA_KEY_2, "Fries");
      appboyExtras.putString(EXAMPLE_APPBOY_EXTRA_KEY_3, "Lemonade");
      notificationExtras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, appboyExtras);
      Notification notification = AppboyNotificationUtils.getActiveNotificationFactory().createNotification(
          mAppConfigurationProvider,
          getContext(),
          notificationExtras,
          appboyExtras);

      if (notification != null) {
        mNotificationManager.notify(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId, notification);
      }
    })).start());
    return view;
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
          if (pushImageUriString.equals(getString(R.string.push_story))) {
            mPushStoryType = 1;
            mUseImage = false;
          } else {
            mPushStoryType = 0;
            mUseImage = true;
            mImage = pushImageUriString;
          }
        } else {
          mUseImage = false;
          mPushStoryType = 0;
        }
        break;
      case R.id.push_image_number_spinner:
        String pushImageNumberString = getResources().getStringArray(R.array.push_image_number_values)[parent.getSelectedItemPosition()];
        mPushStoryNumPages = Integer.parseInt(pushImageNumberString);
        break;
      case R.id.push_story_title_align_spinner:
        mPushStoryTitleGravity = getResources().getStringArray(R.array.push_story_title_align_values)[parent.getSelectedItemPosition()];
        break;
      case R.id.push_story_subtitle_align_spinner:
        mPushStorySubtitleGravity = getResources().getStringArray(R.array.push_story_subtitle_align_values)[parent.getSelectedItemPosition()];
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
        mUseCategory = !StringUtils.isNullOrBlank(mCategory);
        break;
      case R.id.push_visibility_spinner:
        mVisibility = getResources().getStringArray(R.array.push_visibility_values)[parent.getSelectedItemPosition()];
        mUseVisibility = !StringUtils.isNullOrBlank(mVisibility);
        break;
      case R.id.push_action_spinner:
        mActionType = getResources().getStringArray(R.array.push_action_values)[parent.getSelectedItemPosition()];
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
      case R.id.push_notification_factory_spinner:
        mNotificationFactoryType = getResources().getStringArray(R.array.push_notification_factory_values)[parent.getSelectedItemPosition()];
        break;
      case R.id.push_channel_spinner:
        mChannel = getResources().getStringArray(R.array.push_channel_values)[parent.getSelectedItemPosition()];
        mSetChannel = !StringUtils.isNullOrBlank(mChannel);
        break;
      default:
        Log.e(TAG, "Item selected for unknown spinner");
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

  /**
   * Add the push story fields to the notificationExtras bundle.
   *
   * @param notificationExtras Notification extras as provided by FCM/ADM.
   * @return the modified notificationExtras, now including the image/text information for the push story.
   */
  private Bundle addPushStoryPages(Bundle notificationExtras) {
    for (int i = 0; i < mPushStoryNumPages; i++) {
      if (mStoryDeepLink) {
        notificationExtras.putString(Constants.APPBOY_PUSH_STORY_DEEP_LINK_KEY_TEMPLATE.replace("*", Integer.toString(i)), PUSH_STORY_PAGE_VALUES.get(i)[0]);
      }
      if (mStoryTitles) {
        notificationExtras.putString(Constants.APPBOY_PUSH_STORY_TITLE_KEY_TEMPLATE.replace("*", Integer.toString(i)), PUSH_STORY_PAGE_VALUES.get(i)[1]);
      }
      if (mStorySubtitles) {
        notificationExtras.putString(Constants.APPBOY_PUSH_STORY_SUBTITLE_KEY_TEMPLATE.replace("*", Integer.toString(i)), PUSH_STORY_PAGE_VALUES.get(i)[2]);
      }
      notificationExtras.putString(Constants.APPBOY_PUSH_STORY_IMAGE_KEY_TEMPLATE.replace("*", Integer.toString(i)), PUSH_STORY_PAGE_VALUES.get(i)[3]);
      if (!StringUtils.isNullOrBlank(mPushStoryTitleGravity)) {
        notificationExtras.putString(Constants.APPBOY_PUSH_STORY_TITLE_JUSTIFICATION_KEY_TEMPLATE.replace("*", Integer.toString(i)), mPushStoryTitleGravity);
      }
      if (!StringUtils.isNullOrBlank(mPushStorySubtitleGravity)) {
        notificationExtras.putString(Constants.APPBOY_PUSH_STORY_SUBTITLE_JUSTIFICATION_KEY_TEMPLATE.replace("*", Integer.toString(i)), mPushStorySubtitleGravity);
      }
      if (mOpenInWebview) {
        notificationExtras.putString(Constants.APPBOY_PUSH_STORY_USE_WEBVIEW_KEY_TEMPLATE.replace("*", Integer.toString(i)), "true");
      }
    }
    notificationExtras.putBoolean(Constants.APPBOY_PUSH_STORY_IS_NEWLY_RECEIVED, true);
    return notificationExtras;
  }

  private Bundle addActionButtons(Bundle notificationExtras) {
    if (StringUtils.isNullOrBlank(mActionType)) {
      return notificationExtras;
    }
    switch (mActionType) {
      case Constants.APPBOY_PUSH_ACTION_TYPE_OPEN:
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), Constants.APPBOY_PUSH_ACTION_TYPE_OPEN);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Open app");
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), getString(R.string.droidboy_close_button_text));
        break;
      case Constants.APPBOY_PUSH_ACTION_TYPE_URI:
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Appboy (webview)");
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "0"), getString(R.string.braze_homepage_url));
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_USE_WEBVIEW_KEY_TEMPLATE.replace("*", "0"), "true");
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), "Google");
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "1"), getString(R.string.google_url));
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_USE_WEBVIEW_KEY_TEMPLATE.replace("*", "1"), "false");
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "2"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "2"), getString(R.string.droidboy_close_button_text));
        if (mOpenInWebview) {
          notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_USE_WEBVIEW_KEY_TEMPLATE.replace("*", "0"), "true");
          notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_USE_WEBVIEW_KEY_TEMPLATE.replace("*", "1"), "true");
        }
        break;
      case "deep_link":
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "0"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "0"), "Preferences");
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "0"), getString(R.string.droidboy_deep_link));
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "1"), Constants.APPBOY_PUSH_ACTION_TYPE_URI);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "1"), "Telephone");
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE.replace("*", "1"), getString(R.string.telephone_uri));
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE.replace("*", "2"), Constants.APPBOY_PUSH_ACTION_TYPE_NONE);
        notificationExtras.putString(Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE.replace("*", "2"), getString(R.string.droidboy_close_button_text));
        break;
      default:
    }
    return notificationExtras;
  }

  // If shouldOverflowText is specified we concatenate an append string
  // This is to test big text and ellipsis cutoff in varying screen sizes
  private String generateDisplayValue(String field) {
    if (mShouldOverflowText) {
      return field + getString(R.string.overflow_string);
    }
    return field;
  }

  /**
   * Sets the Braze instance's notification factory.
   */
  private void setNotificationFactory() {
    if ("DroidboyNotificationFactory".equals(mNotificationFactoryType)) {
      Appboy.setCustomAppboyNotificationFactory(new DroidboyNotificationFactory());
    } else if ("FullyCustomNotificationFactory".equals(mNotificationFactoryType)) {
      Appboy.setCustomAppboyNotificationFactory(new FullyCustomNotificationFactory());
    } else {
      Appboy.setCustomAppboyNotificationFactory(null);
    }
  }
}
