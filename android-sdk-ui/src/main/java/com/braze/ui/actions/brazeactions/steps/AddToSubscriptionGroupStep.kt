package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.Braze

internal object AddToSubscriptionGroupStep : BaseBrazeActionStep() {
    override fun isValid(data: StepData): Boolean = data.run {
        isArgCountInBounds(fixedArgCount = 1)
            && isArgString(0)
    }

    override fun run(context: Context, data: StepData) {
        val subscriptionGroupId = data.firstArg.toString()
        Braze.getInstance(context).runOnUser {
            it.addToSubscriptionGroup(subscriptionGroupId)
        }
    }
}
