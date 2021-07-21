package com.braze.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils;

/**
 * WebView embedded in Braze html in-app messages.
 */
public class InAppMessageWebView extends WebView {

  public InAppMessageWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * If the back button is pressed while this WebView is in focus,
   * close the current in-app message.
   *
   * Note: When this WebView doesn't have focus, back button events on html in-app messages are
   * captured by {@link InAppMessageHtmlFullView#onKeyDown(int, KeyEvent)}
   *
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && BrazeInAppMessageManager.getInstance().getDoesBackButtonDismissInAppMessageView()) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  /**
   * WebView-based messages can alternatively be closed by the back button.
   *
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (!isInTouchMode() && event.getKeyCode() == KeyEvent.KEYCODE_BACK && BrazeInAppMessageManager.getInstance().getDoesBackButtonDismissInAppMessageView()) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.dispatchKeyEvent(event);
  }
}
