package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.Braze

internal object LogCustomEventStep : BaseBrazeActionStep() {
    override fun isValid(data: StepData): Boolean = data.run {
        isArgCountInBounds(rangedArgCount = 1..2)
            && isArgString(0)
            && isArgOptionalJsonObject(1)
    }

    override fun run(context: Context, data: StepData) {
        Braze.getInstance(context)
            .logCustomEvent(
                data.firstArg.toString(),
                data.coerceArgToPropertiesOrNull(1)
            )
    }
}
