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
import android.widget.RelativeLayout;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.actions.WebAction;
import com.appboy.ui.activities.AppboyBaseActivity;

public class AppboyWebViewActivity extends AppboyBaseActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyWebViewActivity.class.getName());
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
    setWebLayerTypeSafe(webView);

    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
          // If the Uri scheme is not supported by a web action (i.e. if it's not a web url),
          // allow the system to try to open the uri first.  This allows the system to handle,
          // for example, redirects to the play store via a "store://" Uri.
          if (!WebAction.getSupportedSchemes().contains(Uri.parse(url).getScheme())) {
            IAction action = ActionFactory.createViewUriAction(url, getIntent().getExtras());
            action.execute(view.getContext());

            // Close the WebView if the action was executed successfully
            finish();
            return true;
          }
        } catch (Exception e) {
          AppboyLogger.i(TAG, String.format("Unexpected exception while processing url %s. "
              + "Passing url back to WebView.", url), e);
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

  @TargetApi(11)
  private void setZoomSafe(WebSettings webSettings) {
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      webSettings.setDisplayZoomControls(false);
    }
  }

  @TargetApi(11)
  private void setWebLayerTypeSafe(WebView webView) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }
}
