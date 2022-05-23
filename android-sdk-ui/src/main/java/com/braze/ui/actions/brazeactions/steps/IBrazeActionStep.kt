package com.braze.ui.actions.brazeactions.steps

import android.content.Context

internal sealed interface IBrazeActionStep {
    /**
     * Performs a best-effort check on any arguments
     * to assert validity before step execution.
     */
    fun isValid(data: StepData): Boolean

    /**
     * Executes the Braze Action step.
     */
    fun run(
        context: Context,
        data: StepData
    )
}
