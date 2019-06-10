package com.appboy.sample.util;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ViewUtils {

  /**
   * Enables Immersive mode. Basically means the app becomes fullscreen and the notification bar fades away.
   */
  public static void enableImmersiveMode(final View decorView) {
    decorView.setSystemUiVisibility(setSystemUiVisibility());
    decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
      if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
        decorView.setSystemUiVisibility(setSystemUiVisibility());
      }
    });
  }

  public static void enableNoLimitsMode(Window window) {
    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
  }

  private static int setSystemUiVisibility() {
    int visibilityFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_FULLSCREEN;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      visibilityFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }
    return visibilityFlags;
  }
}
