package com.appboy.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.appboy.Appboy;

public class AppboyWebViewActivity extends Activity {

  private WebView mWebView;
  private String mUrl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.webview_layout);

    Intent intent = getIntent();
    mUrl = intent.getStringExtra(Constants.APPBOY_WEBVIEW_URL_KEY);

    mWebView = (WebView) findViewById(R.id.ab_webview);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.loadUrl(mUrl);
    mWebView.setWebViewClient(new AppboyWebviewClient());
  }

  // Handle intent filter
  // http://developer.android.com/resources/tutorials/views/hello-webview.html
  private static class AppboyWebviewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    Appboy.getInstance(this).openSession();
  }

  @Override
  public void onStop() {
    Appboy.getInstance(this).closeSession();
    super.onStop();
  }
}
