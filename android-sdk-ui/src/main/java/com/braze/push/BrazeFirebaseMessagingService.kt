package com.braze.push

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.appboy.BrazeInternal.applyPendingRuntimeConfiguration
import com.braze.Constants
import com.braze.Braze
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class BrazeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        applyPendingRuntimeConfiguration(this)
        val configurationProvider = BrazeConfigurationProvider(this)
        if (Braze.getConfiguredApiKey(configurationProvider).isNullOrEmpty()) {
            brazelog(V) { "No configured API key, not registering token in onNewToken. Token: $newToken" }
            return
        }
        if (!configurationProvider.isFirebaseMessagingServiceOnNewTokenRegistrationEnabled) {
            brazelog(V) {
                "Automatic FirebaseMessagingService.OnNewToken() registration" +
                    " disabled, not registering token: $newToken"
            }
            return
        }
        brazelog(V) { "Registering Firebase push token in onNewToken. Token: $newToken" }
        Braze.getInstance(this).registerPushToken(newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleBrazeRemoteMessage(this, remoteMessage)
    }

    companion object {
        /**
         * Consumes an incoming [RemoteMessage] if it originated from Braze. If the [RemoteMessage] did
         * not originate from Braze, then this method does nothing and returns false.
         *
         * @param remoteMessage The [RemoteMessage] from Firebase.
         * @return true iff the [RemoteMessage] originated from Braze and was consumed. Returns false
         * if the [RemoteMessage] did not originate from Braze or otherwise could not be forwarded.
         */
        @JvmStatic
        fun handleBrazeRemoteMessage(context: Context, remoteMessage: RemoteMessage): Boolean {
            if (!isBrazePushNotification(remoteMessage)) {
                brazelog(I) { "Remote message did not originate from Braze. Not consuming remote message: $remoteMessage" }
                return false
            }
            val remoteMessageData = remoteMessage.data
            brazelog(I) { "Got remote message from FCM: $remoteMessageData" }
            val pushIntent = Intent(BrazePushReceiver.FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION)
            val bundle = Bundle()
            for ((key, value) in remoteMessageData) {
                brazelog(V) { "Adding bundle item from FCM remote data with key: $key and value: $value" }
                bundle.putString(key, value)
            }
            pushIntent.putExtras(bundle)
            BrazePushReceiver.handleReceivedIntent(context, pushIntent)
            return true
        }

        /**
         * Determines if the Firebase [RemoteMessage] originated from Braze and should be
         * forwarded to [BrazeFirebaseMessagingService.handleBrazeRemoteMessage].
         *
         * @param remoteMessage The [RemoteMessage] from [FirebaseMessagingService.onMessageReceived]
         * @return true iff this [RemoteMessage] originated from Braze or otherwise
         * should be passed to [BrazeFirebaseMessagingService.handleBrazeRemoteMessage].
         */
        @JvmStatic
        fun isBrazePushNotification(remoteMessage: RemoteMessage): Boolean {
            val remoteMessageData = remoteMessage.data
            return "true" == remoteMessageData[Constants.BRAZE_PUSH_BRAZE_KEY]
        }
    }
}
