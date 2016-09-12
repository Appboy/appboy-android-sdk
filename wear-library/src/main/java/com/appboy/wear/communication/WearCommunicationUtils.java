package com.appboy.wear.communication;

import com.appboy.wear.enums.Gender;
import com.appboy.wear.enums.Month;
import com.appboy.wear.enums.WearSdkActions;
import com.appboy.wear.models.AppboyProperties;
import com.appboy.wear.models.WearDevice;
import com.google.android.gms.wearable.DataMap;

import java.math.BigDecimal;

/**
 * Handles the data marshalling protocol of the various Appboy SDK actions. SDK actions are bundled
 * in the DataMap object from the GMS API. Serialization is also handled by GMS and not by this class.
 *
 * Enums are encoded with their .name() value unless otherwise specified.
 */
// Default visibility used for testing
public class WearCommunicationUtils {

  // DATA MAP keys
  // Used to specify the type of action. Enums WearSdkActions are used in string form as the value
  static final String ACTION_TYPE = "t";
  // For sdk events with a key, the key is the value
  static final String ACTION_KEY_NAME = "k";
  // These are used to encode the values of the arguments, in the order as they appear
  static final String ACTION_VALUE_0 = "v0";
  static final String ACTION_VALUE_1 = "v1";
  static final String ACTION_VALUE_2 = "v2";
  static final String ACTION_VALUE_3 = "v3";
  // Flag specifying whether the optional AppboyProperties object is present
  static final String ACTION_HAS_APPBOY_PROPERTIES = "h";
  // Encodes the optional AppboyProperties object
  static final String ACTION_APPBOY_PROPERTIES = "p";

  // DATA MAP type values
  // These are used on multi-typed, overloaded actions such as setCustomUserAttribute
  static final int JAVA_TYPE_BOOLEAN = 1;
  static final int JAVA_TYPE_STRING = 2;
  static final int JAVA_TYPE_FLOAT = 3;
  static final int JAVA_TYPE_INT = 4;
  static final int JAVA_TYPE_LONG = 5;

  public static void modifyDataMapWithCustomEvent(DataMap dataMap, String eventName) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.CUSTOM_EVENT.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putString(ACTION_VALUE_0, eventName);
  }

  public static void modifyDataMapWithCustomEvent(DataMap dataMap, AppboyProperties properties, String eventName) {
    // Modify with the standard custom event modification
    modifyDataMapWithCustomEvent(dataMap, eventName);

    // Insert the appboy properties
    addAppboyPropertiesToDataMap(dataMap, properties);
  }

  public static void modifyDataMapWithPurchase(DataMap dataMap, String currencyCode, BigDecimal price, String productId) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.LOG_PURCHASE.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    // Value 0 - productId
    dataMap.putString(ACTION_VALUE_0, productId);

    // Value 1 - currencyCode
    dataMap.putString(ACTION_VALUE_1, currencyCode);

    // Value 2 - price
    // Note, this string conversion is exact and 1-to-1. No price info is lost.
    // http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html#toString()
    dataMap.putString(ACTION_VALUE_2, price.toString());
  }

  public static void modifyDataMapWithPurchase(DataMap dataMap, String currencyCode, BigDecimal price, AppboyProperties properties, String productId) {
    // Modify with the standard custom event modification
    modifyDataMapWithPurchase(dataMap, currencyCode, price, productId);

    // Insert the appboy properties
    addAppboyPropertiesToDataMap(dataMap, properties);
  }

  public static void modifyDataMapWithPushNotificationOpened(DataMap dataMap, String campaignId) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.LOG_PUSH_NOTIFICATION_OPENED.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    // Value 0 - id
    dataMap.putString(ACTION_VALUE_0, campaignId);
  }

  public static void modifyDataMapWithSubmitFeedback(DataMap dataMap, String message, boolean isReportingABug, String replyToEmail) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SUBMIT_FEEDBACK.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    dataMap.putString(ACTION_VALUE_0, replyToEmail);
    dataMap.putString(ACTION_VALUE_1, message);
    dataMap.putBoolean(ACTION_VALUE_2, isReportingABug);
  }

  public static void modifyDataMapWithUserAddToCustomAttributeArray(DataMap dataMap, String value, String key) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.ADD_TO_CUSTOM_ATTRIBUTE_ARRAY.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    dataMap.putString(ACTION_KEY_NAME, key);
    dataMap.putString(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserIncrementCustomAttribute(DataMap dataMap, String key) {
    modifyDataMapWithUserIncrementCustomAttribute(dataMap, 1, key);
  }

  public static void modifyDataMapWithUserIncrementCustomAttribute(DataMap dataMap, int incrementValue, String key) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.INCREMENT_CUSTOM_ATTRIBUTE.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    dataMap.putString(ACTION_KEY_NAME, key);
    dataMap.putInt(ACTION_VALUE_0, incrementValue);
  }

  public static void modifyDataMapWithUserRemoveFromCustomAttributeArray(DataMap dataMap, String value, String key) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.REMOVE_FROM_CUSTOM_ATTRIBUTE_ARRAY.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    dataMap.putString(ACTION_KEY_NAME, key);
    dataMap.putString(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserSetCustomAttributeArray(DataMap dataMap, String[] values, String key) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SET_CUSTOM_ATTRIBUTE_ARRAY.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    dataMap.putString(ACTION_KEY_NAME, key);
    dataMap.putStringArray(ACTION_VALUE_0, values);
  }

  /**
   * Simple function to DRY the other set User custom attribute methods.
   */
  static void setDataMapWithSetUserCustomAttributeBase(DataMap dataMap, String key, int argType) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SET_CUSTOM_ATTRIBUTE.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putString(ACTION_KEY_NAME, key);
    dataMap.putInt(ACTION_VALUE_1, argType);
  }

  public static void modifyDataMapWithUserSetCustomAttribute(DataMap dataMap, boolean value, String key) {
    setDataMapWithSetUserCustomAttributeBase(dataMap, key, JAVA_TYPE_BOOLEAN);
    dataMap.putBoolean(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserSetCustomAttribute(DataMap dataMap, float value, String key) {
    setDataMapWithSetUserCustomAttributeBase(dataMap, key, JAVA_TYPE_FLOAT);
    dataMap.putFloat(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserSetCustomAttribute(DataMap dataMap, int value, String key) {
    setDataMapWithSetUserCustomAttributeBase(dataMap, key, JAVA_TYPE_INT);
    dataMap.putInt(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserSetCustomAttribute(DataMap dataMap, long value, String key) {
    setDataMapWithSetUserCustomAttributeBase(dataMap, key, JAVA_TYPE_LONG);
    dataMap.putLong(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserSetCustomAttribute(DataMap dataMap, String value, String key) {
    setDataMapWithSetUserCustomAttributeBase(dataMap, key, JAVA_TYPE_STRING);
    dataMap.putString(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserSetCustomAttributeToNow(DataMap dataMap, String key) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SET_CUSTOM_ATTRIBUTE_TO_NOW.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putString(ACTION_KEY_NAME, key);
  }

  public static void modifyDataMapWithUserUnsetCustomAttribute(DataMap dataMap, String key) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.UNSET_CUSTOM_ATTRIBUTE.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putString(ACTION_KEY_NAME, key);
  }

  public static void modifyDataMapWithUserSetCustomAttributeToSecondsFromEpoch(DataMap dataMap, long secondsFromEpoch, String key) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SET_CUSTOM_ATTRIBUTE_TO_SECONDS_FROM_EPOCH.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putString(ACTION_KEY_NAME, key);
    dataMap.putLong(ACTION_VALUE_0, secondsFromEpoch);
  }

  public static void modifyDataMapWithUserSetLastKnownLocation(DataMap dataMap, double longitude, Double altitude, Double accuracy, double latitude) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SET_LAST_KNOWN_LOCATION.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    dataMap.putDouble(ACTION_VALUE_0, latitude);
    dataMap.putDouble(ACTION_VALUE_1, longitude);

    // To allow for nullity, these keys will be empty if the value is null
    if (altitude != null) {
      dataMap.putDouble(ACTION_VALUE_2, altitude);
    }
    if (accuracy != null) {
      dataMap.putDouble(ACTION_VALUE_3, accuracy);
    }
  }

  public static void modifyDataMapWithWearDeviceInformation(DataMap dataMap, WearDevice device) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SEND_WEAR_DEVICE.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);

    dataMap.putString(ACTION_VALUE_0, device.forJsonPut().toString());
  }

  public static void modifyDataMapWithUserProfileString(DataMap dataMap, WearSdkActions actionType, String value) {
    dataMap.putString(ACTION_TYPE, actionType.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putString(ACTION_VALUE_0, value);
  }

  public static void modifyDataMapWithUserGender(DataMap dataMap, Gender gender) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SET_GENDER.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putString(ACTION_VALUE_0, gender.name());
  }

  public static void modifyDataMapWithUserDateOfBirth(DataMap dataMap, int year, Month month, int day) {
    dataMap.putString(ACTION_TYPE, WearSdkActions.SET_DATE_OF_BIRTH.name());
    dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, false);
    dataMap.putInt(ACTION_VALUE_0, year);
    dataMap.putString(ACTION_VALUE_1, month.name());
    dataMap.putInt(ACTION_VALUE_2, day);
  }

  // Default visibility for testing
  static void addAppboyPropertiesToDataMap(DataMap dataMap, AppboyProperties properties) {
    if (properties != null) {
      // Dump the appboy properties JSON format into the dataMap
      dataMap.putBoolean(ACTION_HAS_APPBOY_PROPERTIES, true);
      dataMap.putString(ACTION_APPBOY_PROPERTIES, properties.forJsonPut().toString());
    }
  }
}
