package com.appboy.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.enums.NotificationSubscriptionType;
import com.appboy.events.IValueCallback;
import com.appboy.models.outgoing.AttributionData;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.math.BigDecimal;
import java.util.Date;

public class MainFragment extends Fragment {
  private static final String TAG = AppboyLogger.getAppboyLogTag(MainFragment.class);
  private static final String STRING_ARRAY_ATTRIBUTE_KEY = "stringArrayAttribute";
  private static final String ARRAY_ATTRIBUTE_KEY = "arrayAttribute";
  private static final String DATE_ATTRIBUTE_KEY = "dateAttribute";
  private static final String PETS_ARRAY_ATTRIBUTE_KEY = "arrayAttributePets";
  private static final String FLOAT_ATTRIBUTE_KEY = "floatAttribute";
  private static final String BOOL_ATTRIBUTE_KEY = "boolAttribute";
  private static final String INT_ATTRIBUTE_KEY = "intAttribute";
  private static final String LONG_ATTRIBUTE_KEY = "longAttribute";
  private static final String STRING_ATTRIBUTE_KEY = "stringAttribute";
  private static final String DOUBLE_ATTRIBUTE_KEY = "doubleAttribute";
  private static final String INCREMENT_ATTRIBUTE_KEY = "incrementAttribute";
  private static final String ATTRIBUTION_DATA_KEY = "ab_install_attribution";
  private static final String LAST_CUSTOM_EVENT_PREFERENCE_KEY = "last_custom_event";

  public static final String USER_ID_KEY = "user.id";

  private EditText mUserIdEditText;
  private EditText mCustomEventOrPurchaseEditText;
  private Button mUserIdButton;

  private EditText mAliasEditText;
  private EditText mAliasLabelEditText;
  private Button mUserAliasButton;

  private Button mCustomEventButton;
  private Button mLogPurchaseButton;
  private Button mSetUserAttributesButton;
  private Button mUnsetUserAttributesButton;
  private Button mRequestFlushButton;
  private Button mGoogleAdvertisingIdFlushButton;
  private Context mContext;
  private SharedPreferences mSharedPreferences;

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View contentView = layoutInflater.inflate(R.layout.main_fragment, container, false);
    mContext = getContext();
    mSharedPreferences = getActivity().getSharedPreferences("droidboy", Context.MODE_PRIVATE);
    mUserIdEditText = contentView.findViewById(R.id.com_appboy_sample_set_user_id_edit_text);
    mUserIdEditText.setText(mSharedPreferences.getString(USER_ID_KEY, null));
    mUserIdButton = contentView.findViewById(R.id.com_appboy_sample_set_user_id_button);

    mAliasEditText = contentView.findViewById(R.id.com_appboy_sample_set_alias_edit_text);
    mAliasLabelEditText = contentView.findViewById(R.id.com_appboy_sample_set_alias_label_edit_text);
    mUserAliasButton = contentView.findViewById(R.id.com_appboy_sample_set_user_alias_button);

    // Braze methods
    mCustomEventOrPurchaseEditText = contentView.findViewById(R.id.com_appboy_sample_custom_event_or_purchase_edit_text);
    mCustomEventOrPurchaseEditText.setText(mSharedPreferences.getString(LAST_CUSTOM_EVENT_PREFERENCE_KEY, null));
    mCustomEventButton = contentView.findViewById(R.id.com_appboy_sample_log_custom_event_button);
    mLogPurchaseButton = contentView.findViewById(R.id.com_appboy_sample_log_purchase_button);
    // Braze User methods
    mSetUserAttributesButton = contentView.findViewById(R.id.com_appboy_sample_set_user_attributes_button);
    mUnsetUserAttributesButton = contentView.findViewById(R.id.com_appboy_sample_unset_user_attributes_button);
    mRequestFlushButton = contentView.findViewById(R.id.com_appboy_sample_request_flush_button);
    mGoogleAdvertisingIdFlushButton = contentView.findViewById(R.id.com_appboy_sample_collect_and_flush_google_advertising_id_button);
    return contentView;
  }

  @Override
  public void onStart() {
    super.onStart();
    mUserIdButton.setOnClickListener(view -> {
      String userId = mUserIdEditText.getText().toString();
      if (!StringUtils.isNullOrBlank(userId)) {
        Appboy.getInstance(getContext()).changeUser(userId);
        Toast.makeText(getContext(), "Set userId to: " + userId, Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(USER_ID_KEY, userId);
        editor.apply();
        FirebaseCrashlytics.getInstance().setUserId(userId);
      } else {
        Toast.makeText(getContext(), "Please enter a userId.", Toast.LENGTH_SHORT).show();
      }
    });
    mCustomEventButton.setOnClickListener(view -> {
      String customEvent = mCustomEventOrPurchaseEditText.getText().toString();
      if (!StringUtils.isNullOrBlank(customEvent)) {
        Appboy.getInstance(mContext).logCustomEvent(customEvent);
        Toast.makeText(getContext(), String.format("Logged custom event %s.", customEvent), Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(LAST_CUSTOM_EVENT_PREFERENCE_KEY, customEvent);
        editor.apply();
      } else {
        Toast.makeText(getContext(), "Please enter a custom event.", Toast.LENGTH_SHORT).show();
      }
    });
    mLogPurchaseButton.setOnClickListener(view -> {
      String purchase = mCustomEventOrPurchaseEditText.getText().toString();
      if (!StringUtils.isNullOrBlank(purchase)) {
        Appboy.getInstance(mContext).logPurchase(purchase, "USD", BigDecimal.TEN);
        Toast.makeText(getContext(), String.format("Logged purchase %s.", purchase), Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getContext(), "Please enter a purchase.", Toast.LENGTH_SHORT).show();
      }
    });
    mSetUserAttributesButton.setOnClickListener(view -> Appboy.getInstance(mContext).getCurrentUser(new IValueCallback<AppboyUser>() {
      @Override
      public void onSuccess(@NonNull AppboyUser currentUser) {
        currentUser.setFirstName("first name least");
        currentUser.setLastName("lastName");
        currentUser.setEmail("email@test.com");
        currentUser.setGender(Gender.FEMALE);
        currentUser.setCountry("USA");
        currentUser.setLanguage("cs");
        currentUser.setHomeCity("New York");
        currentUser.setPhoneNumber("1234567890");
        currentUser.setDateOfBirth(1984, Month.AUGUST, 18);
        currentUser.setAvatarImageUrl("https://raw.githubusercontent.com/Appboy/appboy-android-sdk/master/braze-logo.png");
        currentUser.setPushNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN);
        currentUser.setEmailNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN);
        currentUser.setCustomUserAttribute(STRING_ATTRIBUTE_KEY, "stringValue");
        currentUser.setCustomUserAttribute(FLOAT_ATTRIBUTE_KEY, 1.5f);
        currentUser.setCustomUserAttribute(INT_ATTRIBUTE_KEY, 100);
        currentUser.setCustomUserAttribute(BOOL_ATTRIBUTE_KEY, true);
        currentUser.setCustomUserAttribute(LONG_ATTRIBUTE_KEY, 10L);
        currentUser.setCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY, 1);
        currentUser.setCustomUserAttribute(DOUBLE_ATTRIBUTE_KEY, 3.1d);
        currentUser.incrementCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY, 4);
        currentUser.setCustomUserAttributeToSecondsFromEpoch(DATE_ATTRIBUTE_KEY, new Date().getTime() / 1000L);
        currentUser.setCustomAttributeArray(STRING_ARRAY_ATTRIBUTE_KEY, new String[]{"a", "b"});
        currentUser.addToCustomAttributeArray(ARRAY_ATTRIBUTE_KEY, "c");
        currentUser.removeFromCustomAttributeArray(ARRAY_ATTRIBUTE_KEY, "b");
        currentUser.addToCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "cat");
        currentUser.addToCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "dog");
        currentUser.removeFromCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "bird");
        currentUser.removeFromCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "deer");
        currentUser.setAttributionData(new AttributionData("network", "campaign", "ad group", "creative"));
        currentUser.setLocationCustomAttribute("Favorite Location", 33.078883d, -116.603131d);
        showToast("Set user attributes.");
      }

      @Override
      public void onError() {
        showToast("Failed to set user attributes.");
      }
    }));
    mUnsetUserAttributesButton.setOnClickListener(view -> Appboy.getInstance(mContext).getCurrentUser(new IValueCallback<AppboyUser>() {
      @Override
      public void onSuccess(@NonNull AppboyUser currentUser) {
        // Unset current user default attributes
        currentUser.setFirstName(null);
        currentUser.setLastName(null);
        currentUser.setEmail(null);
        currentUser.setGender(Gender.UNKNOWN);
        currentUser.setCountry(null);
        currentUser.setLanguage(null);
        currentUser.setHomeCity(null);
        currentUser.setPhoneNumber(null);
        currentUser.setDateOfBirth(1970, Month.JANUARY, 1);
        currentUser.setAvatarImageUrl(null);
        currentUser.setPushNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED);
        currentUser.setEmailNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED);
        // Unset current user custom attributes
        currentUser.unsetCustomUserAttribute(STRING_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(FLOAT_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(INT_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(BOOL_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(LONG_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(DATE_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(ARRAY_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(STRING_ARRAY_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(PETS_ARRAY_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(DOUBLE_ATTRIBUTE_KEY);
        currentUser.unsetCustomUserAttribute(ATTRIBUTION_DATA_KEY);
        currentUser.unsetLocationCustomAttribute("Mediocre Location");
        showToast("Unset user attributes.");
      }

      @Override
      public void onError() {
        showToast("Failed to unset user attributes.");
      }
    }));
    mRequestFlushButton.setOnClickListener(view -> {
      Appboy.getInstance(mContext).requestImmediateDataFlush();
      Toast.makeText(getContext(), "Requested data flush.", Toast.LENGTH_SHORT).show();
    });
    mUserAliasButton.setOnClickListener(view -> handleAliasClick());
    mGoogleAdvertisingIdFlushButton.setOnClickListener((view) -> new CollectGoogleAdvertisingIdTask().execute());
  }

  private void handleAliasClick() {
    String alias = mAliasEditText.getText().toString();
    String label = mAliasLabelEditText.getText().toString();
    Appboy.getInstance(mContext).getCurrentUser(new IValueCallback<AppboyUser>() {
      @Override
      public void onSuccess(@NonNull AppboyUser value) {
        value.addAlias(alias, label);
        Toast.makeText(getContext(), "Added alias " + alias + " with label "
            + label, Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onError() {
        Toast.makeText(getContext(), "Failed to add alias", Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Shows a toast on the activity's UI thread
   */
  private void showToast(final String msg) {
    final FragmentActivity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show());
    }
  }

  private class CollectGoogleAdvertisingIdTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
      try {
        AdvertisingIdClient.Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
        Appboy.getInstance(mContext).setGoogleAdvertisingId(advertisingIdInfo.getId(), advertisingIdInfo.isLimitAdTrackingEnabled());
        Appboy.getInstance(mContext).requestImmediateDataFlush();
      } catch (Exception e) {
        AppboyLogger.e(TAG, "Failed to collect Google Advertising ID information.", e);
      }
      return null;
    }
  }
}
