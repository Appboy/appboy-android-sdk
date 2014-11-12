package com.appboy;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.util.Log;

import com.appboy.configuration.XmlAppConfigurationProvider;

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
    Log.i(TAG, String.format("Received broadcast message. Message: %s", intent.toString()));
    String action = intent.getAction();
    if (GCM_REGISTRATION_INTENT_ACTION.equals(action)) {
      XmlAppConfigurationProvider appConfigurationProvider = new XmlAppConfigurationProvider(context);
      handleRegistrationEventIfEnabled(appConfigurationProvider, context, intent);
    } else if (GCM_RECEIVE_INTENT_ACTION.equals(action) && AppboyNotificationUtils.isAppboyPushMessage(intent)) {
      new HandleAppboyGcmMessageTask(context, intent);
    } else if (Constants.APPBOY_CANCEL_NOTIFICATION_ACTION.equals(action) && intent.hasExtra(Constants.APPBOY_CANCEL_NOTIFICATION_TAG)) {
      int notificationId = intent.getIntExtra(Constants.APPBOY_CANCEL_NOTIFICATION_TAG, Constants.APPBOY_DEFAULT_NOTIFICATION_ID);
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId);
    } else {
      Log.w(TAG, String.format("The GCM receiver received a message not sent from Appboy. Ignoring the message."));
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
        Log.e(TAG, "Unable to authenticate Google account. For Android versions <4.0.4, a valid Google Play account " +
          "is required for Google Cloud Messaging to function. This phone will be unable to receive Google Cloud " +
          "Messages until the user logs in with a valid Google Play account or upgrades the operating system on this device.");
      } else if ("INVALID_SENDER".equals(error)) {
        Log.e(TAG, "One or multiple of the sender IDs provided are invalid.");
      } else if ("PHONE_REGISTRATION_ERROR".equals(error)) {
        Log.e(TAG, "Device does not support GCM.");
      } else if ("INVALID_PARAMETERS".equals(error)) {
        Log.e(TAG, "The request sent by the device does not contain the expected parameters. This phone does not " +
          "currently support GCM.");
      } else {
        Log.w(TAG, String.format("Received an unrecognised GCM registration error type. Ignoring. Error: %s", error));
      }
    } else if (registrationId != null) {
      Appboy.getInstance(context).registerAppboyPushMessages(registrationId);
    } else if (intent.hasExtra(GCM_UNREGISTERED_KEY)) {
      Appboy.getInstance(context).unregisterAppboyPushMessages();
    } else {
      Log.w(TAG, "The GCM registration message is missing error information, registration id, and unregistration " +
        "confirmation. Ignoring.");
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
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String messageType = intent.getStringExtra(GCM_MESSAGE_TYPE_KEY);
    if (GCM_DELETED_MESSAGES_KEY.equals(messageType)) {
      int totalDeleted = intent.getIntExtra(GCM_NUMBER_OF_MESSAGES_DELETED_KEY, -1);
      if (totalDeleted == -1) {
        Log.e(TAG, String.format("Unable to parse GCM message. Intent: %s", intent.toString()));
      } else {
        Log.i(TAG, String.format("GCM deleted %d messages. Fetch them from Appboy.", totalDeleted));
      }
      return false;
    } else {
      Bundle extras = intent.getExtras();

      // Parsing the Appboy data extras (data push).
      Bundle appboyExtrasData = AppboyNotificationUtils.createExtrasBundle(AppboyNotificationUtils.bundleOptString(extras, Constants.APPBOY_PUSH_EXTRAS_KEY, "{}"));
      extras.remove(Constants.APPBOY_PUSH_EXTRAS_KEY);
      extras.putBundle(Constants.APPBOY_PUSH_EXTRAS_KEY, appboyExtrasData);

      if (AppboyNotificationUtils.isNotificationMessage(intent)) {
        int notificationId = getNotificationId(extras);
        extras.putInt(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);
        XmlAppConfigurationProvider appConfigurationProvider = new XmlAppConfigurationProvider(context);

        Notification notification = AppboyNotificationUtils.createNotification(appConfigurationProvider, context,
            extras.getString(Constants.APPBOY_PUSH_TITLE_KEY), extras.getString(Constants.APPBOY_PUSH_CONTENT_KEY), extras);
        notificationManager.notify(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId, notification);
        AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, extras);

        // Since we have received a notification, we want to wake the device screen.
        wakeScreenIfHasPermission(context);

        // Set a custom duration for this notification.
        if (extras != null && extras.containsKey(Constants.APPBOY_PUSH_NOTIFICATION_DURATION_KEY)) {
          int durationInMillis = Integer.parseInt(extras.getString(Constants.APPBOY_PUSH_NOTIFICATION_DURATION_KEY));
          AppboyNotificationUtils.setNotificationDurationAlarm(context, this.getClass(), notificationId, durationInMillis);
        }

        return true;
      } else {
        AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, extras);
        return false;
      }
    }
  }

  /**
   * Returns a new id for the new notification we'll send to the notification center.
   * Notification id is used by Android OS to collapse duplicate notifications.
   * If collapse key present - the new id is the hash of the collapse key.
   * If no collapse key present - we want a unique id so we use a hash of the message title & content.
   * Note: Collapse keys are used by the GCM server to collapse duplicate messages.
   */
  int getNotificationId(Bundle extras) {
    if (extras == null) {
      Log.d(TAG, String.format("message without extras bundle received.  Assigning notification id of " + Constants.APPBOY_DEFAULT_NOTIFICATION_ID));
      return Constants.APPBOY_DEFAULT_NOTIFICATION_ID;
    }
    if (extras.containsKey(Constants.APPBOY_GCM_MESSAGE_TYPE_KEY)) {
      return extras.getString(Constants.APPBOY_GCM_MESSAGE_TYPE_KEY).hashCode();
    } else {
      Log.d(TAG, String.format("message without collapse key received: " + extras.toString()));
      String messageKey = AppboyNotificationUtils.bundleOptString(extras, Constants.APPBOY_PUSH_TITLE_KEY, "")
          + AppboyNotificationUtils.bundleOptString(extras, Constants.APPBOY_PUSH_CONTENT_KEY, "");
      return messageKey.hashCode();
    }
  }

  /**
   * This method will wake the device using a wake lock if the WAKE_LOCK permission is present in the
   * manifest. If the permission is not present, this does nothing. If the screen is already on,
   * and the permission is present, this does nothing.
   */
  private void wakeScreenIfHasPermission(Context context) {
    // Check for the wake lock permission
    if (context.checkCallingOrSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_DENIED) {
      return;
    }

    // Get the power manager for the wake lock
    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
    // Acquire the wake lock for some negligible time, then release it. We just want to wake the screen
    // and not take up more CPU power than necessary.
    wakeLock.acquire();
    wakeLock.release();
  }

  /**
   * Runs the handleAppboyGcmMessage method in a background thread in case of an image push
   * notification, which cannot be downloaded on the main thread.
   *
   */
  public class HandleAppboyGcmMessageTask extends AsyncTask<Void, Void, Void> {
    private final Context context;
    private final Intent intent;

    public HandleAppboyGcmMessageTask(Context context, Intent intent) {
      this.context = context;
      this.intent = intent;
      this.execute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
      handleAppboyGcmMessage(this.context, this.intent);
      return null;
    }
  }

  boolean handleRegistrationEventIfEnabled(XmlAppConfigurationProvider appConfigurationProvider,
                                                   Context context, Intent intent) {
    // Only handle GCM registration events if GCM registration handling is turned on in the
    // configuration file.
    if (appConfigurationProvider.isGcmMessagingRegistrationEnabled()) {
      handleRegistrationIntent(context, intent);
      return true;
    }
    return false;
  }
}
