package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
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
      mMessageWebView = (AppboyInAppMessageWebView) findViewById(R.id.com_appboy_inappmessage_html_full_webview);
      if (mMessageWebView != null) {
        WebSettings webSettings = mMessageWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          webSettings.setDisplayZoomControls(false);
          mMessageWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mMessageWebView.setBackgroundColor(Color.TRANSPARENT);
      }
    }
    return mMessageWebView;
  }
}
