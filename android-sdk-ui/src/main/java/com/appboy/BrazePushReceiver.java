package com.appboy;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationManagerCompat;

import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.models.push.BrazeNotificationPayload;
import com.appboy.push.AppboyNotificationActionUtils;
import com.appboy.push.AppboyNotificationUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

public class BrazePushReceiver extends BroadcastReceiver {
  private static final String TAG = AppboyLogger.getAppboyLogTag(BrazePushReceiver.class);
  private static final String FCM_MESSAGE_TYPE_KEY = "message_type";
  private static final String FCM_DELETED_MESSAGES_KEY = "deleted_messages";
  private static final String FCM_NUMBER_OF_MESSAGES_DELETED_KEY = "total_deleted";

  /**
   * Internal API. Do not use.
   */
  public static final String FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION = "firebase_messaging_service_routing_action";
  /**
   * Internal API. Do not use.
   */
  public static final String HMS_PUSH_SERVICE_ROUTING_ACTION = "hms_push_service_routing_action";

  @Override
  public void onReceive(Context context, Intent intent) {
    handleReceivedIntent(context, intent);
  }

  public static void handleReceivedIntent(Context context, Intent intent) {
    if (intent == null) {
      AppboyLogger.w(TAG, "Received null intent. Doing nothing.");
      return;
    }
    Context applicationContext = context.getApplicationContext();
    PushHandlerRunnable pushHandlerRunnable = new PushHandlerRunnable(applicationContext, intent);
    new Thread(pushHandlerRunnable).start();
  }

  private static class PushHandlerRunnable implements Runnable {
    private final String mAction;
    private final Context mApplicationContext;
    private final Intent mIntent;

    PushHandlerRunnable(Context applicationContext, @NonNull Intent intent) {
      mApplicationContext = applicationContext;
      mIntent = intent;
      mAction = intent.getAction();
    }

    @Override
    public void run() {
      try {
        performWork();
      } catch (Exception e) {
        AppboyLogger.e(TAG, "Caught exception while performing the push "
            + "notification handling work. Action: " + mAction + " Intent: " + mIntent, e);
      }
    }

    private void performWork() {
      AppboyLogger.i(TAG, "Received broadcast message. Message: " + mIntent.toString());
      String action = mIntent.getAction();
      if (StringUtils.isNullOrEmpty(action)) {
        AppboyLogger.w(TAG, "Push action is null. Not handling intent: " + mIntent);
        return;
      }
      switch (action) {
        case FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION:
        case Constants.APPBOY_STORY_TRAVERSE_CLICKED_ACTION:
        case HMS_PUSH_SERVICE_ROUTING_ACTION:
          handlePushNotificationPayload(mApplicationContext, mIntent);
          break;
        case Constants.APPBOY_CANCEL_NOTIFICATION_ACTION:
          AppboyNotificationUtils.handleCancelNotificationAction(mApplicationContext, mIntent);
          break;
        case Constants.APPBOY_ACTION_CLICKED_ACTION:
          AppboyNotificationActionUtils.handleNotificationActionClicked(mApplicationContext, mIntent);
          break;
        case Constants.APPBOY_STORY_CLICKED_ACTION:
          AppboyNotificationUtils.handlePushStoryPageClicked(mApplicationContext, mIntent);
          break;
        case Constants.APPBOY_PUSH_CLICKED_ACTION:
          AppboyNotificationUtils.handleNotificationOpened(mApplicationContext, mIntent);
          break;
        case Constants.APPBOY_PUSH_DELETED_ACTION:
          AppboyNotificationUtils.handleNotificationDeleted(mApplicationContext, mIntent);
          break;
        default:
          AppboyLogger.w(TAG, "Received a message not sent from Braze. Ignoring the message.");
          break;
      }
    }
  }

  @VisibleForTesting
  static boolean handlePushNotificationPayload(Context context, Intent intent) {
    if (!AppboyNotificationUtils.isAppboyPushMessage(intent)) {
      return false;
    }

    String messageType = intent.getStringExtra(FCM_MESSAGE_TYPE_KEY);
    if (FCM_DELETED_MESSAGES_KEY.equals(messageType)) {
      int totalDeleted = intent.getIntExtra(FCM_NUMBER_OF_MESSAGES_DELETED_KEY, -1);
      if (totalDeleted == -1) {
        AppboyLogger.w(TAG, "Unable to parse FCM message. Intent: " + intent.toString());
      } else {
        AppboyLogger.i(TAG, "FCM deleted " + totalDeleted + " messages. Fetch them from Appboy.");
      }
      return false;
    }

    Bundle notificationExtras = intent.getExtras();
    AppboyLogger.i(TAG, "Push message payload received: " + notificationExtras);

    // Convert the JSON in the extras key into a Bundle.
    Bundle appboyExtras = BrazeNotificationPayload.getAttachedAppboyExtras(notificationExtras);
    notificationExtras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, appboyExtras);

    if (!notificationExtras.containsKey(Constants.APPBOY_PUSH_RECEIVED_TIMESTAMP_MILLIS)) {
      notificationExtras.putLong(Constants.APPBOY_PUSH_RECEIVED_TIMESTAMP_MILLIS, System.currentTimeMillis());
    }

    // This call must occur after the "extras" parsing above since we're expecting
    // a bundle instead of a raw JSON string for the APPBOY_PUSH_EXTRAS_KEY key
    if (AppboyNotificationUtils.isUninstallTrackingPush(notificationExtras)) {
      // Note that this re-implementation of this method does not forward the notification to receivers.
      AppboyLogger.i(TAG, "Push message is uninstall tracking push. Doing nothing. Not forwarding this notification to broadcast receivers.");
      return false;
    }

    AppboyConfigurationProvider appConfigurationProvider = new AppboyConfigurationProvider(context);
    if (appConfigurationProvider.getIsInAppMessageTestPushEagerDisplayEnabled()
        && AppboyNotificationUtils.isInAppMessageTestPush(intent)
        && AppboyInAppMessageManager.getInstance().getActivity() != null) {
      // Pass this test in-app message along for eager display and bypass displaying a push
      AppboyLogger.d(TAG, "Bypassing push display due to test in-app message presence and "
          + "eager test in-app message display configuration setting.");
      AppboyInternal.handleInAppMessageTestPush(context, intent);
      return false;
    }

    BrazeNotificationPayload payload = new BrazeNotificationPayload(context,
        appConfigurationProvider,
        notificationExtras,
        appboyExtras
    );
    // Parse the notification for any associated ContentCard
    AppboyNotificationUtils.handleContentCardsSerializedCardIfPresent(payload);

    if (AppboyNotificationUtils.isNotificationMessage(intent)) {
      AppboyLogger.d(TAG, "Received notification push");
      int notificationId = AppboyNotificationUtils.getNotificationId(payload);
      notificationExtras.putInt(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);

      if (payload.isPushStory()) {
        if (!notificationExtras.containsKey(Constants.APPBOY_PUSH_STORY_IS_NEWLY_RECEIVED)) {
          AppboyLogger.d(TAG, "Received the initial push story notification.");
          notificationExtras.putBoolean(Constants.APPBOY_PUSH_STORY_IS_NEWLY_RECEIVED, true);
        }
      }

      Notification notification = createNotification(payload);
      if (notification == null) {
        AppboyLogger.d(TAG, "Notification created by notification factory was null. Not displaying notification.");
        return false;
      }

      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      notificationManager.notify(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId, notification);
      AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, notificationExtras);
      AppboyNotificationUtils.wakeScreenIfAppropriate(context, appConfigurationProvider, notificationExtras);

      // Set a custom duration for this notification.
      if (payload.getPushDuration() != null) {
        AppboyNotificationUtils.setNotificationDurationAlarm(context, BrazePushReceiver.class, notificationId, payload.getPushDuration());
      }
      return true;
    } else {
      AppboyLogger.d(TAG, "Received silent push");
      AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, notificationExtras);
      AppboyNotificationUtils.requestGeofenceRefreshIfAppropriate(context, notificationExtras);
      return false;
    }
  }

  @SuppressWarnings("deprecation") // createNotification() with old method
  private static Notification createNotification(BrazeNotificationPayload payload) {
    AppboyLogger.v(TAG, "Creating notification with payload:\n" + payload);
    IAppboyNotificationFactory appboyNotificationFactory = AppboyNotificationUtils.getActiveNotificationFactory();
    Notification notification = appboyNotificationFactory.createNotification(payload);
    if (notification == null) {
      AppboyLogger.d(TAG, "Calling older notification factory method after null notification returned on newer method");
      // Use the older factory method on null. Potentially only the one method is implemented
      notification = appboyNotificationFactory.createNotification(payload.getAppboyConfigurationProvider(),
          payload.getContext(),
          payload.getNotificationExtras(),
          payload.getAppboyExtras());
    }

    return notification;
  }
}
