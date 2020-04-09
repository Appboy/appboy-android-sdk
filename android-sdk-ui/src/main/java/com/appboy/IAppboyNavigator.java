package com.appboy;

import android.content.Context;

import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.actions.NewsfeedAction;
import com.appboy.ui.actions.UriAction;

/**
 * This class defines the actions that should be taken when Braze attempts to display the news
 * feed or open a URI.
 */
public interface IAppboyNavigator {
  /**
   * This delegate method will be called when Braze wants to display the news feed.
   * <br/>
   * <br/>
   * This method should implement the necessary logic to navigate to the to the Braze news feed
   * that is integrated into the app.
   *
   * @param context The current context.
   * @param newsfeedAction The news feed action to execute.
   */
  void gotoNewsFeed(Context context, NewsfeedAction newsfeedAction);

  /**
   * This delegate method will be called when Braze wants to navigate to a particular URI. If an
   * IAppboyNavigator is set, this method will be called instead of the default method (which
   * is defined in {@link AppboyNavigator}.
   *
   * @param context The current context.
   * @param uriAction The Uri action to execute.
   */
  void gotoUri(Context context, UriAction uriAction);
}
