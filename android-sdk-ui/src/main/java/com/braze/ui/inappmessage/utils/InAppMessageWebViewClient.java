package com.braze.ui.inappmessage.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;

import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.support.BrazeFileUtils;
import com.braze.support.BrazeLogger;
import com.braze.support.HandlerUtils;
import com.braze.support.StringUtils;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.braze.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.braze.ui.inappmessage.listeners.IWebViewClientStateListener;
import com.braze.ui.support.UriUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class InAppMessageWebViewClient extends WebViewClient {
  private static final String TAG = BrazeLogger.getBrazeLogTag(InAppMessageWebViewClient.class);
  private static final String APPBOY_INAPP_MESSAGE_SCHEME = "appboy";
  private static final String AUTHORITY_NAME_CLOSE = "close";
  private static final String AUTHORITY_NAME_NEWSFEED = "feed";
  private static final String AUTHORITY_NAME_CUSTOM_EVENT = "customEvent";

  /**
   * The query key for the button id for tracking
   */
  public static final String QUERY_NAME_BUTTON_ID = "abButtonId";

  /**
   * The query key for opening links externally (i.e. outside your app). Url intents will be opened with
   * the INTENT.ACTION_VIEW intent. Links beginning with the appboy:// scheme are unaffected by this query key.
   */
  public static final String QUERY_NAME_EXTERNAL_OPEN = "abExternalOpen";
  /**
   * Query key for directing Braze to open Url intents using the INTENT.ACTION_VIEW.
   */
  public static final String QUERY_NAME_DEEPLINK = "abDeepLink";
  public static final String JAVASCRIPT_PREFIX = "javascript:";

  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;
  private final IInAppMessage mInAppMessage;
  private final Context mContext;
  @Nullable
  private IWebViewClientStateListener mWebViewClientStateListener;
  private boolean mHasPageFinishedLoading = false;
  private final AtomicBoolean mHasCalledPageFinishedOnListener = new AtomicBoolean(false);
  private final Handler mHandler;
  private final Runnable mPostOnFinishedTimeoutRunnable;
  private final int mMaxOnPageFinishedWaitTimeMs;

  /**
   * @param inAppMessage                      the In-App Message being displayed in this WebView
   * @param inAppMessageWebViewClientListener the client listener. Should be non-null.
   */
  public InAppMessageWebViewClient(Context context,
                                   IInAppMessage inAppMessage,
                                   IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
    mInAppMessage = inAppMessage;
    mContext = context;
    mHandler = HandlerUtils.createHandler();
    mPostOnFinishedTimeoutRunnable = () -> {
      if (mWebViewClientStateListener != null
          && mHasCalledPageFinishedOnListener.compareAndSet(false, true)) {
        BrazeLogger.v(TAG, "Page may not have finished loading, but max wait time has expired."
            + " Calling onPageFinished on listener.");
        mWebViewClientStateListener.onPageFinished();
      }
    };
    mMaxOnPageFinishedWaitTimeMs = new BrazeConfigurationProvider(context).getInAppMessageWebViewClientOnPageFinishedMaxWaitMs();
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    appendBridgeJavascript(view);
    if (mWebViewClientStateListener != null && mHasCalledPageFinishedOnListener.compareAndSet(false, true)) {
      BrazeLogger.v(TAG, "Page has finished loading. Calling onPageFinished on listener");
      mWebViewClientStateListener.onPageFinished();
    }
    mHasPageFinishedLoading = true;

    // Cancel any pending runnables based on the page finished wait
    mHandler.removeCallbacks(mPostOnFinishedTimeoutRunnable);
  }

  private void appendBridgeJavascript(WebView view) {
    String javascriptString = BrazeFileUtils.getAssetFileStringContents(mContext.getAssets(), "appboy-html-in-app-message-javascript-component.js");
    if (javascriptString == null) {

      // Fail instead of present a broken WebView
      BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(false);
      BrazeLogger.w(TAG, "Failed to get HTML in-app message javascript additions");
      return;
    }

    view.loadUrl(JAVASCRIPT_PREFIX + javascriptString);
  }

  /**
   * Handles `appboy` schemed ("appboy://") urls in the HTML content WebViews. If the url isn't
   * `appboy` schemed, then the url is passed to the attached IInAppMessageWebViewClientListener.
   * <p/>
   * We expect the URLs to be hierarchical and have `appboy` equal the scheme.
   * For example, `appboy://close` is one such URL.
   *
   * @return true since all actions in Html In-App Messages are handled outside of the In-App Message itself.
   */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    return handleUrlOverride(request.getUrl().toString());
  }

  @SuppressWarnings("deprecation") // Updated `shouldOverrideUrlLoading()` method is called elsewhere in this client
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    return handleUrlOverride(url);
  }

  public void setWebViewClientStateListener(@Nullable IWebViewClientStateListener listener) {
    // If the page is already done loading, inform the new listener
    if (listener != null && mHasPageFinishedLoading && mHasCalledPageFinishedOnListener.compareAndSet(false, true)) {
      listener.onPageFinished();
    } else {
      mHandler.postDelayed(mPostOnFinishedTimeoutRunnable, mMaxOnPageFinishedWaitTimeMs);
    }
    mWebViewClientStateListener = listener;
  }

  private boolean handleUrlOverride(String url) {
    if (mInAppMessageWebViewClientListener == null) {
      BrazeLogger.i(TAG, "InAppMessageWebViewClient was given null IInAppMessageWebViewClientListener listener. Returning true.");
      return true;
    }

    if (StringUtils.isNullOrBlank(url)) {
      // Null or blank urls shouldn't be passed back to the WebView. We return true here to indicate
      // to the WebView that we handled the url.
      BrazeLogger.i(TAG, "InAppMessageWebViewClient.shouldOverrideUrlLoading was given null or blank url. Returning true.");
      return true;
    }

    Uri uri = Uri.parse(url);
    Bundle queryBundle = getBundleFromUrl(url);
    if (uri.getScheme() != null && uri.getScheme().equals(APPBOY_INAPP_MESSAGE_SCHEME)) {
      // Check the authority
      String authority = uri.getAuthority();
      if (authority != null) {
        switch (authority) {
          case AUTHORITY_NAME_CLOSE:
            mInAppMessageWebViewClientListener.onCloseAction(mInAppMessage, url, queryBundle);
            break;
          case AUTHORITY_NAME_NEWSFEED:
            mInAppMessageWebViewClientListener.onNewsfeedAction(mInAppMessage, url, queryBundle);
            break;
          case AUTHORITY_NAME_CUSTOM_EVENT:
            mInAppMessageWebViewClientListener.onCustomEventAction(mInAppMessage, url, queryBundle);
            break;
          default:
            // continue on
        }
      } else {
        BrazeLogger.d(TAG, "Uri authority was null. Uri: " + uri);
      }
      return true;
    } else {
      BrazeLogger.d(TAG, "Uri scheme was null. Uri: " + uri);
    }
    mInAppMessageWebViewClientListener.onOtherUrlAction(mInAppMessage, url, queryBundle);
    return true;
  }

  /**
   * Returns the string mapping of the query keys and values from the query string of the url. If the query string
   * contains duplicate keys, then the last key in the string will be kept.
   *
   * @param url the url
   * @return a bundle containing the key/value mapping of the query string. Will not be null.
   */
  @VisibleForTesting
  static Bundle getBundleFromUrl(String url) {
    Bundle queryBundle = new Bundle();
    if (StringUtils.isNullOrBlank(url)) {
      return queryBundle;
    }

    Uri uri = Uri.parse(url);
    Map<String, String> queryParameterMap = UriUtils.getQueryParameters(uri);
    for (String queryKeyName : queryParameterMap.keySet()) {
      String queryValue = queryParameterMap.get(queryKeyName);
      queryBundle.putString(queryKeyName, queryValue);
    }
    return queryBundle;
  }
}
