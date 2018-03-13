package com.appboy.ui.inappmessage.listeners;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;

import com.appboy.Appboy;
import com.appboy.enums.Channel;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageHtml;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.support.AppboyFileUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.BundleUtils;
import com.appboy.support.StringUtils;
import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.NewsfeedAction;
import com.appboy.ui.actions.UriAction;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;

public class AppboyInAppMessageWebViewClientListener implements IInAppMessageWebViewClientListener {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageWebViewClientListener.class);
  private static final String HTML_IAM_CUSTOM_EVENT_NAME_KEY = "name";

  @Override
  public void onCloseAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    AppboyLogger.d(TAG, "IInAppMessageWebViewClientListener.onCloseAction called.");

    logHtmlInAppMessageClick(inAppMessage, queryBundle);

    // Dismiss the in-app message due to the close action
    getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(true);
    getInAppMessageManager().getHtmlInAppMessageActionListener().onCloseClicked(inAppMessage, url, queryBundle);
  }

  @Override
  public void onNewsfeedAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    AppboyLogger.d(TAG, "IInAppMessageWebViewClientListener.onNewsfeedAction called.");
    if (getInAppMessageManager().getActivity() == null) {
      AppboyLogger.w(TAG, "Can't perform news feed action because the cached activity is null.");
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
      AppboyNavigator.getAppboyNavigator().gotoNewsFeed(getInAppMessageManager().getActivity(), newsfeedAction);
    }
  }

  @Override
  public void onCustomEventAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    AppboyLogger.d(TAG, "IInAppMessageWebViewClientListener.onCustomEventAction called.");
    if (getInAppMessageManager().getActivity() == null) {
      AppboyLogger.w(TAG, "Can't perform custom event action because the activity is null.");
      return;
    }

    boolean handled = getInAppMessageManager().getHtmlInAppMessageActionListener().onCustomEventFired(inAppMessage, url, queryBundle);
    if (!handled) {
      String customEventName = parseCustomEventNameFromQueryBundle(queryBundle);
      if (StringUtils.isNullOrBlank(customEventName)) {
        return;
      }
      AppboyProperties customEventProperties = parsePropertiesFromQueryBundle(queryBundle);
      Appboy.getInstance(getInAppMessageManager().getActivity()).logCustomEvent(customEventName, customEventProperties);
    }
  }

  @Override
  public void onOtherUrlAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    AppboyLogger.d(TAG, "IInAppMessageWebViewClientListener.onOtherUrlAction called.");
    if (getInAppMessageManager().getActivity() == null) {
      AppboyLogger.w(TAG, "Can't perform other url action because the cached activity is null.");
      return;
    }
    // Log a click since the uri link was followed
    logHtmlInAppMessageClick(inAppMessage, queryBundle);

    boolean handled = getInAppMessageManager().getHtmlInAppMessageActionListener().onOtherUrlAction(inAppMessage, url, queryBundle);
    if (!handled) {
      // Parse the action
      boolean useWebViewForWebLinks = parseUseWebViewFromQueryBundle(inAppMessage, queryBundle);
      UriAction uriAction;
      Bundle inAppMessageBundle = BundleUtils.mapToBundle(inAppMessage.getExtras());
      inAppMessageBundle.putAll(queryBundle);
      uriAction = ActionFactory.createUriActionFromUrlString(url, inAppMessageBundle, useWebViewForWebLinks, Channel.INAPP_MESSAGE);

      // If a local Uri is being handled here, then we want to keep the user in the Html IAM and not hide the current IAM.
      Uri uri = uriAction.getUri();
      if (uri != null && AppboyFileUtils.isLocalUri(uri)) {
        AppboyLogger.w(TAG, "Not passing local URI to AppboyNavigator. Got local uri: " + uri);
        return;
      }

      // Handle the action if it's not a local Uri
      inAppMessage.setAnimateOut(false);
      // Dismiss the in-app message since we're handling the URI outside of the in-app message webView
      getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(false);
      if (uriAction != null) {
        AppboyNavigator.getAppboyNavigator().gotoUri(getInAppMessageManager().getApplicationContext(), uriAction);
      }
    }
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

  private AppboyInAppMessageManager getInAppMessageManager() {
    return AppboyInAppMessageManager.getInstance();
  }

  private void logHtmlInAppMessageClick(IInAppMessage inAppMessage, Bundle queryBundle) {
    if (queryBundle != null && queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_BUTTON_ID)) {
      IInAppMessageHtml inAppMessageHtml = (IInAppMessageHtml) inAppMessage;
      inAppMessageHtml.logButtonClick(queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_BUTTON_ID));
    } else {
      inAppMessage.logClick();
    }
  }

  static String parseCustomEventNameFromQueryBundle(Bundle queryBundle) {
    return queryBundle.getString(HTML_IAM_CUSTOM_EVENT_NAME_KEY);
  }

  static AppboyProperties parsePropertiesFromQueryBundle(Bundle queryBundle) {
    AppboyProperties customEventProperties = new AppboyProperties();
    for (String key: queryBundle.keySet()) {
      if (!key.equals(HTML_IAM_CUSTOM_EVENT_NAME_KEY)) {
        String propertyValue = queryBundle.getString(key, null);
        if (!StringUtils.isNullOrBlank(propertyValue)) {
          customEventProperties.addProperty(key, propertyValue);
        }
      }
    }
    return customEventProperties;
  }
}
