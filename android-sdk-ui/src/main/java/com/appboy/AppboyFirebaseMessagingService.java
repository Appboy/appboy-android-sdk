package com.appboy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.appboy.support.StringUtils;
import com.braze.Braze;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class AppboyFirebaseMessagingService extends FirebaseMessagingService {
  private static final String TAG = BrazeLogger.getBrazeLogTag(AppboyFirebaseMessagingService.class);

  @Override
  public void onNewToken(@NonNull String newToken) {
    super.onNewToken(newToken);
    if (StringUtils.isNullOrEmpty(Braze.getConfiguredApiKey(this))) {
      BrazeLogger.v(TAG, "No configured API key, not registering token in onNewToken. Token: " + newToken);
      return;
    }
    BrazeConfigurationProvider configurationProvider = new BrazeConfigurationProvider(this);
    if (!configurationProvider.getIsFirebaseMessagingServiceOnNewTokenRegistrationEnabled()) {
      BrazeLogger.v(TAG, "Automatic FirebaseMessagingService.OnNewToken() registration"
          + " disabled, not registering token: " + newToken);
      return;
    }
    BrazeLogger.v(TAG, "Registering Firebase push token in onNewToken. Token: " + newToken);
    Braze.getInstance(this).registerAppboyPushMessages(newToken);
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    handleBrazeRemoteMessage(this, remoteMessage);
  }

  /**
   * Consumes an incoming {@link RemoteMessage} if it originated from Braze. If the {@link RemoteMessage} did
   * not originate from Braze, then this method does nothing and returns false.
   *
   * @param remoteMessage The {@link RemoteMessage} from Firebase.
   * @return true iff the {@link RemoteMessage} originated from Braze and was consumed. Returns false
   *         if the {@link RemoteMessage} did not originate from Braze or otherwise could not be forwarded.
   */
  public static boolean handleBrazeRemoteMessage(Context context, RemoteMessage remoteMessage) {
    if (remoteMessage == null) {
      BrazeLogger.w(TAG, "Remote message from FCM was null. Remote message did not originate from Braze.");
      return false;
    }

    if (!isBrazePushNotification(remoteMessage)) {
      BrazeLogger.i(TAG, "Remote message did not originate from Braze. Not consuming remote message: " + remoteMessage);
      return false;
    }

    Map<String, String> remoteMessageData = remoteMessage.getData();
    BrazeLogger.i(TAG, "Got remote message from FCM: " + remoteMessageData);

    Intent pushIntent = new Intent(BrazePushReceiver.FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION);
    Bundle bundle = new Bundle();
    for (Map.Entry<String, String> entry : remoteMessageData.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      BrazeLogger.v(TAG, "Adding bundle item from FCM remote data with key: " + key + " and value: " + value);
      bundle.putString(key, value);
    }
    pushIntent.putExtras(bundle);
    BrazePushReceiver.handleReceivedIntent(context, pushIntent);
    return true;
  }

  /**
   * Determines if the Firebase {@link RemoteMessage} originated from Braze and should be
   * forwarded to {@link AppboyFirebaseMessagingService#handleBrazeRemoteMessage(Context, RemoteMessage)}.
   *
   * @param remoteMessage The {@link RemoteMessage} from {@link FirebaseMessagingService#onMessageReceived(RemoteMessage)}
   * @return true iff this {@link RemoteMessage} originated from Braze or otherwise
   *         should be passed to {@link AppboyFirebaseMessagingService#handleBrazeRemoteMessage(Context, RemoteMessage)}.
   */
  public static boolean isBrazePushNotification(@NonNull RemoteMessage remoteMessage) {
    Map<String, String> remoteMessageData = remoteMessage.getData();
    if (remoteMessageData == null) {
      BrazeLogger.w(TAG, "Remote message data from FCM was null. Returning false for Braze push check. Remote message: " + remoteMessage);
      return false;
    }

    return "true".equals(remoteMessageData.get(Constants.APPBOY_PUSH_APPBOY_KEY));
  }
}
