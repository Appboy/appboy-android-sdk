package com.appboy.push;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.appboy.Appboy;
import com.appboy.AppboyGcmReceiver;
import com.appboy.Constants;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.IntentUtils;
import com.appboy.support.PermissionUtils;
import com.appboy.support.StringUtils;

public class AppboyNotificationActionUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyNotificationActionUtils.class.getName());
  public static final String DEFAULT_LOCAL_STORAGE_FOLDER = "Shared Photos";
  public static final String IMAGE_MIME_TYPE = "image/*";
  public static final String TEXT_MIME_TYPE = "text/plain";

  /**
   * Add notification actions to the provided notification builder.
   *
   * Notification action button schema:
   *
   * “ab_a*_id”: action button id, used for analytics - optional
   * “ab_a*_t”: action button text - optional
   * “ab_a*_a”: action type, one of “ab_uri”, ”ab_none”, “ab_share”, “ab_open” (open the app), or a custom defined action - required
   * “ab_a*_uri”: uri, only used when the action is “uri” - required only when action is “uri”
   *
   * The * is replaced with an integer string depending on the button being described
   * (e.g. the uri for the second button is “ab_a1_uri”).
   * The left-most button is defined as button zero.
   *
   * @param context
   * @param notificationBuilder
   * @param notificationExtras GCM/ADM extras
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static void addNotificationActions(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    try {
      if (notificationExtras == null) {
        AppboyLogger.w(TAG, "Notification extras were null. Doing nothing.");
        return;
      }
      // Notification actions were added in Jelly Bean
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
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
   * Handles clicks on notification action buttons in the notification center.  Called by GCM/ADM
   * receiver when an Appboy notification action button is clicked.  The GCM/ADM receiver passes on
   * the intent from the notification action button click intent.
   *
   * See {@link #logNotificationActionClicked} and {@link #handleShareActionClicked}
   *
   * @param context
   * @param intent the action button click intent
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static void handleNotificationActionClicked(Context context, Intent intent) {
    try {
      String actionType = intent.getStringExtra(Constants.APPBOY_ACTION_TYPE_KEY);
      if (StringUtils.isNullOrBlank(actionType)) {
        AppboyLogger.w(TAG, "Notification action button type was blank or null.  Doing nothing.");
        return;
      }
      int notificationId = intent.getIntExtra(Constants.APPBOY_PUSH_NOTIFICATION_ID, Constants.APPBOY_DEFAULT_NOTIFICATION_ID);

      // Logs that the notification action was clicked.
      // Notification actions with no click actions are not logged.
      if (!actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_NONE)) {
        logNotificationActionClicked(context, intent);
      }
      if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_URI) || actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_OPEN)) {
        AppboyNotificationUtils.cancelNotification(context, notificationId);
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_URI) && intent.getExtras().containsKey(Constants.APPBOY_ACTION_URI_KEY)) {
          // Set the deep link that to open to the correct action's deep link.
          intent.putExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY, intent.getStringExtra(Constants.APPBOY_ACTION_URI_KEY));
        } else {
          // Otherwise, remove any existing deep links.
          intent.removeExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY);
        }
        AppboyNotificationUtils.sendNotificationOpenedBroadcast(context, intent);

        AppboyConfigurationProvider appConfigurationProvider = new AppboyConfigurationProvider(context);
        if (appConfigurationProvider.getHandlePushDeepLinksAutomatically()) {
          AppboyNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
        }
      } else if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_SHARE)) {
        AppboyNotificationUtils.cancelNotification(context, notificationId);
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        handleShareActionClicked(context, intent);
      } else if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_NONE)) {
        AppboyNotificationUtils.cancelNotification(context, notificationId);
      } else {
        AppboyLogger.i(TAG, "Custom notification action button clicked. Doing nothing and passing on data to client receiver.");
        AppboyNotificationUtils.sendNotificationOpenedBroadcast(context, intent);
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Caught exception while handling notification action button click.", e);
    }
  }

  /**
   * Whether the preconditions for sharing an image are met.
   *
   * These include an image URI in the push extras, external write permission, and whether a
   * downloaded image is available for the current push notification.
   *
   * @param context
   * @param appboyExtras
   * @return whether sharing an image is currently possible.
   */
  static boolean canShareImage(Context context, Bundle appboyExtras) {
    return appboyExtras != null
        && appboyExtras.containsKey(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY)
        && PermissionUtils.hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
  }

  /**
   * @param actionType
   * @return true if the given action type is not an Appboy preset action type
   */
  static boolean isCustomActionType(String actionType) {
    if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_URI)) {
      return false;
    } else if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_OPEN)) {
      return false;
    } else if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_NONE)) {
      return false;
    } else if (actionType.equals(Constants.APPBOY_PUSH_ACTION_TYPE_SHARE)) {
      return false;
    }
    return true;
  }

  /**
   * Add the notification action at the specified index to the notification builder.
   *
   * @param context
   * @param notificationBuilder
   * @param notificationExtras
   * @param actionIndex
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private static void addNotificationAction(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras, int actionIndex) {
    Bundle notificationActionExtras = new Bundle(notificationExtras);

    String actionType = getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_TYPE_KEY_TEMPLATE);
    notificationActionExtras.putInt(Constants.APPBOY_ACTION_INDEX_KEY, actionIndex);
    notificationActionExtras.putString(Constants.APPBOY_ACTION_TYPE_KEY, actionType);
    notificationActionExtras.putString(Constants.APPBOY_ACTION_ID_KEY, getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_ID_KEY_TEMPLATE));
    notificationActionExtras.putString(Constants.APPBOY_ACTION_URI_KEY, getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_URI_KEY_TEMPLATE));
    notificationActionExtras.putBoolean(Constants.APPBOY_ACTION_IS_CUSTOM_ACTION_KEY, isCustomActionType(actionType));

    Intent sendIntent = new Intent(Constants.APPBOY_ACTION_CLICKED_ACTION).setClass(context, AppboyNotificationUtils.getNotificationReceiverClass());
    sendIntent.putExtras(notificationActionExtras);

    String actionText = getActionFieldAtIndex(actionIndex, notificationExtras, Constants.APPBOY_PUSH_ACTION_TEXT_KEY_TEMPLATE);
    PendingIntent pendingSendIntent = PendingIntent.getBroadcast(context, IntentUtils.getRequestCode(), sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Action.Builder notificationActionBuilder = new NotificationCompat.Action.Builder(0, actionText, pendingSendIntent);
    notificationActionBuilder.addExtras(new Bundle(notificationActionExtras));
    notificationBuilder.addAction(notificationActionBuilder.build());
  }

  /**
   * Log an action button clicked event.  Logging requires a valid campaign Id and action button Id.
   *
   * @param context
   * @param intent the action button click intent
   */
  private static void logNotificationActionClicked(Context context, Intent intent) {
    String campaignId = intent.getStringExtra(AppboyGcmReceiver.CAMPAIGN_ID_KEY);
    String actionButtonId = intent.getStringExtra(Constants.APPBOY_ACTION_ID_KEY);
    if (StringUtils.isNullOrBlank(campaignId)) {
      AppboyLogger.i(TAG, "No campaign Id associated with this notification.  Not logging push action click to Appboy.");
      return;
    }
    if (StringUtils.isNullOrBlank(actionButtonId)) {
      AppboyLogger.i(TAG, "No action button Id associated with this notification action.  Not logging push action click to Appboy.");
      return;
    }
    AppboyLogger.i(TAG, "Logging push action click to Appboy. Campaign Id: " + campaignId + " Action Button Id: " + actionButtonId);
    Appboy.getInstance(context).logPushNotificationActionClicked(campaignId, actionButtonId);

  }

  /**
   * Handles sharing the action button click intent's contents to apps that can handle
   * the ACTION_SEND intent.
   *
   * @param context
   * @param intent the action button click intent
   */
  private static void handleShareActionClicked(Context context, Intent intent) {
    new ShareTask(context).execute(intent);
  }

  /**
   * Utility class to perform share intent creation on a background thread.
   *
   * Using a background thread is required since when sharing an image, the full image will be
   * re-downloaded, which must take place on a background thread.
   */
  private static class ShareTask extends AsyncTask<Intent, Integer, Intent> {
    private final Context mContext;

    public ShareTask(Context context) {
      mContext = context;
    }

    @Override
    protected Intent doInBackground(Intent... intents) {
      if (mContext != null) {
        return createShareActionIntent(mContext, intents[0]);
      } else {
        return null;
      }
    }

    @Override
    protected void onPostExecute(Intent shareIntent) {
      if (mContext != null) {
        if (shareIntent != null) {
          mContext.startActivity(shareIntent);
        } else {
          AppboyLogger.w(TAG, "Null share intent received.  Not starting share intent.");
        }
      }
    }
  }

  /**
   * Creates an intent that can used to share the contents of the notification intent passed in.
   *
   * Handles downloading and storing any push images locally, which is required for sharing them.
   *
   * Must be executed on a background thread.
   *
   * @param context
   * @param intent the action button click intent
   * @return a shareable intent
   */
  private static Intent createShareActionIntent(Context context, Intent intent) {
    Bundle notificationExtras = intent.getExtras();
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, notificationExtras.getString(Constants.APPBOY_PUSH_TITLE_KEY));
    shareIntent.putExtra(Intent.EXTRA_TEXT, notificationExtras.getString(Constants.APPBOY_PUSH_CONTENT_KEY));

    Bundle appboyExtras = notificationExtras.getBundle(Constants.APPBOY_PUSH_EXTRAS_KEY);
    if (canShareImage(context, appboyExtras)) {
      String fileName = Long.toString(System.currentTimeMillis());

      String imageUrl = appboyExtras.getString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY);
      Bitmap imageBitmap = AppboyImageUtils.getBitmap(Uri.parse(imageUrl));

      Uri localImageUri = AppboyImageUtils
          .storePushBitmapInExternalStorage(context.getApplicationContext(), imageBitmap, fileName, DEFAULT_LOCAL_STORAGE_FOLDER);

      shareIntent.setType(IMAGE_MIME_TYPE);
      shareIntent.putExtra(Intent.EXTRA_STREAM, localImageUri);
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    } else {
      shareIntent.setType(TEXT_MIME_TYPE);
    }
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return shareIntent;
  }

  /**
   * Returns the value for the given action field key template at the specified index.
   *
   * @param actionIndex the index of the desired action
   * @param notificationExtras GCM/ADM notification extras
   * @param actionFieldKeyTemplate the template of the action field
   * @return the desired notification action field value or the empty string if not present
   */
  static String getActionFieldAtIndex(int actionIndex, Bundle notificationExtras, String actionFieldKeyTemplate) {
    String actionFieldKey = actionFieldKeyTemplate.replace("*", String.valueOf(actionIndex));
    String actionFieldValue = notificationExtras.getString(actionFieldKey);
    if (actionFieldValue == null) {
      return "";
    } else {
      return actionFieldValue;
    }
  }
}