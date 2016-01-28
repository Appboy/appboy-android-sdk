package com.appboy.ui.inappmessage;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.appboy.Constants;
import com.appboy.models.IInAppMessage;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.appboy.ui.support.UriUtils;

import java.util.Map;

public class InAppMessageWebViewClient extends WebViewClient {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, InAppMessageWebViewClient.class.getName());
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

  private IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;
  private final IInAppMessage mInAppMessage;

  /**
   * @param inAppMessage                      the In-App Message being displayed in this WebView
   * @param inAppMessageWebViewClientListener the client listener. Should be non-null.
   */
  public InAppMessageWebViewClient(IInAppMessage inAppMessage, IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
    mInAppMessage = inAppMessage;
  }

  /**
   * Handles Appboy schemed ("appboy://") urls in the HTML content WebViews. If the url isn't Appboy schemed, then the url is passed
   * to the attached IInAppMessageWebViewClientListener.
   * <p/>
   * We expect the URLs to be hierarchical and have "appboy" equal the scheme.
   * For example, appboy://close is one such URL.
   *
   * @return true since all actions in Html In-App Messages are handled outside of the In-App Message itself.
   */
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    if (mInAppMessageWebViewClientListener == null) {
      AppboyLogger.i(TAG, "InAppMessageWebViewClient was given null IInAppMessageWebViewClientListener listener. Returning true.");
      return true;
    }

    if (StringUtils.isNullOrBlank(url)) {
      // Null or blank urls shouldn't be passed back to the WebView. We return true here to indicate
      // to the WebView that we handled the url.
      AppboyLogger.i(TAG, "InAppMessageWebViewClient.shouldOverrideUrlLoading was given null or blank url. Returning true.");
      return true;
    }

    Uri uri = Uri.parse(url);
    Bundle queryBundle = getBundleFromUrl(url);
    if (uri.getScheme().equals(APPBOY_INAPP_MESSAGE_SCHEME)) {
      // Check the authority
      String authority = uri.getAuthority();
      if (authority.equals(AUTHORITY_NAME_CLOSE)) {
        mInAppMessageWebViewClientListener.onCloseAction(mInAppMessage, url, queryBundle);
      } else if (authority.equals(AUTHORITY_NAME_NEWSFEED)) {
        mInAppMessageWebViewClientListener.onNewsfeedAction(mInAppMessage, url, queryBundle);
      } else if (authority.equals(AUTHORITY_NAME_CUSTOM_EVENT)) {
        mInAppMessageWebViewClientListener.onCustomEventAction(mInAppMessage, url, queryBundle);
      }
      return true;
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
  // Default visibility for testing
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
