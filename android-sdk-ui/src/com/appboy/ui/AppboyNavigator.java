package com.appboy.ui;

import android.content.Context;

import com.appboy.Constants;
import com.appboy.IAppboyNavigator;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.actions.NewsfeedAction;
import com.appboy.ui.actions.UriAction;

public class AppboyNavigator implements IAppboyNavigator {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyNavigator.class.getName());
  private static volatile IAppboyNavigator sDefaultAppboyNavigator = new AppboyNavigator();
  private static volatile IAppboyNavigator sCustomAppboyNavigator;

  @Override
  public void gotoNewsFeed(Context context, NewsfeedAction newsfeedAction) {
    executeNewsFeedAction(context, newsfeedAction);
  }

  @Override
  public void gotoUri(Context context, UriAction uriAction) {
    executeUriAction(context, uriAction);
  }

  public static void executeNewsFeedAction(Context context, NewsfeedAction newsfeedAction) {
    if (newsfeedAction == null) {
      AppboyLogger.e(TAG, "IAppboyNavigator cannot open News feed because the news feed action object was null.");
      return;
    }
    newsfeedAction.execute(context);
  }

  public static void executeUriAction(Context context, UriAction uriAction) {
    if (uriAction == null) {
      AppboyLogger.e(TAG, "IAppboyNavigator cannot open Uri because the Uri action object was null.");
      return;
    }
    uriAction.execute(context);
  }

  /**
   * Gets the current IAppboyNavigator class that defines the actions that should be taken when Appboy attempts to
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
   * Sets the class that defines the actions that should be taken when Appboy attempts to display the news
   * feed or open a URI from an in-app message.
   *
   * @param appboyNavigator The IAppboyNavigator to use when attempting to display news feed or navigate to
   *                        a URI.
   */
  public static void setAppboyNavigator(IAppboyNavigator appboyNavigator) {
    sCustomAppboyNavigator = appboyNavigator;
  }
}
