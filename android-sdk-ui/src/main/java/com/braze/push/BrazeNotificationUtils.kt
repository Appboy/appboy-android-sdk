package com.braze.push

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.braze.Braze
import com.braze.BrazeInternal
import com.braze.BrazeInternal.addSerializedContentCardToStorage
import com.braze.BrazeInternal.refreshFeatureFlags
import com.braze.BrazeInternal.requestGeofenceRefresh
import com.braze.Constants
import com.braze.Constants.isAmazonDevice
import com.braze.IBrazeNotificationFactory
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.enums.BrazePushEventType
import com.braze.enums.BrazePushEventType.NOTIFICATION_DELETED
import com.braze.enums.BrazePushEventType.NOTIFICATION_OPENED
import com.braze.enums.BrazePushEventType.NOTIFICATION_RECEIVED
import com.braze.enums.BrazeViewBounds
import com.braze.enums.Channel
import com.braze.models.push.BrazeNotificationPayload
import com.braze.push.support.getHtmlSpannedTextIfEnabled
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag
import com.braze.support.IntentUtils.addComponentAndSendBroadcast
import com.braze.support.IntentUtils.getImmutablePendingIntentFlags
import com.braze.support.IntentUtils.getRequestCode
import com.braze.support.getOptionalString
import com.braze.support.hasPermission
import com.braze.support.parseJsonObjectIntoBundle
import com.braze.ui.BrazeDeeplinkHandler.Companion.getInstance
import com.braze.ui.support.getMainActivityIntent
import org.json.JSONObject

@Suppress("LargeClass", "TooManyFunctions")
object BrazeNotificationUtils {
    private enum class BrazeNotificationBroadcastType(val brazePushEventType: BrazePushEventType) {
        OPENED(NOTIFICATION_OPENED),
        RECEIVED(NOTIFICATION_RECEIVED),
        DELETED(NOTIFICATION_DELETED)
    }

    private val TAG = getBrazeLogTag(BrazeNotificationUtils::class.java)
    private const val SOURCE_KEY = "source"

    /**
     * Returns a custom [IBrazeNotificationFactory] if set, else the default [IBrazeNotificationFactory].
     */
    @get:JvmStatic
    val activeNotificationFactory: IBrazeNotificationFactory
        get() {
            return Braze.customBrazeNotificationFactory ?: BrazeNotificationFactory.instance
        }

    /**
     * The [Class] of the notification receiver used by this application.
     */
    @get:JvmStatic
    val notificationReceiverClass: Class<*>
        get() = if (isAmazonDevice) {
            BrazeAmazonDeviceMessagingReceiver::class.java
        } else {
            BrazePushReceiver::class.java
        }

    /**
     * Handles a push notification click. Called by [BrazePushReceiver] when a
     * Braze push notification click intent is received.
     *
     * See [sendNotificationOpenedBroadcast]
     *
     * @param context Application context
     * @param intent  the internal notification clicked intent constructed in
     * [setContentIntentIfPresent]
     */
    @JvmStatic
    fun handleNotificationOpened(context: Context, intent: Intent) {
        try {
            Braze.getInstance(context).logPushNotificationOpened(intent)
            sendNotificationOpenedBroadcast(context, intent)
            val appConfigurationProvider = BrazeConfigurationProvider(context)
            if (appConfigurationProvider.doesHandlePushDeepLinksAutomatically) {
                routeUserWithNotificationOpenedIntent(context, intent)
            } else {
                brazelog(I) { "Not handling deep links automatically, skipping deep link handling" }
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Exception occurred attempting to handle notification opened intent." }
        }
    }

    /**
     * Handles a push notification deletion by the user. Called by [BrazePushReceiver] receiver when a
     * Braze push notification delete intent is received.
     *
     * @see [NotificationCompat.Builder.setDeleteIntent]
     * @param context Application context
     * @param intent  the internal notification delete intent constructed in
     * [setDeleteIntent]
     */
    @JvmStatic
    fun handleNotificationDeleted(context: Context, intent: Intent) {
        try {
            brazelog { "Sending notification deleted broadcast" }
            val notificationExtras = intent.extras
            if (notificationExtras != null) {
                val notificationPayload = BrazeNotificationPayload(notificationExtras, context = context)
                sendPushActionIntent(context, BrazeNotificationBroadcastType.DELETED, notificationExtras, notificationPayload)
            } else {
                sendPushActionIntent(context, BrazeNotificationBroadcastType.DELETED, notificationExtras)
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Exception occurred attempting to handle notification delete intent." }
        }
    }

    /**
     * Opens any available deep links with an Intent.ACTION_VIEW intent, placing the main activity
     * on the back stack. If no deep link is available, opens the main activity.
     *
     * @param context
     * @param intent  the internal notification clicked intent constructed in
     * [setContentIntentIfPresent]
     */
    @JvmStatic
    fun routeUserWithNotificationOpenedIntent(context: Context, intent: Intent) {
        // get extras bundle.
        var extras = intent.getBundleExtra(Constants.BRAZE_PUSH_EXTRAS_KEY)
        if (extras == null) {
            extras = Bundle()
        }
        extras.putString(
            Constants.BRAZE_PUSH_CAMPAIGN_ID_KEY,
            intent.getStringExtra(Constants.BRAZE_PUSH_CAMPAIGN_ID_KEY)
        )
        extras.putString(SOURCE_KEY, Constants.BRAZE)

        // If a deep link exists, start an ACTION_VIEW intent pointing at the deep link.
        // The intent returned from getStartActivityIntent() is placed on the back stack.
        // Otherwise, start the intent defined in getStartActivityIntent().
        val deepLink = intent.getStringExtra(Constants.BRAZE_PUSH_DEEP_LINK_KEY)
        if (!deepLink.isNullOrBlank()) {
            val useWebView = "true".equals(intent.getStringExtra(Constants.BRAZE_PUSH_OPEN_URI_IN_WEBVIEW_KEY), ignoreCase = true)
            brazelog { "Found a deep link: $deepLink. Use webview set to: $useWebView" }

            // Pass deeplink and use webview values to target activity.
            extras.putString(Constants.BRAZE_PUSH_DEEP_LINK_KEY, deepLink)
            extras.putBoolean(Constants.BRAZE_PUSH_OPEN_URI_IN_WEBVIEW_KEY, useWebView)
            getInstance().createUriActionFromUrlString(deepLink, extras, useWebView, Channel.PUSH)?.let {
                getInstance().gotoUri(context, it)
            }
        } else {
            val mainActivityIntent = getMainActivityIntent(context, extras)
            brazelog { "Push notification had no deep link. Opening main activity: $mainActivityIntent" }
            context.startActivity(mainActivityIntent)
        }
    }

    /**
     * Checks the incoming notification intent to determine whether it is a Braze push message.
     *
     * All Braze push messages must contain an extras entry with key set to [Constants.BRAZE_PUSH_BRAZE_KEY] and value set to "true".
     */
    @JvmStatic
    fun Intent.isBrazePushMessage(): Boolean {
        val extras = this.extras ?: return false
        return "true".equals(extras.getString(Constants.BRAZE_PUSH_BRAZE_KEY), ignoreCase = true)
    }

    /**
     * Checks the notification intent to determine whether this is a notification message or a
     * silent push.
     *
     * A notification message is a Braze push message that displays a notification in the
     * notification center (and optionally contains extra information that can be used directly
     * by the app).
     *
     * A silent push is a Braze push message that contains only extra information that can
     * be used directly by the app.
     */
    @JvmStatic
    fun isNotificationMessage(intent: Intent): Boolean {
        val extras = intent.extras ?: return false
        return extras.containsKey(Constants.BRAZE_PUSH_TITLE_KEY) && extras.containsKey(Constants.BRAZE_PUSH_CONTENT_KEY)
    }

    /**
     * Creates and sends a broadcast message that can be listened for by the host app. The broadcast
     * message intent contains all of the data sent as part of the Braze push message.
     */
    @JvmStatic
    fun sendPushMessageReceivedBroadcast(
        context: Context,
        notificationExtras: Bundle,
        payload: BrazeNotificationPayload
    ) {
        brazelog { "Sending push message received broadcast" }
        sendPushActionIntent(context, BrazeNotificationBroadcastType.RECEIVED, notificationExtras, payload)
    }

    /**
     * Requests a geofence refresh from Braze if appropriate based on the payload of the push notification.
     *
     * @return True iff a geofence refresh was requested from Braze.
     */
    @JvmStatic
    fun requestGeofenceRefreshIfAppropriate(payload: BrazeNotificationPayload): Boolean {
        val context = payload.context
        return if (payload.shouldSyncGeofences && context != null) {
            brazelog { "Geofence sync key was true. Syncing geofences." }
            requestGeofenceRefresh(context, true)
            true
        } else {
            brazelog { "Geofence sync key not included in push payload or false. Not syncing geofences." }
            false
        }
    }

    /**
     * Refreshes a feature flags refresh from Braze if appropriate based on the payload of the push notification.
     * The SDK will respect the rate limit for feature flag refreshes.
     *
     * @return True iff a feature flags refresh was requested from Braze.
     */
    @JvmStatic
    fun refreshFeatureFlagsIfAppropriate(payload: BrazeNotificationPayload): Boolean {
        val context = payload.context
        return if (payload.shouldRefreshFeatureFlags && context != null) {
            brazelog { "Feature flag refresh key was true. Refreshing feature flags." }
            refreshFeatureFlags(context)
            true
        } else {
            brazelog(V) { "Feature flag refresh key not included in push payload or false. Not refreshing feature flags." }
            false
        }
    }

    /**
     * Creates an alarm which will issue a broadcast to cancel the notification
     * specified by the given [notificationId] after the given duration.
     */
    @JvmStatic
    fun setNotificationDurationAlarm(context: Context, thisClass: Class<*>?, notificationId: Int, durationInMillis: Int) {
        val cancelIntent = Intent(context, thisClass)
        cancelIntent.action = Constants.BRAZE_CANCEL_NOTIFICATION_ACTION
        cancelIntent.putExtra(Constants.BRAZE_PUSH_NOTIFICATION_ID, notificationId)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or getImmutablePendingIntentFlags()
        val pendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, flags)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (durationInMillis >= Constants.BRAZE_MINIMUM_NOTIFICATION_DURATION_MILLIS) {
            brazelog { "Setting Notification duration alarm for $durationInMillis ms" }
            alarmManager[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + durationInMillis] = pendingIntent
        }
    }

    /**
     * Returns an id for the new notification we'll send to the notification center.
     * Notification id is used by the Android OS to override currently active notifications with identical ids.
     * If a custom notification id is not defined in the payload, Braze derives an id value from the message's contents
     * to prevent duplication in the notification center.
     */
    @JvmStatic
    fun getNotificationId(payload: BrazeNotificationPayload): Int {
        val customNotificationId = payload.customNotificationId
        return if (customNotificationId != null) {
            brazelog { "Using notification id provided in the message's extras bundle: $customNotificationId" }
            customNotificationId
        } else {
            var messageKey: String? = ""
            // Don't concatenate if null
            payload.titleText?.let { messageKey += it }
            payload.contentText?.let { messageKey += it }
            val notificationId = messageKey.hashCode()
            brazelog {
                "Message without notification id provided in the extras bundle " +
                    "received. Using a hash of the message: $notificationId"
            }
            notificationId
        }
    }

    /**
     * This method will retrieve notification priority from notificationExtras bundle if it has been set.
     * Otherwise returns the default priority.
     *
     * Starting with Android O, priority is set on a notification channel and not individually on notifications.
     */
    @JvmStatic
    fun getNotificationPriority(payload: BrazeNotificationPayload): Int {
        val notificationPriority = payload.notificationPriorityInt
        payload.notificationPriorityInt?.let {
            @Suppress("DEPRECATION")
            if (it in Notification.PRIORITY_MIN..Notification.PRIORITY_MAX) {
                return it
            } else {
                brazelog(W) { "Received invalid notification priority $notificationPriority" }
            }
        }
        @Suppress("DEPRECATION")
        return Notification.PRIORITY_DEFAULT
    }

    @JvmStatic
    fun wakeScreenIfAppropriate(context: Context, configurationProvider: BrazeConfigurationProvider, notificationExtras: Bundle?): Boolean {
        return wakeScreenIfAppropriate(
            BrazeNotificationPayload(
                notificationExtras = notificationExtras,
                context = context,
                configurationProvider = configurationProvider
            )
        )
    }

    /**
     * This method will wake the device using a wake lock if the [android.Manifest.permission.WAKE_LOCK] permission is present in the
     * manifest. If the permission is not present, this does nothing. If the screen is already on,
     * and the permission is present, this does nothing. If the priority of the incoming notification
     * is min, this does nothing.
     */
    @SuppressLint("WakelockTimeout")
    @Suppress("ReturnCount")
    @JvmStatic
    fun wakeScreenIfAppropriate(payload: BrazeNotificationPayload): Boolean {
        val context = payload.context ?: return false
        val configurationProvider = payload.configurationProvider ?: return false
        val notificationExtras = payload.notificationExtras

        // Check for the wake lock permission.
        if (!context.hasPermission(Manifest.permission.WAKE_LOCK)
            || !configurationProvider.isPushWakeScreenForNotificationEnabled
        ) {
            return false
        }
        try {
            // Never wake a TV panel
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
                brazelog { "Not waking this TV UI mode device" }
                return false
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to check for TV status during screen wake. Continuing." }
        }

        // Don't wake lock if this is a minimum priority/importance notification.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Get the channel for this notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = getValidNotificationChannel(notificationManager, notificationExtras)
            if (notificationChannel == null) {
                brazelog { "Not waking screen on Android O+ device, could not find notification channel." }
                return false
            }
            if (notificationChannel.importance == NotificationManager.IMPORTANCE_MIN) {
                brazelog { "Not acquiring wake-lock for Android O+ notification with importance: ${notificationChannel.importance}" }
                return false
            }
        } else {
            @Suppress("DEPRECATION")
            if (getNotificationPriority(payload) == Notification.PRIORITY_MIN) {
                return false
            }
        }

        brazelog { "Waking screen for notification" }
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        // Deprecation warning suppressed for PowerManager.FULL_WAKE_LOCK usage.
        // Alternative requires Activity instance which is unavailable in this context.
        @Suppress("DEPRECATION")
        val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG)
        // Acquire the wake lock for some negligible time, then release it. We just want to wake the screen
        // and not take up more CPU power than necessary.
        wakeLock.acquire()
        wakeLock.release()
        return true
    }

    /**
     * Checks that the notification is a story that has only just been received. If so, each
     * image within the story is put in the Braze image loader's cache.
     */
    @JvmStatic
    fun prefetchBitmapsIfNewlyReceivedStoryPush(payload: BrazeNotificationPayload) {
        val context = payload.context ?: return
        if (!payload.isPushStory || !payload.isNewlyReceivedPushStory) return

        payload.pushStoryPages
            .mapNotNull { it.bitmapUrl }
            .forEach {
                brazelog(V) { "Pre-fetching bitmap at URL: $it" }
                Braze.getInstance(context).imageLoader
                    .getPushBitmapFromUrl(context, payload.brazeExtras, it, BrazeViewBounds.NOTIFICATION_ONE_IMAGE_STORY)
            }
        payload.isNewlyReceivedPushStory = false
    }

    @JvmStatic
    fun setTitleIfPresent(
        notificationBuilder: NotificationCompat.Builder,
        payload: BrazeNotificationPayload
    ) {
        brazelog { "Setting title for notification" }
        val titleText = payload.titleText ?: return
        val configurationProvider = payload.configurationProvider ?: return
        notificationBuilder.setContentTitle(titleText.getHtmlSpannedTextIfEnabled(configurationProvider))
    }

    /**
     * Sets notification content if it exists in the payload.
     */
    @JvmStatic
    fun setContentIfPresent(
        notificationBuilder: NotificationCompat.Builder,
        payload: BrazeNotificationPayload
    ) {
        brazelog { "Setting content for notification" }
        val contentText = payload.contentText ?: return
        val configurationProvider = payload.configurationProvider ?: return
        notificationBuilder.setContentText(contentText.getHtmlSpannedTextIfEnabled(configurationProvider))
    }

    /**
     * Sets notification ticker to the title if it exists in the payload.
     */
    @JvmStatic
    fun setTickerIfPresent(
        notificationBuilder: NotificationCompat.Builder,
        payload: BrazeNotificationPayload
    ) {
        brazelog { "Setting ticker for notification" }
        val titleText = payload.titleText ?: return
        notificationBuilder.setTicker(titleText)
    }

    /**
     * Create broadcast intent that will fire when the notification has been opened. [BrazePushReceiver] will be notified,
     * log a click, then send a broadcast to the client receiver.
     */
    @JvmStatic
    fun setContentIntentIfPresent(context: Context, notificationBuilder: NotificationCompat.Builder, notificationExtras: Bundle?) {
        try {
            val pushOpenedPendingIntent = getPushActionPendingIntent(context, Constants.BRAZE_PUSH_CLICKED_ACTION, notificationExtras)
            notificationBuilder.setContentIntent(pushOpenedPendingIntent)
        } catch (e: Exception) {
            brazelog(E, e) { "Error setting content intent." }
        }
    }

    @JvmStatic
    fun setDeleteIntent(context: Context, notificationBuilder: NotificationCompat.Builder, notificationExtras: Bundle?) {
        try {
            val pushDeletedIntent = Intent(Constants.BRAZE_PUSH_DELETED_ACTION).setClass(context, notificationReceiverClass)
            if (notificationExtras != null) {
                pushDeletedIntent.putExtras(notificationExtras)
            }
            val flags = PendingIntent.FLAG_ONE_SHOT or getImmutablePendingIntentFlags()
            val pushDeletedPendingIntent = PendingIntent.getBroadcast(context, getRequestCode(), pushDeletedIntent, flags)
            notificationBuilder.setDeleteIntent(pushDeletedPendingIntent)
        } catch (e: Exception) {
            brazelog(E, e) { "Error setting delete intent." }
        }
    }

    /**
     * Sets the icon used in the notification bar itself.
     * If a drawable defined in braze.xml is found, we use that. Otherwise, fall back to the application icon.
     *
     * @return the resource id of the small icon to be used.
     */
    @JvmStatic
    fun setSmallIcon(appConfigurationProvider: BrazeConfigurationProvider, notificationBuilder: NotificationCompat.Builder): Int {
        var smallNotificationIconResourceId = appConfigurationProvider.smallNotificationIconResourceId
        if (smallNotificationIconResourceId == 0) {
            brazelog {
                "Small notification icon resource was not found. " +
                    "Will use the app icon when displaying notifications."
            }
            smallNotificationIconResourceId = appConfigurationProvider.applicationIconResourceId
        } else {
            brazelog { "Setting small icon for notification via resource id" }
        }
        notificationBuilder.setSmallIcon(smallNotificationIconResourceId)
        return smallNotificationIconResourceId
    }

    /**
     * This method exists to disable [NotificationCompat.Builder.setShowWhen]
     * for push stories.
     */
    @JvmStatic
    fun setSetShowWhen(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload) {
        if (payload.isPushStory) {
            brazelog { "Set show when not supported in story push." }
            notificationBuilder.setShowWhen(false)
        }
    }

    /**
     * Set large icon. We use the large icon URL if it exists in the notificationExtras.
     * Otherwise we search for a drawable defined in braze.xml. If that doesn't exists, we do nothing.
     *
     * @return whether a large icon was successfully set.
     */
    @Suppress("ReturnCount")
    @JvmStatic
    fun setLargeIconIfPresentAndSupported(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload): Boolean {
        if (payload.isPushStory) {
            brazelog { "Large icon not supported in story push." }
            return false
        }
        val context = payload.context ?: return false
        val appConfigurationProvider = payload.configurationProvider ?: return false

        try {
            brazelog { "Setting large icon for notification" }
            payload.largeIcon?.let {
                val largeNotificationBitmap = Braze.getInstance(context)
                    .imageLoader
                    .getPushBitmapFromUrl(
                        context,
                        extras = null,
                        imageUrl = it,
                        BrazeViewBounds.NOTIFICATION_LARGE_ICON
                    )
                notificationBuilder.setLargeIcon(largeNotificationBitmap)
                return true
            }

            brazelog { "Large icon bitmap url not present in extras. Attempting to use resource id instead." }
            val largeNotificationIconResourceId = appConfigurationProvider.largeNotificationIconResourceId
            if (largeNotificationIconResourceId != 0) {
                val largeNotificationBitmap = BitmapFactory.decodeResource(context.resources, largeNotificationIconResourceId)
                notificationBuilder.setLargeIcon(largeNotificationBitmap)
                return true
            } else {
                brazelog { "Large icon resource id not present for notification" }
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Error setting large notification icon" }
        }
        brazelog { "Large icon not set for notification" }
        return false
    }

    /**
     * Notifications can optionally include a sound to play when the notification is delivered.
     *
     * Starting with Android O, sound is set on a notification channel and not individually on notifications.
     */
    @JvmStatic
    fun setSoundIfPresentAndSupported(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload) {
        val soundUri = payload.notificationSound ?: return
        if (soundUri == Constants.BRAZE_PUSH_NOTIFICATION_SOUND_DEFAULT_VALUE) {
            brazelog { "Setting default sound for notification." }
            notificationBuilder.setDefaults(Notification.DEFAULT_SOUND)
        } else {
            brazelog { "Setting sound for notification via uri." }
            notificationBuilder.setSound(Uri.parse(soundUri))
        }
    }

    /**
     * Sets the subText of the notification if a summary is present in the notification extras.
     */
    @JvmStatic
    fun setSummaryTextIfPresentAndSupported(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload) {
        val summaryText = payload.summaryText
        if (summaryText != null) {
            brazelog { "Setting summary text for notification" }
            notificationBuilder.setSubText(summaryText)
        } else {
            brazelog { "Summary text not present. Not setting summary text for notification." }
        }
    }

    /**
     * Sets the priority of the notification if a priority is present in the notification extras.
     *
     * Starting with Android O, priority is set on a notification channel and not individually on notifications.
     */
    @JvmStatic
    fun setPriorityIfPresentAndSupported(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload) {
        brazelog { "Setting priority for notification" }
        notificationBuilder.priority = getNotificationPriority(payload)
    }

    /**
     * Set accent color for devices on Lollipop and above. We use the push-specific accent color if it exists in the notificationExtras,
     * otherwise we search for a default set in braze.xml or don't set the color at all (and the system notification gray
     * default is used).
     *
     * Supported Lollipop+.
     */
    @JvmStatic
    fun setAccentColorIfPresentAndSupported(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        val accentColor = payload.accentColor
        if (accentColor != null) {
            brazelog { "Using accent color for notification from extras bundle" }
            notificationBuilder.color = accentColor
        } else {
            payload.configurationProvider?.let {
                brazelog { "Using default accent color for notification" }
                notificationBuilder.color = it.defaultNotificationAccentColor
            }
        }
    }

    /**
     * Set category for devices on Lollipop and above. Category is one of the predefined notification
     * categories (see the CATEGORY_* constants in Notification)
     * that best describes a Notification. May be used by the system for ranking and filtering.
     *
     * Supported Lollipop+.
     */
    @JvmStatic
    fun setCategoryIfPresentAndSupported(
        notificationBuilder: NotificationCompat.Builder,
        payload: BrazeNotificationPayload
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            brazelog {
                "Notification category not supported on this " +
                    "android version. Not setting category for notification."
            }
            return
        }
        val notificationCategory = payload.notificationCategory
        if (notificationCategory != null) {
            brazelog { "Setting category for notification" }
            notificationBuilder.setCategory(notificationCategory)
        } else {
            brazelog { "Category not present in notification extras. Not setting category for notification." }
        }
    }

    /**
     * Set visibility for devices on Lollipop and above.
     *
     * Sphere of visibility of this notification, which affects how and when the SystemUI reveals the notification's presence and
     * contents in untrusted situations (namely, on the secure lockscreen). The default level, VISIBILITY_PRIVATE, behaves exactly
     * as notifications have always done on Android: The notification's icon and tickerText (if available) are shown in all situations,
     * but the contents are only available if the device is unlocked for the appropriate user. A more permissive policy can be expressed
     * by VISIBILITY_PUBLIC; such a notification can be read even in an "insecure" context (that is, above a secure lockscreen).
     * To modify the public version of this notification—for example, to redact some portions—see setPublicVersion(Notification).
     * Finally, a notification can be made VISIBILITY_SECRET, which will suppress its icon and ticker until the user has bypassed the lockscreen.
     *
     * Supported Lollipop+.
     */
    @JvmStatic
    fun setVisibilityIfPresentAndSupported(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            brazelog {
                "Notification visibility not supported on " +
                    "this android version. Not setting visibility for notification."
            }
            return
        }
        val visibility = payload.notificationVisibility
        if (visibility != null) {
            if (isValidNotificationVisibility(visibility)) {
                brazelog { "Setting visibility for notification" }
                notificationBuilder.setVisibility(visibility)
            } else {
                brazelog(W) { "Received invalid notification visibility $visibility" }
            }
        }
    }

    /**
     * Set the public version of the notification for notifications with private visibility.
     *
     * Supported Lollipop+.
     */
    @JvmStatic
    fun setPublicVersionIfPresentAndSupported(notificationBuilder: NotificationCompat.Builder, payload: BrazeNotificationPayload) {
        val context = payload.context
        val appConfigurationProvider = payload.configurationProvider
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            brazelog { "Cannot set public version before Lollipop" }
            return
        }
        if (context == null
            || payload.publicNotificationExtras == null
            || appConfigurationProvider == null
        ) {
            return
        }
        val notificationChannelId = getOrCreateNotificationChannelId(payload)
        val publicNotificationExtras = payload.publicNotificationExtras.parseJsonObjectIntoBundle()
        if (publicNotificationExtras.isEmpty) return

        val publicPayload = BrazeNotificationPayload(
            notificationExtras = publicNotificationExtras,
            context = context,
            configurationProvider = appConfigurationProvider
        )
        val publicNotificationBuilder = NotificationCompat.Builder(context, notificationChannelId)

        brazelog { "Setting public version of notification with payload: $publicPayload" }
        setContentIfPresent(publicNotificationBuilder, publicPayload)
        setTitleIfPresent(publicNotificationBuilder, publicPayload)
        setSummaryTextIfPresentAndSupported(publicNotificationBuilder, publicPayload)
        setSmallIcon(appConfigurationProvider, publicNotificationBuilder)
        setAccentColorIfPresentAndSupported(publicNotificationBuilder, publicPayload)
        notificationBuilder.setPublicVersion(publicNotificationBuilder.build())
    }

    /**
     * Checks whether the given integer value is a valid Android notification visibility constant.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @JvmStatic
    fun isValidNotificationVisibility(visibility: Int): Boolean =
        visibility == Notification.VISIBILITY_SECRET || visibility == Notification.VISIBILITY_PRIVATE || visibility == Notification.VISIBILITY_PUBLIC

    /**
     * Logs a notification click with Braze if the extras passed down
     * indicate that they are from Braze and contain a campaign Id.
     *
     * A Braze session must be active to log a push notification.
     *
     * @param customContentString extra key value pairs in JSON format.
     */
    @JvmStatic
    fun logBaiduNotificationClick(context: Context?, customContentString: String?) {
        if (customContentString == null) {
            brazelog(W) { "customContentString was null. Doing nothing." }
            return
        }
        if (context == null) {
            brazelog(W) { "Cannot log baidu click with null context. Doing nothing." }
            return
        }
        try {
            val jsonExtras = JSONObject(customContentString)
            val source = jsonExtras.getOptionalString(SOURCE_KEY)
            val campaignId = jsonExtras.getOptionalString(Constants.BRAZE_PUSH_CAMPAIGN_ID_KEY)
            if (source != null && source == Constants.BRAZE && campaignId != null) {
                Braze.getInstance(context).logPushNotificationOpened(campaignId)
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Caught an exception processing customContentString: $customContentString" }
        }
    }

    /**
     * Handles a request to cancel a push notification in the notification center. Called
     * by [BrazePushReceiver] when a Braze cancel notification intent is received.
     *
     * Any existing notification in the notification center with the integer Id specified in the
     * "nid" field of the provided intent's extras is cancelled.
     *
     * If no Id is found, the default Braze notification Id is used.
     *
     * @param context
     * @param intent  the cancel notification intent
     */
    @JvmStatic
    fun handleCancelNotificationAction(context: Context, intent: Intent) {
        try {
            if (intent.hasExtra(Constants.BRAZE_PUSH_NOTIFICATION_ID)) {
                val notificationId = intent.getIntExtra(Constants.BRAZE_PUSH_NOTIFICATION_ID, Constants.BRAZE_DEFAULT_NOTIFICATION_ID)
                brazelog { "Cancelling notification action with id: $notificationId" }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(Constants.BRAZE_PUSH_NOTIFICATION_TAG, notificationId)
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Exception occurred handling cancel notification intent." }
        }
    }

    /**
     * Creates a request to cancel a push notification in the notification center.
     *
     * Sends an intent to the [BrazePushReceiver] requesting Braze to cancel the notification with
     * the specified notification Id.
     *
     * See [handleCancelNotificationAction]
     */
    @JvmStatic
    fun cancelNotification(context: Context, notificationId: Int) {
        try {
            brazelog { "Cancelling notification action with id: $notificationId" }
            val cancelNotificationIntent = Intent(Constants.BRAZE_CANCEL_NOTIFICATION_ACTION).setClass(context, notificationReceiverClass)
            cancelNotificationIntent.setPackage(context.packageName)
            cancelNotificationIntent.putExtra(Constants.BRAZE_PUSH_NOTIFICATION_ID, notificationId)
            addComponentAndSendBroadcast(context, cancelNotificationIntent)
        } catch (e: Exception) {
            brazelog(E, e) { "Exception occurred attempting to cancel notification." }
        }
    }

    /**
     * Returns true if the bundle is from a push sent by
     * Braze for uninstall tracking. Uninstall tracking push can be ignored.
     *
     * @param notificationExtras A notificationExtras bundle that is passed
     * with the push received intent when a notification message is
     * received, and that Braze passes in the intent to registered receivers.
     */
    @JvmStatic
    @Deprecated("Please use BrazeNotificationPayload().isUninstallTracking instead")
    fun isUninstallTrackingPush(notificationExtras: Bundle): Boolean {
        try {
            // The ADM case where extras are flattened
            if (notificationExtras.containsKey(Constants.BRAZE_PUSH_UNINSTALL_TRACKING_KEY)) {
                return true
            }
            // The FCM case where extras are in a separate bundle
            val fcmExtras = notificationExtras.getBundle(Constants.BRAZE_PUSH_EXTRAS_KEY)
            if (fcmExtras != null) {
                return fcmExtras.containsKey(Constants.BRAZE_PUSH_UNINSTALL_TRACKING_KEY)
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to determine if push is uninstall tracking. Returning false." }
        }
        return false
    }

    /**
     * Returns the channel id for a valid [NotificationChannel], creating one if necessary.
     *
     * First, if [Constants.BRAZE_PUSH_NOTIFICATION_CHANNEL_ID_KEY] key is present in
     * notificationExtras's and is the id of a valid NotificationChannel, this id will
     * be returned.
     *
     * Next, if the channel with id [Constants.BRAZE_PUSH_DEFAULT_NOTIFICATION_CHANNEL_ID] exists,
     * then [Constants.BRAZE_PUSH_DEFAULT_NOTIFICATION_CHANNEL_ID] will be returned.
     *
     * Finally, if neither of the cases above is true, a channel with id [Constants.BRAZE_PUSH_DEFAULT_NOTIFICATION_CHANNEL_ID]
     * will be created and [Constants.BRAZE_PUSH_DEFAULT_NOTIFICATION_CHANNEL_ID] will be
     * returned.
     */
    @JvmStatic
    fun getOrCreateNotificationChannelId(payload: BrazeNotificationPayload): String {
        val channelIdFromExtras = payload.notificationChannelId
        val defaultChannelId = Constants.BRAZE_PUSH_DEFAULT_NOTIFICATION_CHANNEL_ID
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // If on Android < O, the channel does not really need to exist
            return channelIdFromExtras ?: defaultChannelId
        }
        val context = payload.context
        val config = payload.configurationProvider
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // First try to get the channel from the extras
        if (channelIdFromExtras != null) {
            if (notificationManager.getNotificationChannel(channelIdFromExtras) != null) {
                brazelog { "Found notification channel in extras with id: $channelIdFromExtras" }
                return channelIdFromExtras
            } else {
                brazelog { "Notification channel from extras is invalid. No channel found with id: $channelIdFromExtras" }
            }
        }

        // If we get here, we need to use the default channel
        if (notificationManager.getNotificationChannel(defaultChannelId) == null) {
            // If the default doesn't exist, create it now
            brazelog { "Braze default notification channel does not exist on device. Creating default channel." }
            val channel = NotificationChannel(
                defaultChannelId,
                config?.defaultNotificationChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = config?.defaultNotificationChannelDescription
            notificationManager.createNotificationChannel(channel)
        }
        return defaultChannelId
    }

    /**
     * Sets the notification number, set via [NotificationCompat.Builder.setNumber].
     * On Android O, this number is used with notification badges.
     */
    @JvmStatic
    fun setNotificationBadgeNumberIfPresent(
        notificationBuilder: NotificationCompat.Builder,
        payload: BrazeNotificationPayload
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            brazelog {
                "Notification badge number not supported on this " +
                    "android version. Not setting badge number for notification."
            }
            return
        }
        val notificationBadgeNumber = payload.notificationBadgeNumber
        if (notificationBadgeNumber != null) {
            notificationBuilder.setNumber(notificationBadgeNumber)
        }
    }

    /**
     * Handles a push story page click. Called by [BrazePushReceiver] when an
     * Braze push story click intent is received.
     *
     * @param context Application context.
     * @param intent  The push story click intent.
     */
    @JvmStatic
    fun handlePushStoryPageClicked(context: Context, intent: Intent) {
        try {
            Braze.getInstance(context)
                .logPushStoryPageClicked(
                    intent.getStringExtra(Constants.BRAZE_CAMPAIGN_ID),
                    intent.getStringExtra(Constants.BRAZE_STORY_PAGE_ID)
                )

            val appConfigurationProvider = BrazeConfigurationProvider(context)

            val notificationId = intent.getIntExtra(Constants.BRAZE_PUSH_NOTIFICATION_ID, 0)
            if (appConfigurationProvider.doesPushStoryDismissOnClick && notificationId != 0) {
                cancelNotification(context, notificationId)
            }

            val deepLink = intent.getStringExtra(Constants.BRAZE_ACTION_URI_KEY)
            if (!deepLink.isNullOrBlank()) {
                // Set the global deep link value to the correct action's deep link.
                intent.putExtra(Constants.BRAZE_PUSH_DEEP_LINK_KEY, intent.getStringExtra(Constants.BRAZE_ACTION_URI_KEY))
                val useWebviewString = intent.getStringExtra(Constants.BRAZE_ACTION_USE_WEBVIEW_KEY)
                if (!useWebviewString.isNullOrBlank()) {
                    intent.putExtra(Constants.BRAZE_PUSH_OPEN_URI_IN_WEBVIEW_KEY, useWebviewString)
                }
            } else {
                // Otherwise, remove any existing deep links.
                intent.removeExtra(Constants.BRAZE_PUSH_DEEP_LINK_KEY)
            }
            sendNotificationOpenedBroadcast(context, intent)

            if (appConfigurationProvider.doesHandlePushDeepLinksAutomatically) {
                routeUserWithNotificationOpenedIntent(context, intent)
            } else {
                brazelog(I) { "Not handling deep links automatically, skipping deep link handling for '$deepLink'" }
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Caught exception while handling story click." }
        }
    }

    /**
     * Parses the notification bundle for any associated
     * ContentCards, if present. If found, the card object
     * is added to card storage.
     */
    @JvmStatic
    fun handleContentCardsSerializedCardIfPresent(payload: BrazeNotificationPayload) {
        val contentCardData = payload.contentCardSyncData
        val contentCardDataUserId = payload.contentCardSyncUserId
        val context = payload.context
        if (contentCardData != null && context != null) {
            brazelog { "Push contains associated Content Cards card. User id: $contentCardDataUserId Card data: $contentCardData" }
            addSerializedContentCardToStorage(context, contentCardData, contentCardDataUserId)
        }
    }

    /**
     * Sends a push notification opened broadcast to the client broadcast receiver.
     *
     * @param context Application context
     * @param intent  The internal notification clicked intent constructed in
     * [setContentIntentIfPresent]
     */
    @JvmStatic
    fun sendNotificationOpenedBroadcast(context: Context, intent: Intent) {
        brazelog { "Sending notification opened broadcast" }
        val notificationExtras = intent.extras
        if (notificationExtras != null) {
            val notificationPayload = BrazeNotificationPayload(notificationExtras, context = context)
            sendPushActionIntent(context, BrazeNotificationBroadcastType.OPENED, notificationExtras, notificationPayload)
        } else {
            sendPushActionIntent(context, BrazeNotificationBroadcastType.OPENED, notificationExtras)
        }
    }

    /**
     * Returns an existing notification channel. The notification extras are first checked for
     * a notification channel that exists. If not, then the default
     * Braze notification channel is returned if it exists. If neither
     * exist on the device, then null is returned.
     *
     * This method does not create a notification channel if a valid channel cannot be found.
     *
     * @param notificationExtras The extras that will be checked for a valid notification channel id.
     * @return A already created notification channel on the device, or null if one cannot be found.
     */
    @TargetApi(Build.VERSION_CODES.O)
    @JvmStatic
    fun getValidNotificationChannel(notificationManager: NotificationManager, notificationExtras: Bundle?): NotificationChannel? {
        if (notificationExtras == null) {
            brazelog { "Notification extras bundle was null. Could not find a valid notification channel" }
            return null
        }
        val channelIdFromExtras = notificationExtras.getString(Constants.BRAZE_PUSH_NOTIFICATION_CHANNEL_ID_KEY, null)
        if (!channelIdFromExtras.isNullOrBlank()) {
            val notificationChannel = notificationManager.getNotificationChannel(channelIdFromExtras)
            if (notificationChannel != null) {
                brazelog { "Found notification channel in extras with id: $channelIdFromExtras" }
                return notificationChannel
            } else {
                brazelog { "Notification channel from extras is invalid, no channel found with id: $channelIdFromExtras" }
            }
        }
        val defaultNotificationChannel = notificationManager.getNotificationChannel(Constants.BRAZE_PUSH_DEFAULT_NOTIFICATION_CHANNEL_ID)
        if (defaultNotificationChannel != null) {
            return defaultNotificationChannel
        } else {
            brazelog { "Braze default notification channel does not exist on device." }
        }
        return null
    }

    /**
     * Creates a [PendingIntent] using the given action and extras specified.
     *
     * @param context            Application context
     * @param action             The action to set for the [PendingIntent]
     * @param notificationExtras The extras to set for the [PendingIntent], if not null
     */
    private fun getPushActionPendingIntent(
        context: Context,
        @Suppress("SameParameterValue") action: String,
        notificationExtras: Bundle?
    ): PendingIntent {
        val pushActionIntent = Intent(action).setClass(context, NotificationTrampolineActivity::class.java)
        if (notificationExtras != null) {
            pushActionIntent.putExtras(notificationExtras)
        }
        val flags = PendingIntent.FLAG_ONE_SHOT or getImmutablePendingIntentFlags()
        return PendingIntent.getActivity(context, getRequestCode(), pushActionIntent, flags)
    }

    /**
     * Broadcasts an intent with the given action suffix. Will copy the extras from the input intent.
     *
     * @param context            Application context.
     * @param notificationExtras The extras to attach to the intent.
     * @param payload The notification payload, may be null. If present, will lead to a callback firing for [IBraze.subscribeToPushNotificationEvents]
     */
    private fun sendPushActionIntent(
        context: Context,
        broadcastType: BrazeNotificationBroadcastType,
        notificationExtras: Bundle?,
        payload: BrazeNotificationPayload? = null
    ) {
        // This is the current intent whose action does
        // not require a prefix of the app package name
        val brazePushIntent: Intent = when (broadcastType) {
            BrazeNotificationBroadcastType.OPENED -> {
                Intent(Constants.BRAZE_PUSH_INTENT_NOTIFICATION_OPENED).setPackage(context.packageName)
            }
            BrazeNotificationBroadcastType.RECEIVED -> {
                Intent(Constants.BRAZE_PUSH_INTENT_NOTIFICATION_RECEIVED).setPackage(context.packageName)
            }
            BrazeNotificationBroadcastType.DELETED -> {
                Intent(Constants.BRAZE_PUSH_INTENT_NOTIFICATION_DELETED).setPackage(context.packageName)
            }
        }
        brazelog(V) { "Sending Braze broadcast receiver intent for $broadcastType" }
        sendPushActionIntent(context, brazePushIntent, notificationExtras)

        if (payload != null) {
            // Send this event to the SDK for publishing
            BrazeInternal.publishBrazePushAction(context, broadcastType.brazePushEventType, payload)
        }
    }

    private fun sendPushActionIntent(context: Context, pushIntent: Intent, notificationExtras: Bundle?) {
        brazelog(V) { "Sending push action intent: $pushIntent" }
        if (notificationExtras != null) {
            pushIntent.putExtras(notificationExtras)
        }
        addComponentAndSendBroadcast(context, pushIntent)
    }
}
