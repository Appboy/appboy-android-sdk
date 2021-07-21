package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;

import com.braze.ui.inappmessage.views.InAppMessageBoundedLayout;

/**
 * @deprecated Please use {@link InAppMessageBoundedLayout} instead. Deprecated since 6/16/21
 */
@Deprecated
public class AppboyInAppMessageBoundedLayout extends InAppMessageBoundedLayout {
  public AppboyInAppMessageBoundedLayout(Context context) {
    super(context);
  }

  public AppboyInAppMessageBoundedLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
