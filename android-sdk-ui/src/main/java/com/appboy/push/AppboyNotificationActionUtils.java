package com.appboy.push;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.IAppboyNavigator;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.models.push.BrazeNotificationPayload;
import com.appboy.support.AppboyLogger;
import com.appboy.support.IntentUtils;
import com.appboy.support.StringUtils;
import com.appboy.ui.AppboyNavigator;

import java.util.List;

public class AppboyNotificationActionUtils {
  private static final String TAG = AppboyLogger.getBrazeLogTag(AppboyNotificationActionUtils.class);

  /**
   * @deprecated Please use {@link #addNotificationActions(NotificationCompat.Builder, BrazeNotificationPayload)}
   */
  @Deprecated
  public static void addNotificationActions(Context context,
                                            NotificationCompat.Builder notificationBuilder,
                                            Bundle notificationExtras) {
    BrazeNotificationPayload payload = new BrazeNotificationPayload(context, notificationExtras);
    addNotificationActions(notificationBuilder, payload);
  }

  /**
   * Add notification actions to the provided notification builder.
   */
  public static void addNotificationActions(@NonNull NotificationCompat.Builder notificationBuilder,
                                            @NonNull BrazeNotificationPayload payload) {
    if (payload.getContext() == null) {
      AppboyLogger.d(TAG, "Context cannot be null when adding notification buttons.");
      return;
    }
    final List<BrazeNotificationPayload.ActionButton> actionButtons = payload.getActionButtons();
    if (actionButtons.isEmpty()) {
      AppboyLogger.d(TAG, "No action buttons present. Not adding notification actions");
      return;
    }

    for (BrazeNotificationPayload.ActionButton actionButton : actionButtons) {
      AppboyLogger.v(TAG, "Adding action button: " + actionButton);
      addNotificationAction(notificationBuilder, payload, actionButton);
    }
  }

  /**
   * Handles clicks on notification action buttons in the notification center. Called by FCM/ADM
   * receiver when an Braze notification action button is clicked. The FCM/ADM receiver passes on
   * the intent from the notification action button click intent.
   *
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
   * @deprecated Please use {@link #addNotificationAction(BrazeNotificationPayload.ActionButton)}
   */
  @Deprecated
  public static void addNotificationAction(Context context,
                                           NotificationCompat.Builder notificationBuilder,
                                           Bundle notificationExtras,
                                           int actionIndex) {
    BrazeNotificationPayload payload = new BrazeNotificationPayload(context, notificationExtras);
    final List<BrazeNotificationPayload.ActionButton> actionButtons = payload.getActionButtons();
    if (actionIndex - 1 > actionButtons.size()) {
      return;
    }
    addNotificationAction(notificationBuilder, payload, actionButtons.get(actionIndex));
  }

  /**
   * Add the notification action at the specified index to the notification builder.
   */
  public static void addNotificationAction(@NonNull NotificationCompat.Builder notificationBuilder,
                                           @NonNull BrazeNotificationPayload payload,
                                           @NonNull BrazeNotificationPayload.ActionButton actionButton) {
    final Context context = payload.getContext();
    if (context == null) {
      AppboyLogger.d(TAG, "Cannot add notification action with null context from payload");
      return;
    }

    final Bundle notificationActionExtras = new Bundle(payload.getNotificationExtras());
    final int actionIndex = actionButton.getActionIndex();
    notificationActionExtras.putInt(Constants.APPBOY_ACTION_INDEX_KEY, actionIndex);

    final String actionType = actionButton.getType();
    notificationActionExtras.putString(Constants.APPBOY_ACTION_TYPE_KEY, actionType);

    final String actionIdAtIndex = actionButton.getActionId();
    notificationActionExtras.putString(Constants.APPBOY_ACTION_ID_KEY, actionIdAtIndex);

    final String uriKeyAtIndex = actionButton.getUri();
    notificationActionExtras.putString(Constants.APPBOY_ACTION_URI_KEY, uriKeyAtIndex);

    final String useWebviewKeyAtIndex = actionButton.getUseWebview();
    notificationActionExtras.putString(Constants.APPBOY_ACTION_USE_WEBVIEW_KEY, useWebviewKeyAtIndex);

    PendingIntent pendingSendIntent;
    Intent sendIntent;
    final int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | IntentUtils.getDefaultPendingIntentFlags();
    if (Constants.APPBOY_PUSH_ACTION_TYPE_NONE.equals(actionType)) {
      // If no action is present, then we don't need the
      // trampoline to route us back to an Activity.
      AppboyLogger.v(TAG, "Adding notification action with type: " + actionType
          + " . Setting intent class to notification receiver: "
          + AppboyNotificationUtils.getNotificationReceiverClass());
      sendIntent = new Intent(Constants.APPBOY_ACTION_CLICKED_ACTION).setClass(context, AppboyNotificationUtils.getNotificationReceiverClass());
      sendIntent.putExtras(notificationActionExtras);
      pendingSendIntent = PendingIntent.getBroadcast(context, IntentUtils.getRequestCode(), sendIntent, pendingIntentFlags);
    } else {
      // However, if an action is present, then we need to
      // route to the trampoline to ensure the user is
      // prompted to open the app on the lockscreen.
      AppboyLogger.v(TAG, "Adding notification action with type: "
          + actionType + " Setting intent class to trampoline activity");
      sendIntent = new Intent(Constants.APPBOY_ACTION_CLICKED_ACTION)
          .setClass(context, NotificationTrampolineActivity.class);
      sendIntent
          .setFlags(sendIntent.getFlags() | AppboyNavigator.getAppboyNavigator().getIntentFlags(IAppboyNavigator.IntentFlagPurpose.NOTIFICATION_ACTION_WITH_DEEPLINK));
      sendIntent.putExtras(notificationActionExtras);
      pendingSendIntent = PendingIntent.getActivity(context, IntentUtils.getRequestCode(), sendIntent, pendingIntentFlags);
    }

    NotificationCompat.Action.Builder notificationActionBuilder = new NotificationCompat.Action.Builder(0, actionButton.getText(), pendingSendIntent);
    notificationActionBuilder.addExtras(new Bundle(notificationActionExtras));
    notificationBuilder.addAction(notificationActionBuilder.build());
    AppboyLogger.v(TAG, "Added action with bundle: " + notificationActionExtras);
  }

  /**
   * Log an action button clicked event. Logging requires a valid campaign Id and action button Id.
   *
   * @param intent the action button click intent
   */
  public static void logNotificationActionClicked(Context context, Intent intent, String actionType) {
    String campaignId = intent.getStringExtra(Constants.APPBOY_PUSH_CAMPAIGN_ID_KEY);
    String actionButtonId = intent.getStringExtra(Constants.APPBOY_ACTION_ID_KEY);
    Appboy.getInstance(context).logPushNotificationActionClicked(campaignId, actionButtonId, actionType);
  }
}
