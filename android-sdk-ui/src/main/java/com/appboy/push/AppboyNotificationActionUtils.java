package com.appboy.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.support.AppboyLogger;
import com.appboy.support.IntentUtils;
import com.appboy.support.StringUtils;

public class AppboyNotificationActionUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyNotificationActionUtils.class);

  /**
   * Add notification actions to the provided notification builder.
   *
   * Notification action button schema:
   *
   * “ab_a*_id”: action button id, used for analytics - optional
   * “ab_a*_t”: action button text - optional
   * “ab_a*_a”: action type, one of “ab_uri”, ”ab_none”, “ab_open” (open the app) - required
   * “ab_a*_uri”: uri, only used when the action is “uri” - required only when action is “uri”
   * “ab_a*_use_webview”: whether to open the web link in a web view - optional
   *
   * The * is replaced with an integer string depending on the button being described
   * (e.g. the uri for the second button is “ab_a1_uri”).
   * The left-most button is defined as button zero.
   *
   * @param context
   * @param notificationBuilder
   * @param notificationExtras FCM/ADM extras
   */
  public static void addNotificationActions(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    try {
      if (notificationExtras == null) {
        AppboyLogger.w(TAG, "Notification extras were null. Doing nothing.");
        return;
      }

      int actionIndex = 0;
      while (!StringUtils.isNullOrBlank(getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE))) {
        addNotificationAction(context, notificationBuilder, notificationExtras, actionIndex);
        actionIndex++;
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Caught exception while adding notification action buttons.", e);
    }
  }

  /**
   * Handles clicks on notification action buttons in the notification center. Called by FCM/ADM
   * receiver when an Braze notification action button is clicked. The FCM/ADM receiver passes on
   * the intent from the notification action button click intent.
   *
   * @param context
   * @param intent the action button click intent
   */
  public static void handleNotificationActionClicked(Context context, Intent intent) {
    try {
      String actionType = intent.getStringExtra(Constants.APPBOY_ACTION_TYPE_KEY);
      if (StringUtils.isNullOrBlank(actionType)) {
        AppboyLogger.w(TAG, "Notification action button type was blank or null. Doing nothing.");
        return;
      }
      int notificationId = intent.getIntExtra(Constants.APPBOY_PUSH_NOTIFICATION_ID, Constants.APPBOY_DEFAULT_NOTIFICATION_ID);

      // Logs that the notification action was clicked.
      // Click analytics for all action types are logged.
      logNotificationActionClicked(context, intent, actionType);

      if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_URI) || actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_OPEN)) {
        AppboyNotificationUtils.cancelNotification(context, notificationId);
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_URI) && intent.getExtras().containsKey(Constants.APPBOY_ACTION_URI_KEY)) {
          // Set the deep link that to open to the correct action's deep link.
          intent.putExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY, intent.getStringExtra(Constants.APPBOY_ACTION_URI_KEY));
          if (intent.getExtras().containsKey(Constants.APPBOY_ACTION_USE_WEBVIEW_KEY)) {
            intent.putExtra(Constants.APPBOY_PUSH_OPEN_URI_IN_WEBVIEW_KEY,
                intent.getStringExtra(Constants.APPBOY_ACTION_USE_WEBVIEW_KEY));
          }
        } else {
          // Otherwise, remove any existing deep links.
          intent.removeExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY);
        }
        AppboyNotificationUtils.sendNotificationOpenedBroadcast(context, intent);

        AppboyConfigurationProvider appConfigurationProvider = new AppboyConfigurationProvider(context);
        if (appConfigurationProvider.getHandlePushDeepLinksAutomatically()) {
          AppboyNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
        }
      } else if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_NONE)) {
        AppboyNotificationUtils.cancelNotification(context, notificationId);
      } else {
        AppboyLogger.w(TAG, "Unknown notification action button clicked. Doing nothing.");
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Caught exception while handling notification action button click.", e);
    }
  }

  /**
   * Add the notification action at the specified index to the notification builder.
   *
   * @param context
   * @param notificationBuilder
   * @param notificationExtras
   * @param actionIndex
   */
  private static void addNotificationAction(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras, int actionIndex) {
    Bundle notificationActionExtras = new Bundle(notificationExtras);

    String actionType = getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE);
    notificationActionExtras.putInt(Constants.APPBOY_ACTION_INDEX_KEY, actionIndex);
    notificationActionExtras.putString(Constants.APPBOY_ACTION_TYPE_KEY, actionType);
    notificationActionExtras.putString(Constants.APPBOY_ACTION_ID_KEY, getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_ID_KEY_TEMPLATE));
    notificationActionExtras.putString(Constants.APPBOY_ACTION_URI_KEY, getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE));
    notificationActionExtras.putString(Constants.APPBOY_ACTION_USE_WEBVIEW_KEY,
        getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_USE_WEBVIEW_KEY_TEMPLATE));

    PendingIntent pendingSendIntent;
    if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_NONE)) {
      // If no action is present, then we don't need the AppboyNotificationRoutingActivity.class to route us back to an Activity.

      AppboyLogger.v(TAG, "Adding notification action with type: " + actionType + " . Setting intent class to notification receiver.");
      Intent sendIntent = new Intent(Constants.APPBOY_ACTION_CLICKED_ACTION).setClass(context, AppboyNotificationUtils.getNotificationReceiverClass());
      sendIntent.putExtras(notificationActionExtras);
      pendingSendIntent = PendingIntent.getBroadcast(context, IntentUtils.getRequestCode(), sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    } else {
      // However, if an action is present, then we need to route to the AppboyNotificationRoutingActivity to ensure
      // the user is prompted to open the app on the lockscreen.

      AppboyLogger.v(TAG, "Adding notification action with type: " + actionType + " Setting intent class to AppboyNotificationRoutingActivity");
      Intent sendIntent = new Intent(Constants.APPBOY_ACTION_CLICKED_ACTION).setClass(context, AppboyNotificationRoutingActivity.class);
      sendIntent.putExtras(notificationActionExtras);
      pendingSendIntent = PendingIntent.getActivity(context, IntentUtils.getRequestCode(), sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    String actionText = getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE);
    NotificationCompat.Action.Builder notificationActionBuilder = new NotificationCompat.Action.Builder(0, actionText, pendingSendIntent);
    notificationActionBuilder.addExtras(new Bundle(notificationActionExtras));
    notificationBuilder.addAction(notificationActionBuilder.build());
  }

  /**
   * Log an action button clicked event. Logging requires a valid campaign Id and action button Id.
   *
   * @param context
   * @param intent the action button click intent
   */
  private static void logNotificationActionClicked(Context context, Intent intent, String actionType) {
    String campaignId = intent.getStringExtra(Constants.APPBOY_PUSH_CAMPAIGN_ID_KEY);
    String actionButtonId = intent.getStringExtra(Constants.APPBOY_ACTION_ID_KEY);
    Appboy.getInstance(context).logPushNotificationActionClicked(campaignId, actionButtonId, actionType);
  }

  /**
   * Returns the value for the given action field key template at the specified index.
   *
   * @param actionIndex the index of the desired action
   * @param notificationExtras FCM/ADM notification extras
   * @param actionFieldKeyTemplate the template of the action field
   * @return the desired notification action field value or the empty string if not present
   */
  public static String getActionFieldAtIndex(int actionIndex, Bundle notificationExtras, String actionFieldKeyTemplate) {
    return getActionFieldAtIndex(actionIndex, notificationExtras, actionFieldKeyTemplate, "");
  }

  /**
   * Returns the value for the given action field key template at the specified index.
   *
   * @param actionIndex the index of the desired action
   * @param notificationExtras FCM/ADM notification extras
   * @param actionFieldKeyTemplate the template of the action field
   * @param defaultValue the default value to return if the value for the key in notificationExtras
   *                     is null.
   * @return the desired notification action field value or the empty string if not present
   */
  public static String getActionFieldAtIndex(int actionIndex, Bundle notificationExtras, String actionFieldKeyTemplate, String defaultValue) {
    String actionFieldKey = actionFieldKeyTemplate.replace("*", String.valueOf(actionIndex));
    String actionFieldValue = notificationExtras.getString(actionFieldKey);
    if (actionFieldValue == null) {
      return defaultValue;
    } else {
      return actionFieldValue;
    }
  }
}
