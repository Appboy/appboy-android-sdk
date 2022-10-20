package com.appboy.unity.enums;

import androidx.annotation.Nullable;

import com.braze.support.BrazeLogger;
import com.braze.ui.inappmessage.InAppMessageOperation;

import java.util.HashMap;
import java.util.Map;

public enum UnityInAppMessageManagerAction {
  UNKNOWN(-1, null),
  /**
   * Maps to {@link InAppMessageOperation#DISPLAY_NOW}
   */
  IAM_DISPLAY_NOW(0, InAppMessageOperation.DISPLAY_NOW),
  /**
   * Maps to {@link InAppMessageOperation#DISPLAY_LATER}
   */
  IAM_DISPLAY_LATER(1, InAppMessageOperation.DISPLAY_LATER),
  /**
   * Maps to {@link InAppMessageOperation#DISCARD}
   */
  IAM_DISCARD(2, InAppMessageOperation.DISCARD)
  ;

  private static final String TAG = BrazeLogger.getBrazeLogTag(UnityInAppMessageManagerAction.class);
  private static final Map<Integer, UnityInAppMessageManagerAction> sTypeLookup = new HashMap<>();

  static {
    for (UnityInAppMessageManagerAction messageType : UnityInAppMessageManagerAction.values()) {
      sTypeLookup.put(messageType.mValue, messageType);
    }
  }

  private final int mValue;
  @Nullable
  private final InAppMessageOperation mInAppMessageOperation;

  UnityInAppMessageManagerAction(int keyArgument, InAppMessageOperation matchingOperation) {
    mValue = keyArgument;
    mInAppMessageOperation = matchingOperation;
  }

  @Nullable
  public static UnityInAppMessageManagerAction getTypeFromValue(int value) {
    if (!sTypeLookup.containsKey(value)) {
      BrazeLogger.v(TAG, "Returning UNKNOWN. Failed to map unity IAM manager value: " + value);
      return UNKNOWN;
    }
    return sTypeLookup.get(value);
  }

  @Nullable
  public InAppMessageOperation getInAppMessageOperation() {
    return mInAppMessageOperation;
  }
}
