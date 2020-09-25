package com.appboy.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.appboy.Constants;
import com.appboy.enums.Channel;
import com.appboy.support.AppboyFileUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.activities.AppboyBaseActivity;
import com.appboy.ui.support.ViewUtils;

@SuppressLint("SetJavaScriptEnabled")
public class AppboyWebViewActivity extends AppboyBaseActivity {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyWebViewActivity.class);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Enables hardware acceleration for the window. See https://developer.android.com/guide/topics/graphics/hardware-accel.html#controlling.
    // With this flag, we can view Youtube videos since HTML5 requires hardware acceleration.
    getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    setContentView(R.layout.com_appboy_webview_activity);

    WebView webView = findViewById(R.id.com_appboy_webview_activity_webview);
    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    WebSettings webSettings = webView.getSettings();
    webSettings.setAllowFileAccess(false);
    webSettings.setBuiltInZoomControls(true);
    webSettings.setJavaScriptEnabled(true);
    webSettings.setUseWideViewPort(true);
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setDisplayZoomControls(false);
    webSettings.setDomStorageEnabled(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ViewUtils.isDeviceInNightMode(this.getApplicationContext())) {
      webSettings.setForceDark(WebSettings.FORCE_DARK_ON);
    }

    webView.setWebChromeClient(new WebChromeClient() {
      @Override
      public boolean onConsoleMessage(ConsoleMessage cm) {
        AppboyLogger.d(TAG, "Braze WebView Activity log. Line: " + cm.lineNumber()
            + ". SourceId: " + cm.sourceId()
            + ". Log Level: " + cm.messageLevel()
            + ". Message: " + cm.message());
        return true;
      }

      @Nullable
      @Override
      public Bitmap getDefaultVideoPoster() {
        // This bitmap is used to eliminate the default black & white
        // play icon used as the default poster.
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
      }
    });

    webView.setWebViewClient(new WebViewClient() {
      @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Boolean didHandleUrl = handleUrlOverride(view.getContext(), request.getUrl().toString());
        if (didHandleUrl != null) {
          return didHandleUrl;
        }
        return super.shouldOverrideUrlLoading(view, request);
      }

      @SuppressWarnings("deprecation") // Updated method is called above
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Boolean didHandleUrl = handleUrlOverride(view.getContext(), url);
        if (didHandleUrl != null) {
          return didHandleUrl;
        }
        return super.shouldOverrideUrlLoading(view, url);
      }

      /**
       * Handles the URL override when the link is not better handled by this WebView instead.
       * <br>
       * <br>
       * E.g. in the presence of a https link, this WebView should handle it and not
       * forward to Chrome. However in the presence of a sms link, the system itself
       * should handle it since a WebView lacks that ability.
       *
       * @return True/False when this URL override was handled. Returns null when the super implementation of
       * {@link WebViewClient#shouldOverrideUrlLoading(WebView, WebResourceRequest)} should be called instead.
       */
      private Boolean handleUrlOverride(Context context, String url) {
        try {
          if (AppboyFileUtils.REMOTE_SCHEMES.contains(Uri.parse(url).getScheme())) {
            return null;
          }

          IAction action = ActionFactory.createUriActionFromUrlString(url, getIntent().getExtras(), false, Channel.UNKNOWN);
          if (action != null) {
            // Instead of using AppboyNavigator, just open directly.
            action.execute(context);

            // Close the WebView if the action was executed successfully
            finish();
            return true;
          } else {
            return false;
          }
        } catch (Exception e) {
          AppboyLogger.e(TAG, "Unexpected exception while processing url " + url + ". Passing url back to WebView.", e);
        }
        return null;
      }
    });

    Bundle extras = getIntent().getExtras();
    // Opens the URL passed as an intent extra (if one exists).
    if (extras != null && extras.containsKey(Constants.APPBOY_WEBVIEW_URL_EXTRA)) {
      String url = extras.getString(Constants.APPBOY_WEBVIEW_URL_EXTRA);
      if (url != null) {
        webView.loadUrl(url);
      }
    }
  }
}
