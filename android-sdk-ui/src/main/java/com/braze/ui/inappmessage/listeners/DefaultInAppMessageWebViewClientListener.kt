package com.braze.ui.inappmessage.listeners

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.braze.enums.Channel
import com.braze.Braze.Companion.getInstance
import com.braze.enums.inappmessage.MessageType
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageHtml
import com.braze.models.outgoing.BrazeProperties
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.isLocalUri
import com.braze.support.toBundle
import com.braze.ui.BrazeDeeplinkHandler.Companion.getInstance as getDeeplinkHandlerInstance
import com.braze.ui.actions.NewsfeedAction
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.utils.InAppMessageWebViewClient
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W

open class DefaultInAppMessageWebViewClientListener : IInAppMessageWebViewClientListener {
    private val inAppMessageManager: BrazeInAppMessageManager
        get() = BrazeInAppMessageManager.getInstance()

    override fun onCloseAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle) {
        brazelog { "IInAppMessageWebViewClientListener.onCloseAction called." }
        logHtmlInAppMessageClick(inAppMessage, queryBundle)

        // Dismiss the in-app message due to the close action
        inAppMessageManager.hideCurrentlyDisplayingInAppMessage(true)
        inAppMessageManager.htmlInAppMessageActionListener.onCloseClicked(
            inAppMessage,
            url,
            queryBundle
        )
    }

    override fun onNewsfeedAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle) {
        brazelog { "IInAppMessageWebViewClientListener.onNewsfeedAction called." }
        if (inAppMessageManager.activity == null) {
            brazelog(W) { "Can't perform news feed action because the cached activity is null." }
            return
        }
        // Log a click since the user left to the newsfeed
        logHtmlInAppMessageClick(inAppMessage, queryBundle)
        val wasHandled = inAppMessageManager.htmlInAppMessageActionListener.onNewsfeedClicked(
            inAppMessage,
            url,
            queryBundle
        )
        if (!wasHandled) {
            inAppMessage.animateOut = false
            // Dismiss the in-app message since we're navigating away to the news feed
            inAppMessageManager.hideCurrentlyDisplayingInAppMessage(false)
            val newsfeedAction = NewsfeedAction(
                inAppMessage.extras.toBundle(),
                Channel.INAPP_MESSAGE
            )
            inAppMessageManager.activity?.let { activity ->
                getDeeplinkHandlerInstance().gotoNewsFeed(activity, newsfeedAction)
            }
        }
    }

    override fun onCustomEventAction(
        inAppMessage: IInAppMessage,
        url: String,
        queryBundle: Bundle
    ) {
        brazelog { "IInAppMessageWebViewClientListener.onCustomEventAction called." }
        if (inAppMessageManager.activity == null) {
            brazelog(W) { "Can't perform custom event action because the activity is null." }
            return
        }
        val wasHandled = inAppMessageManager.htmlInAppMessageActionListener.onCustomEventFired(
            inAppMessage,
            url,
            queryBundle
        )
        if (!wasHandled) {
            val customEventName = parseCustomEventNameFromQueryBundle(queryBundle)
            if (customEventName.isNullOrBlank()) {
                return
            }
            val customEventProperties = parsePropertiesFromQueryBundle(queryBundle)
            inAppMessageManager.activity?.let { activity ->
                getInstance(activity).logCustomEvent(
                    customEventName,
                    customEventProperties
                )
            }
        }
    }

    override fun onOtherUrlAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle) {
        brazelog { "IInAppMessageWebViewClientListener.onOtherUrlAction called." }
        if (inAppMessageManager.activity == null) {
            brazelog(W) { "Can't perform other url action because the cached activity is null. Url: $url" }
            return
        }
        // Log a click since the uri link was followed
        logHtmlInAppMessageClick(inAppMessage, queryBundle)
        val wasHandled = inAppMessageManager.htmlInAppMessageActionListener.onOtherUrlAction(
            inAppMessage,
            url,
            queryBundle
        )
        if (wasHandled) {
            brazelog(V) {
                "HTML message action listener handled url in onOtherUrlAction. Doing nothing further. Url: $url"
            }
            return
        }

        // Parse the action
        val useWebViewForWebLinks = parseUseWebViewFromQueryBundle(inAppMessage, queryBundle)
        val inAppMessageBundle = inAppMessage.extras.toBundle()
        inAppMessageBundle.putAll(queryBundle)
        val uriAction = getDeeplinkHandlerInstance().createUriActionFromUrlString(
            url,
            inAppMessageBundle,
            useWebViewForWebLinks,
            Channel.INAPP_MESSAGE
        )
        if (uriAction == null) {
            brazelog(W) { "UriAction is null. Not passing any URI to BrazeDeeplinkHandler. Url: $url" }
            return
        }

        // If a local Uri is being handled here, then we want to keep the user in the Html in-app message and not hide the current in-app message.
        val uri = uriAction.uri
        if (uri.isLocalUri()) {
            brazelog(W) {
                "Not passing local uri to BrazeDeeplinkHandler. Got local uri: $uri for url: $url"
            }
            return
        }

        // Handle the action if it's not a local Uri
        inAppMessage.animateOut = false
        // Dismiss the in-app message since we're handling the URI outside of the in-app message webView
        inAppMessageManager.hideCurrentlyDisplayingInAppMessage(false)
        inAppMessageManager.activity?.let { activity ->
            getDeeplinkHandlerInstance().gotoUri(activity, uriAction)
        }
    }

    companion object {
        private const val HTML_IN_APP_MESSAGE_CUSTOM_EVENT_NAME_KEY = "name"

        @JvmStatic
        @VisibleForTesting
        fun parseUseWebViewFromQueryBundle(
            inAppMessage: IInAppMessage,
            queryBundle: Bundle
        ): Boolean {
            var isAnyQueryFlagSet = false
            var isDeepLinkFlagSet = false
            if (queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_DEEPLINK)) {
                isDeepLinkFlagSet = queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_DEEPLINK).toBoolean()
                isAnyQueryFlagSet = true
            }
            var isExternalOpenFlagSet = false
            if (queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_EXTERNAL_OPEN)) {
                isExternalOpenFlagSet = queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_EXTERNAL_OPEN).toBoolean()
                isAnyQueryFlagSet = true
            }
            var useWebViewForWebLinks = inAppMessage.openUriInWebView
            if (isAnyQueryFlagSet) {
                useWebViewForWebLinks = !(isDeepLinkFlagSet || isExternalOpenFlagSet)
            }
            return useWebViewForWebLinks
        }

        @JvmStatic
        @VisibleForTesting
        fun logHtmlInAppMessageClick(inAppMessage: IInAppMessage, queryBundle: Bundle) {
            if (queryBundle.containsKey(InAppMessageWebViewClient.QUERY_NAME_BUTTON_ID)) {
                val inAppMessageHtml = inAppMessage as IInAppMessageHtml
                queryBundle.getString(InAppMessageWebViewClient.QUERY_NAME_BUTTON_ID)?.let {
                    inAppMessageHtml.logButtonClick(it)
                }
            } else if (inAppMessage.messageType === MessageType.HTML_FULL) {
                // HTML Full messages are the only html type that log clicks implicitly
                inAppMessage.logClick()
            }
        }

        @JvmStatic
        @VisibleForTesting
        fun parseCustomEventNameFromQueryBundle(queryBundle: Bundle): String? =
            queryBundle.getString(HTML_IN_APP_MESSAGE_CUSTOM_EVENT_NAME_KEY)

        @JvmStatic
        @VisibleForTesting
        fun parsePropertiesFromQueryBundle(queryBundle: Bundle): BrazeProperties {
            val customEventProperties = BrazeProperties()
            for (key in queryBundle.keySet()) {
                if (key != HTML_IN_APP_MESSAGE_CUSTOM_EVENT_NAME_KEY) {
                    val propertyValue = queryBundle.getString(key, null)
                    if (!propertyValue.isNullOrBlank()) {
                        customEventProperties.addProperty(key, propertyValue)
                    }
                }
            }
            return customEventProperties
        }
    }
}
