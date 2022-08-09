package com.braze.ui.actions.brazeactions.steps

import com.braze.Braze
import com.braze.BrazeUser
import com.braze.events.IValueCallback
import com.braze.support.BrazeLogger.brazelog

sealed class BaseBrazeActionStep : IBrazeActionStep {
    companion object {
        internal fun Braze.runOnUser(block: (user: BrazeUser) -> Unit) {
            this.getCurrentUser(object : IValueCallback<BrazeUser> {
                override fun onSuccess(value: BrazeUser) {
                    block(value)
                }

                override fun onError() {
                    brazelog { "Failed to run on Braze user object" }
                }
            })
        }
    }
}
