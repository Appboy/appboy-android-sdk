package com.braze.ui.inappmessage.listeners;

import android.webkit.WebView;

public interface IWebViewClientStateListener {
  /**
   * Fired when {@link android.webkit.WebViewClient#onPageFinished(WebView, String)} has been called.
   */
  void onPageFinished();
}
