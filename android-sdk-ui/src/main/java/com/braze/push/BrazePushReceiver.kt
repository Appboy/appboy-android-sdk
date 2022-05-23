package com.braze.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationManagerCompat
import com.appboy.BrazeInternal.applyPendingRuntimeConfiguration
import com.appboy.BrazeInternal.handleInAppMessageTestPush
import com.braze.Constants
import com.braze.Constants.isAmazonDevice
import com.appboy.models.push.BrazeNotificationPayload
import com.appboy.models.push.BrazeNotificationPayload.Companion.getAttachedBrazeExtras
import com.braze.Braze
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.coroutine.BrazeCoroutineScope
import com.braze.push.BrazeNotificationActionUtils.handleNotificationActionClicked
import com.braze.push.BrazeNotificationUtils.activeNotificationFactory
import com.braze.push.BrazeNotificationUtils.getNotificationId
import com.braze.push.BrazeNotificationUtils.handleCancelNotificationAction
import com.braze.push.BrazeNotificationUtils.handleContentCardsSerializedCardIfPresent
import com.braze.push.BrazeNotificationUtils.handleNotificationDeleted
import com.braze.push.BrazeNotificationUtils.handleNotificationOpened
import com.braze.push.BrazeNotificationUtils.handlePushStoryPageClicked
import com.braze.push.BrazeNotificationUtils.isBrazePushMessage
import com.braze.push.BrazeNotificationUtils.isNotificationMessage
import com.braze.push.BrazeNotificationUtils.isUninstallTrackingPush
import com.braze.push.BrazeNotificationUtils.requestGeofenceRefreshIfAppropriate
import com.braze.push.BrazeNotificationUtils.sendPushMessageReceivedBroadcast
import com.braze.push.BrazeNotificationUtils.setNotificationDurationAlarm
import com.braze.push.BrazeNotificationUtils.wakeScreenIfAppropriate
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import kotlinx.coroutines.launch

open class BrazePushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        handleReceivedIntent(context, intent)
    }

    companion object {
        // ADM keys match FCM for these fields.
        private const val MESSAGE_TYPE_KEY = "message_type"
        private const val DELETED_MESSAGES_KEY = "deleted_messages"
        private const val NUMBER_OF_MESSAGES_DELETED_KEY = "total_deleted"
        private const val ADM_RECEIVE_INTENT_ACTION = "com.amazon.device.messaging.intent.RECEIVE"
        private const val ADM_REGISTRATION_INTENT_ACTION =
            "com.amazon.device.messaging.intent.REGISTRATION"
        private const val ADM_ERROR_KEY = "error"
        private const val ADM_ERROR_DESCRIPTION_KEY = "error_description"
        private const val ADM_REGISTRATION_ID_KEY = "registration_id"
        private const val ADM_UNREGISTERED_KEY = "unregistered"

        /**
         * Internal API. Do not use.
         */
        const val FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION =
            "firebase_messaging_service_routing_action"

        /**
         * Internal API. Do not use.
         */
        const val HMS_PUSH_SERVICE_ROUTING_ACTION = "hms_push_service_routing_action"

        private fun handlePush(
            context: Context,
            intent: Intent
        ) {
            val action: String? = intent.action

            fun performWork() {
                brazelog(I) { "Received broadcast message. Message: $intent" }
                if (action.isNullOrEmpty()) {
                    brazelog(W) { "Push action is null. Not handling intent: $intent" }
                    return
                }
                applyPendingRuntimeConfiguration(context)
                when (action) {
                    FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION,
                    Constants.BRAZE_STORY_TRAVERSE_CLICKED_ACTION,
                    HMS_PUSH_SERVICE_ROUTING_ACTION,
                    ADM_RECEIVE_INTENT_ACTION -> handlePushNotificationPayload(
                        context,
                        intent
                    )
                    ADM_REGISTRATION_INTENT_ACTION -> handleAdmRegistrationEventIfEnabled(
                        BrazeConfigurationProvider(context),
                        context,
                        intent
                    )
                    Constants.BRAZE_CANCEL_NOTIFICATION_ACTION -> handleCancelNotificationAction(
                        context,
                        intent
                    )
                    Constants.BRAZE_ACTION_CLICKED_ACTION -> handleNotificationActionClicked(
                        context,
                        intent
                    )
                    Constants.BRAZE_STORY_CLICKED_ACTION -> handlePushStoryPageClicked(
                        context,
                        intent
                    )
                    Constants.BRAZE_PUSH_CLICKED_ACTION -> handleNotificationOpened(
                        context,
                        intent
                    )
                    Constants.BRAZE_PUSH_DELETED_ACTION -> handleNotificationDeleted(
                        context,
                        intent
                    )
                    else -> brazelog(W) { "Received a message not sent from Braze. Ignoring the message." }
                }
            }

            try {
                performWork()
            } catch (e: Exception) {
                brazelog(E, e) {
                    "Caught exception while performing the push notification handling work. " +
                        "Action: $action Intent: $intent"
                }
            }
        }

        @JvmStatic
        @JvmOverloads
        fun handleReceivedIntent(context: Context, intent: Intent, runOnThread: Boolean = true) {
            if (runOnThread) {
                // Don't pass an Activity context into a background thread
                BrazeCoroutineScope.launch {
                    handlePush(context.applicationContext, intent)
                }
            } else {
                // Run on the caller thread
                handlePush(context, intent)
            }
        }

        @JvmStatic
        @VisibleForTesting
        fun handleAdmRegistrationEventIfEnabled(
            appConfigurationProvider: BrazeConfigurationProvider,
            context: Context,
            intent: Intent
        ): Boolean {
            brazelog(I) { "Received ADM registration. Message: $intent" }
            // Only handle ADM registration events if ADM registration handling is turned on in the
            // configuration file.
            if (isAmazonDevice && appConfigurationProvider.isAdmMessagingRegistrationEnabled) {
                brazelog { "ADM enabled in braze.xml. Continuing to process ADM registration intent." }
                handleAdmRegistrationIntent(context, intent)
                return true
            }
            brazelog(W) {
                "ADM not enabled in braze.xml. Ignoring ADM registration intent. Note: you must set " +
                    "com_braze_push_adm_messaging_registration_enabled to true in your braze.xml to enable ADM."
            }
            return false
        }

        /**
         * Processes the registration/unregistration result returned from the ADM servers. If the
         * registration/unregistration is successful, this will store/clear the registration ID from the
         * device. Otherwise, it will log an error message and the device will not be able to receive ADM
         * messages.
         */
        @JvmStatic
        @VisibleForTesting
        fun handleAdmRegistrationIntent(context: Context, intent: Intent): Boolean {
            val error = intent.getStringExtra(ADM_ERROR_KEY)
            val errorDescription = intent.getStringExtra(ADM_ERROR_DESCRIPTION_KEY)
            val registrationId = intent.getStringExtra(ADM_REGISTRATION_ID_KEY)
            val unregistered = intent.getStringExtra(ADM_UNREGISTERED_KEY)
            when {
                error != null -> {
                    brazelog(W) { "Error during ADM registration: $error description: $errorDescription" }
                }
                registrationId != null -> {
                    brazelog(I) { "Registering for ADM messages with registrationId: $registrationId" }
                    Braze.getInstance(context).registerPushToken(registrationId)
                }
                unregistered != null -> {
                    brazelog(W) { "The device was un-registered from ADM: $unregistered" }
                }
                else -> {
                    brazelog(W) {
                        "The ADM registration intent is missing error information, registration id, and unregistration " +
                            "confirmation. Ignoring."
                    }
                    return false
                }
            }
            return true
        }

        @JvmStatic
        @VisibleForTesting
        @Suppress("LongMethod", "ComplexMethod", "ReturnCount")
        fun handlePushNotificationPayload(context: Context, intent: Intent): Boolean {
            val notificationManager = NotificationManagerCompat.from(context)

            if (notificationManager.areNotificationsEnabled()) {
                brazelog(I) { "Notifications enabled" }
            } else {
                brazelog(I) { "Notifications disabled" }
            }

            fun handleDeletedMessage() {
                val totalDeleted = intent.getIntExtra(NUMBER_OF_MESSAGES_DELETED_KEY, -1)
                if (totalDeleted == -1) {
                    brazelog(W) { "Unable to parse FCM message. Intent: $intent" }
                } else {
                    brazelog(I) { "FCM deleted $totalDeleted messages. Fetch them from Braze." }
                }
            }

            val isNotificationMessage = isNotificationMessage(intent)

            /**
             * Determine if the intent should be handled. It must meet the following criteria:
             * 1. It's a Braze push message
             * 2. If we're on Android N or higher, push is enabled or it's a silent push message
             * 3. It's not a deletion message
             */
            fun isValid(): Boolean {
                return when {
                    !intent.isBrazePushMessage() -> {
                        brazelog { "Not handling non-Braze push message." }
                        false
                    }

                    // If we're on N or above, notifications are disabled, and it's a notification
                    // message (as opposed to a silent push)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                        !notificationManager.areNotificationsEnabled() &&
                        isNotificationMessage -> {
                        brazelog(I) { "Push notifications are not enabled. Cannot display push notification." }
                        false
                    }
                    DELETED_MESSAGES_KEY == intent.getStringExtra(MESSAGE_TYPE_KEY) -> {
                        handleDeletedMessage()
                        false
                    }
                    else -> true
                }
            }

            if (!isValid()) {
                brazelog { "Notification isn't valid. Skipping." }
                return false
            }

            // Since isBrazePushMessage returned true, extras is there. This just keeps the compiler happy.
            val notificationExtras = intent.extras ?: return false

            brazelog(I) { "Push message payload received: $notificationExtras" }

            // Convert the JSON in the extras key into a Bundle.
            val brazeExtras = getAttachedBrazeExtras(notificationExtras)
            notificationExtras.putBundle(Constants.BRAZE_PUSH_EXTRAS_KEY, brazeExtras)
            if (!notificationExtras.containsKey(Constants.BRAZE_PUSH_RECEIVED_TIMESTAMP_MILLIS)) {
                notificationExtras.putLong(
                    Constants.BRAZE_PUSH_RECEIVED_TIMESTAMP_MILLIS,
                    System.currentTimeMillis()
                )
            }

            // This call must occur after the "extras" parsing above since we're expecting
            // a bundle instead of a raw JSON string for the APPBOY_PUSH_EXTRAS_KEY key
            if (isUninstallTrackingPush(notificationExtras)) {
                // Note that this re-implementation of this method does not forward the notification to receivers.
                brazelog(I) {
                    "Push message is uninstall tracking push. Doing nothing. Not forwarding this " +
                        "notification to broadcast receivers."
                }
                return false
            }

            val appConfigurationProvider = BrazeConfigurationProvider(context)
            val payload = createPayload(context, appConfigurationProvider, notificationExtras, brazeExtras)
            if (appConfigurationProvider.isInAppMessageTestPushEagerDisplayEnabled
                && payload.shouldFetchTestTriggers
                && BrazeInAppMessageManager.getInstance().activity != null
            ) {
                // Pass this test in-app message along for eager display and bypass displaying a push
                brazelog {
                    "Bypassing push display due to test in-app message presence and eager test " +
                        "in-app message display configuration setting."
                }
                handleInAppMessageTestPush(context, intent)
                return false
            }

            // Parse the notification for any associated ContentCard
            handleContentCardsSerializedCardIfPresent(payload)
            if (isNotificationMessage) {
                brazelog { "Received notification push" }
                val notificationId = getNotificationId(payload)
                notificationExtras.putInt(Constants.BRAZE_PUSH_NOTIFICATION_ID, notificationId)
                if (payload.isPushStory) {
                    if (isAmazonDevice) {
                        brazelog { "Push stories not supported on Amazon devices." }
                        // In case the backend does send these, handle them gracefully
                        return false
                    }
                    if (!notificationExtras.containsKey(Constants.BRAZE_PUSH_STORY_IS_NEWLY_RECEIVED)) {
                        brazelog { "Received the initial Push Story notification." }
                        notificationExtras.putBoolean(
                            Constants.BRAZE_PUSH_STORY_IS_NEWLY_RECEIVED,
                            true
                        )
                    }
                }

                brazelog(V) { "Creating notification with payload:\n$payload" }
                val notification = activeNotificationFactory.createNotification(payload)
                if (notification == null) {
                    brazelog { "Notification created by notification factory was null. Not displaying notification." }
                    return false
                }
                notificationManager.notify(
                    Constants.BRAZE_PUSH_NOTIFICATION_TAG,
                    notificationId,
                    notification
                )
                sendPushMessageReceivedBroadcast(context, notificationExtras)
                wakeScreenIfAppropriate(context, appConfigurationProvider, notificationExtras)

                // Set a custom duration for this notification.
                payload.pushDuration?.let { duration ->
                    setNotificationDurationAlarm(
                        context,
                        BrazePushReceiver::class.java,
                        notificationId,
                        duration
                    )
                }
                return true
            } else {
                brazelog { "Received silent push" }
                sendPushMessageReceivedBroadcast(context, notificationExtras)
                requestGeofenceRefreshIfAppropriate(payload)
                return false
            }
        }

        @JvmStatic
        @VisibleForTesting
        fun createPayload(
            context: Context,
            appConfigurationProvider: BrazeConfigurationProvider,
            notificationExtras: Bundle,
            brazeExtras: Bundle
        ): BrazeNotificationPayload {
            // ADM uses a different constructor here because the data is already flattened.
            return if (isAmazonDevice) {
                BrazeNotificationPayload(
                    notificationExtras,
                    getAttachedBrazeExtras(
                        notificationExtras
                    ),
                    context, appConfigurationProvider
                )
            } else {
                BrazeNotificationPayload(
                    notificationExtras,
                    brazeExtras,
                    context,
                    appConfigurationProvider
                )
            }
        }
    }
}
