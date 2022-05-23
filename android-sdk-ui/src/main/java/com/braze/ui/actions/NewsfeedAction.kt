package com.braze.ui.actions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.appboy.enums.Channel
import com.appboy.ui.activities.AppboyFeedActivity
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog

open class NewsfeedAction(val extras: Bundle?, override val channel: Channel) : IAction {
    override fun execute(context: Context) {
        try {
            val intent = Intent(context, AppboyFeedActivity::class.java)
            if (extras != null) {
                intent.putExtras(extras)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            brazelog(E, e) { "AppboyFeedActivity was not opened successfully." }
        }
    }
}
