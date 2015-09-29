package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.appboy.ui.R;

public class AppboyInAppMessageHtmlFullView extends AppboyInAppMessageHtmlBaseView {

  private WebView mMessageWebView;

  public AppboyInAppMessageHtmlFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public WebView getMessageWebView() {
    if (mMessageWebView == null) {
      mMessageWebView = (WebView) findViewById(R.id.com_appboy_inappmessage_html_full_webview);
      if (mMessageWebView != null) {
        mMessageWebView.getSettings().setJavaScriptEnabled(true);
      }
    }
    return mMessageWebView;
  }
}
