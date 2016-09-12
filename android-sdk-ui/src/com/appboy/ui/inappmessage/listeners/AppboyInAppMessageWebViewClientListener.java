package com.appboy.ui.inappmessage.listeners;

import android.os.Bundle;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageHtml;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.push.AppboyNotificationUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.BundleUtils;
import com.appboy.support.StringUtils;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;

public class AppboyInAppMessageWebViewClientListener implements IInAppMessageWebViewClientListener {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageWebViewClientListener.class.getName());
  private static final String HTML_IAM_CUSTOM_EVENT_NAME_KEY = "name";

  @Override
  public void onCloseAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    AppboyLogger.d(TAG, "IInAppMessageWebViewClientListener.onCloseAction called.");

    logHtmlInAppMessageClick(inAppMessage, queryBundle);

    getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(false);

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
      getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(false);
      Bundle inAppMessageBundle = BundleUtils.mapToBundle(inAppMessage.getExtras());
      inAppMessageBundle.putAll(queryBundle);
      getInAppMessageManager().getAppboyNavigator().gotoNewsFeed(getInAppMessageManager().getActivity(), inAppMessageBundle);
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
      inAppMessage.setAnimateOut(false);
      getInAppMessageManager().hideCurrentlyDisplayingInAppMessage(false);
      boolean doExternalOpen = false;
      if (queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_EXTERNAL_OPEN)) {
        doExternalOpen = Boolean.parseBoolean(queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_EXTERNAL_OPEN));
      }

      // Handle the action
      IAction urlAction;
      if (doExternalOpen) {
        // Create an Action using using the ACTION_VIEW intent
        Bundle inAppMessageBundle = BundleUtils.mapToBundle(inAppMessage.getExtras());
        inAppMessageBundle.putAll(queryBundle);
        urlAction = ActionFactory.createViewUriAction(url, inAppMessageBundle);
      } else {
        urlAction = ActionFactory.createUriAction(getInAppMessageManager().getActivity(), url);
      }
      if (urlAction != null) {
        urlAction.execute(getInAppMessageManager().getActivity());
      }
    }
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
        String propertyValue = AppboyNotificationUtils.bundleOptString(queryBundle, key, null);
        if (!StringUtils.isNullOrBlank(propertyValue)) {
          customEventProperties.addProperty(key, propertyValue);
        }
      }
    }
    return customEventProperties;
  }
}
