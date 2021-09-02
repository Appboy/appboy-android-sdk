package com.braze.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.appboy.enums.Channel;
import com.braze.IBrazeDeeplinkHandler;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;
import com.braze.ui.actions.NewsfeedAction;
import com.braze.ui.actions.UriAction;

public class BrazeDeeplinkHandler implements IBrazeDeeplinkHandler {
  private static final String TAG = BrazeLogger.getBrazeLogTag(BrazeDeeplinkHandler.class);
  private static final IBrazeDeeplinkHandler sDefaultBrazeDeeplinkHandler = new BrazeDeeplinkHandler();
  private static volatile IBrazeDeeplinkHandler sCustomBrazeDeeplinkHandler;

  @Override
  public void gotoNewsFeed(Context context, NewsfeedAction newsfeedAction) {
    executeNewsFeedAction(context, newsfeedAction);
  }

  @Override
  public void gotoUri(Context context, UriAction uriAction) {
    executeUriAction(context, uriAction);
  }

  @Override
  public int getIntentFlags(IntentFlagPurpose intentFlagPurpose) {
    switch (intentFlagPurpose) {
      case NOTIFICATION_ACTION_WITH_DEEPLINK:
      case NOTIFICATION_PUSH_STORY_PAGE_CLICK:
        return Intent.FLAG_ACTIVITY_NO_HISTORY;
      case URI_ACTION_OPEN_WITH_WEBVIEW_ACTIVITY:
      case URI_ACTION_OPEN_WITH_ACTION_VIEW:
      case URI_UTILS_GET_MAIN_ACTIVITY_INTENT:
        return Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP;
      case URI_ACTION_BACK_STACK_GET_ROOT_INTENT:
      case URI_ACTION_BACK_STACK_ONLY_GET_TARGET_INTENT:
        return Intent.FLAG_ACTIVITY_NEW_TASK;
      default:
        return 0;
    }
  }

  public void executeNewsFeedAction(Context context, NewsfeedAction newsfeedAction) {
    if (newsfeedAction == null) {
      BrazeLogger.w(TAG, "IBrazeDeeplinkHandler cannot open News feed because the news feed action object was null.");
      return;
    }
    newsfeedAction.execute(context);
  }

  public void executeUriAction(Context context, UriAction uriAction) {
    if (uriAction == null) {
      BrazeLogger.w(TAG, "IBrazeDeeplinkHandler cannot open Uri because the Uri action object was null.");
      return;
    }
    uriAction.execute(context);
  }

  @Override
  @Nullable
  public UriAction createUriActionFromUrlString(String url, Bundle extras, boolean openInWebView, Channel channel) {
    if (!StringUtils.isNullOrBlank(url)) {
      Uri uri = Uri.parse(url);
      return createUriActionFromUri(uri, extras, openInWebView, channel);
    }
    return null;
  }

  @Override
  @Nullable
  public UriAction createUriActionFromUri(Uri uri, Bundle extras, boolean openInWebView, Channel channel) {
    if (uri != null) {
      return new UriAction(uri, extras, openInWebView, channel);
    }
    return null;
  }

  /**
   * Gets the current IBrazeDeeplinkHandler class. This will be null if none was set.
   *
   * @return The currently set IBrazeDeeplinkHandler or null if none was set.
   */
  public static <T extends IBrazeDeeplinkHandler> T getInstance() {
    if (sCustomBrazeDeeplinkHandler != null) {
      return (T) sCustomBrazeDeeplinkHandler;
    } else {
      return (T) sDefaultBrazeDeeplinkHandler;
    }
  }

  /**
   * Sets a custom BrazeDeeplinkHandler.
   *
   * @param brazeDeeplinkHandler The custom IBrazeDeeplinkHandler to be used.
   */
  public static void setBrazeDeeplinkHandler(IBrazeDeeplinkHandler brazeDeeplinkHandler) {
    BrazeLogger.d(TAG, "Custom IBrazeDeeplinkHandler set");
    sCustomBrazeDeeplinkHandler = brazeDeeplinkHandler;
  }
}
