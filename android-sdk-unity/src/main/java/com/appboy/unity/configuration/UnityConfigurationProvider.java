package com.appboy.unity.configuration;

import android.content.Context;

import com.appboy.configuration.CachedConfigurationProvider;

public class UnityConfigurationProvider extends CachedConfigurationProvider {
  private static final String INAPP_LISTENER_GAME_OBJECT_NAME_KEY = "com_appboy_inapp_listener_game_object_name";
  private static final String INAPP_LISTENER_CALLBACK_METHOD_NAME_KEY = "com_appboy_inapp_listener_callback_method_name";
  private static final String FEED_LISTENER_GAME_OBJECT_NAME_KEY = "com_appboy_feed_listener_game_object_name";
  private static final String FEED_LISTENER_CALLBACK_METHOD_NAME_KEY = "com_appboy_feed_listener_callback_method_name";
  private static final String INAPP_SHOW_INAPP_MESSAGES_AUTOMATICALLY_KEY = "com_appboy_inapp_show_inapp_messages_automatically";
  private static final String PUSH_RECEIVED_GAME_OBJECT_NAME_KEY = "com_appboy_push_received_game_object_name";
  private static final String PUSH_RECEIVED_CALLBACK_METHOD_NAME_KEY = "com_appboy_push_received_callback_method_name";
  private static final String PUSH_OPENED_GAME_OBJECT_NAME_KEY = "com_appboy_push_opened_game_object_name";
  private static final String PUSH_OPENED_CALLBACK_METHOD_NAME_KEY = "com_appboy_push_opened_callback_method_name";
  private static final String PUSH_DELETED_GAME_OBJECT_NAME_KEY = "com_appboy_push_deleted_game_object_name";
  private static final String PUSH_DELETED_CALLBACK_METHOD_NAME_KEY = "com_appboy_push_deleted_callback_method_name";
  private static final String CONTENT_CARDS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY = "com_appboy_content_cards_updated_listener_game_object_name";
  private static final String CONTENT_CARDS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY = "com_appboy_content_cards_updated_listener_callback_method_name";

  public UnityConfigurationProvider(Context context) {
    super(context);
  }

  public String getInAppMessageListenerGameObjectName() {
    return getStringValue(INAPP_LISTENER_GAME_OBJECT_NAME_KEY, null);
  }

  public String getInAppMessageListenerCallbackMethodName() {
    return getStringValue(INAPP_LISTENER_CALLBACK_METHOD_NAME_KEY, null);
  }

  public String getFeedListenerGameObjectName() {
    return getStringValue(FEED_LISTENER_GAME_OBJECT_NAME_KEY, null);
  }

  public String getFeedListenerCallbackMethodName() {
    return getStringValue(FEED_LISTENER_CALLBACK_METHOD_NAME_KEY, null);
  }

  public boolean getShowInAppMessagesAutomaticallyKey() {
    return getBooleanValue(INAPP_SHOW_INAPP_MESSAGES_AUTOMATICALLY_KEY, true);
  }

  public String getPushReceivedGameObjectName() {
    return getStringValue(PUSH_RECEIVED_GAME_OBJECT_NAME_KEY, null);
  }

  public String getPushReceivedCallbackMethodName() {
    return getStringValue(PUSH_RECEIVED_CALLBACK_METHOD_NAME_KEY, null);
  }

  public String getPushOpenedGameObjectName() {
    return getStringValue(PUSH_OPENED_GAME_OBJECT_NAME_KEY, null);
  }

  public String getPushOpenedCallbackMethodName() {
    return getStringValue(PUSH_OPENED_CALLBACK_METHOD_NAME_KEY, null);
  }

  public String getPushDeletedGameObjectName() {
    return getStringValue(PUSH_DELETED_GAME_OBJECT_NAME_KEY, null);
  }

  public String getPushDeletedCallbackMethodName() {
    return getStringValue(PUSH_DELETED_CALLBACK_METHOD_NAME_KEY, null);
  }

  public String getContentCardsUpdatedListenerGameObjectName() {
    return getStringValue(CONTENT_CARDS_UPDATED_LISTENER_GAME_OBJECT_NAME_KEY, null);
  }

  public String getContentCardsUpdatedListenerCallbackMethodName() {
    return getStringValue(CONTENT_CARDS_UPDATED_LISTENER_CALLBACK_METHOD_NAME_KEY, null);
  }
}
