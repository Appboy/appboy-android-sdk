package com.appboy.wear.models;

import android.util.Log;

import com.appboy.wear.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class WearDeviceIdentifiers implements IPutIntoJson<JSONObject> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, WearDeviceIdentifiers.class.getName());
  private final String mAndroidId;
  private static final String ANDROID_ID_KEY = "android_id";

  public WearDeviceIdentifiers(String androidId) {
    mAndroidId = androidId;
  }

  @Override
  public JSONObject forJsonPut() {
    JSONObject object = new JSONObject();
    try {
      object.put(ANDROID_ID_KEY, mAndroidId);
    } catch (JSONException e) {
      Log.e(TAG, "Caught exception creating wear device identifiers Json.", e);
    }
    return object;
  }
}
