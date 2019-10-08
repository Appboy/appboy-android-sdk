package com.appboy.ui.support;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.appboy.support.AppboyLogger;

public class ViewUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(ViewUtils.class);
  private static final int TABLET_SMALLEST_WIDTH_DP = 600;
  private static int sDisplayHeight;

  public static void removeViewFromParent(View view) {
    if (view != null) {
      if (view.getParent() instanceof ViewGroup) {
        final ViewGroup parent = (ViewGroup) view.getParent();
        setFocusableInTouchModeAndRequestFocus(parent);
        parent.removeView(view);
      }
    }
  }

  public static void setFocusableInTouchModeAndRequestFocus(View view) {
    try {
      view.setFocusableInTouchMode(true);
      view.requestFocus();
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Caught exception while setting view to focusable in touch mode and requesting focus.", e);
    }
  }

  /**
   * Retrieve the size of the display in dp. The display height includes all screen decorations,
   * such as the status bar.
   *
   * @param activity
   * @return
   */
  public static int getDisplayHeight(Activity activity) {
    if (sDisplayHeight > 0) {
      return sDisplayHeight;
    }
    Display display = activity.getWindowManager().getDefaultDisplay();
    Point point = new Point();
    display.getSize(point);
    sDisplayHeight = point.y;
    return sDisplayHeight;
  }

  public static double convertDpToPixels(Context context, double valueInDp) {
    double density = context.getResources().getDisplayMetrics().density;
    return valueInDp * density;
  }

  public static boolean isRunningOnTablet(Activity activity) {
    return activity.getResources().getConfiguration().smallestScreenWidthDp
        >= TABLET_SMALLEST_WIDTH_DP;
  }

  /**
   * Safely calls {@link Activity#setRequestedOrientation(int)}
   */
  public static void setActivityRequestedOrientation(@NonNull Activity activity, int requestedOrientation) {
    try {
      activity.setRequestedOrientation(requestedOrientation);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to set requested orientation " + requestedOrientation + " for activity class: " + activity.getLocalClassName(), e);
    }
  }

  public static void setHeightOnViewLayoutParams(View view, int height) {
    if (view == null) {
      AppboyLogger.w(TAG, "Cannot set height on null view.");
      return;
    }
    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
    layoutParams.height = height;
    view.setLayoutParams(layoutParams);
  }

  /**
   * Checks if the device is in night mode. In Android 10, this corresponds
   * to "Dark Theme" being enabled by the user.
   */
  public static boolean isDeviceInNightMode(Context context) {
    int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
  }
}
