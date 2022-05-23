package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.Braze

internal object RemoveFromCustomAttributeArrayStep : BaseBrazeActionStep() {
    override fun isValid(data: StepData): Boolean = data.run {
        isArgCountInBounds(fixedArgCount = 2)
            && isArgString(0)
            && isArgString(1)
    }

    override fun run(context: Context, data: StepData) {
        Braze.getInstance(context).runOnUser {
            it.removeFromCustomAttributeArray(
                key = data.firstArg.toString(),
                value = data.secondArg.toString()
            )
        }
    }
}
