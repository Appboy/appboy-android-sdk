package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;

import com.braze.ui.inappmessage.views.InAppMessageWebView;

/**
 * @deprecated Please use {@link #InAppMessageWebView} instead. Deprecated since 6/16/21
 */
@Deprecated
public class AppboyInAppMessageWebView extends InAppMessageWebView {
  public AppboyInAppMessageWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
}
