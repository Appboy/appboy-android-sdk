package com.braze.ui.actions

import android.content.Context
import com.braze.enums.Channel

interface IAction {
    val channel: Channel

    fun execute(context: Context)
}
