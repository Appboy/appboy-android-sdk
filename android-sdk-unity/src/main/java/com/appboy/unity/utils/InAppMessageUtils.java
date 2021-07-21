package com.appboy.unity.utils;

import android.content.Context;

import com.braze.Braze;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.IInAppMessageImmersive;
import com.braze.models.inappmessage.MessageButton;
import com.braze.support.BrazeLogger;

public class InAppMessageUtils {
  private static final String TAG = BrazeLogger.getBrazeLogTag(InAppMessageUtils.class);

  public static IInAppMessage inAppMessageFromString(Context context, String messageJSONString) {
    return Braze.getInstance(context).deserializeInAppMessageString(messageJSONString);
  }

  public static void logInAppMessageClick(IInAppMessage inAppMessage) {
    if (inAppMessage != null) {
      inAppMessage.logClick();
    } else {
      BrazeLogger.w(TAG, "The in-app message is null, Not logging in-app message click.");
    }
  }

  public static void logInAppMessageButtonClick(IInAppMessage inAppMessage, int buttonId) {
    if (inAppMessage == null) {
      BrazeLogger.w(TAG, "The in-app message is null. Not logging in-app message button click.");
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
      BrazeLogger.w(TAG, "The in-app message %s isn't an instance of "
          + "InAppMessageImmersive. Not logging in-app message button click.");
    }
  }

  public static void logInAppMessageImpression(IInAppMessage inAppMessage) {
    if (inAppMessage != null) {
      inAppMessage.logImpression();
    } else {
      BrazeLogger.w(TAG, "The in-app message is null, Not logging in-app message impression.");
    }
  }
}
