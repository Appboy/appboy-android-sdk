package com.braze.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.appboy.enums.Channel
import com.braze.IBrazeDeeplinkHandler
import com.braze.IBrazeDeeplinkHandler.IntentFlagPurpose
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.actions.NewsfeedAction
import com.braze.ui.actions.UriAction

open class BrazeDeeplinkHandler : IBrazeDeeplinkHandler {
    override fun gotoNewsFeed(context: Context, newsfeedAction: NewsfeedAction) {
        newsfeedAction.execute(context)
    }

    override fun gotoUri(context: Context, uriAction: UriAction) {
        uriAction.execute(context)
    }

    override fun getIntentFlags(intentFlagPurpose: IntentFlagPurpose): Int {
        return when (intentFlagPurpose) {
            IntentFlagPurpose.NOTIFICATION_ACTION_WITH_DEEPLINK,
            IntentFlagPurpose.NOTIFICATION_PUSH_STORY_PAGE_CLICK ->
                Intent.FLAG_ACTIVITY_NO_HISTORY
            IntentFlagPurpose.URI_ACTION_OPEN_WITH_WEBVIEW_ACTIVITY,
            IntentFlagPurpose.URI_ACTION_OPEN_WITH_ACTION_VIEW,
            IntentFlagPurpose.URI_UTILS_GET_MAIN_ACTIVITY_INTENT ->
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            IntentFlagPurpose.URI_ACTION_BACK_STACK_GET_ROOT_INTENT,
            IntentFlagPurpose.URI_ACTION_BACK_STACK_ONLY_GET_TARGET_INTENT ->
                Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    override fun createUriActionFromUrlString(
        url: String,
        extras: Bundle?,
        openInWebView: Boolean,
        channel: Channel
    ): UriAction? {
        return try {
            if (url.isNotBlank()) {
                val uri = Uri.parse(url)
                createUriActionFromUri(uri, extras, openInWebView, channel)
            } else {
                brazelog(E) { "createUriActionFromUrlString url was null. Returning null." }
                null
            }
        } catch (e: Exception) {
            brazelog(E, e) { "createUriActionFromUrlString failed. Returning null." }
            null
        }
    }

    override fun createUriActionFromUri(
        uri: Uri,
        extras: Bundle?,
        openInWebView: Boolean,
        channel: Channel
    ) =
        UriAction(uri, extras, openInWebView, channel)

    companion object {
        private val defaultHandler: IBrazeDeeplinkHandler = BrazeDeeplinkHandler()

        @Volatile
        private var customHandler: IBrazeDeeplinkHandler? = null

        /**
         * Gets the current IBrazeDeeplinkHandler class.
         *
         * @return The currently set IBrazeDeeplinkHandler or the default handler if none was set.
         */
        @JvmStatic
        fun getInstance() =
            customHandler ?: defaultHandler

        /**
         * Sets a custom BrazeDeeplinkHandler.
         *
         * @param brazeDeeplinkHandler The custom IBrazeDeeplinkHandler to be used.
         */
        @JvmStatic
        fun setBrazeDeeplinkHandler(brazeDeeplinkHandler: IBrazeDeeplinkHandler?) {
            brazelog { "Custom IBrazeDeeplinkHandler ${if (brazeDeeplinkHandler == null) "cleared" else "set"}" }
            customHandler = brazeDeeplinkHandler
        }
    }
}
