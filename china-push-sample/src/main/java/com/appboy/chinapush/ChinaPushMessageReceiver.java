package com.appboy.chinapush;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appboy.Appboy;
import com.baidu.android.pushservice.PushMessageReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class receives event updates from the Baidu push service.  We forward a subset of these
 * events for display in the app's message log.  We also register the Baidu user with Appboy when
 * the service binds and log notification clicks to Appboy when notifications are clicked.
 */
public class ChinaPushMessageReceiver extends PushMessageReceiver {
  public static final String TAG = ChinaPushMessageReceiver.class.getSimpleName();
  public static final String NOTIFICATION_CLICKED_KEY = "notification_clicked";
  public static final String LOG_MESSAGE_KEY = "log_message";

  public static String mApplicationMessageLog = "";

  /**
   * Called when the application binds to the Baidu push service.
   *
   * @param context context in which the receiver is running.
   * @param errorCode request error code (0 on success).
   * @param appId unique Baidu application identifier.
   * @param userId Baidu user identifier.
   * @param channelId Baidu device identifier.
   * @param requestId identifier for the bind request.
   */
  @Override
  public void onBind(Context context, int errorCode, String appId,
                     String userId, String channelId, String requestId) {
    String logMessage = String.format("onBind called with errorCode %d, appId %s, userId %s, channelId %s, requestId %s.",
        errorCode, appId, userId, channelId, requestId);
    Log.d(TAG, logMessage);
    updateApplicationMessageLog(context, logMessage, true);

    // register user with Appboy
    Appboy.getInstance(context).registerAppboyPushMessages(userId);
  }

  /**
   * Called when a message is silently passed through Baidu to the application.
   *
   * @param context context in which the receiver is running.
   * @param message the message content.
   * @param customContentString message extras.
   */
  @Override
  public void onMessage(Context context, String message, String customContentString) {
    String logMessage = String.format("onMessage called with message %s, customContentString %s", message, customContentString);
    Log.d(TAG, logMessage);
    updateApplicationMessageLog(context, logMessage, true);
  }

  /**
   * Called when a notification arrives.
   *
   * @param context context in which the receiver is running.
   * @param title the notification's title.
   * @param message the notification's message content.
   * @param customContentString notification extras.
   */
  @Override
  public void onNotificationArrived(Context context, String title, String message, String customContentString) {
    String logMessage = String.format("onNotificationArrived called with title %s, message %s, customContentString %s",
        title, message, customContentString);
    Log.d(TAG, logMessage);
    updateApplicationMessageLog(context, logMessage, false);
  }

  /**
   * Called when a notification is clicked.
   *
   * @param context context in which the receiver is running.
   * @param title the notification's title.
   * @param message the notification's message content.
   * @param customContentString notification extras.
   */
  @Override
  public void onNotificationClicked(Context context, String title, String message, String customContentString) {
    String logMessage = String.format("onNotificationClicked called with title %s, message %s, customContentString %s",
        title, message, customContentString);
    Log.d(TAG, logMessage);
    updateApplicationMessageLog(context, logMessage, true);

    // log notification click with Appboy
    updateNotificationClicked(context, customContentString);
  }

  /**
   * Called when the application unbinds from the Baidu push service.
   *
   * @param context context in which the receiver is running.
   * @param errorCode request error code (0 on success).
   * @param requestId identifier for the unbind request.
   */
  @Override
  public void onUnbind(Context context, int errorCode, String requestId) {
    String logMessage = String.format("onUnbind called with errorCode %d, requestId %s", errorCode, requestId);
    Log.d(TAG, logMessage);
    updateApplicationMessageLog(context, logMessage, true);
  }

  /**
   * Baidu tags are used to segment users.  However, Appboy does not use Baidu tags,
   * so we do nothing when tags are set.
   *
   * @param context context in which the receiver is running.
   * @param errorCode request error code (0 on success).
   * @param successTags tags set successfully.
   * @param failTags tags not set successfully.
   * @param requestId identifier for the set tags request.
   */
  @Override
  public void onSetTags(Context context, int errorCode,
                        List<String> successTags, List<String> failTags, String requestId) {}

  /**
   * Baidu tags are used to segment users.  However, Appboy does not use Baidu tags,
   * so we do nothing when tags are deleted.
   *
   * @param context context in which the receiver is running.
   * @param errorCode request error code (0 on success).
   * @param successTags tags deleted successfully.
   * @param failTags tags not deleted successfully.
   * @param requestId identifier for the delete tags request.
   */
  @Override
  public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {}

  /**
   * Baidu tags are used to segment users.  However, Appboy does not use Baidu tags,
   * so we do nothing when tags are listed.
   *
   * @param context context in which the receiver is running.
   * @param errorCode request error code (0 on success).
   * @param tags listed tags.
   * @param requestId identifier for the list tags request.
   */
  @Override
  public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {}

  /**
   * Creates a timestamped message to display to the user in the application's Baidu event message log
   * and passes it to {@link com.appboy.chinapush.ChinaPushActivity} in an intent.
   *
   * @param context context in which the receiver is running.
   * @param logMessage the message to log.
   * @param startActivity whether to start the main activity
   */
  private void updateApplicationMessageLog(Context context, String logMessage, boolean startActivity) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentTimeStamp = dateFormat.format(new Date());
    mApplicationMessageLog = mApplicationMessageLog + "\n" + currentTimeStamp + " " + logMessage;
    if (startActivity) {
      Intent intent = getUpdateIntent(context);
      intent.putExtra(LOG_MESSAGE_KEY, mApplicationMessageLog);
      context.getApplicationContext().startActivity(intent);
    }
  }

  /**
   * Packages the notification extras into an intent and passes it to
   * {@link com.appboy.chinapush.ChinaPushActivity}.
   *
   * @param context context in which the receiver is running.
   * @param customContentString notification extras.
   */
  private void updateNotificationClicked(Context context, String customContentString) {
    Intent intent = getUpdateIntent(context);
    intent.putExtra(NOTIFICATION_CLICKED_KEY, customContentString);
    context.getApplicationContext().startActivity(intent);
  }

  /**
   * @param context context in which the receiver is running.
   * @return a generic intent specifying the ChinaPushActivity class
   * as the component to handle it.
   */
  private Intent getUpdateIntent(Context context) {
    Intent intent = new Intent();
    intent.setClass(context.getApplicationContext(), ChinaPushActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return intent;
  }
}