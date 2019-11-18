package com.appboy.ui.inappmessage.jsinterface;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.webkit.JavascriptInterface;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.enums.Gender;
import com.appboy.enums.Month;
import com.appboy.enums.NotificationSubscriptionType;
import com.appboy.support.AppboyLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppboyInAppMessageHtmlUserJavascriptInterface {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageHtmlUserJavascriptInterface.class);
  public static final String JS_BRIDGE_UNSUBSCRIBED = "unsubscribed";
  public static final String JS_BRIDGE_SUBSCRIBED = "subscribed";
  public static final String JS_BRIDGE_OPTED_IN = "opted_in";
  public static final String JS_BRIDGE_GENDER_MALE = Gender.MALE.forJsonPut();
  public static final String JS_BRIDGE_GENDER_FEMALE = Gender.FEMALE.forJsonPut();
  public static final String JS_BRIDGE_GENDER_OTHER = Gender.OTHER.forJsonPut();
  public static final String JS_BRIDGE_GENDER_UNKNOWN = Gender.UNKNOWN.forJsonPut();
  public static final String JS_BRIDGE_GENDER_NOT_APPLICABLE = Gender.NOT_APPLICABLE.forJsonPut();
  public static final String JS_BRIDGE_GENDER_PREFER_NOT_TO_SAY = Gender.PREFER_NOT_TO_SAY.forJsonPut();
  public static final String JS_BRIDGE_ATTRIBUTE_VALUE = "value";

  private Context mContext;

  public AppboyInAppMessageHtmlUserJavascriptInterface(Context context) {
    mContext = context;
  }

  @JavascriptInterface
  public void setFirstName(String firstName) {
    Appboy.getInstance(mContext).getCurrentUser().setFirstName(firstName);
  }

  @JavascriptInterface
  public void setLastName(String lastName) {
    Appboy.getInstance(mContext).getCurrentUser().setLastName(lastName);
  }

  @JavascriptInterface
  public void setEmail(String email) {
    Appboy.getInstance(mContext).getCurrentUser().setEmail(email);
  }

  @JavascriptInterface
  public void setGender(String genderString) {
    Gender gender = parseGender(genderString);
    if (gender == null) {
      AppboyLogger.e(TAG, "Failed to parse gender in Braze HTML in-app message javascript interface with gender: " + genderString);
    } else {
      Appboy.getInstance(mContext).getCurrentUser().setGender(gender);
    }
  }

  @VisibleForTesting
  Gender parseGender(String genderString) {
    if (genderString == null) {
      return null;
    }

    String genderStringLowerCase = genderString.toLowerCase(Locale.US);
    if (genderStringLowerCase.equals(JS_BRIDGE_GENDER_MALE)) {
      return Gender.MALE;
    } else if (genderStringLowerCase.equals(JS_BRIDGE_GENDER_FEMALE)) {
      return Gender.FEMALE;
    } else if (genderStringLowerCase.equals(JS_BRIDGE_GENDER_OTHER)) {
      return Gender.OTHER;
    } else if (genderStringLowerCase.equals(JS_BRIDGE_GENDER_UNKNOWN)) {
      return Gender.UNKNOWN;
    } else if (genderStringLowerCase.equals(JS_BRIDGE_GENDER_NOT_APPLICABLE)) {
      return Gender.NOT_APPLICABLE;
    } else if (genderStringLowerCase.equals(JS_BRIDGE_GENDER_PREFER_NOT_TO_SAY)) {
      return Gender.PREFER_NOT_TO_SAY;
    }

    return null;
  }

  @JavascriptInterface
  public void setDateOfBirth(int year, int monthInt, int day) {
    Month month = monthFromInt(monthInt);
    if (month == null) {
      AppboyLogger.e(TAG, "Failed to parse month for value " + monthInt);
      return;
    }

    Appboy.getInstance(mContext).getCurrentUser().setDateOfBirth(year, month, day);
  }

  @VisibleForTesting
  Month monthFromInt(int monthInt) {
    if (monthInt < 1 || monthInt > 12) {
      return null;
    }

    return Month.getMonth(monthInt - 1);
  }

  @JavascriptInterface
  public void setCountry(String country) {
    Appboy.getInstance(mContext).getCurrentUser().setCountry(country);
  }

  @JavascriptInterface
  public void setLanguage(String language) {
    Appboy.getInstance(mContext).getCurrentUser().setLanguage(language);
  }

  @JavascriptInterface
  public void setHomeCity(String homeCity) {
    Appboy.getInstance(mContext).getCurrentUser().setHomeCity(homeCity);
  }

  @JavascriptInterface
  public void setEmailNotificationSubscriptionType(String subscriptionType) {
    NotificationSubscriptionType subscriptionTypeEnum = subscriptionTypeFromJavascriptString(subscriptionType);
    if (subscriptionTypeEnum == null) {
      AppboyLogger.e(TAG, "Failed to parse email subscription type in Braze HTML in-app message javascript interface with subscription " + subscriptionType);
      return;
    }

    Appboy.getInstance(mContext).getCurrentUser().setEmailNotificationSubscriptionType(subscriptionTypeEnum);
  }

  @JavascriptInterface
  public void setPushNotificationSubscriptionType(String subscriptionType) {
    NotificationSubscriptionType subscriptionTypeEnum = subscriptionTypeFromJavascriptString(subscriptionType);
    if (subscriptionTypeEnum == null) {
      AppboyLogger.e(TAG, "Failed to parse push subscription type in Braze HTML in-app message javascript interface with subscription: " + subscriptionType);
      return;
    }

    Appboy.getInstance(mContext).getCurrentUser().setPushNotificationSubscriptionType(subscriptionTypeEnum);
  }

  @VisibleForTesting
  NotificationSubscriptionType subscriptionTypeFromJavascriptString(String subscriptionType) {
    String subscriptionTypeLowerCase = subscriptionType.toLowerCase(Locale.US);
    if (subscriptionTypeLowerCase.equals(JS_BRIDGE_SUBSCRIBED)) {
      return NotificationSubscriptionType.SUBSCRIBED;
    } else if (subscriptionTypeLowerCase.equals(JS_BRIDGE_UNSUBSCRIBED)) {
      return NotificationSubscriptionType.UNSUBSCRIBED;
    } else if (subscriptionTypeLowerCase.equals(JS_BRIDGE_OPTED_IN)) {
      return NotificationSubscriptionType.OPTED_IN;
    }

    return null;
  }

  @JavascriptInterface
  public void setPhoneNumber(String phoneNumber) {
    Appboy.getInstance(mContext).getCurrentUser().setPhoneNumber(phoneNumber);
  }

  @JavascriptInterface
  public void setCustomUserAttributeJSON(String key, String jsonStringValue) {
    setCustomAttribute(Appboy.getInstance(mContext).getCurrentUser(), key, jsonStringValue);
  }

  @VisibleForTesting
  void setCustomAttribute(AppboyUser user, String key, String jsonStringValue) {
    try {
      JSONObject jsonObject = new JSONObject(jsonStringValue);
      Object valueObject = jsonObject.get(JS_BRIDGE_ATTRIBUTE_VALUE);
      // JSONObject in Android never deals with float values, which
      // accounts for why that instanceof check is missing below
      if (valueObject instanceof String) {
        user.setCustomUserAttribute(key, (String) valueObject);
      } else if (valueObject instanceof Boolean) {
        user.setCustomUserAttribute(key, (Boolean) valueObject);
      } else if (valueObject instanceof Integer) {
        user.setCustomUserAttribute(key, (Integer) valueObject);
      } else if (valueObject instanceof Double) {
        user.setCustomUserAttribute(key, ((Double) valueObject));
      } else {
        AppboyLogger.e(TAG, "Failed to parse custom attribute type for key: " + key
            + " and json string value: " + jsonStringValue);
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to parse custom attribute type for key: " + key
          + " and json string value: " + jsonStringValue, e);
    }
  }

  @JavascriptInterface
  public void setCustomUserAttributeArray(String key, String jsonArrayString) {
    String[] arrayValue = parseStringArrayFromJsonString(jsonArrayString);
    if (arrayValue == null) {
      AppboyLogger.e(TAG, "Failed to set custom attribute array for key " + key);
      return;
    }

    Appboy.getInstance(mContext).getCurrentUser().setCustomAttributeArray(key, arrayValue);
  }

  @VisibleForTesting
  String[] parseStringArrayFromJsonString(String jsonArrayString) {
    try {
      JSONArray parsedArray = new JSONArray(jsonArrayString);
      List<String> list = new ArrayList<String>();
      for (int i = 0; i < parsedArray.length(); i++) {
        list.add(parsedArray.getString(i));
      }
      return list.toArray(new String[0]);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to parse custom attribute array", e);
    }
    return null;
  }

  @JavascriptInterface
  public void addToCustomAttributeArray(String key, String value) {
    Appboy.getInstance(mContext).getCurrentUser().addToCustomAttributeArray(key, value);
  }

  @JavascriptInterface
  public void removeFromCustomAttributeArray(String key, String value) {
    Appboy.getInstance(mContext).getCurrentUser().removeFromCustomAttributeArray(key, value);
  }

  @JavascriptInterface
  public void incrementCustomUserAttribute(String attribute) {
    Appboy.getInstance(mContext).getCurrentUser().incrementCustomUserAttribute(attribute);
  }

  @JavascriptInterface
  public void setLocationCustomUserAttribute(String attribute, double latitude, double longitude) {
    Appboy.getInstance(mContext).getCurrentUser().setLocationCustomAttribute(attribute, latitude, longitude);
  }
}
