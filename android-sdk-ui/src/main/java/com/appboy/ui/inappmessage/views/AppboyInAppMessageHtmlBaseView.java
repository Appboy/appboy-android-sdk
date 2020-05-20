package com.appboy.ui.inappmessage.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.appboy.support.AppboyLogger;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.IInAppMessageView;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;
import com.appboy.ui.inappmessage.listeners.IWebViewClientStateListener;
import com.appboy.ui.support.ViewUtils;

public abstract class AppboyInAppMessageHtmlBaseView extends RelativeLayout implements IInAppMessageView {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageHtmlBaseView.class);
  private static final String HTML_MIME_TYPE = "text/html";
  private static final String HTML_ENCODING = "utf-8";
  private static final String FILE_URI_SCHEME_PREFIX = "file://";

  protected WebView mMessageWebView;

  public static final String APPBOY_BRIDGE_PREFIX = "appboyInternalBridge";

  private InAppMessageWebViewClient mInAppMessageWebViewClient;

  public AppboyInAppMessageHtmlBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * @return The {@link WebView} displaying the HTML content of this in-app message.
   */
  @SuppressLint({"SetJavaScriptEnabled"})
  public WebView getMessageWebView() {
    if (mMessageWebView == null && getWebViewViewId() != 0) {
      mMessageWebView = findViewById(getWebViewViewId());
      if (mMessageWebView != null) {
        WebSettings webSettings = mMessageWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        // This enables hardware acceleration if the manifest also has it defined.
        // If not defined, then the layer type will fallback to software.
        mMessageWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mMessageWebView.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ViewUtils.isDeviceInNightMode(getContext())) {
          webSettings.setForceDark(WebSettings.FORCE_DARK_ON);
        }

        // Set the client for console logging. See https://developer.android.com/guide/webapps/debugging.html
        mMessageWebView.setWebChromeClient(new WebChromeClient() {
          @Override
          public boolean onConsoleMessage(ConsoleMessage cm) {
            AppboyLogger.d(TAG, "Braze HTML In-app Message log. Line: " + cm.lineNumber()
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
      }
    }
    return mMessageWebView;
  }

  public View getMessageClickableView() {
    return this;
  }

  /**
   * Loads the WebView using an html string and local file resource url. This url should be a path
   * to a file on the local filesystem.
   *
   * @param htmlBody   Html text encoded in utf-8
   * @param assetDirectoryUrl path to the local assets file
   */
  public void setWebViewContent(String htmlBody, String assetDirectoryUrl) {
    getMessageWebView().loadDataWithBaseURL(FILE_URI_SCHEME_PREFIX + assetDirectoryUrl + "/", htmlBody, HTML_MIME_TYPE, HTML_ENCODING, null);
  }

  /**
   * Loads the WebView using just an html string.
   *
   * @param htmlBody Html text encoded in utf-8
   */
  public void setWebViewContent(String htmlBody) {
    // File URIs must be loaded with this "file://" scheme
    // since our html might have mixed http/data/file content
    // See https://developer.android.com/reference/android/webkit/WebView#loadData(java.lang.String,%20java.lang.String,%20java.lang.String)
    getMessageWebView().loadDataWithBaseURL(FILE_URI_SCHEME_PREFIX + "/", htmlBody, HTML_MIME_TYPE, HTML_ENCODING, null);
  }

  public void setInAppMessageWebViewClient(InAppMessageWebViewClient inAppMessageWebViewClient) {
    getMessageWebView().setWebViewClient(inAppMessageWebViewClient);
    mInAppMessageWebViewClient = inAppMessageWebViewClient;
  }

  public void setHtmlPageFinishedListener(IWebViewClientStateListener listener) {
    if (mInAppMessageWebViewClient != null) {
      mInAppMessageWebViewClient.setWebViewClientStateListener(listener);
    }
  }

  /**
   * Html in-app messages can alternatively be closed by the back button.
   *
   * Note: If the internal WebView has focus instead of this view, back button events on html
   * in-app messages are handled separately in {@link AppboyInAppMessageWebView#onKeyDown(int, KeyEvent)}
   *
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && AppboyInAppMessageManager.getInstance().getDoesBackButtonDismissInAppMessageView()) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  /**
   * Returns the {@link View#getId()} used in the
   * default {@link AppboyInAppMessageHtmlBaseView#getMessageWebView()}
   * implementation.
   *
   * @return The {@link View#getId()} for the {@link WebView} backing this message.
   */
  public abstract int getWebViewViewId();

  /**
   * HTML messages can alternatively be closed by the back button.
   *
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (!isInTouchMode() && event.getKeyCode() == KeyEvent.KEYCODE_BACK && AppboyInAppMessageManager.getInstance().getDoesBackButtonDismissInAppMessageView()) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.dispatchKeyEvent(event);
  }
}
