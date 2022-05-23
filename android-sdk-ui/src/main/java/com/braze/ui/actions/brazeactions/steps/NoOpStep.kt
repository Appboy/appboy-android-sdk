package com.braze.ui.actions.brazeactions.steps

import android.content.Context

internal object NoOpStep : IBrazeActionStep {
    override fun isValid(data: StepData): Boolean = false

    override fun run(context: Context, data: StepData) {}
}
