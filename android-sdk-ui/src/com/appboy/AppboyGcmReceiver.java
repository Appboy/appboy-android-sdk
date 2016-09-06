package com.appboy;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.appboy.configuration.XmlAppConfigurationProvider;
import com.appboy.push.AppboyNotificationActionUtils;
import com.appboy.push.AppboyNotificationUtils;
import com.appboy.support.AppboyLogger;

public final class AppboyGcmReceiver extends BroadcastReceiver {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyGcmReceiver.class.getName());
  private static final String GCM_RECEIVE_INTENT_ACTION = "com.google.android.c2dm.intent.RECEIVE";
  private static final String GCM_REGISTRATION_INTENT_ACTION = "com.google.android.c2dm.intent.REGISTRATION";
  private static final String GCM_ERROR_KEY = "error";
  private static final String GCM_REGISTRATION_ID_KEY = "registration_id";
  private static final String GCM_UNREGISTERED_KEY = "unregistered";
  private static final String GCM_MESSAGE_TYPE_KEY = "message_type";
  private static final String GCM_DELETED_MESSAGES_KEY = "deleted_messages";
  private static final String GCM_NUMBER_OF_MESSAGES_DELETED_KEY = "total_deleted";
  public static final String CAMPAIGN_ID_KEY = Constants.APPBOY_PUSH_CAMPAIGN_ID_KEY;

  @Override
  public void onReceive(Context context, Intent intent) {
    AppboyLogger.i(TAG, String.format("Received broadcast message. Message: %s", intent.toString()));
    String action = intent.getAction();
    if (GCM_REGISTRATION_INTENT_ACTION.equals(action)) {
      handleRegistrationEventIfEnabled(new XmlAppConfigurationProvider(context), context, intent);
    } else if (GCM_RECEIVE_INTENT_ACTION.equals(action)) {
      handleAppboyGcmReceiveIntent(context, intent);
    } else if (Constants.APPBOY_CANCEL_NOTIFICATION_ACTION.equals(action)) {
      AppboyNotificationUtils.handleCancelNotificationAction(context, intent);
    } else if (Constants.APPBOY_ACTION_CLICKED_ACTION.equals(action)) {
      AppboyNotificationActionUtils.handleNotificationActionClicked(context, intent);
    } else if (Constants.APPBOY_PUSH_CLICKED_ACTION.equals(action)) {
      AppboyNotificationUtils.handleNotificationOpened(context, intent);
    } else {
      AppboyLogger.w(TAG, String.format("The GCM receiver received a message not sent from Appboy. Ignoring the message."));
    }
  }

  /**
   * Processes the registration/unregistration result returned from the GCM servers. If the
   * registration/unregistration is successful, this will store/clear the registration ID from the
   * device. Otherwise, it will log an error message and the device will not be able to receive GCM
   * messages.
   */
  boolean handleRegistrationIntent(Context context, Intent intent) {
    String error = intent.getStringExtra(GCM_ERROR_KEY);
    String registrationId = intent.getStringExtra(GCM_REGISTRATION_ID_KEY);

    if (error != null) {
      if ("SERVICE_NOT_AVAILABLE".equals(error)) {
        Log.e(TAG, "Unable to connect to the GCM registration server. Try again later.");
      } else if ("ACCOUNT_MISSING".equals(error)) {
        Log.e(TAG, "No Google account found on the phone. For pre-3.0 devices, a Google account is required on the device.");
      } else if ("AUTHENTICATION_FAILED".equals(error)) {
        Log.e(TAG, "Unable to authenticate Google account. For Android versions <4.0.4, a valid Google Play account "
            + "is required for Google Cloud Messaging to function. This phone will be unable to receive Google Cloud "
            + "Messages until the user logs in with a valid Google Play account or upgrades the operating system on this device.");
      } else if ("INVALID_SENDER".equals(error)) {
        Log.e(TAG, "One or multiple of the sender IDs provided are invalid.");
      } else if ("PHONE_REGISTRATION_ERROR".equals(error)) {
        Log.e(TAG, "Device does not support GCM.");
      } else if ("INVALID_PARAMETERS".equals(error)) {
        Log.e(TAG, "The request sent by the device does not contain the expected parameters. This phone does not "
            + "currently support GCM.");
      } else {
        AppboyLogger.w(TAG, String.format("Received an unrecognised GCM registration error type. Ignoring. Error: %s", error));
      }
    } else if (registrationId != null) {
      Appboy.getInstance(context).registerAppboyPushMessages(registrationId);
    } else if (intent.hasExtra(GCM_UNREGISTERED_KEY)) {
      Appboy.getInstance(context).unregisterAppboyPushMessages();
    } else {
      AppboyLogger.w(TAG, "The GCM registration message is missing error information, registration id, and unregistration "
          + "confirmation. Ignoring.");
      return false;
    }
    return true;
  }

  /**
   * Handles both Appboy data push GCM messages and notification messages. Notification messages are
   * posted to the notification center if the GCM message contains a title and body and the payload
   * is sent to the application via an Intent. Data push messages do not post to the notification
   * center, although the payload is forwarded to the application via an Intent as well.
   */
  boolean handleAppboyGcmMessage(Context context, Intent intent) {
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    String messageType = intent.getStringExtra(GCM_MESSAGE_TYPE_KEY);
    if (GCM_DELETED_MESSAGES_KEY.equals(messageType)) {
      int totalDeleted = intent.getIntExtra(GCM_NUMBER_OF_MESSAGES_DELETED_KEY, -1);
      if (totalDeleted == -1) {
        Log.e(TAG, String.format("Unable to parse GCM message. Intent: %s", intent.toString()));
      } else {
        AppboyLogger.i(TAG, String.format("GCM deleted %d messages. Fetch them from Appboy.", totalDeleted));
      }
      return false;
    } else {
      Bundle gcmExtras = intent.getExtras();
      AppboyLogger.d(TAG, String.format("Push message payload received: %s", gcmExtras));

      // Parsing the Appboy data extras (data push).
      // We convert the JSON in the extras key into a Bundle.
      Bundle appboyExtras = AppboyNotificationUtils.getAppboyExtrasWithoutPreprocessing(gcmExtras);
      gcmExtras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, appboyExtras);

      if (AppboyNotificationUtils.isNotificationMessage(intent)) {
        int notificationId = AppboyNotificationUtils.getNotificationId(gcmExtras);
        gcmExtras.putInt(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);
        XmlAppConfigurationProvider appConfigurationProvider = new XmlAppConfigurationProvider(context);

        IAppboyNotificationFactory appboyNotificationFactory = AppboyNotificationUtils.getActiveNotificationFactory();
        Notification notification = appboyNotificationFactory.createNotification(appConfigurationProvider, context, gcmExtras, appboyExtras);

        if (notification == null) {
          AppboyLogger.d(TAG, "Notification created by notification factory was null. Not displaying notification.");
          return false;
        }

        notificationManager.notify(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId, notification);
        AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, gcmExtras);

        // Since we have received a notification, we want to wake the device screen.
        AppboyNotificationUtils.wakeScreenIfHasPermission(context, gcmExtras);

        // Set a custom duration for this notification.
        if (gcmExtras != null && gcmExtras.containsKey(Constants.APPBOY_PUSH_NOTIFICATION_DURATION_KEY)) {
          int durationInMillis = Integer.parseInt(gcmExtras.getString(Constants.APPBOY_PUSH_NOTIFICATION_DURATION_KEY));
          AppboyNotificationUtils.setNotificationDurationAlarm(context, this.getClass(), notificationId, durationInMillis);
        }

        return true;
      } else {
        AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, gcmExtras);
        return false;
      }
    }
  }

  /**
   * Runs the handleAppboyGcmMessage method in a background thread in case of an image push
   * notification, which cannot be downloaded on the main thread.
   */
  public class HandleAppboyGcmMessageTask extends AsyncTask<Void, Void, Void> {
    private final Context mContext;
    private final Intent mIntent;

    public HandleAppboyGcmMessageTask(Context context, Intent intent) {
      mContext = context;
      mIntent = intent;
      execute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        handleAppboyGcmMessage(mContext, mIntent);
      } catch (Exception e) {
        AppboyLogger.e(TAG, "Failed to create and display notification.", e);
      }
      return null;
    }
  }

  void handleAppboyGcmReceiveIntent(Context context, Intent intent) {
    if (AppboyNotificationUtils.isAppboyPushMessage(intent)) {
      new HandleAppboyGcmMessageTask(context, intent);
    }
  }

  boolean handleRegistrationEventIfEnabled(XmlAppConfigurationProvider appConfigurationProvider, Context context, Intent intent) {
    // Only handle GCM registration events if GCM registration handling is turned on in the
    // configuration file.
    if (appConfigurationProvider.isGcmMessagingRegistrationEnabled()) {
      handleRegistrationIntent(context, intent);
      return true;
    }
    return false;
  }
}
