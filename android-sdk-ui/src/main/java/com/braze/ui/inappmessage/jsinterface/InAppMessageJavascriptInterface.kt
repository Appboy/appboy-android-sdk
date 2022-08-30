package com.braze.ui.inappmessage.jsinterface

import android.content.Context
import android.webkit.JavascriptInterface
import androidx.annotation.VisibleForTesting
import com.braze.Braze
import com.braze.models.inappmessage.IInAppMessageHtml
import com.braze.models.outgoing.BrazeProperties
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.requestPushPermissionPrompt
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import org.json.JSONObject
import java.math.BigDecimal

/**
 * Used to generate the javascript API in html in-app messages.
 */
class InAppMessageJavascriptInterface(
    private val context: Context,
    private val inAppMessage: IInAppMessageHtml
) {
    @get:JavascriptInterface
    val user: InAppMessageUserJavascriptInterface = InAppMessageUserJavascriptInterface(context)

    @JavascriptInterface
    fun changeUser(userId: String, sdkAuthSignature: String?) {
        Braze.getInstance(context).changeUser(userId, sdkAuthSignature)
    }

    @JavascriptInterface
    fun requestImmediateDataFlush() {
        Braze.getInstance(context).requestImmediateDataFlush()
    }

    @JavascriptInterface
    fun logCustomEventWithJSON(eventName: String?, propertiesJSON: String?) {
        val brazeProperties = parseProperties(propertiesJSON)
        Braze.getInstance(context).logCustomEvent(eventName, brazeProperties)
    }

    @JavascriptInterface
    fun logPurchaseWithJSON(
        productId: String?,
        price: Double,
        currencyCode: String?,
        quantity: Int,
        propertiesJSON: String?
    ) {
        val brazeProperties = parseProperties(propertiesJSON)
        Braze.getInstance(context).logPurchase(
            productId,
            currencyCode,
            BigDecimal(price.toString()),
            quantity,
            brazeProperties
        )
    }

    @JavascriptInterface
    fun logButtonClick(buttonId: String?) {
        buttonId?.let { inAppMessage.logButtonClick(it) }
    }

    @JavascriptInterface
    fun logClick() {
        inAppMessage.logClick()
    }

    @JavascriptInterface
    fun requestPushPermission() {
        BrazeInAppMessageManager.getInstance().activity.requestPushPermissionPrompt()
    }

    @VisibleForTesting
    fun parseProperties(propertiesJSON: String?): BrazeProperties? {
        try {
            if (propertiesJSON != null && propertiesJSON != "undefined"
                && propertiesJSON != "null"
            ) {
                return BrazeProperties(JSONObject(propertiesJSON))
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to parse properties JSON String: $propertiesJSON" }
        }
        return null
    }
}
