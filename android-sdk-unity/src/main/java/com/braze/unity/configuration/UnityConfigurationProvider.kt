package com.braze.unity.configuration

import android.content.Context
import com.braze.unity.enums.UnityMessageType
import com.braze.configuration.CachedConfigurationProvider
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.InAppMessageOperation
import com.braze.ui.inappmessage.InAppMessageOperation.Companion.fromValue

class UnityConfigurationProvider(context: Context) : CachedConfigurationProvider(
    context, false
) {
    val inAppMessageListenerGameObjectName: String?
        get() = getStringValue(INAPP_LISTENER_GAME_OBJECT_NAME_KEY, null)
    val inAppMessageListenerCallbackMethodName: String?
        get() = getStringValue(INAPP_LISTENER_CALLBACK_METHOD_NAME_KEY, null)
    val feedListenerGameObjectName: String?
        get() = getStringValue(FEED_LISTENER_GAME_OBJECT_NAME_KEY, null)
    val feedListenerCallbackMethodName: String?
        get() = getStringValue(FEED_LISTENER_CALLBACK_METHOD_NAME_KEY, null)
    @Suppress("BooleanPropertyNaming")
    val showInAppMessagesAutomaticallyKey: Boolean
        get() = getBooleanValue(INAPP_SHOW_INAPP_MESSAGES_AUTOMATICALLY_KEY, true)
    val pushReceivedGameObjectName: String?
        get() = getStringValue(PUSH_RECEIVED_GAME_OBJECT_NAME_KEY, null)
    val pushReceivedCallbackMethodName: String?
        get() = getStringValue(PUSH_RECEIVED_CALLBACK_METHOD_NAME_KEY, null)
    val pushOpenedGameObjectName: String?
        get() = getStringValue(PUSH_OPENED_GAME_OBJECT_NAME_KEY, null)
    val pushOpenedCallbackMethodName: String?
        get() = getStringValue(PUSH_OPENED_CALLBACK_METHOD_NAME_KEY, null)
    val pushDeletedGameObjectName: String?
        get() = getStringValue(PUSH_DELETED_GAME_OBJECT_NAME_KEY, null)
    val pushDeletedCallbackMethodName: String?
        get() = getStringValue(PUSH_DELETED_CALLBACK_METHOD_NAME_KEY, null)
    val contentCardsUpdatedListenerGameObjectName: String?
        get() = getStringValue(CONTENT_CARDS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY, null)
    val contentCardsUpdatedListenerCallbackMethodName: String?
        get() = getStringValue(CONTENT_CARDS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY, null)
    val featureFlagsUpdatedListenerGameObjectName: String?
        get() = getStringValue(FEATURE_FLAGS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY, null)
    val featureFlagsUpdatedListenerCallbackMethodName: String?
        get() = getStringValue(FEATURE_FLAGS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY, null)
    val sdkAuthenticationFailureListenerGameObjectName: String?
        get() = getStringValue(SDK_AUTHENTICATION_FAILURE_LISTENER_GAME_OBJECT_NAME_KEY, null)
    val sdkAuthenticationFailureListenerCallbackMethodName: String?
        get() = getStringValue(SDK_AUTHENTICATION_FAILURE_LISTENER_CALLBACK_METHOD_NAME_KEY, null)
    @Suppress("BooleanPropertyNaming")
    val autoSetInAppMessageManagerListener: Boolean
        get() = getBooleanValue(INAPP_AUTO_SET_MANAGER_LISTENER_KEY, true)
    val initialInAppMessageDisplayOperation: InAppMessageOperation
        get() {
            val rawValue = getStringValue(INAPP_INITIAL_DISPLAY_OPERATION_KEY, null)
            val operation = fromValue(rawValue)
            return operation ?: InAppMessageOperation.DISPLAY_NOW
        }

    @Suppress("LongMethod")
    fun configureListener(messageTypeValue: Int, gameObject: String, methodName: String) {
        val messageType = UnityMessageType.getTypeFromValue(messageTypeValue)
        if (messageType == null) {
            brazelog {
                "Got bad message type $messageTypeValue. Cannot configure a listener on object " +
                    "$gameObject for method $methodName"
            }
            return
        }
        when (messageType) {
            UnityMessageType.PUSH_PERMISSIONS_PROMPT_RESPONSE,
            UnityMessageType.PUSH_TOKEN_RECEIVED_FROM_SYSTEM -> {}
            UnityMessageType.PUSH_RECEIVED -> {
                putStringIntoRuntimeConfiguration(PUSH_RECEIVED_GAME_OBJECT_NAME_KEY, gameObject)
                putStringIntoRuntimeConfiguration(
                    PUSH_RECEIVED_CALLBACK_METHOD_NAME_KEY,
                    methodName
                )
            }
            UnityMessageType.PUSH_OPENED -> {
                putStringIntoRuntimeConfiguration(PUSH_OPENED_GAME_OBJECT_NAME_KEY, gameObject)
                putStringIntoRuntimeConfiguration(PUSH_OPENED_CALLBACK_METHOD_NAME_KEY, methodName)
            }
            UnityMessageType.PUSH_DELETED -> {
                putStringIntoRuntimeConfiguration(PUSH_DELETED_GAME_OBJECT_NAME_KEY, gameObject)
                putStringIntoRuntimeConfiguration(PUSH_DELETED_CALLBACK_METHOD_NAME_KEY, methodName)
            }
            UnityMessageType.IN_APP_MESSAGE -> {
                putStringIntoRuntimeConfiguration(INAPP_LISTENER_GAME_OBJECT_NAME_KEY, gameObject)
                putStringIntoRuntimeConfiguration(
                    INAPP_LISTENER_CALLBACK_METHOD_NAME_KEY,
                    methodName
                )
            }
            UnityMessageType.NEWS_FEED -> {
                putStringIntoRuntimeConfiguration(FEED_LISTENER_GAME_OBJECT_NAME_KEY, gameObject)
                putStringIntoRuntimeConfiguration(
                    FEED_LISTENER_CALLBACK_METHOD_NAME_KEY,
                    methodName
                )
            }
            UnityMessageType.CONTENT_CARDS_UPDATED -> {
                putStringIntoRuntimeConfiguration(
                    CONTENT_CARDS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY, gameObject
                )
                putStringIntoRuntimeConfiguration(
                    CONTENT_CARDS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY, methodName
                )
            }
            UnityMessageType.SDK_AUTHENTICATION_FAILURE -> {
                putStringIntoRuntimeConfiguration(
                    SDK_AUTHENTICATION_FAILURE_LISTENER_GAME_OBJECT_NAME_KEY, gameObject
                )
                putStringIntoRuntimeConfiguration(
                    SDK_AUTHENTICATION_FAILURE_LISTENER_CALLBACK_METHOD_NAME_KEY, methodName
                )
            }
            UnityMessageType.FEATURE_FLAGS_UPDATED -> {
                putStringIntoRuntimeConfiguration(
                    FEATURE_FLAGS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY, gameObject
                )
                putStringIntoRuntimeConfiguration(
                    FEATURE_FLAGS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY, methodName
                )
            }
        }
    }

    private fun putStringIntoRuntimeConfiguration(key: String, value: String) {
        runtimeAppConfigurationProvider.startEdit()
        runtimeAppConfigurationProvider.putString(key, value)
        runtimeAppConfigurationProvider.applyEdit()
    }

    companion object {
        private const val INAPP_SHOW_INAPP_MESSAGES_AUTOMATICALLY_KEY =
            "com_braze_inapp_show_inapp_messages_automatically"

        // In App Messages
        private const val INAPP_LISTENER_GAME_OBJECT_NAME_KEY =
            "com_braze_inapp_listener_game_object_name"
        private const val INAPP_LISTENER_CALLBACK_METHOD_NAME_KEY =
            "com_braze_inapp_listener_callback_method_name"
        private const val INAPP_AUTO_SET_MANAGER_LISTENER_KEY =
            "com_braze_inapp_auto_set_manager_listener_key"
        private const val INAPP_INITIAL_DISPLAY_OPERATION_KEY =
            "com_braze_inapp_initial_display_operation_key"

        // News Feed listener
        private const val FEED_LISTENER_GAME_OBJECT_NAME_KEY =
            "com_braze_feed_listener_game_object_name"
        private const val FEED_LISTENER_CALLBACK_METHOD_NAME_KEY =
            "com_braze_feed_listener_callback_method_name"

        // Push received
        private const val PUSH_RECEIVED_GAME_OBJECT_NAME_KEY =
            "com_braze_push_received_game_object_name"
        private const val PUSH_RECEIVED_CALLBACK_METHOD_NAME_KEY =
            "com_braze_push_received_callback_method_name"

        // Push opened
        private const val PUSH_OPENED_GAME_OBJECT_NAME_KEY =
            "com_braze_push_opened_game_object_name"
        private const val PUSH_OPENED_CALLBACK_METHOD_NAME_KEY =
            "com_braze_push_opened_callback_method_name"

        // Push deleted
        private const val PUSH_DELETED_GAME_OBJECT_NAME_KEY =
            "com_braze_push_deleted_game_object_name"
        private const val PUSH_DELETED_CALLBACK_METHOD_NAME_KEY =
            "com_braze_push_deleted_callback_method_name"

        // Content Cards
        private const val CONTENT_CARDS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY =
            "com_braze_content_cards_updated_listener_game_object_name"
        private const val CONTENT_CARDS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY =
            "com_braze_content_cards_updated_listener_callback_method_name"

        // Feature Flags
        private const val FEATURE_FLAGS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY =
            "com_braze_feature_flags_updated_listener_game_object_name"
        private const val FEATURE_FLAGS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY =
            "com_braze_feature_flags_updated_listener_callback_method_name"

        // SDK Authentication Failure
        private const val SDK_AUTHENTICATION_FAILURE_LISTENER_GAME_OBJECT_NAME_KEY =
            "com_braze_sdk_authentication_failure_listener_game_object_name"
        private const val SDK_AUTHENTICATION_FAILURE_LISTENER_CALLBACK_METHOD_NAME_KEY =
            "com_braze_sdk_authentication_failure_listener_callback_method_name"
    }
}
