package com.braze.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.appboy.Constants
import com.appboy.enums.Channel
import com.appboy.ui.R
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.REMOTE_SCHEMES
import com.braze.ui.BrazeDeeplinkHandler.Companion.getInstance
import com.braze.ui.actions.IAction
import com.braze.ui.support.isDeviceInNightMode

/**
 * Note that this Activity is not and should not be exported by default in
 * the AndroidManifest so external applications are not able to pass
 * arbitrary URLs via this intent.
 *
 * From https://developer.android.com/guide/topics/manifest/activity-element#exported ->
 * "If "false", the activity can be launched only by components of the same
 * application or applications with the same user ID."
 */
@SuppressLint("SetJavaScriptEnabled")
open class BrazeWebViewActivity : FragmentActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables hardware acceleration for the window.
        // See https://developer.android.com/guide/topics/graphics/hardware-accel.html#controlling.
        // With this flag, we can view Youtube videos since HTML5 requires hardware acceleration.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        setContentView(R.layout.com_braze_webview_activity)
        val webView = findViewById<WebView>(R.id.com_braze_webview_activity_webview)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val webSettings = webView.settings
        webSettings.allowFileAccess = false
        webSettings.builtInZoomControls = true
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.displayZoomControls = false
        webSettings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isDeviceInNightMode(this.applicationContext)) {
            webSettings.forceDark = WebSettings.FORCE_DARK_ON
        }
        webView.webChromeClient = createWebChromeClient()
        webView.webViewClient = createWebViewClient()

        // Opens the URL passed as an intent extra (if one exists).
        intent.extras?.getString(Constants.BRAZE_WEBVIEW_URL_EXTRA)?.let { url ->
            webView.loadUrl(url)
        }
    }

    open fun createWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                brazelog {
                    "Braze WebView Activity log. Line: ${cm.lineNumber()}. SourceId: ${cm.sourceId()}. " +
                        "Log Level: ${cm.messageLevel()}. Message: ${cm.message()}"
                }
                return true
            }

            override fun getDefaultVideoPoster(): Bitmap? {
                // This bitmap is used to eliminate the default black & white
                // play icon used as the default poster.
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        }
    }

    open fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val didHandleUrl = handleUrlOverride(view.context, request.url.toString())
                return didHandleUrl ?: super.shouldOverrideUrlLoading(view, request)
            }

            @Suppress("deprecation")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val didHandleUrl = handleUrlOverride(view.context, url)
                return didHandleUrl ?: super.shouldOverrideUrlLoading(view, url)
            }

            /**
             * Handles the URL override when the link is not better handled by this WebView instead.
             *
             * E.g. in the presence of a https link, this WebView should handle it and not
             * forward to Chrome. However in the presence of a sms link, the system itself
             * should handle it since a WebView lacks that ability.
             *
             * @return True/False when this URL override was handled. Returns null when the super implementation of
             * [WebViewClient.shouldOverrideUrlLoading] should be called instead.
             */
            private fun handleUrlOverride(context: Context, url: String): Boolean? {
                try {
                    if (REMOTE_SCHEMES.contains(Uri.parse(url).scheme)) {
                        return null
                    }
                    val action: IAction? = getInstance().createUriActionFromUrlString(url, intent.extras, false, Channel.UNKNOWN)
                    return if (action != null) {
                        // Instead of using BrazeDeeplinkHandler, just open directly.
                        action.execute(context)

                        // Close the WebView if the action was executed successfully
                        finish()
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    brazelog(E, e) { "Unexpected exception while processing url $url. Passing url back to WebView." }
                }
                return null
            }

            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                brazelog(I) { "The webview rendering process crashed, returning true" }

                // The app crashes after detecting the renderer crashed. Returning true to avoid app crash.
                return true
            }
        }
    }
}
