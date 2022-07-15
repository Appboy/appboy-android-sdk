package com.braze.ui.inappmessage.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.coroutine.BrazeCoroutineScope
import com.braze.models.inappmessage.IInAppMessage
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.getAssetFileStringContents
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.listeners.IInAppMessageWebViewClientListener
import com.braze.ui.inappmessage.listeners.IWebViewClientStateListener
import com.braze.ui.support.getQueryParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

open class InAppMessageWebViewClient(
    private val context: Context,
    private val inAppMessage: IInAppMessage,
    private val inAppMessageWebViewClientListener: IInAppMessageWebViewClientListener?
) : WebViewClient() {
    private var webViewClientStateListener: IWebViewClientStateListener? = null
    private var hasPageFinishedLoading = false
    private val hasCalledPageFinishedOnListener = AtomicBoolean(false)
    private var markPageFinishedJob: Job? = null

    private val maxOnPageFinishedWaitTimeMs: Int =
        BrazeConfigurationProvider(context).inAppMessageWebViewClientOnPageFinishedMaxWaitMs

    override fun onPageFinished(view: WebView, url: String) {
        appendBridgeJavascript(view)
        webViewClientStateListener?.let { stateListener ->
            if (hasCalledPageFinishedOnListener.compareAndSet(false, true)) {
                brazelog(V) { "Page has finished loading. Calling onPageFinished on listener" }
                stateListener.onPageFinished()
            }
        }
        hasPageFinishedLoading = true

        // Cancel any pending jobs based on the page finished wait
        markPageFinishedJob?.cancel()
        markPageFinishedJob = null
    }

    private fun markPageFinished() {
        webViewClientStateListener?.let { stateListener ->
            if (hasCalledPageFinishedOnListener.compareAndSet(false, true)) {
                brazelog(V) {
                    "Page may not have finished loading, but max wait time has expired." +
                        " Calling onPageFinished on listener."
                }
                stateListener.onPageFinished()
            }
        }
    }

    private fun appendBridgeJavascript(view: WebView) {
        val javascriptString: String = try {
            context.assets.getAssetFileStringContents(BRIDGE_JS_FILE)
        } catch (e: Exception) {
            // Fail instead of present a broken WebView
            BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(false)
            brazelog(E, e) { "Failed to get HTML in-app message javascript additions" }
            return
        }
        view.loadUrl(JAVASCRIPT_PREFIX + javascriptString)
    }

    /**
     * Handles `appboy` schemed ("appboy://") urls in the HTML content WebViews. If the url isn't
     * `appboy` schemed, then the url is passed to the attached IInAppMessageWebViewClientListener.
     *
     * We expect the URLs to be hierarchical and have `appboy` equal the scheme.
     * For example, `appboy://close` is one such URL.
     *
     * @return true since all actions in Html In-App Messages are handled outside of the In-App Message itself.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) =
        handleUrlOverride(request.url.toString())

    override fun shouldOverrideUrlLoading(view: WebView, url: String) = handleUrlOverride(url)

    fun setWebViewClientStateListener(listener: IWebViewClientStateListener?) {
        // If the page is already done loading, inform the new listener
        if (listener != null &&
            hasPageFinishedLoading &&
            hasCalledPageFinishedOnListener.compareAndSet(false, true)
        ) {
            listener.onPageFinished()
        } else {
            markPageFinishedJob = BrazeCoroutineScope.launchDelayed(maxOnPageFinishedWaitTimeMs) {
                withContext(Dispatchers.Main) {
                    markPageFinished()
                }
            }
        }
        webViewClientStateListener = listener
    }

    private fun handleUrlOverride(url: String): Boolean {
        if (inAppMessageWebViewClientListener == null) {
            brazelog(I) { "InAppMessageWebViewClient was given null IInAppMessageWebViewClientListener listener. Returning true." }
            return true
        }
        if (url.isBlank()) {
            // Blank urls shouldn't be passed back to the WebView. We return true here to indicate
            // to the WebView that we handled the url.
            brazelog(I) { "InAppMessageWebViewClient.shouldOverrideUrlLoading was given blank url. Returning true." }
            return true
        }
        val uri = Uri.parse(url)
        val queryBundle = getBundleFromUrl(url)
        if (uri.scheme != null && uri.scheme == APPBOY_INAPP_MESSAGE_SCHEME) {
            // Check the authority
            when (uri.authority) {
                null -> brazelog { "Uri authority was null. Uri: $uri" }
                AUTHORITY_NAME_CLOSE -> inAppMessageWebViewClientListener.onCloseAction(
                    inAppMessage,
                    url,
                    queryBundle
                )
                AUTHORITY_NAME_NEWSFEED -> inAppMessageWebViewClientListener.onNewsfeedAction(
                    inAppMessage,
                    url,
                    queryBundle
                )
                AUTHORITY_NAME_CUSTOM_EVENT -> inAppMessageWebViewClientListener.onCustomEventAction(
                    inAppMessage,
                    url,
                    queryBundle
                )
            }
            return true
        } else {
            brazelog { "Uri scheme was null or not an appboy url. Uri: $uri" }
        }
        inAppMessageWebViewClientListener.onOtherUrlAction(inAppMessage, url, queryBundle)
        return true
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        brazelog(I) { "The webview rendering process crashed, returning true" }

        // The app crashes after detecting the renderer crashed. Returning true to avoid app crash.
        return true
    }

    companion object {
        private const val BRIDGE_JS_FILE = "appboy-html-in-app-message-javascript-component.js"

        private const val APPBOY_INAPP_MESSAGE_SCHEME = "appboy"
        private const val AUTHORITY_NAME_CLOSE = "close"
        private const val AUTHORITY_NAME_NEWSFEED = "feed"
        private const val AUTHORITY_NAME_CUSTOM_EVENT = "customEvent"

        /**
         * The query key for the button id for tracking
         */
        const val QUERY_NAME_BUTTON_ID = "abButtonId"

        /**
         * The query key for opening links externally (i.e. outside your app). Url intents will be opened with
         * the INTENT.ACTION_VIEW intent. Links beginning with the appboy:// scheme are unaffected by this query key.
         */
        const val QUERY_NAME_EXTERNAL_OPEN = "abExternalOpen"

        /**
         * Query key for directing Braze to open Url intents using the INTENT.ACTION_VIEW.
         */
        const val QUERY_NAME_DEEPLINK = "abDeepLink"
        const val JAVASCRIPT_PREFIX = "javascript:"

        /**
         * Returns the string mapping of the query keys and values from the query string of the url. If the query string
         * contains duplicate keys, then the last key in the string will be kept.
         *
         * @param url the url
         * @return a bundle containing the key/value mapping of the query string. Will not be null.
         */
        @JvmStatic
        @VisibleForTesting
        fun getBundleFromUrl(url: String): Bundle {
            val queryBundle = Bundle()
            if (url.isBlank()) {
                return queryBundle
            }
            val uri = Uri.parse(url)

            uri.getQueryParameters().forEach { entry ->
                queryBundle.putString(entry.key, entry.value)
            }

            return queryBundle
        }
    }
}
