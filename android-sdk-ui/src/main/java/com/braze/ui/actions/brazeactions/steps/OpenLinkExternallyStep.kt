package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.ui.BrazeDeeplinkHandler

internal object OpenLinkExternallyStep : BaseBrazeActionStep() {
    override fun isValid(data: StepData): Boolean = data.run {
        // The second parameter "openInNewTab" is not
        // used on Android, but may be present. It is
        // not checked for validity.
        isArgCountInBounds(rangedArgCount = 1..2)
            && isArgString(0)
    }

    override fun run(context: Context, data: StepData) {
        val url = data.firstArg.toString()
        val handler = BrazeDeeplinkHandler.getInstance()
        handler.createUriActionFromUrlString(
            url = url,
            extras = null,
            openInWebView = false,
            channel = data.channel
        )?.let {
            handler.gotoUri(context, it)
        }
    }
}
