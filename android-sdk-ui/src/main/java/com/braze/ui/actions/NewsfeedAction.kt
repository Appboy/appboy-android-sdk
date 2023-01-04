package com.braze.ui.actions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.braze.enums.Channel
import com.braze.ui.activities.BrazeFeedActivity
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog

open class NewsfeedAction(val extras: Bundle?, override val channel: Channel) : IAction {
    override fun execute(context: Context) {
        try {
            val intent = Intent(context, BrazeFeedActivity::class.java)
            if (extras != null) {
                intent.putExtras(extras)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            brazelog(E, e) { "BrazeFeedActivity was not opened successfully." }
        }
    }
}
