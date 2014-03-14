package com.appboy.ui.support;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {
  public static void removeViewFromParent(View view) {
    if (view != null) {
      if (view.getParent() instanceof ViewGroup) {
        ((ViewGroup) view.getParent()).removeView(view);
      }
    }
  }
}
