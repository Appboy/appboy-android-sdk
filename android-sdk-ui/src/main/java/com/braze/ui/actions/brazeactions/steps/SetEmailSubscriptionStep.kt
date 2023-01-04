package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.enums.NotificationSubscriptionType
import com.braze.Braze
import com.braze.support.BrazeLogger.brazeLogTag
import com.braze.support.BrazeLogger.brazelog

internal object SetEmailSubscriptionStep : BaseBrazeActionStep() {
    val TAG = SetEmailSubscriptionStep.brazeLogTag()

    override fun isValid(data: StepData): Boolean = data.run {
        isArgCountInBounds(fixedArgCount = 1)
            && isArgString(0)
    }

    override fun run(context: Context, data: StepData) {
        val subscriptionType = NotificationSubscriptionType.fromValue(data.firstArg.toString())
        if (subscriptionType == null) {
            brazelog { "Could not parse subscription type from data: $data" }
            return
        }
        Braze.getInstance(context).runOnUser {
            it.setEmailNotificationSubscriptionType(subscriptionType)
        }
    }
}
