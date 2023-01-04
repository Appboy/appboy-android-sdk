package com.appboy.unity.enums

/**
 * Types of messages that Braze can be configured to send to a GameObject method at runtime.
 */
@Suppress("MagicNumber")
enum class UnityMessageType(private val value: Int) {
    PUSH_PERMISSIONS_PROMPT_RESPONSE(0),
    PUSH_TOKEN_RECEIVED_FROM_SYSTEM(1),
    PUSH_RECEIVED(2),
    PUSH_OPENED(3),
    PUSH_DELETED(4),
    IN_APP_MESSAGE(5),
    NEWS_FEED(6),
    CONTENT_CARDS_UPDATED(7),
    SDK_AUTHENTICATION_FAILURE(8);

    companion object {
        fun getTypeFromValue(value: Int) =
            values().firstOrNull { it.value == value }
    }
}
