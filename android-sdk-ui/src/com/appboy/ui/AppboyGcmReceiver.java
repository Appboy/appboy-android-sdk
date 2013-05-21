package com.appboy.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.configuration.ManifestConfigurationProvider;

public final class AppboyGcmReceiver extends BroadcastReceiver {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyGcmReceiver.class.getName());
  private static final String GCM_RECEIVE_INTENT_ACTION = "com.google.android.c2dm.intent.RECEIVE";
  private static final String GCM_REGISTRATION_INTENT_ACTION = "com.google.android.c2dm.intent.REGISTRATION";
  private static final String GCM_ERROR_KEY = "error";
  private static final String GCM_REGISTRATION_ID_KEY = "registration_id";
  private static final String GCM_UNREGISTERED_KEY = "unregistered";
  private static final String GCM_MESSAGE_TYPE_KEY = "message_type";
  private static final String GCM_DELETED_MESSAGES_KEY = "deleted_messages";
  private static final String GCM_NUMBER_OF_MESSAGES_DELETED_KEY = "total_deleted";
  private static final String NOTIFICATION_TAG = "appboy_notification";
  public static final String MESSAGE_TYPE_KEY = "collapse_key";
  public static final String MESSAGE_TYPE_FEED = "card";
  public static final String MESSAGE_TYPE_FEEDBACK = "feedback";
  public static final String MESSAGE_TYPE_CAMPAIGN = "campaign";
  public static final String CAMPAIGN_ID_KEY = "cid";
  public static final String TITLE_KEY = "t";
  public static final String CONTENT_KEY = "a";

  private Integer mSmallNotificationIconResourceId;

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, String.format("Received GCM message. Message: %s", intent.toString()));

    String action = intent.getAction();
    if (GCM_REGISTRATION_INTENT_ACTION.equals(action)) {
      handleRegistrationIntent(context, intent);
    } else if (GCM_RECEIVE_INTENT_ACTION.equals(action)) {
      handleGcmMessage(context, intent);
      sendGcmMessageReceivedBroadcast(context, intent.getExtras());
    } else {
      Log.w(TAG, String.format("The GCM receiver received an unexpected message. Ignoring. Message: %s", intent.toString()));
    }
  }

  boolean handleRegistrationIntent(Context context, Intent intent) {
    String error = intent.getStringExtra(GCM_ERROR_KEY);
    String registrationId = intent.getStringExtra(GCM_REGISTRATION_ID_KEY);

    if (error != null) {
      if ("SERVICE_NOT_AVAILABLE".equals(error)) {
        Log.e(TAG, "Unable to connect to the GCM registration server. Try again later.");
        // TODO(martin) - We should try to register again.
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
      Appboy.getInstance(context).registerAppboyGcmMessages(registrationId);
    } else if (intent.hasExtra(GCM_UNREGISTERED_KEY)) {
      Appboy.getInstance(context).unregisterAppboyGcmMessages();
    } else {
      Log.w(TAG, "The GCM registration message is missing error information, registration id, and unregistration " +
        "confirmation. Ignoring.");
      return false;
    }
    return true;
  }

  boolean handleGcmMessage(Context context, Intent intent) {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    String messageType = intent.getStringExtra(GCM_MESSAGE_TYPE_KEY);
    if (GCM_DELETED_MESSAGES_KEY.equals(messageType)) {
      int totalDeleted = intent.getIntExtra(GCM_NUMBER_OF_MESSAGES_DELETED_KEY, -1);
      if (totalDeleted == -1) {
        Log.e(TAG, String.format("Unable to parse GCM message. Intent: %s", intent.toString()));
        return false;
      } else {
        Log.i(TAG, String.format("GCM deleted %d messages. Fetch them from Appboy.", totalDeleted));
      }
    } else {
      Bundle extras = intent.getExtras();
      String collapseKey = extras.getString(MESSAGE_TYPE_KEY);
      String title = extras.getString(TITLE_KEY);
      String content = extras.getString(CONTENT_KEY);
      int notificationId = collapseKey.hashCode();

      Notification notification = createNotification(context, title, content, extras);
      notificationManager.notify(NOTIFICATION_TAG, notificationId, notification);
    }
    return true;
  }

  private void sendGcmMessageReceivedBroadcast(Context context, Bundle extras) {
    String pushReceivedAction = context.getPackageName() + ".intent.APPBOY_PUSH_RECEIVED";
    Intent pushReceivedIntent = new Intent(pushReceivedAction);
    if (extras != null) {
      pushReceivedIntent.putExtras(extras);
    }
    context.sendBroadcast(pushReceivedIntent);
  }

  Notification createNotification(Context context, String title, String content, Bundle extras) {
    int smallNotificationIconResourceId = getSmallNotificationIconResourceId(context);
    if (smallNotificationIconResourceId == 0) {
      Log.e(TAG, "Problem creating notification. Cannot find small notification icon.");
      return null;
    }

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
    notificationBuilder.setSmallIcon(smallNotificationIconResourceId);
    notificationBuilder.setContentTitle(title);
    notificationBuilder.setContentText(content);
    notificationBuilder.setTicker(title);
    notificationBuilder.setAutoCancel(true);

    // Create broadcast intent that will fire when the notification has been opened. To action on these messages,
    // register a broadcast receiver that listens to intent <your_package_name>.intent.APPBOY_NOTIFICATION_OPENED
    // and <your_package_name>.intent.APPBOY_PUSH_RECEIVED.
    String pushOpenedAction = context.getPackageName() + ".intent.APPBOY_NOTIFICATION_OPENED";
    Intent pushOpenedIntent = new Intent(pushOpenedAction);
    if (extras != null) {
      pushOpenedIntent.putExtras(extras);
    }
    PendingIntent pushOpenedPendingIntent = PendingIntent.getBroadcast(context, 0, pushOpenedIntent, PendingIntent.FLAG_ONE_SHOT);
    notificationBuilder.setContentIntent(pushOpenedPendingIntent);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      // Set the remote view
      notificationBuilder.setContent(null);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
    } else {

    }

    return notificationBuilder.build();
  }

  /**
   * Returns the small notification icon resource ID used to display GCM messages. A return value of 0 indicates that
   * the resource cannot be found.
   */
  int getSmallNotificationIconResourceId(Context context) {
    if (mSmallNotificationIconResourceId == null) {
      ConfigurationProvider manifestConfigurationProvider = new ConfigurationProvider(context);
      int resourceId = manifestConfigurationProvider.getSmallNotificationIconResourceId();
      if (resourceId == 0) {
        resourceId = getApplicationIconResourceId(context);
      }
      mSmallNotificationIconResourceId = resourceId;
    }
    return mSmallNotificationIconResourceId;
  }

  int getApplicationIconResourceId(Context context) {
    PackageManager packageManager = context.getPackageManager();
    String packageName = context.getPackageName();
    int resourceId = 0;
    try {
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
      resourceId = applicationInfo.icon;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, String.format("Cannot find package named %s", packageManager));
    }
    return resourceId;
  }
}
