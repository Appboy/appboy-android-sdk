package com.appboy.unity.utils;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.events.ContentCardsUpdatedEvent;
import com.appboy.events.FeedUpdatedEvent;
import com.appboy.models.IInAppMessage;
import com.appboy.models.cards.Card;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagingUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(MessagingUtils.class);

  public static boolean sendInAppMessageReceivedMessage(String unityGameObjectName, String unityCallbackFunctionName, IInAppMessage inAppMessage) {
    if (StringUtils.isNullOrBlank(unityGameObjectName)) {
      AppboyLogger.d(TAG, "There is no Unity GameObject registered in the appboy.xml configuration file to receive "
          + "in app messages. Not sending the message to the Unity Player.");
      return false;
    }
    if (StringUtils.isNullOrBlank(unityCallbackFunctionName)) {
      AppboyLogger.d(TAG, "There is no Unity callback method name registered to receive in app messages in "
          + "the appboy.xml configuration file. Not sending the message to the Unity Player.");
      return false;
    }
    AppboyLogger.d(TAG, "Sending a message to " + unityGameObjectName + ":" + unityCallbackFunctionName + ".");
    UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, inAppMessage.forJsonPut().toString());
    return true;
  }

  public static boolean sendPushMessageToUnity(@Nullable String unityGameObjectName, @Nullable String unityCallbackFunctionName, @NonNull Intent pushIntent, @NonNull String pushAction) {
    if (StringUtils.isNullOrBlank(unityGameObjectName)) {
      AppboyLogger.d(TAG, "There is no Unity GameObject registered in the appboy.xml configuration file to receive "
          + pushAction + " messages. Not sending the message to the Unity Player.");
      return false;
    }
    if (StringUtils.isNullOrBlank(unityCallbackFunctionName)) {
      AppboyLogger.d(TAG, "There is no Unity callback method name registered to receive " + pushAction + " messages in "
          + "the appboy.xml configuration file. Not sending the message to the Unity Player.");
      return false;
    }
    AppboyLogger.v(TAG, "Sending a " + pushAction + " message to " + unityGameObjectName + ":" + unityCallbackFunctionName + ".");
    Bundle appboyExtras = pushIntent.getExtras();
    UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, getPushBundleExtras(appboyExtras).toString());
    return true;
  }

  public static boolean sendFeedUpdatedEventToUnity(@Nullable String unityGameObjectName,
                                                    @Nullable String unityCallbackFunctionName,
                                                    @NonNull FeedUpdatedEvent feedUpdatedEvent) {
    if (StringUtils.isNullOrBlank(unityGameObjectName)) {
      AppboyLogger.d(TAG, "There is no Unity GameObject registered in the appboy.xml configuration "
          + "file to receive feed updates. Not sending the message to the Unity Player.");
      return false;
    }
    if (StringUtils.isNullOrBlank(unityCallbackFunctionName)) {
      AppboyLogger.d(TAG, "There is no Unity callback method name registered to receive feed updates in "
          + "the appboy.xml configuration file. Not sending the message to the Unity Player.");
      return false;
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
    AppboyLogger.v(TAG, "Sending a feed updated event message to " + unityGameObjectName + ":" + unityCallbackFunctionName + ".");
    UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, object.toString());
    return true;
  }

  public static boolean sendContentCardsUpdatedEventToUnity(@Nullable String unityGameObjectName,
                                                            @Nullable String unityCallbackFunctionName,
                                                            @NonNull ContentCardsUpdatedEvent contentCardsUpdatedEvent) {
    if (StringUtils.isNullOrBlank(unityGameObjectName)) {
      AppboyLogger.d(TAG, "There is no Unity GameObject registered in the appboy.xml configuration file "
          + "to receive Content Cards updated event messages. Not sending the message to the Unity Player.");
      return false;
    }
    if (StringUtils.isNullOrBlank(unityCallbackFunctionName)) {
      AppboyLogger.d(TAG, "There is no Unity callback method name registered to receive Content "
          + "Cards updated event messages in the appboy.xml configuration file. Not sending the message to the Unity Player.");
      return false;
    }

    JSONArray jsonArray = new JSONArray();
    List<Card> cards = contentCardsUpdatedEvent.getAllCards();
    for (Card card : cards) {
      jsonArray.put(card.forJsonPut());
    }
    JSONObject object = new JSONObject();
    try {
      object.put("mContentCards", jsonArray);
      object.put("mFromOfflineStorage", contentCardsUpdatedEvent.isFromOfflineStorage());
    } catch (JSONException e) {
      AppboyLogger.e(TAG, "Caught exception creating Content Cards updated event Json.", e);
    }
    AppboyLogger.v(TAG, "Sending a Content Cards update message to " + unityGameObjectName + ":" + unityCallbackFunctionName + ".");
    UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, object.toString());
    return true;
  }

  /**
   * De-serializes a bundle into a key value pair that can be represented as a {@link JSONObject}.
   * Nested bundles are also converted recursively to have a single hierarchical structure.
   *
   * @param appboyExtras The bundle received whenever a push notification is received, opened or deleted.
   * @return A {@link JSONObject} that represents this bundle in string format.
   */
  private static JSONObject getPushBundleExtras(Bundle appboyExtras) {
    Map<String, String> extras = new HashMap<>();
    if (appboyExtras != null) {
      for (String key : appboyExtras.keySet()) {
        Object value = appboyExtras.get(key);
        if (value instanceof Bundle) {
          value = getPushBundleExtras((Bundle) value).toString();
        }
        extras.put(key, String.valueOf(value));
      }
    }
    return new JSONObject(extras);
  }
}
