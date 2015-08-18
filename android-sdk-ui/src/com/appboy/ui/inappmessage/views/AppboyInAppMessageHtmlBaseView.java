package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.appboy.Constants;
import com.appboy.ui.inappmessage.IInAppMessageView;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;

public abstract class AppboyInAppMessageHtmlBaseView extends RelativeLayout implements IInAppMessageView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageHtmlBaseView.class.getName());
  private static final String HTML_MIME_TYPE = "text/html";
  private static final String HTML_ENCODING = "utf-8";

  public AppboyInAppMessageHtmlBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public View getMessageClickableView() {
    return this;
  }

  public abstract WebView getMessageWebView();

  /**
   * Loads the WebView using an html string and local file resource url. This url is expected to point to a location
   * on the device, i.e. a "file://" url.
   *
   * @param htmlBody   Html text encoded in utf-8
   * @param assetDirectoryUrl the local file url
   */
  public void setWebViewContent(String htmlBody, String assetDirectoryUrl) {
    getMessageWebView().loadDataWithBaseURL(assetDirectoryUrl, htmlBody, HTML_MIME_TYPE, HTML_ENCODING, null);
  }

  public void setInAppMessageWebViewClient(InAppMessageWebViewClient inAppMessageWebViewClient) {
    getMessageWebView().setWebViewClient(inAppMessageWebViewClient);
  }

  /**
   * Html full screen messages can alternatively be closed by the back button.
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
