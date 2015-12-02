package com.appboy.wear.models;

import android.util.Log;

import com.appboy.wear.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class WearDevice implements IPutIntoJson<JSONObject> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, WearDevice.class.getName());
  private final Integer mAndroidVersion;
  private final String mDeviceType;
  private final String mModel;
  private final WearDisplay mWearDisplay;
  private final WearDeviceIdentifiers mWearDeviceIdentifiers;

  public WearDevice(Integer androidVersion, String wearDeviceType, String model, WearDisplay wearDisplay, WearDeviceIdentifiers wearDeviceIdentifiers) {
    mAndroidVersion = androidVersion;
    mDeviceType = wearDeviceType;
    mModel = model;
    mWearDisplay = wearDisplay;
    mWearDeviceIdentifiers = wearDeviceIdentifiers;
  }

  @Override
  public JSONObject forJsonPut() {
    JSONObject object = new JSONObject();
    try {
      object.putOpt("android_version", mAndroidVersion);
      object.putOpt("model", mModel);
      object.putOpt("type", mDeviceType);

      // The keys below are null checked so an empty json object isn't inserted into the jsonPut
      // and to avoid null pointer exceptions.
      if (mWearDisplay != null) {
        object.putOpt("display", mWearDisplay.forJsonPut());
      }
      if (mWearDeviceIdentifiers != null) {
        object.putOpt("device_identifiers", mWearDeviceIdentifiers.forJsonPut());
      }
    } catch (JSONException e) {
      Log.e(TAG, "Caught exception creating wear device Json.", e);
    }
    return object;
  }
}
