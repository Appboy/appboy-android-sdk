package com.braze.ui.inappmessage.listeners

import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageThemeable
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.InAppMessageOperation
import com.braze.ui.support.isDeviceInNightMode

/**
 * Default implementation of [IInAppMessageManagerListener]
 *
 * This only overrides the [beforeInAppMessageDisplayed]. The rest of the functions take the
 * defaults in [IInAppMessageManagerListener]
 */
open class DefaultInAppMessageManagerListener : IInAppMessageManagerListener {
    override fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage): InAppMessageOperation {
        if (inAppMessage is IInAppMessageThemeable) {
            BrazeInAppMessageManager.getInstance().applicationContext?.let { appContext ->
                if (isDeviceInNightMode(appContext)) {
                    inAppMessage.enableDarkTheme()
                }
            }
        }
        return InAppMessageOperation.DISPLAY_NOW
    }
}
