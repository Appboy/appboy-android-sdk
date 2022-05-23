package com.braze.push

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.braze.Constants.isAmazonDevice
import com.braze.coroutine.BrazeCoroutineScope
import com.braze.push.BrazeNotificationUtils.notificationReceiverClass
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog

class NotificationTrampolineActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        brazelog(V) { "NotificationTrampolineActivity created" }
    }

    override fun onResume() {
        super.onResume()
        try {
            val receivedIntent = intent
            if (receivedIntent == null) {
                brazelog { "Notification trampoline activity received null intent. Doing nothing." }
                finish()
                return
            }
            val action = receivedIntent.action
            if (action == null) {
                brazelog { "Notification trampoline activity received intent with null action. Doing nothing." }
                finish()
                return
            }
            brazelog(V) { "Notification trampoline activity received intent: $receivedIntent" }
            // Route the intent back to the receiver
            val sendIntent = Intent(action).setClass(this, notificationReceiverClass)

            receivedIntent.extras?.let {
                sendIntent.putExtras(it)
            }
            if (isAmazonDevice) {
                BrazePushReceiver.handleReceivedIntent(this, sendIntent)
            } else {
                BrazePushReceiver.handleReceivedIntent(this, sendIntent, false)
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to route intent to notification receiver" }
        }

        // Now that this Activity has been created, we are safe to finish it in accordance with
        // https://developer.android.com/guide/components/activities/background-starts#exceptions
        // Guarantee that this Activity gets finished if onPause() is never called

        brazelog(V) { "Notification trampoline activity finished processing. Delaying before finishing activity." }
        @Suppress("MagicNumber")
        BrazeCoroutineScope.launchDelayed(200) {
            brazelog(V) { "Delay complete. Finishing Notification trampoline activity now" }
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        brazelog(V) { "Notification trampoline activity paused and finishing" }
        finish()
    }
}
