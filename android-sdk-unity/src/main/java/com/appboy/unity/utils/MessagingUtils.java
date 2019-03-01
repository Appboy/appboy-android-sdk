package com.appboy.unity.utils;

import com.appboy.models.IInAppMessage;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.unity3d.player.UnityPlayer;

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
}
