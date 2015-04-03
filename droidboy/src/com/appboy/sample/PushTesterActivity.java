package com.appboy.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.appboy.configuration.XmlAppConfigurationProvider;
import com.appboy.Constants;
import com.appboy.push.AppboyNotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PushTesterActivity extends AppboyFragmentActivity implements AdapterView.OnItemSelectedListener {
  XmlAppConfigurationProvider mAppConfigurationProvider;
  NotificationManager mNotificationManager;
  private String mTitle = "Welcome to Appboy (title)!";
  private String mContent = "We hope you're enjoying our product (content).";
  private String mBigTitle = "Appmazing (big title).";
  private boolean mUseBigSummary = false;
  private String mBigSummary = "The Big Picture (big summary)";
  private String mSummary = "This is what it's all about (summary)";
  private String mPriority = String.valueOf(Notification.PRIORITY_DEFAULT);
  private String mImage;
  private String mClickActionUrl;
  private String mCategory;
  private String mVisibility;

  private boolean mUseSummary = false;
  private boolean mUseImage = false;
  private boolean mShouldOverflowText = false;
  private boolean mUseBigTitle = false;
  private boolean mUseClickAction = false;
  private boolean mUseCategory = false;
  private boolean mUseVisibility = false;
  private boolean mSetPublicVersion = false;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.push_tester);
    setTitle("Push");
    mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

    ((CheckBox) findViewById(R.id.push_tester_big_title)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mUseBigTitle = isChecked;
      }
    });
    ((CheckBox) findViewById(R.id.push_tester_summary)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mUseSummary = isChecked;
      }
    });
    ((CheckBox) findViewById(R.id.push_tester_big_summary)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mUseBigSummary = isChecked;
      }
    });
    ((CheckBox) findViewById(R.id.push_tester_overflow_text)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mShouldOverflowText = isChecked;
      }
    });
    ((CheckBox) findViewById(R.id.push_tester_set_public_version)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mSetPublicVersion = isChecked;
      }
    });

    // Creates the push image spinner.
    setupSpinner(
      (Spinner) findViewById(R.id.push_image_spinner),
      ArrayAdapter.createFromResource(this, R.array.push_image_options, android.R.layout.simple_spinner_item)
    );

    // Creates the push priority spinner.
    setupSpinner(
      (Spinner) findViewById(R.id.push_priority_spinner),
      ArrayAdapter.createFromResource(this, R.array.push_priority_options, android.R.layout.simple_spinner_item)
    );

    // Creates the push click action spinner.
    setupSpinner(
      (Spinner) findViewById(R.id.push_click_action_spinner),
      ArrayAdapter.createFromResource(this,R.array.push_click_action_options, android.R.layout.simple_spinner_item)
    );

    // Creates the notification category spinner.
    setupSpinner(
      (Spinner) findViewById(R.id.push_category_spinner),
      ArrayAdapter.createFromResource(this,R.array.push_category_options, android.R.layout.simple_spinner_item)
    );

    // Creates the visiblity spinner.
    setupSpinner(
      (Spinner) findViewById(R.id.push_visibility_spinner),
      ArrayAdapter.createFromResource(this,R.array.push_visibility_options, android.R.layout.simple_spinner_item)
    );

    mAppConfigurationProvider = new XmlAppConfigurationProvider(this);
    Button pushTestButton = (Button) findViewById(R.id.test_push_button);
    pushTestButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        (new Thread(new Runnable() {
          public void run() {
            Bundle notificationExtras = new Bundle();
            notificationExtras.putString(Constants.APPBOY_PUSH_TITLE_KEY, generateDisplayValue(mTitle, mShouldOverflowText));
            notificationExtras.putString(Constants.APPBOY_PUSH_CONTENT_KEY, generateDisplayValue(mContent, mShouldOverflowText));

            Bundle appboyExtras = new Bundle();
            if (mUseSummary) {
              notificationExtras.putString(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY, generateDisplayValue(mSummary, mShouldOverflowText));
            }
            if (mUseClickAction) {
              notificationExtras.putString(Constants.APPBOY_PUSH_DEEP_LINK_KEY, mClickActionUrl);
            }
            notificationExtras.putString(Constants.APPBOY_PUSH_PRIORITY_KEY, mPriority);
            if (mUseBigTitle) {
              notificationExtras.putString(Constants.APPBOY_PUSH_BIG_TITLE_TEXT_KEY, mBigTitle);
            }
            if (mUseBigSummary) {
              notificationExtras.putString(Constants.APPBOY_PUSH_BIG_SUMMARY_TEXT_KEY, mBigSummary);
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
            if (mUseImage) {
              if (Constants.IS_AMAZON) {
                // Amazon flattens the extras bundle so we have to put it in the regular notification
                // extras to imitate that funcitonality.
                notificationExtras.putString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY, mImage.replaceAll("&amp;", "&"));
              } else {
                appboyExtras.putString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY, mImage.replaceAll("&amp;", "&"));
              }
            }

            notificationExtras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, appboyExtras);
            Notification notification = AppboyNotificationUtils.getActiveNotificationFactory().createNotification(
              mAppConfigurationProvider,
              getApplicationContext(),
              notificationExtras,
              AppboyNotificationUtils.getAppboyExtras(notificationExtras));
            mNotificationManager.notify(AppboyNotificationUtils.getNotificationId(notificationExtras), notification);
          }
        })).start();
      }
    });
  }

  public void onItemSelected(AdapterView<?> parent, View view,
                             int pos, long id) {
    int parentId = parent.getId();
    switch (parentId) {
      case R.id.push_image_spinner:
        String pushImageUriString = getResources().getStringArray(R.array.push_image_values)[parent.getSelectedItemPosition()];
        if (pushImageUriString != null && pushImageUriString.length() > 0) {
          mUseImage = true;
          mImage = pushImageUriString;
        } else {
          mUseImage = false;
        }
        break;
      case R.id.push_priority_spinner:
        mPriority = getResources().getStringArray(R.array.push_priority_values)[parent.getSelectedItemPosition()];
        break;
      case R.id.push_click_action_spinner:
        String pushClickActionUriString = getResources().getStringArray(R.array.push_click_action_values)[parent.getSelectedItemPosition()];
        if (pushClickActionUriString != null && pushClickActionUriString.length() > 0) {
          mUseClickAction = true;
          mClickActionUrl = pushClickActionUriString;
        } else {
          mUseClickAction = false;
        }
        break;
      case R.id.push_category_spinner:
        mCategory = getResources().getStringArray(R.array.push_category_values)[parent.getSelectedItemPosition()];
        if (mCategory != null && mCategory.length() > 0) {
          mUseCategory = true;
        } else {
          mUseCategory = false;
        }
        break;
      case R.id.push_visibility_spinner:
        mVisibility = getResources().getStringArray(R.array.push_visibility_values)[parent.getSelectedItemPosition()];
        if (mVisibility != null && mVisibility.length() > 0) {
          mUseVisibility = true;
        } else {
          mUseVisibility = false;
        }
        break;
      default:
        Log.e(TAG, "Item selected for unknown spinner");
    }
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

  private void setupSpinner(Spinner spinner, ArrayAdapter arrayAdapter) {
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(this);
  }

  // If shouldOverflowText is specified we concatenate an append string 5 times
  // This is to test big text and ellipsis cutoff in varying screen sizes
  private String generateDisplayValue(String field, boolean shouldOverFlowText) {
    String appendString = " 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20";
    if (shouldOverFlowText) {
      String returnText = field;
      for (int i = 0; i < 5; i++) {
        returnText = returnText + appendString;
      }
      return returnText;
    } else {
      return field;
    }
  }
}
