package com.appboy.push;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.AppboyAdmReceiver;
import com.appboy.AppboyGcmReceiver;
import com.appboy.Constants;
import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.XmlAppConfigurationProvider;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.IntentUtils;
import com.appboy.support.PermissionUtils;
import com.appboy.support.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class AppboyNotificationUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyNotificationUtils.class.getName());
  private static final String SOURCE_KEY = "source";
  public static final String APPBOY_NOTIFICATION_OPENED_SUFFIX = ".intent.APPBOY_NOTIFICATION_OPENED";
  public static final String APPBOY_NOTIFICATION_RECEIVED_SUFFIX = ".intent.APPBOY_PUSH_RECEIVED";

  /**
   * Get the Appboy extras Bundle from the notification extras. Notification extras must be in a Bundle.
   * <p/>
   * Amazon ADM recursively flattens all JSON messages, so we just return the original bundle.
   *
   * @deprecated use {@link AppboyNotificationUtils#getAppboyExtrasWithoutPreprocessing(android.os.Bundle) instead.
   * Note that notification extras must be in GCM/ADM format instead of a Bundle.}
   */
  @Deprecated
  public static Bundle getAppboyExtras(Bundle notificationExtras) {
    if (notificationExtras == null) {
      return null;
    }
    if (!Constants.IS_AMAZON) {
      return notificationExtras.getBundle(Constants.APPBOY_PUSH_EXTRAS_KEY);
    } else {
      return notificationExtras;
    }
  }

  /**
   * Handles a push notification click.  Called by GCM/ADM receiver when an
   * Appboy push notification click intent is received.
   * <p/>
   * See {@link #logNotificationOpened} and {@link #sendNotificationOpenedBroadcast}
   *
   * @param context
   * @param intent the internal notification clicked intent constructed in
   *               {@link #setContentIntentIfPresent}
   */
  public static void handleNotificationOpened(Context context, Intent intent) {
    try {
      logNotificationOpened(context, intent);
      sendNotificationOpenedBroadcast(context, intent);
      XmlAppConfigurationProvider appConfigurationProvider = new XmlAppConfigurationProvider(context);
      if (appConfigurationProvider.getHandlePushDeepLinksAutomatically()) {
        routeUserWithNotificationOpenedIntent(context, intent);
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Exception occurred attempting to handle notification.", e);
    }
  }

  /**
   * Opens any available deep links with an Intent.ACTION_VIEW intent, placing the main activity
   * on the back stack. If no deep link is available, opens the main activity.
   *
   * @param context
   * @param intent the internal notification clicked intent constructed in
   *                {@link #setContentIntentIfPresent}
   */
  public static void routeUserWithNotificationOpenedIntent(Context context, Intent intent) {
    // get extras bundle.
    Bundle extras = intent.getBundleExtra(Constants.APPBOY_PUSH_EXTRAS_KEY);
    if (extras == null) {
      extras = new Bundle();
    }
    extras.putString(AppboyGcmReceiver.CAMPAIGN_ID_KEY, intent.getStringExtra(AppboyGcmReceiver.CAMPAIGN_ID_KEY));
    extras.putString(SOURCE_KEY, Constants.APPBOY);

    // get main activity intent.
    Intent startActivityIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    if (extras != null) {
      startActivityIntent.putExtras(extras);
    }

    // If a deep link exists, start an ACTION_VIEW intent pointing at the deep link.
    // The intent returned from getStartActivityIntent() is placed on the back stack.
    // Otherwise, start the intent defined in getStartActivityIntent().
    String deepLink = intent.getStringExtra(Constants.APPBOY_PUSH_DEEP_LINK_KEY);
    if (!StringUtils.isNullOrBlank(deepLink)) {
      Log.d(TAG, String.format("Found a deep link %s.", deepLink));
      Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
          .putExtras(extras);
      TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
      stackBuilder.addNextIntent(startActivityIntent);
      stackBuilder.addNextIntent(uriIntent);
      try {
        stackBuilder.startActivities(extras);
      } catch (ActivityNotFoundException e) {
        Log.w(TAG, String.format("Could not find appropriate activity to open for deep link %s.", deepLink));
      }
    } else {
      Log.d(TAG, "Push notification had no deep link. Opening main activity.");
      context.startActivity(startActivityIntent);
    }
  }

  /**
   * Get the Appboy extras Bundle from the notification extras.  Notification extras must be in GCM/ADM format.
   *
   * @param notificationExtras Notification extras as provided by GCM/ADM.
   * @return Returns the Appboy extras Bundle from the notification extras. Amazon ADM recursively flattens all JSON messages,
   * so for Amazon devices we just return a copy of the original bundle.
   */
  public static Bundle getAppboyExtrasWithoutPreprocessing(Bundle notificationExtras) {
    if (notificationExtras == null) {
      return null;
    }
    if (!Constants.IS_AMAZON) {
      return AppboyNotificationUtils.parseJSONStringDictionaryIntoBundle(AppboyNotificationUtils.bundleOptString(notificationExtras, Constants.APPBOY_PUSH_EXTRAS_KEY, "{}"));
    } else {
      return new Bundle(notificationExtras);
    }
  }

  /**
   * Returns the specified String if it is found in the bundle; otherwise it returns the defaultString.
   */
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
   * Parses the JSON into a bundle.  The JSONObject parsed from the input string must be a flat
   * dictionary with all string values.
   */
  public static Bundle parseJSONStringDictionaryIntoBundle(String jsonStringDictionary) {
    try {
      Bundle bundle = new Bundle();
      JSONObject json = new JSONObject(jsonStringDictionary);
      Iterator keys = json.keys();
      while (keys.hasNext()) {
        String key = (String) keys.next();
        bundle.putString(key, json.getString(key));
      }
      return bundle;
    } catch (JSONException e) {
      AppboyLogger.e(TAG, String.format("Unable parse JSON into a bundle."), e);
      return null;
    }
  }

  /**
   * Checks the incoming GCM/ADM intent to determine whether this is an Appboy push message.
   * <p/>
   * All Appboy push messages must contain an extras entry with key set to "_ab" and value set to "true".
   */
  public static boolean isAppboyPushMessage(Intent intent) {
    Bundle extras = intent.getExtras();
    return extras != null && "true".equals(extras.getString(Constants.APPBOY_PUSH_APPBOY_KEY));
  }

  /**
   * Checks the intent received from GCM to determine whether this is a notification message or a
   * data push.
   * <p/>
   * A notification message is an Appboy push message that displays a notification in the
   * notification center (and optionally contains extra information that can be used directly
   * by the app).
   * <p/>
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
  public static void sendPushMessageReceivedBroadcast(Context context, Bundle notificationExtras) {
    String pushReceivedAction = context.getPackageName() + APPBOY_NOTIFICATION_RECEIVED_SUFFIX;
    Intent pushReceivedIntent = new Intent(pushReceivedAction);
    if (notificationExtras != null) {
      pushReceivedIntent.putExtras(notificationExtras);
    }
    context.sendBroadcast(pushReceivedIntent);
  }

  /**
   * Creates an alarm which will issue a broadcast to cancel the notification specified by the given notificationId after the given duration.
   */
  public static void setNotificationDurationAlarm(Context context, Class<?> thisClass, int notificationId, int durationInMillis) {
    Intent cancelIntent = new Intent(context, thisClass);
    cancelIntent.setAction(Constants.APPBOY_CANCEL_NOTIFICATION_ACTION);
    cancelIntent.putExtra(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);

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
  public static int getNotificationId(Bundle notificationExtras) {
    if (notificationExtras != null) {
      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_CUSTOM_NOTIFICATION_ID)) {
        try {
          int notificationId = Integer.parseInt(notificationExtras.getString(Constants.APPBOY_PUSH_CUSTOM_NOTIFICATION_ID));
          AppboyLogger.d(TAG, String.format("Using notification id provided in the message's extras bundle: " + notificationId));
          return notificationId;

        } catch (NumberFormatException e) {
          AppboyLogger.e(TAG, String.format("Unable to parse notification id provided in the "
              + "message's extras bundle. Using default notification id instead: "
              + Constants.APPBOY_DEFAULT_NOTIFICATION_ID), e);
          return Constants.APPBOY_DEFAULT_NOTIFICATION_ID;
        }
      } else {
        String messageKey = AppboyNotificationUtils.bundleOptString(notificationExtras, Constants.APPBOY_PUSH_TITLE_KEY, "")
            + AppboyNotificationUtils.bundleOptString(notificationExtras, Constants.APPBOY_PUSH_CONTENT_KEY, "");
        int notificationId = messageKey.hashCode();
        AppboyLogger.d(TAG, String.format("Message without notification id provided in the extras bundle received.  Using a hash of the message: " + notificationId));
        return notificationId;
      }
    } else {
      AppboyLogger.d(TAG, String.format("Message without extras bundle received.  Using default notification id: " + Constants.APPBOY_DEFAULT_NOTIFICATION_ID));
      return Constants.APPBOY_DEFAULT_NOTIFICATION_ID;
    }
  }

  /**
   * This method will retrieve notification priority from notificationExtras bundle if it has been set.
   * Otherwise returns the default priority.
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static int getNotificationPriority(Bundle notificationExtras) {
    if (notificationExtras != null && notificationExtras.containsKey(Constants.APPBOY_PUSH_PRIORITY_KEY)) {
      try {
        int notificationPriority = Integer.parseInt(notificationExtras.getString(Constants.APPBOY_PUSH_PRIORITY_KEY));
        if (isValidNotificationPriority(notificationPriority)) {
          return notificationPriority;
        } else {
          AppboyLogger.e(TAG, String.format("Received invalid notification priority %d", notificationPriority));
        }
      } catch (NumberFormatException e) {
        AppboyLogger.e(TAG, String.format("Unable to parse custom priority. Returning default priority of " + Notification.PRIORITY_DEFAULT), e);
      }
    }
    return Notification.PRIORITY_DEFAULT;
  }

  /**
   * Checks whether the given integer value is a valid Android notification priority constant.
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static boolean isValidNotificationPriority(int priority) {
    return (priority >= Notification.PRIORITY_MIN && priority <= Notification.PRIORITY_MAX);
  }

  /**
   * This method will wake the device using a wake lock if the WAKE_LOCK permission is present in the
   * manifest. If the permission is not present, this does nothing. If the screen is already on,
   * and the permission is present, this does nothing.  If the priority of the incoming notification
   * is min, this does nothing.
   */
  public static boolean wakeScreenIfHasPermission(Context context, Bundle notificationExtras) {
    // Check for the wake lock permission.
    if (!PermissionUtils.hasPermission(context, Manifest.permission.WAKE_LOCK)) {
      return false;
    }
    // Don't wake lock if this is a minimum priority notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      if (getNotificationPriority(notificationExtras) == Notification.PRIORITY_MIN) {
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

  /**
   * Returns a custom AppboyNotificationFactory if set, else the default AppboyNotificationFactory
   */
  public static IAppboyNotificationFactory getActiveNotificationFactory() {
    IAppboyNotificationFactory customAppboyNotificationFactory = Appboy.getCustomAppboyNotificationFactory();
    if (customAppboyNotificationFactory == null) {
      return AppboyNotificationFactory.getInstance();
    } else {
      return customAppboyNotificationFactory;
    }
  }

  /**
   * Sets notification title if it exists in the notificationExtras.
   */
  public static void setTitleIfPresent(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (notificationExtras != null) {
      notificationBuilder.setContentTitle(notificationExtras.getString(Constants.APPBOY_PUSH_TITLE_KEY));
    }
  }

  /**
   * Sets notification content if it exists in the notificationExtras.
   */
  public static void setContentIfPresent(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (notificationExtras != null) {
      notificationBuilder.setContentText(notificationExtras.getString(Constants.APPBOY_PUSH_CONTENT_KEY));
    }
  }

  /**
   * Sets notification ticker to the title if it exists in the notificationExtras.
   */
  public static void setTickerIfPresent(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (notificationExtras != null) {
      notificationBuilder.setTicker(notificationExtras.getString(Constants.APPBOY_PUSH_TITLE_KEY));
    }
  }

  /**
   * Create broadcast intent that will fire when the notification has been opened. The GCM or ADM receiver will be notified,
   * log a click, then send a broadcast to the client receiver.
   *
   * @param context
   * @param notificationBuilder
   * @param notificationExtras
   */
  public static void setContentIntentIfPresent(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    try {
      Intent pushOpenedIntent = new Intent(Constants.APPBOY_PUSH_CLICKED_ACTION).setClass(context, AppboyNotificationUtils.getNotificationReceiverClass());
      if (notificationExtras != null) {
        pushOpenedIntent.putExtras(notificationExtras);
      }
      PendingIntent pushOpenedPendingIntent = PendingIntent.getBroadcast(context, IntentUtils.getRequestCode(), pushOpenedIntent, PendingIntent.FLAG_ONE_SHOT);
      notificationBuilder.setContentIntent(pushOpenedPendingIntent);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Error setting content.", e);
    }
  }

  /**
   * Sets the icon used in the notification bar itself.
   * If a drawable defined in appboy.xml is found, we use that.  Otherwise, fall back to the application icon.
   *
   * @return the resource id of the small icon to be used.
   */
  public static int setSmallIcon(XmlAppConfigurationProvider appConfigurationProvider, NotificationCompat.Builder notificationBuilder) {
    int smallNotificationIconResourceId = appConfigurationProvider.getSmallNotificationIconResourceId();
    if (smallNotificationIconResourceId == 0) {
      AppboyLogger.d(TAG, "Small notification icon resource was not found. Will use the app icon when "
          + "displaying notifications.");
      smallNotificationIconResourceId = appConfigurationProvider.getApplicationIconResourceId();
    }
    notificationBuilder.setSmallIcon(smallNotificationIconResourceId);
    return smallNotificationIconResourceId;
  }

  /**
   * Set large icon for devices on Honeycomb and above.  We use the large icon URL if it exists in
   * the notificationExtras.  Otherwise we search for a drawable defined in appboy.xml.  If that
   * doesn't exists, we do nothing.
   * <p/>
   * Supported HoneyComb+.
   *
   * @return whether a large icon was successfully set.
   */
  public static boolean setLargeIconIfPresentAndSupported(
      Context context, XmlAppConfigurationProvider appConfigurationProvider,
      NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      return false;
    }
    try {
      if (notificationExtras != null
          && notificationExtras.containsKey(Constants.APPBOY_PUSH_LARGE_ICON_KEY)) {
        String bitmapUrl = notificationExtras.getString(Constants.APPBOY_PUSH_LARGE_ICON_KEY);
        Bitmap largeNotificationBitmap = AppboyImageUtils.getBitmap(Uri.parse(bitmapUrl));
        notificationBuilder.setLargeIcon(largeNotificationBitmap);
        return true;
      }
      int largeNotificationIconResourceId = appConfigurationProvider
          .getLargeNotificationIconResourceId();
      if (largeNotificationIconResourceId != 0) {
        Bitmap largeNotificationBitmap = BitmapFactory.decodeResource(context.getResources(),
            largeNotificationIconResourceId);
        notificationBuilder.setLargeIcon(largeNotificationBitmap);
        return true;
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Error setting large notification icon", e);
    }
    return false;
  }

  /**
   * @Deprecated use {@link #setLargeIconIfPresentAndSupported(Context, XmlAppConfigurationProvider, NotificationCompat.Builder, Bundle)}
   */
  @Deprecated
  public static boolean setLargeIconIfPresentAndSupported(
      Context context, XmlAppConfigurationProvider appConfigurationProvider,
      NotificationCompat.Builder notificationBuilder) {
    return setLargeIconIfPresentAndSupported(context, appConfigurationProvider, notificationBuilder, null);
  }

  /**
   * In devices running Honeycomb+ notifications can optionally include a sound to play when the notification is delivered.
   * <p/>
   * Supported HoneyComb+.
   */
  public static void setSoundIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      if (notificationExtras != null) {
        // Retrieve sound uri if included in notificationExtras bundle.
        if (notificationExtras.containsKey(Constants.APPBOY_PUSH_NOTIFICATION_SOUND_KEY)) {
          String soundUri = notificationExtras.getString(Constants.APPBOY_PUSH_NOTIFICATION_SOUND_KEY);
          if (soundUri != null) {
            if (soundUri.equals(Constants.APPBOY_PUSH_NOTIFICATION_SOUND_DEFAULT_VALUE)) {
              notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
            } else {
              notificationBuilder.setSound(Uri.parse(soundUri));
            }
          }
        }
      }
    }
  }

  /**
   * Sets the subText of the notification if a summary is present in the notification extras.
   * <p/>
   * Supported on JellyBean+.
   */
  public static void setSummaryTextIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      if (notificationExtras != null) {
        // Retrieve summary text if included in notificationExtras bundle.
        if (notificationExtras.containsKey(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY)) {
          String summaryText = notificationExtras.getString(Constants.APPBOY_PUSH_SUMMARY_TEXT_KEY);
          if (summaryText != null) {
            notificationBuilder.setSubText(summaryText);
          }
        }
      }
    }
  }

  /**
   * Sets the priority of the notification if a priority is present in the notification extras.
   * <p/>
   * Supported JellyBean+.
   */
  public static void setPriorityIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      if (notificationExtras != null) {
        notificationBuilder.setPriority(AppboyNotificationUtils.getNotificationPriority(notificationExtras));
      }
    }
  }

  /**
   * Sets the style of the notification if supported.
   * <p/>
   * If there is an image url found in the extras payload and the image can be downloaded, then
   * use the android BigPictureStyle as the notification. Else, use the BigTextStyle instead.
   * <p/>
   * Supported JellyBean+.
   */
  public static void setStyleIfSupported(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras, Bundle appboyExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      if (notificationExtras != null) {
        NotificationCompat.Style style = AppboyNotificationStyleFactory.getBigNotificationStyle(context, notificationExtras, appboyExtras);
        notificationBuilder.setStyle(style);
      }
    }
  }

  /**
   * Set accent color for devices on Lollipop and above.  We use the push-specific accent color if it exists in the notificationExtras,
   * otherwise we search for a default set in appboy.xml or don't set the color at all (and the system notification gray
   * default is used).
   * <p/>
   * Supported Lollipop+.
   */
  public static void setAccentColorIfPresentAndSupported(XmlAppConfigurationProvider appConfigurationProvider, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (notificationExtras != null && notificationExtras.containsKey(Constants.APPBOY_PUSH_ACCENT_KEY)) {
        // Color is an unsigned integer, so we first parse it as a long.
        notificationBuilder.setColor((int) Long.parseLong(notificationExtras.getString(Constants.APPBOY_PUSH_ACCENT_KEY)));
      } else {
        notificationBuilder.setColor(appConfigurationProvider.getDefaultNotificationAccentColor());
      }
    }
  }

  /**
   * Set category for devices on Lollipop and above.  Category is one of the predefined notification categories (see the CATEGORY_* constants in Notification)
   * that best describes a Notification. May be used by the system for ranking and filtering.
   * <p/>
   * Supported Lollipop+.
   */
  public static void setCategoryIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (notificationExtras != null && notificationExtras.containsKey(Constants.APPBOY_PUSH_CATEGORY_KEY)) {
        String notificationCategory = notificationExtras.getString(Constants.APPBOY_PUSH_CATEGORY_KEY);
        notificationBuilder.setCategory(notificationCategory);
      }
    }
  }

  /**
   * Set visibility for devices on Lollipop and above.
   * <p/>
   * Sphere of visibility of this notification, which affects how and when the SystemUI reveals the notification's presence and
   * contents in untrusted situations (namely, on the secure lockscreen). The default level, VISIBILITY_PRIVATE, behaves exactly
   * as notifications have always done on Android: The notification's icon and tickerText (if available) are shown in all situations,
   * but the contents are only available if the device is unlocked for the appropriate user. A more permissive policy can be expressed
   * by VISIBILITY_PUBLIC; such a notification can be read even in an "insecure" context (that is, above a secure lockscreen).
   * To modify the public version of this notification—for example, to redact some portions—see setPublicVersion(Notification).
   * Finally, a notification can be made VISIBILITY_SECRET, which will suppress its icon and ticker until the user has bypassed the lockscreen.
   * <p/>
   * Supported Lollipop+.
   */
  public static void setVisibilityIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (notificationExtras != null && notificationExtras.containsKey(Constants.APPBOY_PUSH_VISIBILITY_KEY)) {
        try {
          int visibility = Integer.parseInt(notificationExtras.getString(Constants.APPBOY_PUSH_VISIBILITY_KEY));
          if (isValidNotificationVisibility(visibility)) {
            notificationBuilder.setVisibility(visibility);
          } else {
            AppboyLogger.e(TAG, String.format("Received invalid notification visibility %d", visibility));
          }
        } catch (Exception e) {
          AppboyLogger.e(TAG, "Failed to parse visibility from notificationExtras", e);
        }
      }
    }
  }

  /**
   * Set the public version of the notification for notifications with private visibility.
   * <p/>
   * Supported Lollipop+.
   */
  public static void setPublicVersionIfPresentAndSupported(
      Context context, XmlAppConfigurationProvider xmlAppConfigurationProvider,
      NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (notificationExtras != null && notificationExtras.containsKey(Constants.APPBOY_PUSH_PUBLIC_NOTIFICATION_KEY)) {
        String publicNotificationExtrasString = notificationExtras.getString(Constants.APPBOY_PUSH_PUBLIC_NOTIFICATION_KEY);
        Bundle publicNotificationExtras = parseJSONStringDictionaryIntoBundle(publicNotificationExtrasString);
        NotificationCompat.Builder publicNotificationBuilder = new NotificationCompat.Builder(context);
        setContentIfPresent(publicNotificationBuilder, publicNotificationExtras);
        setTitleIfPresent(publicNotificationBuilder, publicNotificationExtras);
        setSummaryTextIfPresentAndSupported(publicNotificationBuilder, publicNotificationExtras);
        setSmallIcon(xmlAppConfigurationProvider, publicNotificationBuilder);
        setAccentColorIfPresentAndSupported(xmlAppConfigurationProvider, publicNotificationBuilder, publicNotificationExtras);
        notificationBuilder.setPublicVersion(publicNotificationBuilder.build());
      }
    }
  }

  /**
   * Checks whether the given integer value is a valid Android notification visibility constant.
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public static boolean isValidNotificationVisibility(int visibility) {
    return (visibility == Notification.VISIBILITY_SECRET || visibility == Notification.VISIBILITY_PRIVATE || visibility == Notification.VISIBILITY_PUBLIC);
  }

  /**
   * Logs a notification click with Appboy if the extras passed down
   * indicate that they are from Appboy and contain a campaign Id.
   * <p/>
   * An Appboy session must be active to log a push notification.
   *
   * @param customContentString extra key value pairs in JSON format.
   */
  public static void logBaiduNotificationClick(Context context, String customContentString) {
    if (customContentString == null) {
      AppboyLogger.w(TAG, "customContentString was null. Doing nothing.");
      return;
    }
    try {
      JSONObject jsonExtras = new JSONObject(customContentString);
      String source = jsonExtras.optString(SOURCE_KEY, null);
      String campaignId = jsonExtras.optString(Constants.APPBOY_PUSH_CAMPAIGN_ID_KEY, null);
      if (source != null && Constants.APPBOY.equals(source) && campaignId != null) {
        Appboy.getInstance(context).logPushNotificationOpened(campaignId);
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, String.format("Caught an exception processing customContentString: %s", customContentString), e);
    }
  }

  /**
   * Handles a request to cancel a push notification in the notification center.  Called by GCM/ADM receiver when an
   * Appboy cancel notification intent is received.
   * <p/>
   * Any existing notification in the notification center with the integer Id specified in the
   * "nid" field of the provided intent's extras is cancelled.
   * <p/>
   * If no Id is found, the defaut Appboy notification Id is used.
   *
   * @param context
   * @param intent  the cancel notification intent
   */
  public static void handleCancelNotificationAction(Context context, Intent intent) {
    try {
      if (intent.hasExtra(Constants.APPBOY_PUSH_NOTIFICATION_ID)) {
        int notificationId = intent.getIntExtra(Constants.APPBOY_PUSH_NOTIFICATION_ID, Constants.APPBOY_DEFAULT_NOTIFICATION_ID);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.APPBOY_PUSH_NOTIFICATION_TAG, notificationId);
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Exception occurred handling cancel notification intent.", e);
    }
  }

  /**
   * Creates a request to cancel a push notification in the notification center.
   * <p/>
   * Sends an intent to the GCM/ADM receiver requesting Appboy to cancel the notification with
   * the specified notification Id.
   * <p/>
   * See {@link #handleCancelNotificationAction}
   *
   * @param context
   * @param notificationId
   */
  public static void cancelNotification(Context context, int notificationId) {
    try {
      Intent cancelNotificationIntent = new Intent(Constants.APPBOY_CANCEL_NOTIFICATION_ACTION).setClass(context, AppboyNotificationUtils.getNotificationReceiverClass());
      cancelNotificationIntent.putExtra(Constants.APPBOY_PUSH_NOTIFICATION_ID, notificationId);
      context.sendBroadcast(cancelNotificationIntent);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Exception occurred attempting to cancel notification.", e);
    }
  }

  /**
   * @return the Class of the notification receiver used by this application.
   */
  public static Class<?> getNotificationReceiverClass() {
    if (Constants.IS_AMAZON) {
      return AppboyAdmReceiver.class;
    } else {
      return AppboyGcmReceiver.class;
    }
  }

  /**
   * Returns true if the bundle is from a push sent by Appboy for uninstall tracking.
   * <p/>
   * Uninstall tracking push messages are content-only (i.e. non-display) push messages. You can use this
   * method to detect that a push is from an Appboy uninstall tracking push and ensure your broadcast receiver
   * doesn't take any actions it shouldn't if a content push is from Appboy uninstall tracking (e.g. don't ping your server
   * for content on Appboy uninstall push).
   *
   * @param notificationExtras A notificationExtras bundle that is passed with the push recieved intent when a GCM/ADM message is
   *                           received, and that Appboy passes in the intent to registered receivers.
   */
  public static boolean isUninstallTrackingPush(Bundle notificationExtras) {
    if (notificationExtras != null) {
      // The ADM case where extras are flattened
      if (notificationExtras.containsKey(Constants.APPBOY_PUSH_UNINSTALL_TRACKING_KEY)) {
        return true;
      }
      // The GCM case where extras are in a separate bundle
      Bundle appboyExtras = notificationExtras.getBundle(Constants.APPBOY_PUSH_EXTRAS_KEY);
      if (appboyExtras != null) {
        return appboyExtras.containsKey(Constants.APPBOY_PUSH_UNINSTALL_TRACKING_KEY);
      }
    }
    return false;
  }

  /**
   * Returns the specified String resource if it is found; otherwise it returns the defaultString.
   */
  static String getOptionalStringResource(Resources resources, int stringResourceId, String defaultString) {
    try {
      return resources.getString(stringResourceId);
    } catch (Resources.NotFoundException e) {
      return defaultString;
    }
  }

  /**
   * Sends a push notification opened broadcast to the client broadcast receiver.
   * The broadcast message action is <host-app-package-name>.intent.APPBOY_NOTIFICATION_OPENED.
   *
   * @param context
   * @param intent  the internal notification clicked intent constructed in
   *                {@link #setContentIntentIfPresent}
   */
  static void sendNotificationOpenedBroadcast(Context context, Intent intent) {
    String pushOpenedAction = context.getPackageName() + AppboyNotificationUtils.APPBOY_NOTIFICATION_OPENED_SUFFIX;
    Intent pushOpenedIntent = new Intent(pushOpenedAction);
    if (intent.getExtras() != null) {
      pushOpenedIntent.putExtras(intent.getExtras());
    }
    context.sendBroadcast(pushOpenedIntent);
  }

  /**
   * Logs a push notification open.
   *
   * @param context
   * @param intent  the internal notification clicked intent constructed in
   *                {@link #setContentIntentIfPresent}
   */
  private static void logNotificationOpened(Context context, Intent intent) {
    Appboy.getInstance(context).logPushNotificationOpened(intent);
  }
}
