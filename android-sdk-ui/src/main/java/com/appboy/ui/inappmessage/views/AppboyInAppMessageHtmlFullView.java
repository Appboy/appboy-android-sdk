package com.appboy.ui.inappmessage.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.jsinterface.AppboyInAppMessageHtmlJavascriptInterface;

public class AppboyInAppMessageHtmlFullView extends AppboyInAppMessageHtmlBaseView {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageHtmlFullView.class);
  public static final String APPBOY_BRIDGE_PREFIX = "appboyInternalBridge";

  private WebView mMessageWebView;

  public AppboyInAppMessageHtmlFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
  @Override
  public WebView getMessageWebView() {
    if (mMessageWebView == null) {
      mMessageWebView = findViewById(R.id.com_appboy_inappmessage_html_full_webview);
      if (mMessageWebView != null) {
        WebSettings webSettings = mMessageWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDisplayZoomControls(false);
        // This enables hardware acceleration if the manifest also has it defined. If not defined, then the layer type will fallback to software
        mMessageWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mMessageWebView.setBackgroundColor(Color.TRANSPARENT);

        // Set the client for console logging. See https://developer.android.com/guide/webapps/debugging.html
        mMessageWebView.setWebChromeClient(new WebChromeClient() {
          @Override
          public boolean onConsoleMessage(ConsoleMessage cm) {
            AppboyLogger.d(TAG, "Html In-app log. Line: " + cm.lineNumber() + ". SourceId: " + cm.sourceId() + ". Log Level: " + cm.messageLevel() + ". Message: " + cm.message());
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

        mMessageWebView.addJavascriptInterface(new AppboyInAppMessageHtmlJavascriptInterface(getContext()), APPBOY_BRIDGE_PREFIX);
      }
    }
    return mMessageWebView;
  }

  @Override
  public void applyWindowInsets(WindowInsetsCompat insets) {
    // HTML in-app messages don't have special behavior with respect to notched devices at the View level.
  }
}
