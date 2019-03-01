package com.appboy.unity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class AppboyOverlayActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    grantImmersiveModeToWindow();
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

  /**
   * Grants immersive mode to the current window. Note that this MUST be called
   * before setContentView().
   */
  private void grantImmersiveModeToWindow() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    int decorFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_FULLSCREEN;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      decorFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }
    getWindow().getDecorView().setSystemUiVisibility(decorFlags);
  }
}
