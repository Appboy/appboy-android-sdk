package com.appboy.unity.utils

import android.os.Bundle
import com.braze.events.FeedUpdatedEvent
import com.braze.events.BrazePushEvent
import com.braze.events.BrazeSdkAuthenticationErrorEvent
import com.braze.events.ContentCardsUpdatedEvent
import com.braze.models.inappmessage.IInAppMessage
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag
import com.braze.support.constructJsonArray
import com.unity3d.player.UnityPlayer
import org.json.JSONObject

object MessagingUtils {
    private val TAG = getBrazeLogTag(MessagingUtils::class.java)
    private const val BRAZE_INTERNAL_GAME_OBJECT = "BrazeInternalComponent"

    enum class BrazeInternalComponentMethod(val methodName: String) {
        BEFORE_IAM_DISPLAYED("beforeInAppMessageDisplayed"),
        ON_IAM_DISMISSED("onInAppMessageDismissed"),
        ON_IAM_CLICKED("onInAppMessageClicked"),
        ON_IAM_BUTTON_CLICKED("onInAppMessageButtonClicked"),
        ON_IAM_HTML_CLICKED("onInAppMessageHTMLClicked");
    }

    fun sendInAppMessageReceivedMessage(
        unityGameObjectName: String?,
        unityCallbackFunctionName: String?,
        inAppMessage: IInAppMessage
    ): Boolean {
        if (unityGameObjectName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity GameObject configured to receive" +
                    " in app messages. Not sending the message to Unity."
            }
            return false
        }
        if (unityCallbackFunctionName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity callback method name registered to receive in app messages in " +
                    "the braze.xml configuration file. Not sending the message to Unity."
            }
            return false
        }
        brazelog(TAG) { "Sending a message to $unityGameObjectName:$unityCallbackFunctionName." }
        UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, inAppMessage.forJsonPut().toString())
        return true
    }

    fun sendPushEventToUnity(
        unityGameObjectName: String?,
        unityCallbackFunctionName: String?,
        event: BrazePushEvent
    ): Boolean {
        if (unityGameObjectName.isNullOrBlank()) {
            brazelog(TAG) {
                "No Unity game object configured to " +
                    "receive ${event.eventType} messages. Not sending the message to Unity."
            }
            return false
        }
        if (unityCallbackFunctionName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity callback method name registered to receive ${event.eventType} messages " +
                    "in the braze.xml configuration file. Not sending the message to Unity."
            }
            return false
        }
        brazelog(TAG) { "Sending a ${event.eventType} message to $unityGameObjectName:$unityCallbackFunctionName." }
        UnityPlayer.UnitySendMessage(
            unityGameObjectName,
            unityCallbackFunctionName,
            getPushBundleExtras(event.notificationPayload.notificationExtras).toString()
        )
        return true
    }

    fun sendFeedUpdatedEventToUnity(
        unityGameObjectName: String?,
        unityCallbackFunctionName: String?,
        feedUpdatedEvent: FeedUpdatedEvent
    ): Boolean {
        if (unityGameObjectName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity GameObject registered in the braze.xml configuration " +
                    "file to receive feed updates. Not sending the message to Unity."
            }
            return false
        }
        if (unityCallbackFunctionName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity callback method name registered to receive feed updates in " +
                    "the braze.xml configuration file. Not sending the message to Unity."
            }
            return false
        }
        val json = JSONObject()
            .put("mFeedCards", feedUpdatedEvent.feedCards.constructJsonArray())
            .put("mFromOfflineStorage", feedUpdatedEvent.isFromOfflineStorage)
        brazelog(TAG) { "Sending a feed updated event message to $unityGameObjectName:$unityCallbackFunctionName." }
        UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, json.toString())
        return true
    }

    fun sendContentCardsUpdatedEventToUnity(
        unityGameObjectName: String?,
        unityCallbackFunctionName: String?,
        contentCardsUpdatedEvent: ContentCardsUpdatedEvent
    ): Boolean {
        if (unityGameObjectName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity GameObject configured " +
                    "to receive Content Cards updated event messages. Not sending the message to Unity."
            }
            return false
        }
        if (unityCallbackFunctionName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity callback method name registered to receive Content " +
                    "Cards updated event messages in the braze.xml configuration file. Not sending the message to Unity."
            }
            return false
        }
        val json = JSONObject()
            .put("mContentCards", contentCardsUpdatedEvent.allCards.constructJsonArray())
            .put("mFromOfflineStorage", contentCardsUpdatedEvent.isFromOfflineStorage)
        brazelog(TAG) { "Sending a Content Cards update message to $unityGameObjectName:$unityCallbackFunctionName." }
        UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, json.toString())
        return true
    }

    fun sendSdkAuthErrorEventToUnity(
        unityGameObjectName: String?,
        unityCallbackFunctionName: String?,
        sdkAuthError: BrazeSdkAuthenticationErrorEvent
    ): Boolean {
        if (unityGameObjectName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity GameObject configured " +
                    "to receive SDK Authentication Failure event messages. Not sending the message to Unity."
            }
            return false
        }
        if (unityCallbackFunctionName.isNullOrBlank()) {
            brazelog(TAG) {
                "There is no Unity callback method name registered to receive " +
                    "SDK Authentication Failure event messages in the braze.xml configuration file. Not sending the message to Unity."
            }
            return false
        }
        val json = JSONObject()
            .put("code", sdkAuthError.errorCode)
            .put("reason", sdkAuthError.errorReason)
            .put("userId", sdkAuthError.userId)
            .put("signature", sdkAuthError.signature)

        brazelog(TAG) { "Sending an SDK Authentication Failure message to $unityGameObjectName:$unityCallbackFunctionName." }
        UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, json.toString())
        return true
    }

    /**
     * Sends some structured data to the BrazeInternalComponent in C# in the Unity binding.
     */
    fun sendToBrazeInternalComponent(method: BrazeInternalComponentMethod, json: String) {
        UnityPlayer.UnitySendMessage(BRAZE_INTERNAL_GAME_OBJECT, method.methodName, json)
    }

    /**
     * De-serializes a bundle into a key value pair that can be represented as a [JSONObject].
     * Nested bundles are also converted recursively to have a single hierarchical structure.
     *
     * @param pushExtras The bundle received whenever a push notification is received, opened or deleted.
     * @return A [JSONObject] that represents this bundle in string format.
     */
    internal fun getPushBundleExtras(pushExtras: Bundle?): JSONObject {
        val json = JSONObject()
        if (pushExtras == null) {
            return json
        }
        for (key in pushExtras.keySet()) {
            @Suppress("DEPRECATION")
            pushExtras[key]?.let {
                if (it is Bundle) {
                    json.put(key, getPushBundleExtras(it).toString())
                } else {
                    json.put(key, it.toString())
                }
            }
        }
        return json
    }
}
