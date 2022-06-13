package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.support.iterator
import com.braze.ui.actions.brazeactions.BrazeActionParser
import org.json.JSONObject

internal object ContainerStep : BaseBrazeActionStep() {
    internal const val STEPS = "steps"

    override fun isValid(data: StepData): Boolean = data.srcJson.has(STEPS)

    /**
     * Container steps contain other steps under a special json array "steps".
     */
    override fun run(context: Context, data: StepData) {
        for (step in getChildStepIterator(data)) {
            // Each step is an action to parse
            BrazeActionParser.parse(context, data.copy(srcJson = step))
        }
    }

    @JvmSynthetic
    internal fun getChildStepIterator(data: StepData): Iterator<JSONObject> =
        data.srcJson.getJSONArray(STEPS).iterator()
}
