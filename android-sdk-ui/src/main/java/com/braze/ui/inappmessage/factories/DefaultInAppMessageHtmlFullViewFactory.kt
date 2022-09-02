package com.braze.ui.inappmessage.factories

import android.annotation.SuppressLint
import android.app.Activity
import com.appboy.ui.R
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.InAppMessageHtmlFull
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.IInAppMessageViewFactory
import com.braze.ui.inappmessage.jsinterface.InAppMessageJavascriptInterface
import com.braze.ui.inappmessage.listeners.IInAppMessageWebViewClientListener
import com.braze.ui.inappmessage.utils.InAppMessageWebViewClient
import com.braze.ui.inappmessage.views.InAppMessageHtmlFullView
import com.braze.ui.support.isDeviceNotInTouchMode

open class DefaultInAppMessageHtmlFullViewFactory(private val inAppMessageWebViewClientListener: IInAppMessageWebViewClientListener) :
    IInAppMessageViewFactory {
    @SuppressLint("AddJavascriptInterface")
    override fun createInAppMessageView(
        activity: Activity,
        inAppMessage: IInAppMessage
    ): InAppMessageHtmlFullView? {
        val view = activity.layoutInflater
            .inflate(R.layout.com_braze_inappmessage_html_full, null) as InAppMessageHtmlFullView
        val config = BrazeConfigurationProvider(activity.applicationContext)
        if (config.isTouchModeRequiredForHtmlInAppMessages && isDeviceNotInTouchMode(view)) {
            brazelog(W) {
                "The device is not currently in touch mode. " +
                    "This message requires user touch interaction to display properly. Please set " +
                    "setIsTouchModeRequiredForHtmlInAppMessages to false to change this behavior."
            }
            return null
        }
        val context = activity.applicationContext
        val inAppMessageHtmlFull = inAppMessage as InAppMessageHtmlFull
        val javascriptInterface = InAppMessageJavascriptInterface(context, inAppMessageHtmlFull)
        view.setWebViewContent(inAppMessage.message, inAppMessageHtmlFull.localAssetsDirectoryUrl)
        view.setInAppMessageWebViewClient(
            InAppMessageWebViewClient(
                context,
                inAppMessage,
                inAppMessageWebViewClientListener
            )
        )
        view.messageWebView.addJavascriptInterface(
            javascriptInterface,
            InAppMessageHtmlFullView.BRAZE_BRIDGE_PREFIX
        )
        return view
    }
}
