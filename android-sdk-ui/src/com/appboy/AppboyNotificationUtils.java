package com.appboy;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.appboy.Constants;
import com.appboy.configuration.XmlAppConfigurationProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class AppboyNotificationUtils
{
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyNotificationUtils.class.getName());
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
    PendingIntent pushOpenedPendingIntent = PendingIntent.getBroadcast(context, 0, pushOpenedIntent, PendingIntent.FLAG_ONE_SHOT);
    notificationBuilder.setContentIntent(pushOpenedPendingIntent);

    // Sets the icon used in the notification bar itself.
    notificationBuilder.setSmallIcon(smallNotificationIconResourceId);
    notificationBuilder.setContentTitle(title);
    notificationBuilder.setContentText(content);

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

    // If we're using Jelly Bean, we can use the BigTextStyle, which lets the notification layout size grow to
    // accommodate longer text.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      // If there is an image url found in the extras payload and the image can be downloaded, then
      // use the android BigPictureStyle as the notification. Else, use the BigTextStyle instead.
      if (intentExtras != null) {
        Bundle extrasBundle = intentExtras.getBundle(Constants.APPBOY_PUSH_EXTRAS_KEY);
        if (extrasBundle!=null && extrasBundle.containsKey(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY)) {
          String imageUrl = extrasBundle.getString(Constants.APPBOY_PUSH_BIG_IMAGE_URL_KEY);
          Bitmap imageBitmap = downloadImageBitmap(imageUrl);
          if (imageBitmap != null) {
            Log.d(TAG, "Rendering push notification with BigPictureStyle");
            return new NotificationCompat.BigPictureStyle(notificationBuilder)
                .bigPicture(imageBitmap)
                .setSummaryText(content)
                .build();
          } else {
            Log.d(TAG, "Bitmap download failed for push notification");
          }
        }
      }
      Log.d(TAG, "Rendering push notification with BigTextStyle");
      return new NotificationCompat.BigTextStyle(notificationBuilder)
          .bigText(content).build();
    }
    return notificationBuilder.build();
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

  @TargetApi(12)
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
   * Checks the intent to determine whether this is an Appboy ADM message.
   *
   * All Appboy ADM messages must contain an extras entry with key set to "_ab" and value set to "true".
   */
  public static boolean isAppboyPushMessage(Intent intent) {
    Bundle extras = intent.getExtras();
    return extras != null && "true".equals(extras.getString(Constants.APPBOY_PUSH_APPBOY_KEY));
  }

  /**
   * Checks the intent to determine whether this is a notification message or a data push.
   *
   * A notification message is an Appboy ADM message that displays a notification in the
   * notification center (and optionally contains extra information that can be used directly
   * by the app).
   *
   * A data push is an Appboy GCM message that contains only extra information that can
   * be used directly by the app.
   */
  public static boolean isNotificationMessage(Intent intent) {
    Bundle extras = intent.getExtras();
    return extras != null && extras.containsKey(Constants.APPBOY_PUSH_TITLE_KEY) && extras.containsKey(Constants.APPBOY_PUSH_CONTENT_KEY);
  }

  /**
   * Creates and sends a broadcast message that can be listened for by the host app. The broadcast
   * message intent contains all of the data sent as part of the Appboy GCM/ADM message. The broadcast
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
      Log.e(TAG, String.format("Unable to parse the Appboy GCM data extras."));
      return null;
    }
  }
}
