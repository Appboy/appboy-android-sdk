@file:JvmName("HtmlUtils")

package com.braze.push.support

import android.os.Build
import android.text.Html
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag

private val TAG = "HtmlUtils".getBrazeLogTag()

/**
 * Returns displayable styled text from the provided HTML
 * string, if [com.braze.configuration.BrazeConfigurationProvider.getIsPushNotificationHtmlRenderingEnabled] is enabled.
 * When disabled, returns the input text.
 */
@Suppress("deprecation")
fun String.getHtmlSpannedTextIfEnabled(
    configurationProvider: BrazeConfigurationProvider
): CharSequence {
    if (this.isBlank()) {
        brazelog(TAG) { "Cannot create html spanned text on blank text. Returning blank string." }
        return this
    }
    return if (configurationProvider.isPushNotificationHtmlRenderingEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(this)
        }
    } else {
        this
    }
}
