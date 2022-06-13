package com.braze.push

import android.app.Notification
import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.appboy.models.push.BrazeNotificationPayload
import com.braze.IBrazeNotificationFactory
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.push.BrazeNotificationActionUtils.addNotificationActions
import com.braze.push.BrazeNotificationStyleFactory.Companion.setStyleIfSupported
import com.braze.push.BrazeNotificationUtils.getOrCreateNotificationChannelId
import com.braze.push.BrazeNotificationUtils.prefetchBitmapsIfNewlyReceivedStoryPush
import com.braze.push.BrazeNotificationUtils.setAccentColorIfPresentAndSupported
import com.braze.push.BrazeNotificationUtils.setCategoryIfPresentAndSupported
import com.braze.push.BrazeNotificationUtils.setContentIfPresent
import com.braze.push.BrazeNotificationUtils.setContentIntentIfPresent
import com.braze.push.BrazeNotificationUtils.setDeleteIntent
import com.braze.push.BrazeNotificationUtils.setLargeIconIfPresentAndSupported
import com.braze.push.BrazeNotificationUtils.setNotificationBadgeNumberIfPresent
import com.braze.push.BrazeNotificationUtils.setPriorityIfPresentAndSupported
import com.braze.push.BrazeNotificationUtils.setPublicVersionIfPresentAndSupported
import com.braze.push.BrazeNotificationUtils.setSetShowWhen
import com.braze.push.BrazeNotificationUtils.setSmallIcon
import com.braze.push.BrazeNotificationUtils.setSoundIfPresentAndSupported
import com.braze.push.BrazeNotificationUtils.setSummaryTextIfPresentAndSupported
import com.braze.push.BrazeNotificationUtils.setTickerIfPresent
import com.braze.push.BrazeNotificationUtils.setTitleIfPresent
import com.braze.push.BrazeNotificationUtils.setVisibilityIfPresentAndSupported
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog

open class BrazeNotificationFactory : IBrazeNotificationFactory {
    /**
     * Creates the rich notification. The notification content varies based on the Android version on the
     * device, but each notification can contain an icon, image, title, and content.
     *
     * Opening a notification from the notification center triggers a broadcast message to be sent.
     * The broadcast message action is com.braze.push.intent.NOTIFICATION_OPENED.
     */
    override fun createNotification(payload: BrazeNotificationPayload): Notification? {
        val builder = populateNotificationBuilder(payload)
        return if (builder != null) {
            builder.build()
        } else {
            brazelog(I) { "Notification could not be built. Returning null as created notification" }
            null
        }
    }

    /**
     * Please use [createNotification] directly instead.
     */
    fun createNotification(
        appConfigurationProvider: BrazeConfigurationProvider?,
        context: Context?,
        notificationExtras: Bundle?,
        brazeExtras: Bundle?
    ): Notification? {
        val payload = BrazeNotificationPayload(
            notificationExtras,
            brazeExtras,
            context,
            appConfigurationProvider
        )
        return createNotification(payload)
    }

    /**
     * Equivalent to [createNotification]
     */
    fun populateNotificationBuilder(
        configurationProvider: BrazeConfigurationProvider?,
        context: Context?,
        notificationExtras: Bundle?,
        brazeExtras: Bundle?
    ): NotificationCompat.Builder? {
        val payload = BrazeNotificationPayload(
            notificationExtras,
            brazeExtras,
            context,
            configurationProvider
        )
        return populateNotificationBuilder(payload)
    }

    companion object {
        @Volatile
        private var internalInstance: BrazeNotificationFactory = BrazeNotificationFactory()

        /**
         * Returns the singleton [BrazeNotificationFactory] instance.
         */
        @JvmStatic
        val instance: BrazeNotificationFactory
            get() {
                return internalInstance
            }

        /**
         * Returns a notification builder populated with all fields from the notification extras and
         * Braze extras.
         *
         * To create a notification object, call `build()` on the returned builder instance.
         */
        @JvmStatic
        fun populateNotificationBuilder(payload: BrazeNotificationPayload): NotificationCompat.Builder? {
            brazelog(V) { "Using BrazeNotificationPayload: $payload" }
            val context = payload.context
            if (context == null) {
                brazelog { "BrazeNotificationPayload has null context. Not creating notification" }
                return null
            }
            val brazeConfigurationProvider = payload.configurationProvider
            if (brazeConfigurationProvider == null) {
                brazelog { "BrazeNotificationPayload has null app configuration provider. Not creating notification" }
                return null
            }
            val notificationExtras = payload.notificationExtras

            // We build up the notification by setting values if they are present in the extras and supported
            // on the device. The notification building is currently order/combination independent, but
            // the addition of new RemoteViews options could mean that some methods conflict/overwrite. For clarity
            // we build the notification up in the order that each feature was supported.

            // If this notification is a push story,
            // make a best effort to preload bitmap images into the cache.
            prefetchBitmapsIfNewlyReceivedStoryPush(payload)
            val notificationChannelId = getOrCreateNotificationChannelId(payload)
            val notificationBuilder =
                NotificationCompat.Builder(context, notificationChannelId)
                    .setAutoCancel(true)
            setTitleIfPresent(notificationBuilder, payload)
            setContentIfPresent(notificationBuilder, payload)
            setTickerIfPresent(notificationBuilder, payload)
            setSetShowWhen(notificationBuilder, payload)

            // Add intent to fire when the notification is opened or deleted.
            setContentIntentIfPresent(context, notificationBuilder, notificationExtras)
            setDeleteIntent(context, notificationBuilder, notificationExtras)
            setSmallIcon(brazeConfigurationProvider, notificationBuilder)
            setLargeIconIfPresentAndSupported(notificationBuilder, payload)
            setSoundIfPresentAndSupported(notificationBuilder, payload)

            // Subtext, priority, notification actions, and styles were added in JellyBean.
            setSummaryTextIfPresentAndSupported(notificationBuilder, payload)
            setPriorityIfPresentAndSupported(notificationBuilder, payload)
            setStyleIfSupported(notificationBuilder, payload)
            addNotificationActions(notificationBuilder, payload)

            // Accent color, category, visibility, and public notification were added in Lollipop.
            setAccentColorIfPresentAndSupported(notificationBuilder, payload)
            setCategoryIfPresentAndSupported(notificationBuilder, payload)
            setVisibilityIfPresentAndSupported(notificationBuilder, payload)
            setPublicVersionIfPresentAndSupported(notificationBuilder, payload)

            // Notification priority and sound were deprecated in Android O
            setNotificationBadgeNumberIfPresent(notificationBuilder, payload)
            return notificationBuilder
        }
    }
}
