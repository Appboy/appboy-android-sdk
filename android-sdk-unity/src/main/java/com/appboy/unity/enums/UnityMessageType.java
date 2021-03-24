package com.appboy.unity.enums;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Types of messages that Braze can be configured to send to a GameObject method at runtime.
 */
public enum UnityMessageType {
  PUSH_PERMISSIONS_PROMPT_RESPONSE(0),
  PUSH_TOKEN_RECEIVED_FROM_SYSTEM(1),
  PUSH_RECEIVED(2),
  PUSH_OPENED(3),
  PUSH_DELETED(4),
  IN_APP_MESSAGE(5),
  NEWS_FEED(6),
  CONTENT_CARDS_UPDATED(7),
  ;

  private static final Map<Integer, UnityMessageType> sTypeLookup = new HashMap<>();

  static {
    for (UnityMessageType messageType : UnityMessageType.values()) {
      sTypeLookup.put(messageType.mValue, messageType);
    }
  }

  private final int mValue;

  UnityMessageType(int keyArgument) {
    mValue = keyArgument;
  }

  @Nullable
  public static UnityMessageType getTypeFromValue(int value) {
    return sTypeLookup.get(value);
  }
}
