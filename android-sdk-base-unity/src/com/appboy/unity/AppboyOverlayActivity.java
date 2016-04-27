package com.appboy.unity;

import android.app.Activity;
import android.os.Bundle;

public class AppboyOverlayActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_inappmessage_overlay);
  }

  @Override
  protected void onResume() {
    super.onResume();
    AppboyUnityNativeInAppMessageManagerListener.getInstance().registerOverlayActivityAndRequestDisplay(this);
  }

  @Override
  public void onPause() {
    AppboyUnityNativeInAppMessageManagerListener.getInstance().unregisterOverlayActivityAndReRegisterContainer();
    super.onPause();
  }
}