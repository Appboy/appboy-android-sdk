package com.braze.unity

import com.braze.events.FeedUpdatedEvent
import com.braze.unity.configuration.UnityConfigurationProvider
import com.braze.enums.BrazePushEventType
import com.braze.events.BrazePushEvent
import com.braze.events.BrazeSdkAuthenticationErrorEvent
import com.braze.events.ContentCardsUpdatedEvent
import com.braze.events.FeatureFlagsUpdatedEvent
import com.braze.events.IEventSubscriber
import com.braze.events.InAppMessageEvent
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag
import com.braze.unity.utils.MessagingUtils.sendContentCardsUpdatedEventToUnity
import com.braze.unity.utils.MessagingUtils.sendFeatureFlagsUpdatedEventToUnity
import com.braze.unity.utils.MessagingUtils.sendFeedUpdatedEventToUnity
import com.braze.unity.utils.MessagingUtils.sendInAppMessageReceivedMessage
import com.braze.unity.utils.MessagingUtils.sendPushEventToUnity
import com.braze.unity.utils.MessagingUtils.sendSdkAuthErrorEventToUnity

object EventSubscriberFactory {
    private val TAG = getBrazeLogTag(EventSubscriberFactory::class.java)

    fun createInAppMessageEventSubscriber(config: UnityConfigurationProvider): IEventSubscriber<InAppMessageEvent> {
        return IEventSubscriber { inAppMessageEvent: InAppMessageEvent ->
            val isInAppMessageEventSent =
                sendInAppMessageReceivedMessage(
                    config.inAppMessageListenerGameObjectName,
                    config.inAppMessageListenerCallbackMethodName,
                    inAppMessageEvent.inAppMessage
                )
            brazelog(TAG) { "Did send in-app message event to Unity Player?: $isInAppMessageEventSent" }
        }
    }

    fun createFeedUpdatedEventSubscriber(config: UnityConfigurationProvider): IEventSubscriber<FeedUpdatedEvent> {
        return IEventSubscriber { feedUpdatedEvent: FeedUpdatedEvent ->
            val isFeedUpdatedEventSent = sendFeedUpdatedEventToUnity(
                config.feedListenerGameObjectName,
                config.feedListenerCallbackMethodName,
                feedUpdatedEvent
            )
            brazelog(TAG) { "Did send Feed updated event to Unity Player?: $isFeedUpdatedEventSent" }
        }
    }

    fun createContentCardsEventSubscriber(config: UnityConfigurationProvider): IEventSubscriber<ContentCardsUpdatedEvent> {
        return IEventSubscriber { contentCardsUpdatedEvent: ContentCardsUpdatedEvent ->
            val isContentCardsEventSent =
                sendContentCardsUpdatedEventToUnity(
                    config.contentCardsUpdatedListenerGameObjectName,
                    config.contentCardsUpdatedListenerCallbackMethodName,
                    contentCardsUpdatedEvent
                )
            brazelog(TAG) { "Did send Content Cards updated event to Unity Player?: $isContentCardsEventSent" }
        }
    }

    fun createFeatureFlagsEventSubscriber(config: UnityConfigurationProvider): IEventSubscriber<FeatureFlagsUpdatedEvent> {
        return IEventSubscriber { featureFlagsUpdatedEvent: FeatureFlagsUpdatedEvent ->
            val isFeatureFlagUpdatedEventSent =
                sendFeatureFlagsUpdatedEventToUnity(
                    config.featureFlagsUpdatedListenerGameObjectName,
                    config.featureFlagsUpdatedListenerCallbackMethodName,
                    featureFlagsUpdatedEvent
                )
            brazelog(TAG) { "Did send Content Cards updated event to Unity Player?: $isFeatureFlagUpdatedEventSent" }
        }
    }

    fun createSdkAuthenticationFailureSubscriber(config: UnityConfigurationProvider): IEventSubscriber<BrazeSdkAuthenticationErrorEvent> {
        return IEventSubscriber { sdkAuthErrorEvent: BrazeSdkAuthenticationErrorEvent ->
            val isSdkAuthErrorSent =
                sendSdkAuthErrorEventToUnity(
                    config.sdkAuthenticationFailureListenerGameObjectName,
                    config.sdkAuthenticationFailureListenerCallbackMethodName,
                    sdkAuthErrorEvent
                )
            brazelog { "Did send SDK Authentication failure event to Unity Player?: $isSdkAuthErrorSent" }
        }
    }

    fun createPushEventSubscriber(config: UnityConfigurationProvider): IEventSubscriber<BrazePushEvent> {
        return IEventSubscriber { event: BrazePushEvent ->
            val (callback, gameObject) = when (event.eventType) {
                BrazePushEventType.NOTIFICATION_RECEIVED -> Pair(config.pushReceivedCallbackMethodName, config.pushReceivedGameObjectName)
                BrazePushEventType.NOTIFICATION_DELETED -> Pair(config.pushDeletedCallbackMethodName, config.pushDeletedGameObjectName)
                BrazePushEventType.NOTIFICATION_OPENED -> Pair(config.pushOpenedCallbackMethodName, config.pushOpenedGameObjectName)
                else -> return@IEventSubscriber
            }
            val wasMessageSent = sendPushEventToUnity(gameObject, callback, event)
            brazelog { "Did send Braze Push event to Unity Player?: $wasMessageSent \nEvent: $event" }
        }
    }
}
