package com.appboy.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appboy.ui.activities.AppboyBaseActivity;

public class AppboyWebViewActivity extends AppboyBaseActivity {
  // The Intent extra string containing the URL to open.
  public static final String URL_EXTRA = "url";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_PROGRESS);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.com_appboy_webview_activity);
    setProgressBarVisibility(true);

    WebView webView = (WebView) findViewById(R.id.com_appboy_webview_activity_webview);

    WebSettings webSettings = webView.getSettings();
    // JavaScript is enabled by default to support a larger number of web pages. If JavaScript support is not
    // necessary, then it should be disabled.
    webSettings.setJavaScriptEnabled(true);
    webSettings.setAllowFileAccess(false);
    // Plugin support is disabled by default. If plugins, such as flash, are required, change the PluginState.
    webSettings.setPluginState(WebSettings.PluginState.OFF);
    setZoomSafe(webSettings);

    webSettings.setBuiltInZoomControls(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setLoadWithOverviewMode(true);

    webView.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        if (progress < 100){
          setProgressBarVisibility(true);
        } else {
          setProgressBarVisibility(false);
        }
      }
    });

    webView.setDownloadListener(new DownloadListener() {
      public void onDownloadStart(String url, String userAgent,
                                  String contentDisposition, String mimetype,
                                  long contentLength) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
      }
    });

    webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    setWebLayerTypeSafe(webView);

    webView.setWebViewClient(new WebViewClient());

    Bundle extras = getIntent().getExtras();
    // Opens the URL passed as an intent extra (if one exists).
    if (extras != null && extras.containsKey(URL_EXTRA)) {
      String url = extras.getString(URL_EXTRA);
      webView.loadUrl(url);
    }
  }

  @TargetApi(11)
  private void setZoomSafe(WebSettings webSettings) {
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      webSettings.setDisplayZoomControls(false);
    }
  }

  @TargetApi(11)
  private void setWebLayerTypeSafe(WebView webView) {
    if (Build.VERSION.SDK_INT >= 11) {
      webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }
}
