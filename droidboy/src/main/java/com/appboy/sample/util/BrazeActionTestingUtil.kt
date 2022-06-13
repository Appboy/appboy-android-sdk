package com.appboy.sample.util

import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.braze.enums.inappmessage.ClickAction
import com.braze.enums.inappmessage.DismissType
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.InAppMessageModal
import com.braze.models.inappmessage.MessageButton

object BrazeActionTestingUtil {

    @JvmStatic
    fun getPushPromptInAppMessageModal(context: Context): IInAppMessage {
        val pushPromptModal = InAppMessageModal().apply {
            backgroundColor = Color.LTGRAY
            dismissType = DismissType.MANUAL
            header = "Allow Braze Push"
            message = "No-Code Braze Actions push primer. Try it out!"
        }
        pushPromptModal.messageButtons = listOf(
            MessageButton().apply {
                text = "Cancel :("
                setClickBehavior(ClickAction.NONE)
                backgroundColor = Color.BLACK
            },
            MessageButton().apply {
                text = "Show Me Push!"
                setClickBehavior(ClickAction.URI, getPushPromptUri(context))
            }
        )
        return pushPromptModal
    }

    private fun getPushPromptUri(context: Context): Uri {
        val filename = "braze_actions/show_push_prompt.txt"
        val contents = context.assets
            .open(filename)
            .bufferedReader()
            .use {
                it.readText()
            }
        return Uri.parse(contents)
    }
}
