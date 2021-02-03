package com.appboy.unity.utils;

import android.content.Context;

import com.appboy.Appboy;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.MessageButton;
import com.appboy.support.AppboyLogger;

public class InAppMessageUtils {
  private static final String TAG = AppboyLogger.getBrazeLogTag(InAppMessageUtils.class);

  public static IInAppMessage inAppMessageFromString(Context context, String messageJSONString) {
    return Appboy.getInstance(context).deserializeInAppMessageString(messageJSONString);
  }

  public static void logInAppMessageClick(IInAppMessage inAppMessage) {
    if (inAppMessage != null) {
      inAppMessage.logClick();
    } else {
      AppboyLogger.w(TAG, "The in-app message is null, Not logging in-app message click.");
    }
  }

  public static void logInAppMessageButtonClick(IInAppMessage inAppMessage, int buttonId) {
    if (inAppMessage == null) {
      AppboyLogger.w(TAG, "The in-app message is null. Not logging in-app message button click.");
      return;
    }
    if (inAppMessage instanceof IInAppMessageImmersive) {
      IInAppMessageImmersive inAppMessageImmersive = (IInAppMessageImmersive)inAppMessage;
      for (MessageButton button : inAppMessageImmersive.getMessageButtons()) {
        if (button.getId() == buttonId) {
          inAppMessageImmersive.logButtonClick(button);
          break;
        }
      }
    } else {
      AppboyLogger.w(TAG, "The in-app message %s isn't an instance of "
          + "InAppMessageImmersive. Not logging in-app message button click.");
    }
  }

  public static void logInAppMessageImpression(IInAppMessage inAppMessage) {
    if (inAppMessage != null) {
      inAppMessage.logImpression();
    } else {
      AppboyLogger.w(TAG, "The in-app message is null, Not logging in-app message impression.");
    }
  }
}
