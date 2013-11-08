package com.appboy.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.appboy.Appboy;

public class AppboyWebViewActivity extends Activity {
  // The Intent extra string containing the URL to open.
  public static final String URL_EXTRA = "url";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WebView webView = new WebView(this);

    WebSettings webSettings = webView.getSettings();
    // JavaScript is enabled by default to support a larger number of web pages. If JavaScript support is not
    // necessary, then it should be disabled.
    webSettings.setJavaScriptEnabled(true);
    webSettings.setAllowFileAccess(false);
    // Plugin support is disabled by default. If plugins, such as flash, are required, change the PluginState.
    webSettings.setPluginState(WebSettings.PluginState.OFF);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      webSettings.setDisplayZoomControls(false);
    }
    webSettings.setBuiltInZoomControls(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setLoadWithOverviewMode(true);

    webView.setWebViewClient(new WebViewClient());

    Bundle extras = getIntent().getExtras();
    // Opens the URL passed as an intent extra (if one exists).
    if (extras != null && extras.containsKey(URL_EXTRA)) {
      String url = extras.getString(URL_EXTRA);
      webView.loadUrl(url);
    }
    setContentView(webView);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session.
    Appboy.getInstance(this).openSession(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession(this);
  }
}
