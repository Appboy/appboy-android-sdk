package com.braze.push;

import static com.appboy.models.push.BrazeNotificationPayload.getAttachedBrazeExtras;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationManagerCompat;

import com.appboy.BrazeInternal;
import com.appboy.Constants;
import com.appboy.models.push.BrazeNotificationPayload;
import com.braze.Braze;
import com.braze.IBrazeNotificationFactory;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;

public class BrazePushReceiver extends BroadcastReceiver {
  private static final String TAG = BrazeLogger.getBrazeLogTag(BrazePushReceiver.class);

  // ADM keys match FCM for these fields.
  private static final String MESSAGE_TYPE_KEY = "message_type";
  private static final String DELETED_MESSAGES_KEY = "deleted_messages";
  private static final String NUMBER_OF_MESSAGES_DELETED_KEY = "total_deleted";

  private static final String ADM_RECEIVE_INTENT_ACTION = "com.amazon.device.messaging.intent.RECEIVE";
  private static final String ADM_REGISTRATION_INTENT_ACTION = "com.amazon.device.messaging.intent.REGISTRATION";
  private static final String ADM_ERROR_KEY = "error";
  private static final String ADM_ERROR_DESCRIPTION_KEY = "error_description";
  private static final String ADM_REGISTRATION_ID_KEY = "registration_id";
  private static final String ADM_UNREGISTERED_KEY = "unregistered";

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
    handleReceivedIntent(context, intent, true);
  }

  public static void handleReceivedIntent(Context context, Intent intent, boolean runOnThread) {
    if (intent == null) {
      BrazeLogger.w(TAG, "Received null intent. Doing nothing.");
      return;
    }
    PushHandlerRunnable pushHandlerRunnable;
    if (runOnThread) {
      // Don't pass an Activity context into a background thread
      pushHandlerRunnable = new PushHandlerRunnable(context.getApplicationContext(), intent);
      new Thread(pushHandlerRunnable).start();
    } else {
      // Run on the caller thread
      pushHandlerRunnable = new PushHandlerRunnable(context, intent);
      pushHandlerRunnable.run();
    }
  }

  private static class PushHandlerRunnable implements Runnable {
    private final String mAction;
    private final Context mContext;
    private final Intent mIntent;

    PushHandlerRunnable(Context context, @NonNull Intent intent) {
      mContext = context;
      mIntent = intent;
      mAction = intent.getAction();
    }

    @Override
    public void run() {
      try {
        performWork();
      } catch (Exception e) {
        BrazeLogger.e(TAG, "Caught exception while performing the push "
            + "notification handling work. Action: " + mAction + " Intent: " + mIntent, e);
      }
    }

    private void performWork() {
      BrazeLogger.i(TAG, "Received broadcast message. Message: " + mIntent.toString());
      String action = mIntent.getAction();
      if (StringUtils.isNullOrEmpty(action)) {
        BrazeLogger.w(TAG, "Push action is null. Not handling intent: " + mIntent);
        return;
      }
      switch (action) {
        case FIREBASE_MESSAGING_SERVICE_ROUTING_ACTION:
        case Constants.APPBOY_STORY_TRAVERSE_CLICKED_ACTION:
        case HMS_PUSH_SERVICE_ROUTING_ACTION:
        case ADM_RECEIVE_INTENT_ACTION:
          handlePushNotificationPayload(mContext, mIntent);
          break;
        case ADM_REGISTRATION_INTENT_ACTION:
          handleAdmRegistrationEventIfEnabled(new BrazeConfigurationProvider(mContext), mContext, mIntent);
          break;
        case Constants.APPBOY_CANCEL_NOTIFICATION_ACTION:
          BrazeNotificationUtils.handleCancelNotificationAction(mContext, mIntent);
          break;
        case Constants.APPBOY_ACTION_CLICKED_ACTION:
          BrazeNotificationActionUtils.handleNotificationActionClicked(mContext, mIntent);
          break;
        case Constants.APPBOY_STORY_CLICKED_ACTION:
          BrazeNotificationUtils.handlePushStoryPageClicked(mContext, mIntent);
          break;
        case Constants.APPBOY_PUSH_CLICKED_ACTION:
          BrazeNotificationUtils.handleNotificationOpened(mContext, mIntent);
          break;
        case Constants.APPBOY_PUSH_DELETED_ACTION:
          BrazeNotificationUtils.handleNotificationDeleted(mContext, mIntent);
          break;
        default:
          BrazeLogger.w(TAG, "Received a message not sent from Braze. Ignoring the message.");
          break;
      }
    }
  }

  @VisibleForTesting
  static boolean handleAdmRegistrationEventIfEnabled(BrazeConfigurationProvider appConfigurationProvider, Context context, Intent intent) {
    BrazeLogger.i(TAG, "Received ADM registration. Message: " + intent.toString());
    // Only handle ADM registration events if ADM registration handling is turned on in the
    // configuration file.
    if (Constants.isAmazonDevice() && appConfigurationProvider.isAdmMessagingRegistrationEnabled()) {
      BrazeLogger.d(TAG, "ADM enabled in braze.xml. Continuing to process ADM registration intent.");
      handleAdmRegistrationIntent(context, intent);
      return true;
    }
    BrazeLogger.w(TAG, "ADM not enabled in braze.xml. Ignoring ADM registration intent. Note: you must set "
            + "com_appboy_push_adm_messaging_registration_enabled to true in your braze.xml to enable ADM.");
    return false;
  }

  /**
   * Processes the registration/unregistration result returned from the ADM servers. If the
   * registration/unregistration is successful, this will store/clear the registration ID from the
   * device. Otherwise, it will log an error message and the device will not be able to receive ADM
   * messages.
   */
  @VisibleForTesting
  static boolean handleAdmRegistrationIntent(Context context, Intent intent) {
    String error = intent.getStringExtra(ADM_ERROR_KEY);
    String errorDescription = intent.getStringExtra(ADM_ERROR_DESCRIPTION_KEY);
    String registrationId = intent.getStringExtra(ADM_REGISTRATION_ID_KEY);
    String unregistered = intent.getStringExtra(ADM_UNREGISTERED_KEY);

    if (error != null) {
      BrazeLogger.w(TAG, "Error during ADM registration: " + error + " description: " + errorDescription);
    } else if (registrationId != null) {
      BrazeLogger.i(TAG, "Registering for ADM messages with registrationId: " + registrationId);
      Braze.getInstance(context).registerAppboyPushMessages(registrationId);
    } else if (unregistered != null) {
      BrazeLogger.w(TAG, "The device was un-registered from ADM: " + unregistered);
    } else {
      BrazeLogger.w(TAG, "The ADM registration intent is missing error information, registration id, and unregistration "
              + "confirmation. Ignoring.");
      return false;
    }
    return true;
  }

  @VisibleForTesting
  static boolean handlePushNotificationPayload(Context context, Intent intent) {
    if (!BrazeNotificationUtils.isAppboyPushMessage(intent)) {
      return false;
    }

    String messageType = intent.getStringExtra(MESSAGE_TYPE_KEY);
    if (DELETED_MESSAGES_KEY.equals(messageType)) {
      int totalDeleted = intent.getIntExtra(NUMBER_OF_MESSAGES_DELETED_KEY, -1);
      if (totalDeleted == -1) {
        BrazeLogger.w(TAG, "Unable to parse FCM message. Intent: " + intent.toString());
      } else {
        BrazeLogger.i(TAG, "FCM deleted " + totalDeleted + " messages. Fetch them from Appboy.");
      }
      return false;
    }

    Bundle notificationExtras = intent.getExtras();
    BrazeLogger.i(TAG, "Push message payload received: " + notificationExtras);

    // Convert the JSON in the extras key into a Bundle.
    Bundle brazeExtras = getAttachedBrazeExtras(notificationExtras);
    notificationExtras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, brazeExtras);

    if (!notificationExtras.containsKey(Constants.APPBOY_PUSH_RECEIVED_TIMESTAMP_MILLIS)) {
      notificationExtras.putLong(Constants.APPBOY_PUSH_RECEIVED_TIMESTAMP_MILLIS, System.currentTimeMillis());
    }

    // This call must occur after the "extras" parsing above since we're expecting
    // a bundle instead of a raw JSON string for the APPBOY_PUSH_EXTRAS_KEY key
    if (BrazeNotificationUtils.isUninstallTrackingPush(notificationExtras)) {
      // Note that this re-implementation of this method does not forward the notification to receivers.
      BrazeLogger.i(TAG, "Push message is uninstall tracking push. Doing nothing. Not forwarding this notification to broadcast receivers.");
      return false;
    }

    BrazeConfigurationProvider appConfigurationProvider = new BrazeConfigurationProvider(context);
    if (appConfigurationProvider.isInAppMessageTestPushEagerDisplayEnabled()
        && BrazeNotificationUtils.isInAppMessageTestPush(intent)
        && BrazeInAppMessageManager.getInstance().getActivity() != null) {
      // Pass this test in-app message along for eager display and bypass displaying a push
      BrazeLogger.d(TAG, "Bypassing push display due to test in-app message presence and "
          + "eager test in-app message display configuration setting.");
      BrazeInternal.handleInAppMessageTestPush(context, intent);
      return false;
    }

    BrazeNotificationPayload payload = createPayload(context, appConfigurationProvider, notificationExtras, brazeExtras);

    // Parse the notification for any associated ContentCard
    BrazeNotificationUtils.handleContentCardsSerializedCardIfPresent(payload);

    if (BrazeNotificationUtils.isNotificationMessage(intent)) {
      BrazeLogger.d(TAG, "Received notification push");
      int notificationId = BrazeNotificationUtils.getNotificationId(payload);
      notificationExtras.putInt(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);

      if (payload.isPushStory()) {
        if (Constants.isAmazonDevice()) {
          // In case the backend does send these, handle them gracefully
          return false;
        }
        if (!notificationExtras.containsKey(Constants.APPBOY_PUSH_STORY_IS_NEWLY_RECEIVED)) {
          BrazeLogger.d(TAG, "Received the initial push story notification.");
          notificationExtras.putBoolean(Constants.APPBOY_PUSH_STORY_IS_NEWLY_RECEIVED, true);
        }
      }

      Notification notification = createNotification(payload);
      if (notification == null) {
        BrazeLogger.d(TAG, "Notification created by notification factory was null. Not displaying notification.");
        return false;
      }

      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      notificationManager.notify(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId, notification);
      BrazeNotificationUtils.sendPushMessageReceivedBroadcast(context, notificationExtras);
      BrazeNotificationUtils.wakeScreenIfAppropriate(context, appConfigurationProvider, notificationExtras);

      // Set a custom duration for this notification.
      if (payload.getPushDuration() != null) {
        BrazeNotificationUtils.setNotificationDurationAlarm(context, BrazePushReceiver.class, notificationId, payload.getPushDuration());
      }
      return true;
    } else {
      BrazeLogger.d(TAG, "Received silent push");
      BrazeNotificationUtils.sendPushMessageReceivedBroadcast(context, notificationExtras);
      BrazeNotificationUtils.requestGeofenceRefreshIfAppropriate(context, notificationExtras);
      return false;
    }
  }

  @VisibleForTesting
  static BrazeNotificationPayload createPayload(Context context, BrazeConfigurationProvider appConfigurationProvider, Bundle notificationExtras, Bundle brazeExtras) {
    // ADM uses a different constructor here because the data is already flattened.
    if (Constants.isAmazonDevice()) {
      return new BrazeNotificationPayload(notificationExtras, getAttachedBrazeExtras(
          notificationExtras
      ), context, appConfigurationProvider);
    } else {
      return new BrazeNotificationPayload(notificationExtras, brazeExtras, context, appConfigurationProvider);
    }
  }

  @SuppressWarnings("deprecation") // createNotification() with old method
  private static Notification createNotification(BrazeNotificationPayload payload) {
    BrazeLogger.v(TAG, "Creating notification with payload:\n" + payload);
    IBrazeNotificationFactory appboyNotificationFactory = BrazeNotificationUtils.getActiveNotificationFactory();
    return appboyNotificationFactory.createNotification(payload);
  }
}
