package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.WebView;

/**
 * WebView embedded in Appboy html in-app messages.
 */
public class AppboyInAppMessageWebView extends WebView {

  public AppboyInAppMessageWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * If the back button is pressed while this WebView is in focus,
   * close the current in-app message.
   *
   * Note: When this WebView doesn't have focus, back button events on html in-app messages are
   * captured by {@link AppboyInAppMessageHtmlFullView#onKeyDown(int, KeyEvent)}
   *
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
