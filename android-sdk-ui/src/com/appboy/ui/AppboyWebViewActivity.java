package com.appboy.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.appboy.Constants;
import com.appboy.enums.Channel;
import com.appboy.support.AppboyFileUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.activities.AppboyBaseActivity;

@SuppressLint("SetJavaScriptEnabled")
public class AppboyWebViewActivity extends AppboyBaseActivity {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyWebViewActivity.class);
  // The Intent extra string containing the URL to open.
  /**
   * @Deprecated use {@link Constants#APPBOY_WEBVIEW_URL_EXTRA} instead.
   */
  @Deprecated
  public static final String URL_EXTRA = Constants.APPBOY_WEBVIEW_URL_EXTRA;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_PROGRESS);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    // Enables hardware acceleration for the window. See https://developer.android.com/guide/topics/graphics/hardware-accel.html#controlling.
    // With this flag, we can view Youtube videos since HTML5 requires hardware acceleration.
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
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
    webSettings.setDisplayZoomControls(false);

    webSettings.setBuiltInZoomControls(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setDomStorageEnabled(true);

    // Instruct webview to be as large as its parent view.
    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    webView.setLayoutParams(layoutParams);

    webView.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        if (progress < 100) {
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
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
      }
    });

    webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
          // If the Uri scheme is not supported by a web action (i.e. if it's not a web url),
          // allow the system to try to open the uri first. This allows the system to handle,
          // for example, redirects to the play store via a "store://" Uri.
          if (!AppboyFileUtils.REMOTE_SCHEMES.contains(Uri.parse(url).getScheme())) {
            IAction action = ActionFactory.createUriActionFromUrlString(url, getIntent().getExtras(), false, Channel.UNKNOWN);
            // Instead of using AppboyNavigator, just open directly.
            action.execute(view.getContext());

            // Close the WebView if the action was executed successfully
            finish();
            return true;
          }
        } catch (Exception e) {
          AppboyLogger.i(TAG, "Unexpected exception while processing url " + url + ". Passing url back to WebView.", e);
        }
        return super.shouldOverrideUrlLoading(view, url);
      }
    });

    Bundle extras = getIntent().getExtras();
    // Opens the URL passed as an intent extra (if one exists).
    if (extras != null && extras.containsKey(URL_EXTRA)) {
      String url = extras.getString(URL_EXTRA);
      webView.loadUrl(url);
    }
  }
}
