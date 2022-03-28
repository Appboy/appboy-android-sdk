package com.braze.ui.actions

import android.content.Context
import com.appboy.enums.Channel

interface IAction {
    val channel: Channel

    fun execute(context: Context)
}
