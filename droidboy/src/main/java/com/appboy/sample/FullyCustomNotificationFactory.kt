package com.appboy.sample

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.appboy.models.push.BrazeNotificationPayload
import com.braze.IBrazeNotificationFactory
import com.braze.push.BrazeNotificationUtils.getOrCreateNotificationChannelId
import com.braze.push.BrazeNotificationUtils.setAccentColorIfPresentAndSupported

class FullyCustomNotificationFactory : IBrazeNotificationFactory {
    override fun createNotification(payload: BrazeNotificationPayload): Notification? {
        val context = payload.context ?: return null

        val notificationChannelId = getOrCreateNotificationChannelId(payload)
        val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
        notificationBuilder.setContentTitle(payload.titleText)
        notificationBuilder.setSmallIcon(R.drawable.com_braze_push_small_notification_icon)
        setAccentColorIfPresentAndSupported(notificationBuilder, payload)
        val contentString = parseContentsFromExtras(payload.extras)
        notificationBuilder.setContentText(contentString)
        return notificationBuilder.build()
    }

    private fun parseContentsFromExtras(extras: Map<String, String>): String = """
        Your order: ${extras[PushTesterFragment.EXAMPLE_APPBOY_EXTRA_KEY_1]}, 
        ${extras[PushTesterFragment.EXAMPLE_APPBOY_EXTRA_KEY_2]}, 
        ${extras[PushTesterFragment.EXAMPLE_APPBOY_EXTRA_KEY_3]}.
    """.trimIndent()
}
