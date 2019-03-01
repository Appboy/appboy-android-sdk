package com.appboy.unity.configuration;

import android.content.Context;

import com.appboy.configuration.CachedConfigurationProvider;

public class UnityConfigurationProvider extends CachedConfigurationProvider {
  private static final String INAPP_LISTENER_GAME_OBJECT_NAME_KEY = "com_appboy_inapp_listener_game_object_name";
  private static final String INAPP_LISTENER_CALLBACK_METHOD_NAME_KEY = "com_appboy_inapp_listener_callback_method_name";
  private static final String FEED_LISTENER_GAME_OBJECT_NAME_KEY = "com_appboy_feed_listener_game_object_name";
  private static final String FEED_LISTENER_CALLBACK_METHOD_NAME_KEY = "com_appboy_feed_listener_callback_method_name";
  private static final String INAPP_SHOW_INAPP_MESSAGES_AUTOMATICALLY_KEY = "com_appboy_inapp_show_inapp_messages_automatically";

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
}
