package com.appboy.unity.utils

import android.content.Context
import com.braze.Braze.Companion.getInstance
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageImmersive
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog

object InAppMessageUtils {
    fun inAppMessageFromString(context: Context?, messageJSONString: String?): IInAppMessage? {
        return if (messageJSONString == null || context == null) {
            null
        } else {
            getInstance(context)
                .deserializeInAppMessageString(messageJSONString)
        }
    }

    fun logInAppMessageClick(inAppMessage: IInAppMessage?) {
        if (inAppMessage != null) {
            inAppMessage.logClick()
        } else {
            brazelog(W) {
                "The in-app message is null. Not logging in-app message click."
            }
        }
    }

    fun logInAppMessageButtonClick(inAppMessage: IInAppMessage?, buttonId: Int) {
        if (inAppMessage == null) {
            brazelog(W) { "The in-app message is null. Not logging in-app message button $buttonId click." }
            return
        }
        if (inAppMessage is IInAppMessageImmersive) {
            inAppMessage.messageButtons
                .firstOrNull() { it.id == buttonId }
                ?.let { inAppMessage.logButtonClick(it) }
        } else {
            brazelog(W) {
                "The in-app message isn't an instance of InAppMessageImmersive. " +
                    "Not logging in-app message button click."
            }
        }
    }

    fun logInAppMessageImpression(inAppMessage: IInAppMessage?) {
        if (inAppMessage != null) {
            inAppMessage.logImpression()
        } else {
            brazelog(W) {
                "The in-app message is null, Not logging in-app message impression."
            }
        }
    }
}
