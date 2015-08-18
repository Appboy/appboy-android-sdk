package com.appboy.wear.managers;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.appboy.wear.models.WearDevice;
import com.appboy.wear.models.WearDeviceIdentifiers;
import com.appboy.wear.models.WearDisplay;

public class WearDeviceDataProvider {
  private static final String CONNECTED_DEVICE_TYPE_WATCH = "watch";

  private final Context mContext;
  private final WearDeviceIdentifiers mWearDeviceIdentifiers;
  private final String mScreenType;

  public WearDeviceDataProvider(Context context, AppboyWearDeviceIdReader deviceIdReader, String screenType) {
    mContext = context;
    mWearDeviceIdentifiers = new WearDeviceIdentifiers(deviceIdReader.readGeneratedDeviceId());
    mScreenType = screenType;
  }

  public WearDevice getWearDevice() {
    return new WearDevice(readAndroidVersion(), readDeviceType(), readModel(), readDisplay(), mWearDeviceIdentifiers);
  }

  private WearDisplay readDisplay() {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    return new WearDisplay(displayMetrics.widthPixels, displayMetrics.heightPixels, displayMetrics.xdpi, displayMetrics.ydpi, displayMetrics.densityDpi, mScreenType);
  }

  private String readModel() {
    return android.os.Build.MODEL;
  }

  private String readDeviceType() {
    return CONNECTED_DEVICE_TYPE_WATCH;
  }

  private int readAndroidVersion() {
    return android.os.Build.VERSION.SDK_INT;
  }
}
