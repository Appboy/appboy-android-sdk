package com.appboy.unity

import com.appboy.events.FeedUpdatedEvent
import com.appboy.events.IEventSubscriber
import com.appboy.unity.configuration.UnityConfigurationProvider
import com.appboy.unity.utils.MessagingUtils.sendContentCardsUpdatedEventToUnity
import com.appboy.unity.utils.MessagingUtils.sendFeedUpdatedEventToUnity
import com.appboy.unity.utils.MessagingUtils.sendInAppMessageReceivedMessage
import com.braze.events.ContentCardsUpdatedEvent
import com.braze.events.InAppMessageEvent
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag

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
}
