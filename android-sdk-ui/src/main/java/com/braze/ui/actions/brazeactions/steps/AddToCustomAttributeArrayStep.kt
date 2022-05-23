package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.Braze

internal object AddToCustomAttributeArrayStep : BaseBrazeActionStep() {
    override fun isValid(data: StepData): Boolean = data.run {
        isArgCountInBounds(fixedArgCount = 2)
            && isArgString(0)
            && isArgString(1)
    }

    override fun run(context: Context, data: StepData) {
        val key = data.firstArg.toString()
        val value = data.secondArg.toString()
        Braze.getInstance(context).runOnUser {
            it.addToCustomAttributeArray(key, value)
        }
    }
}
