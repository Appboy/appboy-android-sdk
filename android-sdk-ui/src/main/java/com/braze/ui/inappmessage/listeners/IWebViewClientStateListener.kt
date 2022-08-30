package com.braze.ui.inappmessage.listeners

fun interface IWebViewClientStateListener {
    /**
     * Fired when [android.webkit.WebViewClient.onPageFinished] has been called.
     */
    fun onPageFinished()
}
