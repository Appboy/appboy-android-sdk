package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.Braze
import com.braze.support.BrazeLogger.brazeLogTag

internal object SetCustomUserAttributeStep : BaseBrazeActionStep() {
    val TAG = SetCustomUserAttributeStep.brazeLogTag()

    override fun isValid(data: StepData): Boolean = data.run {
        isArgCountInBounds(fixedArgCount = 2)
            && isArgString(0)
            && secondArg != null
    }

    override fun run(context: Context, data: StepData) {
        // This value was already checked
        // for nullity in `isValid()`
        val value = data.secondArg ?: return
        Braze.getInstance(context).runOnUser {
            it.setCustomAttribute(
                key = data.firstArg.toString(),
                value
            )
        }
    }
}
