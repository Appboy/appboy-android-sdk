package com.appboy.unity.utils;

import android.content.Context;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.MessageButton;

public class InAppMessageUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, InAppMessageUtils.class.getName());

  public static IInAppMessage inAppMessageFromString(Context context, String messageJSONString) {
    return Appboy.getInstance(context).deserializeInAppMessageString(messageJSONString);
  }

  public static void logInAppMessageClick(IInAppMessage inAppMessage) {
    if (inAppMessage != null) {
      inAppMessage.logClick();
    } else {
      Log.e(TAG, "The in-app message is null, Not logging in-app message click.");
    }
  }

  public static void logInAppMessageButtonClick(IInAppMessage inAppMessage, int buttonId) {
    if (inAppMessage == null) {
      Log.e(TAG, "The in-app message is null. Not logging in-app message button click.");
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
      Log.e(TAG, "The in-app message %s isn't an instance of "
          + "InAppMessageImmersive. Not logging in-app message button click.");
    }
  }

  public static void logInAppMessageImpression(IInAppMessage inAppMessage) {
    if (inAppMessage != null) {
      inAppMessage.logImpression();
    } else {
      Log.e(TAG, "The in-app message is null, Not logging in-app message impression.");
    }
  }
}
