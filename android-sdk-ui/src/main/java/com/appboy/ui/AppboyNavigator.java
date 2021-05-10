package com.appboy.ui;

import android.content.Context;
import android.content.Intent;

import com.appboy.IAppboyNavigator;
import com.appboy.ui.actions.NewsfeedAction;
import com.appboy.ui.actions.UriAction;
import com.braze.support.BrazeLogger;

public class AppboyNavigator implements IAppboyNavigator {
  private static final String TAG = BrazeLogger.getBrazeLogTag(AppboyNavigator.class);
  private static final IAppboyNavigator sDefaultAppboyNavigator = new AppboyNavigator();
  private static volatile IAppboyNavigator sCustomAppboyNavigator;

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

  public static void executeNewsFeedAction(Context context, NewsfeedAction newsfeedAction) {
    if (newsfeedAction == null) {
      BrazeLogger.w(TAG, "IAppboyNavigator cannot open News feed because the news feed action object was null.");
      return;
    }
    newsfeedAction.execute(context);
  }

  public static void executeUriAction(Context context, UriAction uriAction) {
    if (uriAction == null) {
      BrazeLogger.w(TAG, "IAppboyNavigator cannot open Uri because the Uri action object was null.");
      return;
    }
    uriAction.execute(context);
  }

  /**
   * Gets the current IAppboyNavigator class that defines the actions that should be taken when Braze attempts to
   * display the news feed or open a URI from an in-app message. This will be null if none was set.
   *
   * @return The currently set IAppboyNavigator or null if none was set.
   */
  public static IAppboyNavigator getAppboyNavigator() {
    if (sCustomAppboyNavigator != null) {
      return sCustomAppboyNavigator;
    } else {
      return sDefaultAppboyNavigator;
    }
  }

  /**
   * Sets the class that defines the actions that should be taken when Braze attempts to display the news
   * feed or open a URI from an in-app message.
   *
   * @param appboyNavigator The IAppboyNavigator to use when attempting to display news feed or navigate to
   *                        a URI.
   */
  public static void setAppboyNavigator(IAppboyNavigator appboyNavigator) {
    BrazeLogger.d(TAG, "Custom IAppboyNavigator set");
    sCustomAppboyNavigator = appboyNavigator;
  }
}
