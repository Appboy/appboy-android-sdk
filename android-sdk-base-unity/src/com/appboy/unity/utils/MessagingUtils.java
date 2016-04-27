package com.appboy.unity.utils;

import android.util.Log;

import com.appboy.Constants;
import com.appboy.models.IInAppMessage;
import com.appboy.support.StringUtils;
import com.unity3d.player.UnityPlayer;

public class MessagingUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, MessagingUtils.class.getName());

  public static boolean sendInAppMessageReceivedMessage(String unityGameObjectName, String unityCallbackFunctionName, IInAppMessage inAppMessage) {
    if (StringUtils.isNullOrBlank(unityGameObjectName)) {
      Log.d(TAG, "There is no Unity GameObject registered in the appboy.xml configuration file to receive "
          + "in app messages. Not sending the message to the Unity Player.");
      return false;
    }
    if (StringUtils.isNullOrBlank(unityCallbackFunctionName)) {
      Log.d(TAG, "There is no Unity callback method name registered to receive in app messages in "
          + "the appboy.xml configuration file. Not sending the message to the Unity Player.");
      return false;
    }
    Log.d(TAG, String.format("Sending a message to %s:%s.", unityGameObjectName, unityCallbackFunctionName));
    UnityPlayer.UnitySendMessage(unityGameObjectName, unityCallbackFunctionName, inAppMessage.forJsonPut().toString());
    return true;
  }
}
