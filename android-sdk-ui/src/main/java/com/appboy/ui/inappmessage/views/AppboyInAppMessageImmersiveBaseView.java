package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;

import com.braze.ui.inappmessage.views.InAppMessageImmersiveBaseView;

/**
 * @deprecated Please use {@link #InAppMessageImmersiveBaseView} instead. Deprecated since 6/16/21
 */
@Deprecated
public abstract class AppboyInAppMessageImmersiveBaseView extends InAppMessageImmersiveBaseView {
  public AppboyInAppMessageImmersiveBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
