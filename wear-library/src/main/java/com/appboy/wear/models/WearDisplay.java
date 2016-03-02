package com.appboy.wear.models;

import android.util.Log;

import com.appboy.wear.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public final class WearDisplay implements IPutIntoJson<JSONObject> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, WearDeviceIdentifiers.class.getName());
  private final int mResolutionWidth;
  private final int mResolutionHeight;
  private final float mDpiX;
  private final float mDpiY;
  private final int mDensity;
  private final String mScreenType;

  private static final String RESOLUTION_WIDTH_KEY = "resolution_width";
  private static final String RESOLUTION_HEIGHT_KEY = "resolution_height";
  private static final String X_DPI_KEY = "x_dpi";
  private static final String Y_DPI_KEY = "y_dpi";
  private static final String DENSITY_DEFAULT_KEY = "density_default";
  private static final String SCREEN_TYPE_KEY = "screen_type";

  /**
   * Constructs a display model for a Wear device. Since the screen type is optional, its value can be null.
   */
  public WearDisplay(int resolutionWidth, int resolutionHeight, float dpiX, float dpiY, int density, String screenType) {
    mResolutionWidth = resolutionWidth;
    mResolutionHeight = resolutionHeight;
    mDpiX = dpiX;
    mDpiY = dpiY;
    mDensity = density;
    mScreenType = screenType;
  }

  public String getScreenType() {
    return mScreenType;
  }

  @Override
  public JSONObject forJsonPut() {
    JSONObject object = new JSONObject();
    try {
      object.put(RESOLUTION_HEIGHT_KEY, mResolutionHeight);
      object.put(RESOLUTION_WIDTH_KEY, mResolutionWidth);
      object.put(X_DPI_KEY, mDpiX);
      object.put(Y_DPI_KEY, mDpiY);
      object.put(DENSITY_DEFAULT_KEY, mDensity);
      if (mScreenType != null) {
        object.put(SCREEN_TYPE_KEY, mScreenType);
      }
    } catch (JSONException e) {
      Log.e(TAG, "Caught exception creating wear display Json.", e);
    }
    return object;
  }
}
