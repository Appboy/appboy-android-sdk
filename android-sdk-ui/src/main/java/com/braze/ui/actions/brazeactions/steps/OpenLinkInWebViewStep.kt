package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.ui.BrazeDeeplinkHandler

internal object OpenLinkInWebViewStep : BaseBrazeActionStep() {
    override fun isValid(data: StepData): Boolean = data.run {
        isArgCountInBounds(fixedArgCount = 1)
            && isArgString(0)
    }

    override fun run(context: Context, data: StepData) {
        val url = data.firstArg.toString()
        val handler = BrazeDeeplinkHandler.getInstance()
        handler.createUriActionFromUrlString(
            url = url,
            extras = null,
            openInWebView = true,
            channel = data.channel
        )?.let {
            handler.gotoUri(context, it)
        }
    }
}
