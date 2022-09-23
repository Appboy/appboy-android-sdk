package com.braze.ui.inappmessage.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.listeners.IWebViewClientStateListener
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.closeInAppMessageOnKeycodeBack
import com.braze.ui.inappmessage.utils.InAppMessageWebViewClient
import com.braze.ui.support.getMaxSafeBottomInset
import com.braze.ui.support.getMaxSafeLeftInset
import com.braze.ui.support.getMaxSafeRightInset
import com.braze.ui.support.getMaxSafeTopInset
import com.braze.ui.support.isDeviceInNightMode
import com.braze.ui.support.setFocusableInTouchModeAndRequestFocus

abstract class InAppMessageHtmlBaseView(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs), IInAppMessageView {

    /**
     * This should be accessed through [messageWebView] to ensure that the [WebView]
     * is configured with all the various settings once and only once.
     */
    private var configuredMessageWebView: WebView? = null
    private var inAppMessageWebViewClient: InAppMessageWebViewClient? = null
    private var isFinished = false
    override var hasAppliedWindowInsets: Boolean = false

    override val messageClickableView: View?
        get() {
            return this
        }

    @get:SuppressLint("SetJavaScriptEnabled")
    open val messageWebView: WebView?
        get() {
            if (isFinished) {
                brazelog(W) { "Cannot return the WebView for an already finished message" }
                return null
            }
            val webViewViewId = getWebViewViewId()
            if (webViewViewId == 0) {
                brazelog { "Cannot find WebView. getWebViewViewId() returned 0." }
                return null
            }
            if (configuredMessageWebView != null) {
                return configuredMessageWebView
            }
            val webView: WebView? = findViewById(webViewViewId)
            if (webView == null) {
                brazelog { "findViewById for $webViewViewId returned null. Returning null for WebView." }
                return null
            }
            val webSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.useWideViewPort = true
            webSettings.loadWithOverviewMode = true
            webSettings.displayZoomControls = false
            webSettings.domStorageEnabled = true
            // Needed since locally downloaded assets are under `file://` schemes
            webSettings.allowFileAccess = true
            // This enables hardware acceleration if the manifest also has it defined.
            // If not defined, then the layer type will fallback to software.
            webView.setLayerType(LAYER_TYPE_HARDWARE, null)
            webView.setBackgroundColor(Color.TRANSPARENT)
            try {
                // Note that this check is OS version agnostic since the Android WebView can be
                // updated independently
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)
                    && isDeviceInNightMode(context)
                ) {
                    WebSettingsCompat.setForceDark(
                        webSettings,
                        WebSettingsCompat.FORCE_DARK_ON
                    )
                }
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                    WebSettingsCompat.setForceDarkStrategy(
                        webSettings,
                        WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY
                    )
                }
            } catch (e: Throwable) {
                brazelog(E, e) { "Failed to set dark mode WebView settings" }
            }

            // Set the client for console logging. See https://developer.android.com/guide/webapps/debugging.html
            webView.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                    this@InAppMessageHtmlBaseView.brazelog {
                        (
                            "Braze HTML In-app Message log. Line: " + cm.lineNumber()
                                + ". SourceId: " + cm.sourceId()
                                + ". Log Level: " + cm.messageLevel()
                                + ". Message: " + cm.message()
                            )
                    }
                    return true
                }

                override fun getDefaultVideoPoster() =
                    // This bitmap is used to eliminate the default black & white
                    // play icon used as the default poster.
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
            configuredMessageWebView = webView
            return configuredMessageWebView
        }

    /**
     * Should be called when the held [WebView] of this class is
     * done displaying its message. Future calls to
     * [messageWebView] will return null afterwards.
     */
    open fun finishWebViewDisplay() {
        brazelog { "Finishing WebView display" }
        // Note that WebView.destroy() is not called here since that
        // causes immense issues with the system's own closing of
        // the WebView after we're done with it.
        isFinished = true
        configuredMessageWebView?.let {
            it.loadUrl(FINISHED_WEBVIEW_URL)
            it.onPause()
            it.removeAllViews()
            configuredMessageWebView = null
        }
    }

    /**
     * Loads the WebView using an html string and local file resource url. This url should be a path
     * to a file on the local filesystem.
     *
     * @param htmlBody          Html text encoded in utf-8
     * @param assetDirectoryUrl path to the local assets file
     */
    @JvmOverloads
    open fun setWebViewContent(htmlBody: String?, assetDirectoryUrl: String? = null) {
        // File URIs must be loaded with this "file://" scheme
        // since our html might have mixed http/data/file content
        // See https://developer.android.com/reference/android/webkit/WebView#loadData(java.lang.String,%20java.lang.String,%20java.lang.String)
        if (htmlBody != null) {
            messageWebView?.loadDataWithBaseURL(
                "$FILE_URI_SCHEME_PREFIX$assetDirectoryUrl/",
                htmlBody,
                HTML_MIME_TYPE,
                HTML_ENCODING,
                null
            )
        } else {
            brazelog { "Cannot load WebView. htmlBody was null." }
        }
    }

    open fun setInAppMessageWebViewClient(inAppMessageWebViewClient: InAppMessageWebViewClient) {
        messageWebView?.webViewClient = inAppMessageWebViewClient
        this.inAppMessageWebViewClient = inAppMessageWebViewClient
    }

    open fun setHtmlPageFinishedListener(listener: IWebViewClientStateListener?) {
        inAppMessageWebViewClient?.setWebViewClientStateListener(listener)
    }

    /**
     * Html in-app messages can alternatively be closed by the back button.
     *
     * Note: If the internal WebView has focus instead of this view, back button events on html
     * in-app messages are handled separately in [InAppMessageWebView.onKeyDown]
     *
     * @return If the button pressed was the back button, close the in-app message
     * and return true to indicate that the event was handled.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && BrazeInAppMessageManager.getInstance().doesBackButtonDismissInAppMessageView) {
            closeInAppMessageOnKeycodeBack()
            return true
        }
        configuredMessageWebView?.setFocusableInTouchModeAndRequestFocus()
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Returns the [View.getId] used in the
     * default [InAppMessageHtmlBaseView.messageWebView]
     * implementation.
     *
     * @return The [View.getId] for the [WebView] backing this message.
     */
    abstract fun getWebViewViewId(): Int

    /**
     * HTML messages can alternatively be closed by the back button.
     *
     * @return If the button pressed was the back button, close the in-app message
     * and return true to indicate that the event was handled.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!isInTouchMode && event.keyCode == KeyEvent.KEYCODE_BACK && BrazeInAppMessageManager.getInstance().doesBackButtonDismissInAppMessageView) {
            closeInAppMessageOnKeycodeBack()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun applyWindowInsets(insets: WindowInsetsCompat) {
        hasAppliedWindowInsets = true
        if (!BrazeConfigurationProvider(this.context).isHtmlInAppMessageApplyWindowInsetsEnabled) {
            return
        }
        if (layoutParams == null || layoutParams !is MarginLayoutParams) {
            return
        }

        // Offset the existing margin with whatever the inset margins safe area values are
        val layoutParams = layoutParams as MarginLayoutParams
        layoutParams.setMargins(
            getMaxSafeLeftInset(insets) + layoutParams.leftMargin,
            getMaxSafeTopInset(insets) + layoutParams.topMargin,
            getMaxSafeRightInset(insets) + layoutParams.rightMargin,
            getMaxSafeBottomInset(insets) + layoutParams.bottomMargin
        )
    }

    companion object {
        private const val HTML_MIME_TYPE = "text/html"
        private const val HTML_ENCODING = "utf-8"
        private const val FILE_URI_SCHEME_PREFIX = "file://"

        /**
         * A url for the [WebView] to load when display is finished.
         */
        private const val FINISHED_WEBVIEW_URL = "about:blank"

        const val BRAZE_BRIDGE_PREFIX = "brazeInternalBridge"
    }
}
