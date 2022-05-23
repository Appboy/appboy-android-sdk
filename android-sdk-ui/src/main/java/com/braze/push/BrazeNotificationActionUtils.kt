package com.braze.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.braze.Constants
import com.appboy.models.push.BrazeNotificationPayload
import com.appboy.models.push.BrazeNotificationPayload.ActionButton
import com.braze.Braze
import com.braze.IBrazeDeeplinkHandler.IntentFlagPurpose
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.push.BrazeNotificationUtils.cancelNotification
import com.braze.push.BrazeNotificationUtils.notificationReceiverClass
import com.braze.push.BrazeNotificationUtils.routeUserWithNotificationOpenedIntent
import com.braze.push.BrazeNotificationUtils.sendNotificationOpenedBroadcast
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.IntentUtils.getImmutablePendingIntentFlags
import com.braze.support.IntentUtils.getRequestCode
import com.braze.ui.BrazeDeeplinkHandler.Companion.getInstance

object BrazeNotificationActionUtils {
    /**
     * Add notification actions to the provided notification builder
     */
    @JvmStatic
    fun addNotificationActions(
        notificationBuilder: NotificationCompat.Builder,
        payload: BrazeNotificationPayload
    ) {
        if (payload.context == null) {
            brazelog { "Context cannot be null when adding notification buttons." }
            return
        }
        val actionButtons = payload.actionButtons
        if (actionButtons.isEmpty()) {
            brazelog { "No action buttons present. Not adding notification actions" }
            return
        }
        for (actionButton in actionButtons) {
            brazelog(V) { "Adding action button: $actionButton" }
            addNotificationAction(notificationBuilder, payload, actionButton)
        }
    }

    /**
     * Handles clicks on notification action buttons in the notification center. Called by FCM/ADM
     * receiver when an Braze notification action button is clicked. The FCM/ADM receiver passes on
     * the intent from the notification action button click intent.
     *
     * @param context [Context]
     * @param intent the action button click intent
     */
    @JvmStatic
    @Suppress("NestedBlockDepth")
    fun handleNotificationActionClicked(context: Context, intent: Intent) {
        try {
            val actionType = intent.getStringExtra(Constants.BRAZE_ACTION_TYPE_KEY)
            if (actionType.isNullOrBlank()) {
                brazelog(W) { "Notification action button type was blank or null. Doing nothing." }
                return
            }
            val notificationId = intent.getIntExtra(
                Constants.BRAZE_PUSH_NOTIFICATION_ID,
                Constants.BRAZE_DEFAULT_NOTIFICATION_ID
            )

            // Logs that the notification action was clicked.
            // Click analytics for all action types are logged.
            logNotificationActionClicked(context, intent, actionType)
            if (actionType == Constants.BRAZE_PUSH_ACTION_TYPE_URI || actionType == Constants.BRAZE_PUSH_ACTION_TYPE_OPEN) {
                cancelNotification(context, notificationId)
                if (actionType == Constants.BRAZE_PUSH_ACTION_TYPE_URI &&
                    intent.extras?.containsKey(Constants.BRAZE_ACTION_URI_KEY) == true
                ) {
                    // Set the deep link that to open to the correct action's deep link.
                    intent.putExtra(
                        Constants.BRAZE_PUSH_DEEP_LINK_KEY,
                        intent.getStringExtra(Constants.BRAZE_ACTION_URI_KEY)
                    )
                    if (intent.extras?.containsKey(Constants.BRAZE_ACTION_USE_WEBVIEW_KEY) == true) {
                        intent.putExtra(
                            Constants.BRAZE_PUSH_OPEN_URI_IN_WEBVIEW_KEY,
                            intent.getStringExtra(Constants.BRAZE_ACTION_USE_WEBVIEW_KEY)
                        )
                    }
                } else {
                    // Otherwise, remove any existing deep links.
                    intent.removeExtra(Constants.BRAZE_PUSH_DEEP_LINK_KEY)
                }
                sendNotificationOpenedBroadcast(context, intent)
                val appConfigurationProvider = BrazeConfigurationProvider(context)
                if (appConfigurationProvider.doesHandlePushDeepLinksAutomatically) {
                    routeUserWithNotificationOpenedIntent(context, intent)
                } else {
                    brazelog(I) { "Not handling deep links automatically, skipping deep link handling" }
                }
            } else if (actionType == Constants.BRAZE_PUSH_ACTION_TYPE_NONE) {
                cancelNotification(context, notificationId)
            } else {
                brazelog(W) { "Unknown notification action button clicked. Doing nothing." }
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Caught exception while handling notification action button click." }
        }
    }

    /**
     * Add the notification action at a specified index to the notification builder.
     *
     * @param notificationBuilder
     * @param payload
     * @param actionButton that is to be added at actionButton.actionIndex
     */
    fun addNotificationAction(
        notificationBuilder: NotificationCompat.Builder,
        payload: BrazeNotificationPayload,
        actionButton: ActionButton
    ) {
        val context = payload.context
        if (context == null) {
            brazelog { "Cannot add notification action with null context from payload" }
            return
        }
        val actionExtras = Bundle(payload.notificationExtras)
        actionButton.putIntoBundle(actionExtras)
        val actionType = actionButton.type

        val pendingSendIntent: PendingIntent
        val sendIntent: Intent
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or getImmutablePendingIntentFlags()
        if (Constants.BRAZE_PUSH_ACTION_TYPE_NONE == actionType) {
            // If no action is present, then we don't need the
            // trampoline to route us back to an Activity.
            brazelog(V) {
                "Adding notification action with type: $actionType" +
                    "Setting intent class to notification receiver: $notificationReceiverClass"
            }
            sendIntent = Intent(Constants.BRAZE_ACTION_CLICKED_ACTION).setClass(
                context,
                notificationReceiverClass
            )
            sendIntent.putExtras(actionExtras)
            pendingSendIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(),
                sendIntent,
                pendingIntentFlags
            )
        } else {
            // However, if an action is present, then we need to
            // route to the trampoline to ensure the user is
            // prompted to open the app on the lockscreen.
            brazelog(V) { "Adding notification action with type: $actionType Setting intent class to trampoline activity" }
            sendIntent = Intent(Constants.BRAZE_ACTION_CLICKED_ACTION)
                .setClass(context, NotificationTrampolineActivity::class.java)
            sendIntent.flags =
                sendIntent.flags or getInstance().getIntentFlags(IntentFlagPurpose.NOTIFICATION_ACTION_WITH_DEEPLINK)
            sendIntent.putExtras(actionExtras)
            pendingSendIntent = PendingIntent.getActivity(
                context,
                getRequestCode(),
                sendIntent,
                pendingIntentFlags
            )
        }
        val notificationActionBuilder =
            NotificationCompat.Action.Builder(0, actionButton.text, pendingSendIntent)
        notificationActionBuilder.addExtras(Bundle(actionExtras))
        notificationBuilder.addAction(notificationActionBuilder.build())
        brazelog(V) { "Added action with bundle: $actionExtras" }
    }

    /**
     * Log an action button clicked event.
     *
     * @param context
     * @param intent the action button click intent
     */
    fun logNotificationActionClicked(context: Context, intent: Intent, actionType: String?) {
        val campaignId = intent.getStringExtra(Constants.BRAZE_PUSH_CAMPAIGN_ID_KEY)
        val actionButtonId = intent.getStringExtra(Constants.BRAZE_ACTION_ID_KEY)
        Braze.getInstance(context)
            .logPushNotificationActionClicked(campaignId, actionButtonId, actionType)
    }
}
