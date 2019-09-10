package com.appboy.unity;

import com.appboy.events.ContentCardsUpdatedEvent;
import com.appboy.events.FeedUpdatedEvent;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.InAppMessageEvent;
import com.appboy.support.AppboyLogger;
import com.appboy.unity.configuration.UnityConfigurationProvider;
import com.appboy.unity.utils.MessagingUtils;

public class EventSubscriberFactory {
  private static final String TAG = AppboyLogger.getAppboyLogTag(EventSubscriberFactory.class);

  public static IEventSubscriber<InAppMessageEvent> createInAppMessageEventSubscriber(final UnityConfigurationProvider unityConfigurationProvider) {
    return new IEventSubscriber<InAppMessageEvent>() {
      @Override
      public void trigger(InAppMessageEvent inAppMessageEvent) {
        String unityGameObjectName = unityConfigurationProvider.getInAppMessageListenerGameObjectName();
        String unityCallbackFunctionName = unityConfigurationProvider.getInAppMessageListenerCallbackMethodName();
        boolean isInAppMessageEventSent = MessagingUtils.sendInAppMessageReceivedMessage(unityGameObjectName, unityCallbackFunctionName, inAppMessageEvent.getInAppMessage());
        AppboyLogger.d(TAG, (isInAppMessageEventSent ? "Successfully sent" : "Failure to send") + " in-app message event to Unity Player");
      }
    };
  }

  public static IEventSubscriber<FeedUpdatedEvent> createFeedUpdatedEventSubscriber(final UnityConfigurationProvider unityConfigurationProvider) {
    return new IEventSubscriber<FeedUpdatedEvent>() {
      @Override
      public void trigger(FeedUpdatedEvent feedUpdatedEvent) {
        String unityGameObjectName = unityConfigurationProvider.getFeedListenerGameObjectName();
        String unityCallbackFunctionName = unityConfigurationProvider.getFeedListenerCallbackMethodName();
        boolean isFeedUpdatedEventSent = MessagingUtils.sendFeedUpdatedEventToUnity(unityGameObjectName, unityCallbackFunctionName, feedUpdatedEvent);
        AppboyLogger.d(TAG, (isFeedUpdatedEventSent ? "Successfully sent" : "Failure to send") + " Feed updated event to Unity Player");
      }
    };
  }

  public static IEventSubscriber<ContentCardsUpdatedEvent> createContentCardsEventSubscriber(final UnityConfigurationProvider unityConfigurationProvider) {
    return new IEventSubscriber<ContentCardsUpdatedEvent>() {
      @Override
      public void trigger(ContentCardsUpdatedEvent contentCardsUpdatedEvent) {
        String unityGameObjectName = unityConfigurationProvider.getContentCardsUpdatedListenerGameObjectName();
        String unityCallbackFunctionName = unityConfigurationProvider.getContentCardsUpdatedListenerCallbackMethodName();
        boolean isContentCardsEventSent = MessagingUtils.sendContentCardsUpdatedEventToUnity(unityGameObjectName, unityCallbackFunctionName, contentCardsUpdatedEvent);
        AppboyLogger.d(TAG, (isContentCardsEventSent ? "Successfully sent" : "Failure to send") + " Content Cards updated event to Unity Player");
      }
    };
  }
}
