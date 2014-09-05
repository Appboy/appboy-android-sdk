package com.appboy;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.app.Notification;
import android.app.NotificationManager;
import com.appboy.configuration.XmlAppConfigurationProvider;

public final class AppboyAdmReceiver extends BroadcastReceiver {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyAdmReceiver.class.getName());
  private static final String ADM_RECEIVE_INTENT_ACTION = "com.amazon.device.messaging.intent.RECEIVE";
  private static final String ADM_REGISTRATION_INTENT_ACTION = "com.amazon.device.messaging.intent.REGISTRATION";
  private static final String ADM_ERROR_KEY = "error";
  private static final String ADM_REGISTRATION_ID_KEY = "registration_id";
  private static final String ADM_UNREGISTERED_KEY = "unregistered";
  private static final String ADM_MESSAGE_TYPE_KEY = "message_type";
  private static final String ADM_DELETED_MESSAGES_KEY = "deleted_messages";
  private static final String ADM_NUMBER_OF_MESSAGES_DELETED_KEY = "total_deleted";
  public static final String CAMPAIGN_ID_KEY = Constants.APPBOY_PUSH_CAMPAIGN_ID_KEY;

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, String.format("Received ADM message. Message: %s", intent.toString()));
    String action = intent.getAction();
    if (ADM_REGISTRATION_INTENT_ACTION.equals(action)) {
      Log.i(TAG, String.format("Received ADM REGISTRATION. Message: %s", intent.toString()));
      XmlAppConfigurationProvider appConfigurationProvider = new XmlAppConfigurationProvider(context);
      handleRegistrationEventIfEnabled(appConfigurationProvider, context, intent);
    } else if (ADM_RECEIVE_INTENT_ACTION.equals(action) && AppboyNotificationUtils.isAppboyPushMessage(intent)) {
      new HandleAppboyAdmMessageTask(context, intent);
    } else {
      Log.w(TAG, String.format("The ADM receiver received a message not sent from Appboy. Ignoring the message."));
    }
  }

  /**
   * Processes the registration/unregistration result returned from the ADM servers. If the
   * registration/unregistration is successful, this will store/clear the registration ID from the
   * device. Otherwise, it will log an error message and the device will not be able to receive ADM
   * messages.
   */
  boolean handleRegistrationIntent(Context context, Intent intent) {
    String error = intent.getStringExtra(ADM_ERROR_KEY);
    String registrationId = intent.getStringExtra(ADM_REGISTRATION_ID_KEY);

    if (error != null) {
      Log.e(TAG, error);
    } else if (registrationId != null) {
      android.util.Log.i(TAG, "Registering for Adm messages with registrationId: " + registrationId);
      Appboy.getInstance(context).registerAppboyPushMessages(registrationId);
    } else if (intent.hasExtra(ADM_UNREGISTERED_KEY)) {
      Appboy.getInstance(context).unregisterAppboyPushMessages();
    } else {
      Log.w(TAG, "The ADM registration message is missing error information, registration id, and unregistration " +
          "confirmation. Ignoring.");
      return false;
    }
    return true;
  }

  /**
   * Handles both Appboy data push ADM messages and notification messages. Notification messages are
   * posted to the notification center if the ADM message contains a title and body and the payload
   * is sent to the application via an Intent. Data push messages do not post to the notification
   * center, although the payload is forwarded to the application via an Intent as well.
   */
  boolean handleAppboyAdmMessage(Context context, Intent intent) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String messageType = intent.getStringExtra(ADM_MESSAGE_TYPE_KEY);
    if (ADM_DELETED_MESSAGES_KEY.equals(messageType)) {
      int totalDeleted = intent.getIntExtra(ADM_NUMBER_OF_MESSAGES_DELETED_KEY, -1);
      if (totalDeleted == -1) {
        Log.e(TAG, String.format("Unable to parse ADM message. Intent: %s", intent.toString()));
      } else {
        Log.i(TAG, String.format("ADM deleted %d messages. Fetch them from Appboy.", totalDeleted));
      }
      return false;
    } else {
      Bundle extras = intent.getExtras();

      // Parsing the Appboy data extras (data push).
      Bundle appboyExtrasData = AppboyNotificationUtils.createExtrasBundle(AppboyNotificationUtils.bundleOptString(extras, Constants.APPBOY_PUSH_EXTRAS_KEY, "{}"));
      extras.remove(Constants.APPBOY_PUSH_EXTRAS_KEY);
      extras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, appboyExtrasData);

      if (AppboyNotificationUtils.isNotificationMessage(intent)) {
        int notificationId = extras.getString(Constants.APPBOY_ADM_MESSAGE_TYPE_KEY).hashCode();
        extras.putInt(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);
        XmlAppConfigurationProvider appConfigurationProvider = new XmlAppConfigurationProvider(context);
        Notification notification = AppboyNotificationUtils.createNotification(appConfigurationProvider, context,
            extras.getString(Constants.APPBOY_PUSH_TITLE_KEY), extras.getString(Constants.APPBOY_PUSH_CONTENT_KEY), extras);
        notificationManager.notify(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId, notification);
        AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, extras);
        return true;
      } else {
        AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, extras);
        return false;
      }
    }
  }

  /**
   * Runs the handleAppboyAdmMessage method in a background thread in case of an image push
   * notification, which cannot be downloaded on the main thread.
   */
  public class HandleAppboyAdmMessageTask extends AsyncTask<Void, Void, Void> {
    private final Context context;
    private final Intent intent;

    public HandleAppboyAdmMessageTask(Context context, Intent intent) {
      this.context = context;
      this.intent = intent;
      this.execute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
      handleAppboyAdmMessage(this.context, this.intent);
      return null;
    }
  }

  boolean handleRegistrationEventIfEnabled(XmlAppConfigurationProvider appConfigurationProvider,
                                           Context context, Intent intent) {
    // Only handle ADM registration events if ADM registration handling is turned on in the
    // configuration file.
    if (appConfigurationProvider.isAdmMessagingRegistrationEnabled()) {
      handleRegistrationIntent(context, intent);
      return true;
    }
    return false;
  }
}

