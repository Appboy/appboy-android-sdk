package com.appboy.wear;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.appboy.wear.communication.WearCommunicationUtils;
import com.appboy.wear.enums.Gender;
import com.appboy.wear.enums.Month;
import com.appboy.wear.enums.WearScreenShape;
import com.appboy.wear.enums.WearSdkActions;
import com.appboy.wear.managers.AppboyWearDeviceIdReader;
import com.appboy.wear.managers.WearDeviceDataProvider;
import com.appboy.wear.models.AppboyProperties;
import com.appboy.wear.models.WearDevice;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.math.BigDecimal;

/**
 * Use this class to log Appboy SDK events from a wearable. The wearable must be paired with a device
 * running the Appboy SDK for the events to be logged.
 * <p/>
 * Methods containing 'User', such as addToUserCustomAttributeArray(), get called on the current AppboyUser
 * running on the phone.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class AppboyWearableAdapter implements GoogleApiClient.ConnectionCallbacks {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyWearableAdapter.class.getName());
  private static final String DATA_SYNC_PATH_PREFIX = "/appboy-data-sync/";

  private final GoogleApiClient mGoogleApiClient;
  private final AppboyWearDeviceIdReader mDeviceIdReader;
  private final Context mContext;

  private static AppboyWearableAdapter sInstance;

  private String mWearScreenType = null;

  public static AppboyWearableAdapter getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new AppboyWearableAdapter(context);
    }

    return sInstance;
  }

  AppboyWearableAdapter(final Context context) {
    mContext = context.getApplicationContext();
    mDeviceIdReader = new AppboyWearDeviceIdReader(mContext);
    mGoogleApiClient = new GoogleApiClient.Builder(mContext)
            .addApiIfAvailable(Wearable.API)
            .build();

    // When the api client has connected, send the wear device.
    // We have to register the connection callback on this to not throw AbstractMethodErrors.
    mGoogleApiClient.registerConnectionCallbacks(this);
    mGoogleApiClient.connect();

    Log.i(TAG, "Adapter started");
  }

  /**
   * Logs the shape of this wearable screen. See https://github.com/tajchert/ShapeWear for instructions
   * on how to collect this information.
   *
   * @param shape The screen shape. Either round or square.
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean logWearScreenShape(WearScreenShape shape) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }

    mWearScreenType = shape.forJsonPut();
    sendWearDeviceData();
    return true;
  }

  /**
   * see IAppboy#logCustomEvent(String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean logCustomEvent(String eventName) {
    return logCustomEvent(eventName, null);
  }

  /**
   * see IAppboy#logCustomEvent(String, AppboyProperties)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean logCustomEvent(String eventName, AppboyProperties properties) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(eventName)) {
      Log.w(TAG, "Event name null or empty. Ignoring custom event.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    if (properties == null) {
      WearCommunicationUtils.modifyDataMapWithCustomEvent(dataMap, eventName);
    } else {
      WearCommunicationUtils.modifyDataMapWithCustomEvent(dataMap, properties, eventName);
    }
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see IAppboy.logPurchase(String, String, BigDecimal)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean logPurchase(String productId, String currencyCode, BigDecimal price) {
    return logPurchase(productId, currencyCode, price, null);
  }

  /**
   * see IAppboy#logPurchase(String, String, BigDecimal, int, AppboyProperties)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean logPurchase(String productId, String currencyCode, BigDecimal price, AppboyProperties properties) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(productId)) {
      Log.w(TAG, "Product id null or empty. Not logging in-app purchase to Appboy.");
      return false;
    }
    if (isNullOrEmpty(currencyCode)) {
      Log.w(TAG, "Currency code null or empty. Not logging in-app purchase to Appboy.");
      return false;
    }
    if (price == null) {
      Log.w(TAG, "Price is null. Not logging in-app purchase to Appboy.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    if (properties == null) {
      WearCommunicationUtils.modifyDataMapWithPurchase(dataMap, currencyCode, price, productId);
    } else {
      WearCommunicationUtils.modifyDataMapWithPurchase(dataMap, currencyCode, price, properties, productId);
    }
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see IAppboy#logPushNotificationOpened(String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean logPushNotificationOpened(String campaignId) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(campaignId)) {
      Log.w(TAG, "Campaign id null or empty. Not logging push open.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithPushNotificationOpened(dataMap, campaignId);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see IAppboy#submitFeedback(String, String, boolean)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean submitFeedback(String replyToEmail, String message, boolean isReportingABug) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(replyToEmail)) {
      Log.w(TAG, "Reply email null or empty. Not submitting feedback.");
      return false;
    }
    if (isNullOrEmpty(message)) {
      Log.w(TAG, "Message null or empty. Not submitting feedback.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithSubmitFeedback(dataMap, message, isReportingABug, replyToEmail);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#addToCustomAttributeArray(String, String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean addToUserCustomAttributeArray(String key, String value) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not adding to custom attribute array.");
      return false;
    }
    if (isNullOrEmpty(value)) {
      Log.w(TAG, "Value null or empty. Not adding to custom attribute array.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserAddToCustomAttributeArray(dataMap, value, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#incrementCustomUserAttribute(String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean incrementCustomUserAttribute(String key) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not incrementing custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserIncrementCustomAttribute(dataMap, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#incrementCustomUserAttribute(String, int)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean incrementCustomUserAttribute(String key, int incrementValue) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not incrementing custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserIncrementCustomAttribute(dataMap, incrementValue, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#removeFromCustomAttributeArray(String, String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean removeFromUserCustomAttributeArray(String key, String value) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not removing from custom user attribute array.");
      return false;
    }
    if (isNullOrEmpty(value)) {
      Log.w(TAG, "Value null or empty. Not removing from custom user attribute array.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserRemoveFromCustomAttributeArray(dataMap, value, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomAttributeArray(String, String[])
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttributeArray(String key, String[] values) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute array.");
      return false;
    }
    if (values == null) {
      Log.w(TAG, "Values array null. Not setting custom user attribute array.");
      return false;
    }
    for (String value : values) {
      if (isNullOrEmpty(value)) {
        Log.w(TAG, "Value null or empty. Not setting custom user attribute array.");
        return false;
      }
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttributeArray(dataMap, values, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomUserAttribute(String, boolean)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttribute(String key, boolean value) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttribute(dataMap, value, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomUserAttribute(String, float)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttribute(String key, float value) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttribute(dataMap, value, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomUserAttribute(String, int)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttribute(String key, int value) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttribute(dataMap, value, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomUserAttribute(String, long)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttribute(String key, long value) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttribute(dataMap, value, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomUserAttribute(String, String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttribute(String key, String value) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute.");
      return false;
    }
    if (isNullOrEmpty(value)) {
      Log.w(TAG, "Value null or empty. Not setting custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttribute(dataMap, value, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomUserAttributeToNow(String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttributeToNow(String key) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute to now.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttributeToNow(dataMap, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#unsetCustomUserAttribute(String)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean unsetUserCustomAttribute(String key) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not unsetting custom user attribute.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserUnsetCustomAttribute(dataMap, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setCustomUserAttributeToSecondsFromEpoch(String, long)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCustomAttributeToSecondsFromEpoch(String key, long secondsFromEpoch) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    if (isNullOrEmpty(key)) {
      Log.w(TAG, "Key null or empty. Not setting custom user attribute to seconds from epoch.");
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetCustomAttributeToSecondsFromEpoch(dataMap, secondsFromEpoch, key);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * Convenience method to DRY the other user profile methods
   *
   * @param value      the string argument
   * @param actionType the type of action
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  private boolean sendBasicUserProfileStringValue(String value, WearSdkActions actionType) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserProfileString(dataMap, actionType, value);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserAvatarImageUrl(String url) {
    if (isNullOrEmpty(url)) {
      Log.w(TAG, "Url null or empty. Not setting user avatar image url.");
      return false;
    }
    return sendBasicUserProfileStringValue(url, WearSdkActions.SET_AVATAR_IMAGE_URL);
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserCountry(String country) {
    if (isNullOrEmpty(country)) {
      Log.w(TAG, "Country null or empty. Not setting user country");
      return false;
    }
    return sendBasicUserProfileStringValue(country, WearSdkActions.SET_COUNTRY);
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserEmail(String email) {
    if (isNullOrEmpty(email)) {
      Log.w(TAG, "Email null or empty. Not setting user email");
      return false;
    }
    return sendBasicUserProfileStringValue(email, WearSdkActions.SET_EMAIL);
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserFirstName(String firstName) {
    if (isNullOrEmpty(firstName)) {
      Log.w(TAG, "First name null or empty. Not setting user first name");
      return false;
    }
    return sendBasicUserProfileStringValue(firstName, WearSdkActions.SET_FIRST_NAME);
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserHomeCity(String homeCity) {
    if (isNullOrEmpty(homeCity)) {
      Log.w(TAG, "Home city null or empty. Not setting user home city");
      return false;
    }
    return sendBasicUserProfileStringValue(homeCity, WearSdkActions.SET_HOME_CITY);
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserLastName(String lastName) {
    if (isNullOrEmpty(lastName)) {
      Log.w(TAG, "Last name null or empty. Not setting user last name");
      return false;
    }
    return sendBasicUserProfileStringValue(lastName, WearSdkActions.SET_LAST_NAME);
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserPhoneNumber(String phoneNumber) {
    if (isNullOrEmpty(phoneNumber)) {
      Log.w(TAG, "Phone number null or empty. Not setting user phone number");
      return false;
    }
    return sendBasicUserProfileStringValue(phoneNumber, WearSdkActions.SET_PHONE_NUMBER);
  }

  public boolean setUserDateOfBirth(int year, Month month, int day) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserDateOfBirth(dataMap, year, month, day);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  public boolean setUserGender(Gender gender) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserGender(dataMap, gender);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  /**
   * see com.appboy.AppboyUser#setLastKnownLocation(double, double, Double, Double)
   *
   * @return a boolean indicating whether or not this action has been sent to the phone.
   */
  public boolean setUserLastKnownLocation(double latitude, double longitude, Double altitude, Double accuracy) {
    if (!isWearableApiConnectionAvailable()) {
      return false;
    }
    PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
    DataMap dataMap = putDataMapRequest.getDataMap();
    WearCommunicationUtils.modifyDataMapWithUserSetLastKnownLocation(dataMap, longitude, altitude, accuracy, latitude);
    syncDataMapRequest(putDataMapRequest);
    return true;
  }

  public PutDataMapRequest getNewPutDataMapRequest() {
    // Create a data item request with the path of the current time. Paths must start with slashes
    return PutDataMapRequest.createWithAutoAppendedId(DATA_SYNC_PATH_PREFIX);
  }

  public void syncDataMapRequest(PutDataMapRequest putDataMapRequest) {
    // Sync the data over the Data Sync API
    PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
    final PendingResult<DataApi.DataItemResult> pendingResult =
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

    Thread sendCustomEventActionThread = new Thread(new Runnable() {
      @Override
      public void run() {
        DataApi.DataItemResult dataItemResult = pendingResult.await();
        if (!dataItemResult.getStatus().isSuccess()) {
          // We can't return this asynchronous value, but we can log it
          Log.w(TAG, "Appboy sdk action failed for reason: " + dataItemResult.getStatus().getStatusMessage());
        }
      }
    });
    sendCustomEventActionThread.start();
  }

  /**
   * Checks if we can communicate via GMS over the Wearable API. If the GoogleApiClient is null or
   * the Wearable API is unconnected then returns false.
   *
   * @return a boolean indicating whether or not this action has been sent to the phone. false if the connection cannot be made.
   */
  public boolean isWearableApiConnectionAvailable() {
    if (mGoogleApiClient == null) {
      Log.w(TAG, "Google Api Client null. Wearable connection could not be made.");
      return false;
    }

    if (!mGoogleApiClient.hasConnectedApi(Wearable.API)) {
      Log.w(TAG, "Google Wearable Api not connected to the client. Wearable connection could not be made.");
      return false;
    }
    return true;
  }

  /**
   * @return a boolean indicating whether or not this action has been sent to the phone. Wear device information
   */
  public WearDevice getWearDeviceData() {
    WearDeviceDataProvider wearDeviceDataProvider = new WearDeviceDataProvider(mContext, mDeviceIdReader, mWearScreenType);
    return wearDeviceDataProvider.getWearDevice();
  }

  /**
   * Collects the wearable device info and sends it over to the phone.
   */
  private void sendWearDeviceData() {
    WearDevice wearDevice = getWearDeviceData();
    if (wearDevice != null) {
      PutDataMapRequest putDataMapRequest = getNewPutDataMapRequest();
      DataMap dataMap = putDataMapRequest.getDataMap();
      WearCommunicationUtils.modifyDataMapWithWearDeviceInformation(dataMap, wearDevice);
      syncDataMapRequest(putDataMapRequest);
    }
  }

  @Override
  public void onConnected(Bundle bundle) {
    sendWearDeviceData();
    // We only need to send the device data once, so unregister ourselves from further callbacks
    mGoogleApiClient.unregisterConnectionCallbacks(this);
  }

  @Override
  public void onConnectionSuspended(int cause) {
    // GMS will auto re-connect so we don't have to handle the reconnection ourselves.
    // https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.ConnectionCallbacks.html#onConnectionSuspended(int)
  }

  static boolean isNullOrEmpty(String reference) {
    return reference == null || reference.length() == 0;
  }
}
