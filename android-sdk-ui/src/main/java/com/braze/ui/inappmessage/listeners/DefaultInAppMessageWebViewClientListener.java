package com.braze.ui.inappmessage.listeners;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.VisibleForTesting;

import com.appboy.enums.Channel;
import com.appboy.models.outgoing.AppboyProperties;
import com.braze.Braze;
import com.braze.enums.inappmessage.MessageType;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.IInAppMessageHtml;
import com.braze.support.BrazeFileUtils;
import com.braze.support.BrazeLogger;
import com.braze.support.BundleUtils;
import com.braze.support.StringUtils;
import com.braze.ui.BrazeDeeplinkHandler;
import com.braze.ui.actions.NewsfeedAction;
import com.braze.ui.actions.UriAction;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.braze.ui.inappmessage.utils.InAppMessageWebViewClient;

public class DefaultInAppMessageWebViewClientListener implements IInAppMessageWebViewClientListener {
  private static final String TAG = BrazeLogger.getBrazeLogTag(DefaultInAppMessageWebViewClientListener.class);
  private static final String HTML_IN_APP_MESSAGE_CUSTOM_EVENT_NAME_KEY = "name";

  @Override
  public void onCloseAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    BrazeLogger.d(TAG, "IInAppMessageWebViewClientListener.onCloseAction called.");

    logHtmlInAppMessageClick(inAppMessage, queryBundle);

    // Dismiss the in-app message due to the close action
    getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(true);
    getInAppMessageManager().getHtmlInAppMessageActionListener().onCloseClicked(inAppMessage, url, queryBundle);
  }

  @Override
  public void onNewsfeedAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    BrazeLogger.d(TAG, "IInAppMessageWebViewClientListener.onNewsfeedAction called.");
    if (getInAppMessageManager().getActivity() == null) {
      BrazeLogger.w(TAG, "Can't perform news feed action because the cached activity is null.");
      return;
    }
    // Log a click since the user left to the newsfeed
    logHtmlInAppMessageClick(inAppMessage, queryBundle);

    boolean handled = getInAppMessageManager().getHtmlInAppMessageActionListener().onNewsfeedClicked(inAppMessage, url, queryBundle);
    if (!handled) {
      inAppMessage.setAnimateOut(false);
      // Dismiss the in-app message since we're navigating away to the news feed
      getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(false);
      NewsfeedAction newsfeedAction = new NewsfeedAction(BundleUtils.mapToBundle(inAppMessage.getExtras()),
          Channel.INAPP_MESSAGE);
      BrazeDeeplinkHandler.getInstance().gotoNewsFeed(getInAppMessageManager().getActivity(), newsfeedAction);
    }
  }

  @Override
  public void onCustomEventAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    BrazeLogger.d(TAG, "IInAppMessageWebViewClientListener.onCustomEventAction called.");
    if (getInAppMessageManager().getActivity() == null) {
      BrazeLogger.w(TAG, "Can't perform custom event action because the activity is null.");
      return;
    }

    boolean handled = getInAppMessageManager().getHtmlInAppMessageActionListener().onCustomEventFired(inAppMessage, url, queryBundle);
    if (!handled) {
      String customEventName = parseCustomEventNameFromQueryBundle(queryBundle);
      if (StringUtils.isNullOrBlank(customEventName)) {
        return;
      }
      AppboyProperties customEventProperties = parsePropertiesFromQueryBundle(queryBundle);
      Braze.getInstance(getInAppMessageManager().getActivity()).logCustomEvent(customEventName, customEventProperties);
    }
  }

  @Override
  public void onOtherUrlAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    BrazeLogger.d(TAG, "IInAppMessageWebViewClientListener.onOtherUrlAction called.");
    if (getInAppMessageManager().getActivity() == null) {
      BrazeLogger.w(TAG, "Can't perform other url action because the cached activity is null. Url: " + url);
      return;
    }
    // Log a click since the uri link was followed
    logHtmlInAppMessageClick(inAppMessage, queryBundle);

    boolean handled = getInAppMessageManager().getHtmlInAppMessageActionListener().onOtherUrlAction(inAppMessage, url, queryBundle);
    if (handled) {
      BrazeLogger.v(TAG, "HTML message action listener handled url in onOtherUrlAction. Doing nothing further. Url: " + url);
      return;
    }

    // Parse the action
    boolean useWebViewForWebLinks = parseUseWebViewFromQueryBundle(inAppMessage, queryBundle);
    Bundle inAppMessageBundle = BundleUtils.mapToBundle(inAppMessage.getExtras());
    inAppMessageBundle.putAll(queryBundle);
    UriAction uriAction = BrazeDeeplinkHandler.getInstance().createUriActionFromUrlString(url, inAppMessageBundle, useWebViewForWebLinks, Channel.INAPP_MESSAGE);

    if (uriAction == null) {
      BrazeLogger.w(TAG, "UriAction is null. Not passing any URI to BrazeDeeplinkHandler. Url: " + url);
      return;
    }

    // If a local Uri is being handled here, then we want to keep the user in the Html in-app message and not hide the current in-app message.
    Uri uri = uriAction.getUri();
    if (BrazeFileUtils.isLocalUri(uri)) {
      BrazeLogger.w(TAG, "Not passing local uri to BrazeDeeplinkHandler. Got local uri: " + uri + " for url: " + url);
      return;
    }

    // Handle the action if it's not a local Uri
    inAppMessage.setAnimateOut(false);
    // Dismiss the in-app message since we're handling the URI outside of the in-app message webView
    getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(false);
    BrazeDeeplinkHandler.getInstance().gotoUri(getInAppMessageManager().getActivity(), uriAction);
  }

  @VisibleForTesting
  static boolean parseUseWebViewFromQueryBundle(IInAppMessage inAppMessage, Bundle queryBundle) {
    boolean anyQueryFlagSet = false;
    boolean deepLinkFlag = false;
    if (queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_DEEPLINK)) {
      deepLinkFlag = Boolean.parseBoolean(queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_DEEPLINK));
      anyQueryFlagSet = true;
    }
    boolean externalOpenFlag = false;
    if (queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_EXTERNAL_OPEN)) {
      externalOpenFlag = Boolean.parseBoolean(queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_EXTERNAL_OPEN));
      anyQueryFlagSet = true;
    }
    boolean useWebViewForWebLinks = inAppMessage.getOpenUriInWebView();
    if (anyQueryFlagSet) {
      useWebViewForWebLinks = !(deepLinkFlag || externalOpenFlag);
    }
    return useWebViewForWebLinks;
  }

  private BrazeInAppMessageManager getInAppMessageManager() {
    return BrazeInAppMessageManager.getInstance();
  }

  @VisibleForTesting
  static void logHtmlInAppMessageClick(IInAppMessage inAppMessage, Bundle queryBundle) {
    if (queryBundle != null && queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_BUTTON_ID)) {
      IInAppMessageHtml inAppMessageHtml = (IInAppMessageHtml) inAppMessage;
      inAppMessageHtml.logButtonClick(queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_BUTTON_ID));
    } else {
      if (inAppMessage.getMessageType() == MessageType.HTML_FULL) {
        // HTML Full messages are the only html type that log clicks implicitly
        inAppMessage.logClick();
      }
    }
  }

  @VisibleForTesting
  static String parseCustomEventNameFromQueryBundle(Bundle queryBundle) {
    return queryBundle.getString(HTML_IN_APP_MESSAGE_CUSTOM_EVENT_NAME_KEY);
  }

  @VisibleForTesting
  static AppboyProperties parsePropertiesFromQueryBundle(Bundle queryBundle) {
    AppboyProperties customEventProperties = new AppboyProperties();
    for (String key: queryBundle.keySet()) {
      if (!key.equals(HTML_IN_APP_MESSAGE_CUSTOM_EVENT_NAME_KEY)) {
        String propertyValue = queryBundle.getString(key, null);
        if (!StringUtils.isNullOrBlank(propertyValue)) {
          customEventProperties.addProperty(key, propertyValue);
        }
      }
    }
    return customEventProperties;
  }
}
