package com.appboy.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.enums.NotificationSubscriptionType;
import com.appboy.support.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

public class MainFragment extends Fragment {
  private static final String USER_ID_KEY = "user.id";
  private static final String ARRAY_ATTRIBUTE_KEY = "arrayAttribute";
  private static final String DATE_ATTRIBUTE_KEY = "dateAttribute";
  private static final String FLOAT_ATTRIBUTE_KEY = "floatAttribute";
  private static final String BOOL_ATTRIBUTE_KEY = "boolAttribute";
  private static final String INT_ATTRIBUTE_KEY = "intAttribute";
  private static final String LONG_ATTRIBUTE_KEY = "longAttribute";
  private static final String STRING_ATTRIBUTE_KEY = "stringAttribute";
  private static final String INCREMENT_ATTRIBUTE_KEY = "incrementAttribute";

  private EditText mUserIdEditText;
  private EditText mCustomEventOrPurchaseEditText;
  private Button mUserIdButton;
  private Button mCustomEventButton;
  private Button mLogPurchaseButton;
  private Button mSubmitFeedbackButton;
  private Button mSetUserAttributesButton;
  private Button mUnsetCustomUserAttributesButton;
  private Button mRequestFlushButton;
  private Context mContext;
  private SharedPreferences mSharedPreferences;

  public MainFragment() {}

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View contentView = layoutInflater.inflate(R.layout.main_fragment, container, false);
    mContext = getContext();
    mSharedPreferences = getActivity().getSharedPreferences("droidboy", Context.MODE_PRIVATE);
    mUserIdEditText = (EditText) contentView.findViewById(R.id.com_appboy_sample_set_user_id_edit_text);
    mUserIdEditText.setText(mSharedPreferences.getString(USER_ID_KEY, null));
    mUserIdButton = (Button) contentView.findViewById(R.id.com_appboy_sample_set_user_id_button);
    // Appboy methods
    mCustomEventOrPurchaseEditText = (EditText) contentView.findViewById(R.id.com_appboy_sample_custom_event_or_purchase_edit_text);
    mCustomEventButton = (Button) contentView.findViewById(R.id.com_appboy_sample_log_custom_event_button);
    mLogPurchaseButton = (Button) contentView.findViewById(R.id.com_appboy_sample_log_purchase_button);
    mSubmitFeedbackButton = (Button) contentView.findViewById(R.id.com_appboy_sample_submit_feedback_button);
    // Appboy User methods
    mSetUserAttributesButton = (Button) contentView.findViewById(R.id.com_appboy_sample_set_user_attributes_button);
    mUnsetCustomUserAttributesButton = (Button) contentView.findViewById(R.id.com_appboy_sample_unset_custom_attributes_button);
    mRequestFlushButton = (Button) contentView.findViewById(R.id.com_appboy_sample_request_flush_button);
    return contentView;
  }

  @Override
  public void onStart() {
    super.onStart();
    mUserIdButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        String userId = mUserIdEditText.getText().toString();
        if (!StringUtils.isNullOrBlank(userId)) {
          SharedPreferences.Editor editor = mSharedPreferences.edit();
          Appboy.getInstance(getContext()).changeUser(userId);
          editor.putString(USER_ID_KEY, userId);
          editor.apply();
          Toast.makeText(getContext(), "Set userId to: " + userId, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getContext(), "Please enter a userId.", Toast.LENGTH_SHORT).show();
        }
      }
    });
    mCustomEventButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        String customEvent = mCustomEventOrPurchaseEditText.getText().toString();
        if (!StringUtils.isNullOrBlank(customEvent)) {
          Appboy.getInstance(mContext).logCustomEvent(customEvent);
          Toast.makeText(getContext(), String.format("Logged custom event %s.", customEvent), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getContext(), "Please enter a custom event.", Toast.LENGTH_SHORT).show();

        }
      }
    });
    mLogPurchaseButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        String purchase = mCustomEventOrPurchaseEditText.getText().toString();
        if (!StringUtils.isNullOrBlank(purchase)) {
          Appboy.getInstance(mContext).logPurchase(purchase, "USD", BigDecimal.TEN);
          Toast.makeText(getContext(), String.format("Logged purchase %s.", purchase), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getContext(), "Please enter a purchase.", Toast.LENGTH_SHORT).show();
        }
      }
    });
    mSubmitFeedbackButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Appboy.getInstance(mContext).submitFeedback("droidboy@appboy.com", "nice app!", true);
        Toast.makeText(getContext(), "Submitted feedback.", Toast.LENGTH_SHORT).show();
      }
    });
    mSetUserAttributesButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Appboy.getInstance(mContext).getCurrentUser().setFirstName("firstName");
        Appboy.getInstance(mContext).getCurrentUser().setLastName("lastName");
        Appboy.getInstance(mContext).getCurrentUser().setEmail("email@test.com");
        Appboy.getInstance(mContext).getCurrentUser().setGender(Gender.FEMALE);
        Appboy.getInstance(mContext).getCurrentUser().setCountry("USA");
        Appboy.getInstance(mContext).getCurrentUser().setHomeCity("New York");
        Appboy.getInstance(mContext).getCurrentUser().setPhoneNumber("1234567890");
        Appboy.getInstance(mContext).getCurrentUser().setDateOfBirth(1984, Month.AUGUST, 18);
        Appboy.getInstance(mContext).getCurrentUser().setAvatarImageUrl("https://raw.githubusercontent.com/Appboy/appboy-android-sdk/master/Appboy_Logo_400x100.png");
        Appboy.getInstance(mContext).getCurrentUser().setPushNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN);
        Appboy.getInstance(mContext).getCurrentUser().setEmailNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN);
        Appboy.getInstance(mContext).getCurrentUser().setCustomUserAttribute(STRING_ATTRIBUTE_KEY, "stringValue");
        Appboy.getInstance(mContext).getCurrentUser().setCustomUserAttribute(FLOAT_ATTRIBUTE_KEY, 1.5f);
        Appboy.getInstance(mContext).getCurrentUser().setCustomUserAttribute(INT_ATTRIBUTE_KEY, 100);
        Appboy.getInstance(mContext).getCurrentUser().setCustomUserAttribute(BOOL_ATTRIBUTE_KEY, true);
        Appboy.getInstance(mContext).getCurrentUser().setCustomUserAttribute(LONG_ATTRIBUTE_KEY, 10L);
        Appboy.getInstance(mContext).getCurrentUser().setCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY, 1);
        Appboy.getInstance(mContext).getCurrentUser().incrementCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY, 4);
        Appboy.getInstance(mContext).getCurrentUser().setCustomUserAttributeToSecondsFromEpoch(DATE_ATTRIBUTE_KEY, new Date().getTime());
        Appboy.getInstance(mContext).getCurrentUser().setCustomAttributeArray(ARRAY_ATTRIBUTE_KEY, new String[]{"a", "b"});
        Appboy.getInstance(mContext).getCurrentUser().addToCustomAttributeArray(ARRAY_ATTRIBUTE_KEY, "c");
        Appboy.getInstance(mContext).getCurrentUser().removeFromCustomAttributeArray(ARRAY_ATTRIBUTE_KEY, "b");
        Toast.makeText(getContext(), "Set user attributes.", Toast.LENGTH_SHORT).show();
      }
    });
    mUnsetCustomUserAttributesButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(STRING_ATTRIBUTE_KEY);
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(FLOAT_ATTRIBUTE_KEY);
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(INT_ATTRIBUTE_KEY);
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(BOOL_ATTRIBUTE_KEY);
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(LONG_ATTRIBUTE_KEY);
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(DATE_ATTRIBUTE_KEY);
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(ARRAY_ATTRIBUTE_KEY);
        Appboy.getInstance(mContext).getCurrentUser().unsetCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY);
        Toast.makeText(getContext(), "Unset custom user attributes.", Toast.LENGTH_SHORT).show();
      }
    });
    mRequestFlushButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Appboy.getInstance(mContext).requestImmediateDataFlush();
        Toast.makeText(getContext(), "Requested data flush.", Toast.LENGTH_SHORT).show();
      }
    });
  }
}
