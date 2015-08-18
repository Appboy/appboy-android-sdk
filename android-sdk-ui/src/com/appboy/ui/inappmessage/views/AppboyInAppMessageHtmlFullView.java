package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.appboy.Constants;
import com.appboy.ui.R;

public class AppboyInAppMessageHtmlFullView extends AppboyInAppMessageHtmlBaseView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageHtmlFullView.class.getName());

  public AppboyInAppMessageHtmlFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public WebView getMessageWebView() {
    return (WebView) findViewById(R.id.com_appboy_inappmessage_html_full_webview);
  }
}
