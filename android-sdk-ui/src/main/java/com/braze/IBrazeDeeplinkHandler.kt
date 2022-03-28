package com.braze

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.appboy.enums.Channel
import com.braze.push.NotificationTrampolineActivity
import com.braze.ui.actions.NewsfeedAction
import com.braze.ui.actions.UriAction

/**
 * This class defines the actions that should be taken when Braze attempts to follow a deeplink.
 */
interface IBrazeDeeplinkHandler {
    enum class IntentFlagPurpose {
        /**
         * Used for notification actions using a deeplink.
         */
        NOTIFICATION_ACTION_WITH_DEEPLINK,

        /**
         * Used when generating the intent to the [NotificationTrampolineActivity]
         * on a Push Story page traversal.
         */
        NOTIFICATION_PUSH_STORY_PAGE_CLICK,

        /**
         * Used in the default [UriAction] when opening
         * a deeplink with the WebView activity.
         */
        URI_ACTION_OPEN_WITH_WEBVIEW_ACTIVITY,

        /**
         * Used in the default [UriAction] when opening
         * a deeplink with [android.content.Intent.ACTION_VIEW].
         */
        URI_ACTION_OPEN_WITH_ACTION_VIEW,

        /**
         * Used in the default [UriAction] when creating the
         * root backstack activity when opening a deeplink from a
         * push notification.
         */
        URI_ACTION_BACK_STACK_GET_ROOT_INTENT,

        /**
         * Used in the default [UriAction] when creating the
         * the backstack only contains the target intent and no
         * root intent.
         */
        URI_ACTION_BACK_STACK_ONLY_GET_TARGET_INTENT,

        /**
         * Used in push notifications when only opening the main
         * Activity and not a deeplink.
         */
        URI_UTILS_GET_MAIN_ACTIVITY_INTENT
    }

    /**
     * This delegate method will be called when Braze wants to display the news feed.
     *
     * This method should implement the necessary logic to navigate to the to the Braze news feed
     * that is integrated into the app.
     *
     * @param context        The current context.
     * @param newsfeedAction The news feed action to execute.
     */
    fun gotoNewsFeed(context: Context, newsfeedAction: NewsfeedAction)

    /**
     * This delegate method will be called when Braze wants to navigate to a particular URI. If an
     * IBrazeDeeplinkHandler is set, this method will be called instead of the default method (which
     * is defined in [BrazeDeeplinkHandler].
     *
     * @param context   The current context.
     * @param uriAction The Uri action to execute.
     */
    fun gotoUri(context: Context, uriAction: UriAction)

    /**
     * Get the flag mask used for [android.content.Intent.setFlags] based
     * on the Intent usage.
     */
    fun getIntentFlags(intentFlagPurpose: IntentFlagPurpose): Int

    /**
     * Convenience method for creating [UriAction] instances. Returns null if the supplied url
     * is blank or can not be parsed into a valid Uri.
     */
    fun createUriActionFromUrlString(url: String, extras: Bundle?, openInWebView: Boolean, channel: Channel): UriAction?

    /**
     * Convenience method for creating [UriAction] instances.
     */
    fun createUriActionFromUri(uri: Uri, extras: Bundle?, openInWebView: Boolean, channel: Channel): UriAction
}
