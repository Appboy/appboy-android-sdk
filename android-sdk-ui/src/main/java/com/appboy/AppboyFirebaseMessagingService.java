package com.appboy;

import android.content.Intent;
import android.os.Bundle;

import com.appboy.support.AppboyLogger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class AppboyFirebaseMessagingService extends FirebaseMessagingService {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyFirebaseMessagingService.class);
  private static final AppboyFcmReceiver mAppboyFcmReceiver = new AppboyFcmReceiver();

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Map<String, String> remoteMessageData = remoteMessage.getData();
    AppboyLogger.i(TAG, "Got remote message from FCM: " + remoteMessageData);

    if (remoteMessageData == null) {
      AppboyLogger.w(TAG, "Remote message from FCM was null. Not passing along intent to Braze");
      return;
    }

    Intent pushIntent = new Intent(AppboyFcmReceiver.FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION);
    Bundle bundle = new Bundle();
    for (Map.Entry<String, String> entry : remoteMessageData.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      AppboyLogger.v(TAG, "Adding bundle item from FCM remote data with key: " + key + " and value: " + value);
      bundle.putString(key, value);
    }
    pushIntent.putExtras(bundle);
    mAppboyFcmReceiver.onReceive(this, pushIntent);
  }
}
