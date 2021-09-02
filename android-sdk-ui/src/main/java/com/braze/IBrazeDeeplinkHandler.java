package com.braze;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.appboy.enums.Channel;
import com.braze.push.NotificationTrampolineActivity;
import com.braze.ui.actions.NewsfeedAction;
import com.braze.ui.actions.UriAction;

/**
 * This class defines the actions that should be taken when Braze attempts to follow a deeplink.
 */
public interface IBrazeDeeplinkHandler {
  enum IntentFlagPurpose {
    /**
     * Used for notification actions using a deeplink.
     */
    NOTIFICATION_ACTION_WITH_DEEPLINK,
    /**
     * Used when generating the intent to the {@link NotificationTrampolineActivity}
     * on a Push Story page traversal.
     */
    NOTIFICATION_PUSH_STORY_PAGE_CLICK,
    /**
     * Used in the default {@link UriAction} when opening
     * a deeplink with the WebView activity.
     */
    URI_ACTION_OPEN_WITH_WEBVIEW_ACTIVITY,
    /**
     * Used in the default {@link UriAction} when opening
     * a deeplink with {@link android.content.Intent#ACTION_VIEW}.
     */
    URI_ACTION_OPEN_WITH_ACTION_VIEW,
    /**
     * Used in the default {@link UriAction} when creating the
     * root backstack activity when opening a deeplink from a
     * push notification.
     */
    URI_ACTION_BACK_STACK_GET_ROOT_INTENT,
    /**
     * Used in the default {@link UriAction} when creating the
     * the backstack only contains the target intent and no
     * root intent.
     */
    URI_ACTION_BACK_STACK_ONLY_GET_TARGET_INTENT,
    /**
     * Used in push notifications when only opening the main
     * Activity and not a deeplink.
     */
    URI_UTILS_GET_MAIN_ACTIVITY_INTENT,
  }

  /**
   * This delegate method will be called when Braze wants to display the news feed.
   * <br/>
   * <br/>
   * This method should implement the necessary logic to navigate to the to the Braze news feed
   * that is integrated into the app.
   *
   * @param context        The current context.
   * @param newsfeedAction The news feed action to execute.
   */
  void gotoNewsFeed(Context context, NewsfeedAction newsfeedAction);

  /**
   * This delegate method will be called when Braze wants to navigate to a particular URI. If an
   * IBrazeDeeplinkHandler is set, this method will be called instead of the default method (which
   * is defined in {@link BrazeDeeplinkHandler}.
   *
   * @param context   The current context.
   * @param uriAction The Uri action to execute.
   */
  void gotoUri(Context context, UriAction uriAction);

  /**
   * Get the flag mask used for {@link android.content.Intent#setFlags(int)} based
   * on the Intent usage.
   */
  int getIntentFlags(IntentFlagPurpose intentFlagPurpose);

  /**
   * Convenience method for creating {@link UriAction} instances. Returns null if the supplied url
   * is null, blank, or can not be parsed into a valid Uri.
   */
  @Nullable
  UriAction createUriActionFromUrlString(String url, Bundle extras, boolean openInWebView, Channel channel);

  /**
   * Convenience method for creating {@link UriAction} instances. Returns null if the supplied uri
   * is null.
   */
  @Nullable
  UriAction createUriActionFromUri(Uri uri, Bundle extras, boolean openInWebView, Channel channel);
}
