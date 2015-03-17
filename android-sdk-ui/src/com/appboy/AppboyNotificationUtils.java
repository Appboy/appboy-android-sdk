package com.appboy;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.appboy.configuration.XmlAppConfigurationProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

public class AppboyNotificationUtils
{
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyNotificationUtils.class.getName());
  private static final Random mRandom = new Random();
  public static final String APPBOY_NOTIFICATION_ID = "com_appboy_notification";
  public static final String APPBOY_NOTIFICATION_TITLE_ID = "com_appboy_notification_title";
  public static final String APPBOY_NOTIFICATION_CONTENT_ID = "com_appboy_notification_content";
  public static final String APPBOY_NOTIFICATION_ICON_ID = "com_appboy_notification_icon";
  public static final String APPBOY_NOTIFICATION_TIME_ID = "com_appboy_notification_time";
  public static final String APPBOY_NOTIFICATION_TWENTY_FOUR_HOUR_FORMAT_ID = "com_appboy_notification_time_twenty_four_hour_format";
  public static final String APPBOY_NOTIFICATION_TWELVE_HOUR_FORTMAT_ID = "com_appboy_notification_time_twelve_hour_format";
  /**
   * Creates the rich notification. The notification varies based on the Android version on the
   * device, but each notification contains an icon, image, title, and content.
   *
   * Opening a notification from the notification center triggers a broadcast message to be sent.
   * The broadcast message action is <host-app-package-name>.intent.APPBOY_NOTIFICATION_OPENED.
   *
   * Note: Froyo and Gingerbread notifications are limited to one line of content.
   */
  public static Notification createNotification(XmlAppConfigurationProvider appConfigurationProvider,
                                                Context context, String title, String content, Bundle intentExtras) {
    int smallNotificationIconResourceId = appConfigurationProvider.getSmallNotificationIconResourceId();
    if (smallNotificationIconResourceId == 0) {
      Log.d(TAG, "Small notification icon resource was not found. Will use the app icon when " +
          "displaying notifications.");
      smallNotificationIconResourceId = appConfigurationProvider.getApplicationIconResourceId();
    }

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
    notificationBuilder.setTicker(title);
    notificationBuilder.setAutoCancel(true);

    // Create broadcast intent that will fire when the notification has been opened. To action on these messages,
    // register a broadcast receiver that listens to intent <your_package_name>.intent.APPBOY_NOTIFICATION_OPENED
    // and <your_package_name>.intent.APPBOY_PUSH_RECEIVED.
    String pushOpenedAction = context.getPackageName() + ".intent.APPBOY_NOTIFICATION_OPENED";
    Intent pushOpenedIntent = new Intent(pushOpenedAction);
    if (intentExtras != null) {
      pushOpenedIntent.putExtras(intentExtras);
    }
    PendingIntent pushOpenedPendingIntent = PendingIntent.getBroadcast(context, getRequestCode(), pushOpenedIntent, PendingIntent.FLAG_ONE_SHOT);
    notificationBuilder.setContentIntent(pushOpenedPendingIntent);


    // Sets the icon used in the notification bar itself.
    notificationBuilder.setSmallIcon(smallNotificationIconResourceId);

    // Set accent color for devices on Lollipop and above.  We use the push-specific accent color if it exists in the intentExtras,
    // otherwise we search for a default set in appboy.xml or don't set the color at all (and the system notification gray
    // default is used).
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (intentExtras != null && intentExtras.containsKey(Constants.APPBOY_PUSH_ACCENT_KEY)) {
        // Color is an unsigned integer, so we first parse it as a long.
        notificationBuilder.setColor((int) Long.parseLong(intentExtras.getString(Constants.APPBOY_PUSH_ACCENT_KEY)));
      } else {
        notificationBuilder.setColor(appConfigurationProvider.getDefaultNotificationAccentColor());
      }
    }

    notificationBuilder.setContentTitle(title);
    notificationBuilder.setContentText(content);

    // Setting notification sounds are supported after Honeycomb.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && intentExtras != null) {
      // Retrieve sound uri if included in intentExtras bundle.
      if (intentExtras.containsKey(Constants.APPBOY_PUSH_NOTIFICATION_SOUND_KEY)) {
        String soundURI = intentExtras.getString(Constants.APPBOY_PUSH_NOTIFICATION_SOUND_KEY);
        if(soundURI != null) {
          if (soundURI.equals(Constants.APPBOY_PUSH_NOTIFICATION_SOUND_DEFAULT_VALUE)) {
            notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
          } else {
            notificationBuilder.setSound(Uri.parse(soundURI));
          }

        }
      }
    }

    // From Honeycomb to ICS, we can use a custom view for our notifications which will allow them to be taller than
    // the standard one line of text.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      Resources resources = context.getResources();
      String packageName = context.getPackageName();

      int layoutResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_ID, "layout", packageName);
      int titleResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TITLE_ID, "id", packageName);
      int contentResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_CONTENT_ID, "id", packageName);
      int iconResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_ICON_ID, "id", packageName);
      int timeViewResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TIME_ID, "id", packageName);
      int twentyFourHourFormatResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TWENTY_FOUR_HOUR_FORMAT_ID, "string", packageName);
      int twelveHourFormatResourceId = resources.getIdentifier(APPBOY_NOTIFICATION_TWELVE_HOUR_FORTMAT_ID, "string", packageName);

      String twentyFourHourTimeFormat = getOptionalStringResource(resources,
          twentyFourHourFormatResourceId, Constants.DEFAULT_TWENTY_FOUR_HOUR_TIME_FORMAT);
      String twelveHourTimeFormat = getOptionalStringResource(resources,
          twelveHourFormatResourceId, Constants.DEFAULT_TWELVE_HOUR_TIME_FORMAT);

      if (layoutResourceId == 0 || titleResourceId == 0 || contentResourceId == 0 || iconResourceId == 0
          || timeViewResourceId == 0) {
        Log.w(TAG, String.format("Couldn't find all resource IDs for custom notification view, extended view will " +
                "not be used for push notifications. Received %d for layout, %d for title, %d for content, %d for icon, " +
                "and %d for time.",
            layoutResourceId, titleResourceId, contentResourceId, iconResourceId, timeViewResourceId));
      } else {
        Log.d(TAG, "Using RemoteViews for rendering of push notification.");
        RemoteViews remoteViews = new RemoteViews(packageName, layoutResourceId);
        remoteViews.setTextViewText(titleResourceId, title);
        remoteViews.setTextViewText(contentResourceId, content);
        remoteViews.setImageViewResource(iconResourceId, smallNotificationIconResourceId);

        // Custom views cannot be used as part of a RemoteViews so we're using a TextView widget instead. This
        // view will always display the time without date information (even after the day has changed).
        SimpleDateFormat timeFormat = new SimpleDateFormat(
            android.text.format.DateFormat.is24HourFormat(context) ? twentyFourHourTimeFormat : twelveHourTimeFormat);
        String notificationTime = timeFormat.format(new Date());
        remoteViews.setTextViewText(timeViewResourceId, notificationTime);
        notificationBuilder.setContent(remoteViews);
        return notificationBuilder.build();
      }
    }

    // If we're using Jelly Bean, we can use the Big View Style, which lets the notification layout size grow to
    // accommodate longer text and images.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

      // Summary text which appears with content in expanded view.
      String summaryText = "";

      // If there is an image url found in the extras payload and the image can be downloaded, then
      // use the android BigPictureStyle as the notification. Else, use the BigTextStyle instead.
      if (intentExtras != null) {

        // Retrieve summary text if included in intentExtras bundle.
        if (intentExtras.containsKey(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY)) {
          summaryText = intentExtras.getString(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY);
        }

        // Set a custom notification priority if defined in the bundle.
        notificationBuilder.setPriority(getNotificationPriority(intentExtras));

        Bundle extrasBundle = getExtrasBundle(intentExtras);
        if (extrasBundle!=null && extrasBundle.containsKey(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY)) {
          String imageUrl = extrasBundle.getString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY);
          Bitmap imageBitmap = downloadImageBitmap(imageUrl);
          if (imageBitmap != null) {
            Log.d(TAG, "Rendering push notification with BigPictureStyle");

            return getBigPictureNotification(notificationBuilder, imageBitmap, content, summaryText);
          } else {
            Log.d(TAG, "Bitmap download failed for push notification");
          }
        }
      }
      Log.d(TAG, "Rendering push notification with BigTextStyle");
      return getBigTextNotification(notificationBuilder, content, summaryText);
    }
    return notificationBuilder.build();
  }

  /**
   * Get the extras JSON bundle from the push payload.
   *
   * Amazon ADM recursively flattens all JSON messages, so we just return the original bundle.
   */
  public static Bundle getExtrasBundle(Bundle intentExtras) {
    if (!Constants.IS_AMAZON) {
      return intentExtras.getBundle(Constants.APPBOY_PUSH_EXTRAS_KEY);
    } else {
      return intentExtras;
    }
  }

  /**
   * Returns a big-picture style notification initialized with the specified bitmap, content, and summary text.
   * If summary text is specified, it will be shown in the expanded notification view.
   * If no summary text is specified, the content text will be shown in its place.
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static Notification getBigPictureNotification(NotificationCompat.Builder notificationBuilder, Bitmap imageBitmap, String content, String summaryText) {
    NotificationCompat.BigPictureStyle bigPictureNotificationBuilder = new NotificationCompat.BigPictureStyle(notificationBuilder);
    if (summaryText != null && !summaryText.isEmpty()) {
      bigPictureNotificationBuilder.setSummaryText(summaryText);
    } else {
      bigPictureNotificationBuilder.setSummaryText(content);
    }
    return bigPictureNotificationBuilder
      .bigPicture(imageBitmap)
      .build();
  }

  /**
   * Returns a big-text style notification initialized with the specified content and summary text.
   * If no summary text is specified, summary text will not be added to the notification.
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static Notification getBigTextNotification(NotificationCompat.Builder notificationBuilder, String content, String summaryText) {
    NotificationCompat.BigTextStyle bigTextNotificationBuilder = new NotificationCompat.BigTextStyle(notificationBuilder);
    if (summaryText != null && !summaryText.isEmpty()) {
      bigTextNotificationBuilder.setSummaryText(summaryText);
    }
    return bigTextNotificationBuilder
      .bigText(content)
      .build();
  }

  static String getOptionalStringResource(Resources resources, int stringResourceId, String defaultString) {
    try {
      return resources.getString(stringResourceId);
    } catch (Resources.NotFoundException e) {
      return defaultString;
    }
  }

  /**
   * Downloads an image and returns a bitmap object. The image should be less than 450dp for the
   * push notification. An aspect ratio of 2:1 is recommended. This should always be run in a background
   * thread.
   *
   * According to http://developer.android.com/guide/appendix/media-formats.html, the supported file
   * types are jpg and png.
   *
   * @param imageUrl The url where the image is found
   * @return An image in Bitmap form. If the image cannot be downloaded, or cannot be decoded into
   * a bitmap, then null is returned.
   */
  public static Bitmap downloadImageBitmap(String imageUrl) {
    Bitmap bitmap = null;
    try {
      InputStream in = new java.net.URL(imageUrl).openStream();
      bitmap = BitmapFactory.decodeStream(in);
    } catch (OutOfMemoryError e) {
      Log.e(TAG, "Out of Memory Error in image bitmap download");
      e.printStackTrace();
    } catch (Exception e) {
      Log.e(TAG, "General exception in image bitmap download");
      e.printStackTrace();
    }
    return bitmap;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
  public static String bundleOptString(Bundle bundle, String key, String defaultValue) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
      return bundle.getString(key, defaultValue);
    } else {
      String result = bundle.getString(key);
      if (result == null) {
        result = defaultValue;
      }
      return result;
    }
  }

  /**
   * Checks the intent to determine whether this is an Appboy push message.
   *
   * All Appboy push messages must contain an extras entry with key set to "_ab" and value set to "true".
   */
  public static boolean isAppboyPushMessage(Intent intent) {
    Bundle extras = intent.getExtras();
    return extras != null && "true".equals(extras.getString(Constants.APPBOY_PUSH_APPBOY_KEY));
  }

  /**
   * Checks the intent to determine whether this is a notification message or a data push.
   *
   * A notification message is an Appboy push message that displays a notification in the
   * notification center (and optionally contains extra information that can be used directly
   * by the app).
   *
   * A data push is an Appboy push message that contains only extra information that can
   * be used directly by the app.
   */
  public static boolean isNotificationMessage(Intent intent) {
    Bundle extras = intent.getExtras();
    return extras != null && extras.containsKey(Constants.APPBOY_PUSH_TITLE_KEY) && extras.containsKey(Constants.APPBOY_PUSH_CONTENT_KEY);
  }

  /**
   * Creates and sends a broadcast message that can be listened for by the host app. The broadcast
   * message intent contains all of the data sent as part of the Appboy push message. The broadcast
   * message action is <host-app-package-name>.intent.APPBOY_PUSH_RECEIVED.
   */
  public static void sendPushMessageReceivedBroadcast(Context context, Bundle extras) {
    String pushReceivedAction = context.getPackageName() + ".intent.APPBOY_PUSH_RECEIVED";
    Intent pushReceivedIntent = new Intent(pushReceivedAction);
    if (extras != null) {
      pushReceivedIntent.putExtras(extras);
    }
    context.sendBroadcast(pushReceivedIntent);
  }

  /**
   * Creates an alarm which will issue a broadcast to cancel the notification specified by the given notificationId after the given duration.
   */
  public static void setNotificationDurationAlarm(Context context, Class<?> thisClass, int notificationId, int durationInMillis) {
    Intent cancelIntent = new Intent(context, thisClass);
    cancelIntent.setAction(Constants.APPBOY_CANCEL_NOTIFICATION_ACTION);
    cancelIntent.putExtra(Constants.APPBOY_CANCEL_NOTIFICATION_TAG, notificationId);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    if (durationInMillis >= Constants.APPBOY_MINIMUM_NOTIFICATION_DURATION_MILLIS) {
      alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + durationInMillis, pendingIntent);
    }
  }

  /**
   * Returns an id for the new notification we'll send to the notification center.
   * Notification id is used by the Android OS to override currently active notifications with identical ids.
   * If a custom notification id is not defined in the payload, Appboy derives an id value from the message's contents
   * to prevent duplication in the notification center.
   */
  public static int getNotificationId(Bundle extras) {
    if (extras != null) {
      if (extras.containsKey(Constants.APPBOY_PUSH_CUSTOM_NOTIFICATION_ID)) {
        try {
          int notificationId = Integer.parseInt(extras.getString(Constants.APPBOY_PUSH_CUSTOM_NOTIFICATION_ID));
          Log.d(TAG, String.format("Using notification id provided in the message's extras bundle: " + notificationId));
          return notificationId;

        } catch (NumberFormatException e) {
          Log.e(TAG, String.format("Unable to parse notification id provided in the message's extras bundle. Using default notification id instead: " + Constants.APPBOY_DEFAULT_NOTIFICATION_ID));
          e.printStackTrace();
          return Constants.APPBOY_DEFAULT_NOTIFICATION_ID;
        }
      } else {
        String messageKey = AppboyNotificationUtils.bundleOptString(extras, Constants.APPBOY_PUSH_TITLE_KEY, "")
            + AppboyNotificationUtils.bundleOptString(extras, Constants.APPBOY_PUSH_CONTENT_KEY, "");
        int notificationId = messageKey.hashCode();
        Log.d(TAG, String.format("Message without notification id provided in the extras bundle received.  Using a hash of the message: " + notificationId));
        return notificationId;
      }
    } else {
      Log.d(TAG, String.format("Message without extras bundle received.  Using default notification id: " + Constants.APPBOY_DEFAULT_NOTIFICATION_ID));
      return Constants.APPBOY_DEFAULT_NOTIFICATION_ID;
    }
  }

  /**
   * This method will retrieve notification priority from intentExtras bundle if it has been set.
   * Otherwise returns the default priority.
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static int getNotificationPriority(Bundle intentExtras) {
    if (intentExtras != null && intentExtras.containsKey(Constants.APPBOY_PUSH_PRIORITY_KEY)) {
      try {
        int notificationPriority = Integer.parseInt(intentExtras.getString(Constants.APPBOY_PUSH_PRIORITY_KEY));
        if (isValidNotificationPriority(notificationPriority)) {
          return notificationPriority;
        }
      } catch (NumberFormatException e) {
        Log.e(TAG, String.format("Unable to parse custom priority. Returning default priority of " + Notification.PRIORITY_DEFAULT));
        e.printStackTrace();
      }
    }
    return Notification.PRIORITY_DEFAULT;
  }

  /**
   * Checks whether the given integer value is a valid Android notification priority constant
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static boolean isValidNotificationPriority(int priority) {
    return (priority >= Notification.PRIORITY_MIN && priority <= Notification.PRIORITY_MAX);
  }

  /**
   * Returns a random request code for this notification.
   * Request codes are used to differentiate between multiple active pending intents.
   */
  public static int getRequestCode() {
    return mRandom.nextInt();
  }

  /**
   * This method will wake the device using a wake lock if the WAKE_LOCK permission is present in the
   * manifest. If the permission is not present, this does nothing. If the screen is already on,
   * and the permission is present, this does nothing.  If the priority of the incoming notification
   * is min, this does nothing.
   */
  public static boolean wakeScreenIfHasPermission(Context context, Bundle intentExtras) {
    // Check for the wake lock permission.
    if (context.checkCallingOrSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_DENIED) {
      return false;
    }
    // Don't wake lock if this is a minimum priority notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      if (getNotificationPriority(intentExtras) == Notification.PRIORITY_MIN) {
        return false;
      }
    }

    // Get the power manager for the wake lock.
    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
    // Acquire the wake lock for some negligible time, then release it. We just want to wake the screen
    // and not take up more CPU power than necessary.
    wakeLock.acquire();
    wakeLock.release();
    return true;
  }

  public static Bundle createExtrasBundle(String jsonString) {
    try {
      Bundle bundle = new Bundle();
      JSONObject json = new JSONObject(jsonString);
      Iterator keys = json.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        bundle.putString(key, json.getString(key));
      }
      return bundle;
    } catch (JSONException e) {
      Log.e(TAG, String.format("Unable to parse the Appboy push data extras."));
      e.printStackTrace();
      return null;
    }
  }
}
