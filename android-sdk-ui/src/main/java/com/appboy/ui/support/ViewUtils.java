package com.appboy.ui.support;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.view.DisplayCutoutCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.view.View;
import android.view.ViewGroup;

import com.appboy.enums.inappmessage.Orientation;
import com.appboy.support.AppboyLogger;

public class ViewUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(ViewUtils.class);
  private static final int TABLET_SMALLEST_WIDTH_DP = 600;

  public static void removeViewFromParent(View view) {
    if (view != null) {
      if (view.getParent() instanceof ViewGroup) {
        final ViewGroup parent = (ViewGroup) view.getParent();
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

  /**
   * @return Whether the current screen orientation (e.g. {@link Configuration#ORIENTATION_LANDSCAPE})
   * matches the preferred orientation (e.g. {@link Orientation#LANDSCAPE}.
   */
  public static boolean isCurrentOrientationValid(int currentScreenOrientation, Orientation preferredOrientation) {
    if (currentScreenOrientation == Configuration.ORIENTATION_LANDSCAPE
        && preferredOrientation == Orientation.LANDSCAPE) {
      AppboyLogger.d(TAG, "Current and preferred orientation are landscape.");
      return true;
    } else if (currentScreenOrientation == Configuration.ORIENTATION_PORTRAIT
        && preferredOrientation == Orientation.PORTRAIT) {
      AppboyLogger.d(TAG, "Current and preferred orientation are portrait.");
      return true;
    } else {
      AppboyLogger.d(TAG, "Current orientation " + currentScreenOrientation
          + " and preferred orientation " + preferredOrientation + " don't match");
      return false;
    }
  }

  /**
   * @return The maximum of the display cutout left inset and the system window left inset.
   */
  public static int getMaxSafeLeftInset(@NonNull WindowInsetsCompat windowInsets) {
    if (windowInsets.getDisplayCutout() != null) {
      final DisplayCutoutCompat displayCutout = windowInsets.getDisplayCutout();
      return Math.max(displayCutout.getSafeInsetLeft(), windowInsets.getSystemWindowInsetLeft());
    } else {
      // The max inset is just the system value since the display cutout does not exist
      return windowInsets.getSystemWindowInsetLeft();
    }
  }

  /**
   * @return The maximum of the display cutout right inset and the system window right inset.
   */
  public static int getMaxSafeRightInset(@NonNull WindowInsetsCompat windowInsets) {
    if (windowInsets.getDisplayCutout() != null) {
      final DisplayCutoutCompat displayCutout = windowInsets.getDisplayCutout();
      return Math.max(displayCutout.getSafeInsetRight(), windowInsets.getSystemWindowInsetRight());
    } else {
      // The max inset is just the system value since the display cutout does not exist
      return windowInsets.getSystemWindowInsetRight();
    }
  }

  /**
   * @return The maximum of the display cutout top inset and the system window top inset.
   */
  public static int getMaxSafeTopInset(@NonNull WindowInsetsCompat windowInsets) {
    if (windowInsets.getDisplayCutout() != null) {
      final DisplayCutoutCompat displayCutout = windowInsets.getDisplayCutout();
      return Math.max(displayCutout.getSafeInsetTop(), windowInsets.getSystemWindowInsetTop());
    } else {
      // The max inset is just the system value since the display cutout does not exist
      return windowInsets.getSystemWindowInsetTop();
    }
  }

  /**
   * @return The maximum of the display cutout bottom inset and the system window bottom inset.
   */
  public static int getMaxSafeBottomInset(@NonNull WindowInsetsCompat windowInsets) {
    if (windowInsets.getDisplayCutout() != null) {
      final DisplayCutoutCompat displayCutout = windowInsets.getDisplayCutout();
      return Math.max(displayCutout.getSafeInsetBottom(), windowInsets.getSystemWindowInsetBottom());
    } else {
      // The max inset is just the system value since the display cutout does not exist
      return windowInsets.getSystemWindowInsetBottom();
    }
  }

  /**
   * Detects if this device is currently in touch mode given a {@link View}.
   */
  public static boolean isDeviceNotInTouchMode(View view) {
    return !view.isInTouchMode();
  }
}
