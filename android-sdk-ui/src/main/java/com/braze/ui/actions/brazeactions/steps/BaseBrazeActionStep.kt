package com.braze.ui.actions.brazeactions.steps

import com.appboy.events.SimpleValueCallback
import com.braze.Braze
import com.braze.BrazeUser
import com.braze.support.BrazeLogger.brazelog

sealed class BaseBrazeActionStep : IBrazeActionStep {
    companion object {
        internal fun Braze.runOnUser(block: (user: BrazeUser) -> Unit) {
            this.getCurrentUser(object : SimpleValueCallback<BrazeUser>() {
                override fun onSuccess(user: BrazeUser) {
                    super.onSuccess(user)
                    block(user)
                }

                override fun onError() {
                    super.onError()
                    brazelog { "Failed to run on Braze user object" }
                }
            })
        }
    }
}
