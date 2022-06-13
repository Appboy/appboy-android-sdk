package com.braze.ui.actions.brazeactions.steps

import android.content.Context
import com.braze.support.requestPushPermissionPrompt
import com.braze.ui.inappmessage.BrazeInAppMessageManager

internal object RequestPushPermissionStep : BaseBrazeActionStep() {
    /**
     * This step does not require any arguments.
     */
    override fun isValid(data: StepData): Boolean = true

    override fun run(context: Context, data: StepData) {
        BrazeInAppMessageManager.getInstance().activity.requestPushPermissionPrompt()
    }
}
