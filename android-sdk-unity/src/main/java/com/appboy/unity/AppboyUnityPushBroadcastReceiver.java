package com.appboy.unity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.appboy.push.AppboyNotificationUtils;
import com.appboy.unity.configuration.UnityConfigurationProvider;
import com.appboy.unity.utils.MessagingUtils;
import com.braze.support.BrazeLogger;

public class AppboyUnityPushBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = BrazeLogger.getBrazeLogTag(AppboyUnityPushBroadcastReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    String packageName = context.getPackageName();
    String pushReceivedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_RECEIVED_SUFFIX;
    String pushOpenedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX;
    String pushDeletedAction = packageName + AppboyNotificationUtils.APPBOY_NOTIFICATION_DELETED_SUFFIX;
    String action = intent.getAction();
    BrazeLogger.i(TAG, "Received a push broadcast intent with action: " + action);

    UnityConfigurationProvider unityConfigurationProvider = new UnityConfigurationProvider(context);
    if (pushReceivedAction.equals(action)) {
      String unityGameObjectName = unityConfigurationProvider.getPushReceivedGameObjectName();
      String unityCallbackFunctionName = unityConfigurationProvider.getPushReceivedCallbackMethodName();
      boolean isPushMessageSent = MessagingUtils.sendPushMessageToUnity(unityGameObjectName, unityCallbackFunctionName, intent, "push received");
      BrazeLogger.d(TAG, (isPushMessageSent ? "Successfully sent" : "Failure to send") + " push received message to Unity Player");
    } else if (pushOpenedAction.equals(action)) {
      String unityGameObjectName = unityConfigurationProvider.getPushOpenedGameObjectName();
      String unityCallbackFunctionName = unityConfigurationProvider.getPushOpenedCallbackMethodName();
      boolean isPushMessageSent = MessagingUtils.sendPushMessageToUnity(unityGameObjectName, unityCallbackFunctionName, intent, "push opened");
      BrazeLogger.d(TAG, (isPushMessageSent ? "Successfully sent" : "Failure to send") + " push opened message to Unity Player");
    } else if (pushDeletedAction.equals(action)) {
      String unityGameObjectName = unityConfigurationProvider.getPushDeletedGameObjectName();
      String unityCallbackFunctionName = unityConfigurationProvider.getPushDeletedCallbackMethodName();
      boolean isPushMessageSent = MessagingUtils.sendPushMessageToUnity(unityGameObjectName, unityCallbackFunctionName, intent, "push deleted");
      BrazeLogger.d(TAG, (isPushMessageSent ? "Successfully sent" : "Failure to send") + " push deleted message to Unity Player");
    }
  }
}
