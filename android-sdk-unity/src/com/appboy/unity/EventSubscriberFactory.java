package com.appboy.unity;

import android.util.Log;

import com.appboy.Constants;
import com.appboy.events.FeedUpdatedEvent;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.InAppMessageEvent;
import com.appboy.models.cards.Card;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.unity.configuration.UnityConfigurationProvider;
import com.appboy.unity.utils.MessagingUtils;
import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EventSubscriberFactory {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, EventSubscriberFactory.class.getName());

  public static IEventSubscriber<InAppMessageEvent> createInAppMessageEventSubscriber(final UnityConfigurationProvider unityConfigurationProvider) {
    return new IEventSubscriber<InAppMessageEvent>() {
      @Override
      public void trigger(InAppMessageEvent inAppMessageEvent) {
        String unityGameObjectName = unityConfigurationProvider.getInAppMessageListenerGameObjectName();
        String unityCallbackFunctionName = unityConfigurationProvider.getInAppMessageListenerCallbackMethodName();
        MessagingUtils.sendInAppMessageReceivedMessage(unityGameObjectName, unityCallbackFunctionName, inAppMessageEvent.getInAppMessage());
      }
    };
  }

  public static IEventSubscriber<FeedUpdatedEvent> createFeedUpdatedEventSubscriber(final UnityConfigurationProvider unityConfigurationProvider) {
    return new IEventSubscriber<FeedUpdatedEvent>() {
      @Override
      public void trigger(FeedUpdatedEvent feedUpdatedEvent) {
        String unityGameObjectName = unityConfigurationProvider.getFeedListenerGameObjectName();
        if (StringUtils.isNullOrBlank(unityGameObjectName)) {
          Log.d(TAG, "There is no Unity GameObject registered in the appboy.xml configuration file to receive "
              + "feed updates. Not sending the message to the Unity Player.");
          return;
        }
        String unityCallbackFunctionName = unityConfigurationProvider.getFeedListenerCallbackMethodName();
        if (StringUtils.isNullOrBlank(unityCallbackFunctionName)) {
          Log.d(TAG, "There is no Unity callback method name registered to receive feed updates in "
              + "the appboy.xml configuration file. Not sending the message to the Unity Player.");
          return;
        }
        JSONArray jsonArray = new JSONArray();
        List<Card> cards = feedUpdatedEvent.getFeedCards();
        for (Card card : cards) {
          jsonArray.put(card.forJsonPut());
        }
        JSONObject object = new JSONObject();
        try {
          object.put("mFeedCards", jsonArray);
          object.put("mFromOfflineStorage", feedUpdatedEvent.isFromOfflineStorage());
        } catch (JSONException e) {
          AppboyLogger.e(TAG, "Caught exception creating feed updated event Json.", e);
        }
        UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, object.toString());
      }
    };
  }
}
