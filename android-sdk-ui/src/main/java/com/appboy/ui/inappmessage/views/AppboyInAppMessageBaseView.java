package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;

import com.braze.ui.inappmessage.views.InAppMessageBaseView;

/**
 * @deprecated Please use {@link #InAppMessageBaseView} instead. Deprecated since 6/16/21
 */
@Deprecated
public abstract class AppboyInAppMessageBaseView extends InAppMessageBaseView {
  public AppboyInAppMessageBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
