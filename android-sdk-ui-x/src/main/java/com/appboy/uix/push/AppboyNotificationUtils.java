package com.appboy.uix.push;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.support.AppboyLogger;
import com.appboy.uix.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AppboyNotificationUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyNotificationUtils.class);
  private static final Map<String, Method> METHOD_MAPPING = new HashMap<String, Method>();

  static {
    Class clazz = com.appboy.push.AppboyNotificationUtils.class;
    Method[] classMethods = clazz.getMethods();

    for (Method classMethod : classMethods) {
      METHOD_MAPPING.put(classMethod.getName(), classMethod);
    }
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#handleNotificationOpened(Context, Intent)
   */
  public static void handleNotificationOpened(Context context, Intent intent) {
    com.appboy.push.AppboyNotificationUtils.handleNotificationOpened(context, intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#handleNotificationDeleted(Context, Intent)
   */
  public static void handleNotificationDeleted(Context context, Intent intent) {
    com.appboy.push.AppboyNotificationUtils.handleNotificationDeleted(context, intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#routeUserWithNotificationOpenedIntent(Context, Intent)
   */
  public static void routeUserWithNotificationOpenedIntent(Context context, Intent intent) {
    com.appboy.push.AppboyNotificationUtils.routeUserWithNotificationOpenedIntent(context, intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#getAppboyExtrasWithoutPreprocessing(Bundle)
   */
  public static Bundle getAppboyExtrasWithoutPreprocessing(Bundle notificationExtras) {
    return com.appboy.push.AppboyNotificationUtils.getAppboyExtrasWithoutPreprocessing(notificationExtras);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#parseJSONStringDictionaryIntoBundle(String)
   */
  public static Bundle parseJSONStringDictionaryIntoBundle(String jsonStringDictionary) {
    return com.appboy.push.AppboyNotificationUtils.parseJSONStringDictionaryIntoBundle(jsonStringDictionary);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#isAppboyPushMessage(Intent)
   */
  public static boolean isAppboyPushMessage(Intent intent) {
    return com.appboy.push.AppboyNotificationUtils.isAppboyPushMessage(intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#isNotificationMessage(Intent)
   */
  public static boolean isNotificationMessage(Intent intent) {
    return com.appboy.push.AppboyNotificationUtils.isNotificationMessage(intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#sendPushMessageReceivedBroadcast(Context, Bundle)
   */
  public static void sendPushMessageReceivedBroadcast(Context context, Bundle notificationExtras) {
    com.appboy.push.AppboyNotificationUtils.sendPushMessageReceivedBroadcast(context, notificationExtras);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#requestGeofenceRefreshIfAppropriate(Context, Bundle)
   */
  public static boolean requestGeofenceRefreshIfAppropriate(Context context, Bundle notificationExtras) {
    return com.appboy.push.AppboyNotificationUtils.requestGeofenceRefreshIfAppropriate(context, notificationExtras);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#setNotificationDurationAlarm(Context, Class, int, int)
   */
  public static void setNotificationDurationAlarm(Context context, Class<?> thisClass, int notificationId, int durationInMillis) {
    com.appboy.push.AppboyNotificationUtils.setNotificationDurationAlarm(context, thisClass, notificationId, durationInMillis);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#getNotificationId(Bundle)
   */
  public static int getNotificationId(Bundle notificationExtras) {
    return com.appboy.push.AppboyNotificationUtils.getNotificationId(notificationExtras);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#getNotificationPriority(Bundle)
   */
  public static int getNotificationPriority(Bundle notificationExtras) {
    return com.appboy.push.AppboyNotificationUtils.getNotificationPriority(notificationExtras);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#isValidNotificationPriority(int)
   */
  public static boolean isValidNotificationPriority(int priority) {
    return com.appboy.push.AppboyNotificationUtils.isValidNotificationVisibility(priority);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#wakeScreenIfAppropriate(Context, AppboyConfigurationProvider, Bundle)
   */
  public static boolean wakeScreenIfAppropriate(Context context, AppboyConfigurationProvider configurationProvider, Bundle notificationExtras) {
    return com.appboy.push.AppboyNotificationUtils.wakeScreenIfAppropriate(context, configurationProvider, notificationExtras);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#getActiveNotificationFactory()
   */
  public static IAppboyNotificationFactory getActiveNotificationFactory() {
    return com.appboy.push.AppboyNotificationUtils.getActiveNotificationFactory();
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#prefetchBitmapsIfNewlyReceivedStoryPush(Context, Bundle, Bundle)
   */
  public static void prefetchBitmapsIfNewlyReceivedStoryPush(Context context, Bundle notificationExtras, Bundle appboyExtras) {
    com.appboy.push.AppboyNotificationUtils.prefetchBitmapsIfNewlyReceivedStoryPush(context, notificationExtras, appboyExtras);
  }

  public static void setTitleIfPresent(AppboyConfigurationProvider appboyConfigurationProvider, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setTitleIfPresent";
    Object[] args = new Object[]{appboyConfigurationProvider, notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setContentIfPresent(AppboyConfigurationProvider appboyConfigurationProvider, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setContentIfPresent";
    Object[] args = new Object[]{appboyConfigurationProvider, notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setTickerIfPresent(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setTickerIfPresent";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setContentIntentIfPresent(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setContentIntentIfPresent";
    Object[] args = new Object[]{context, notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setDeleteIntent(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setDeleteIntent";
    Object[] args = new Object[]{context, notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static int setSmallIcon(AppboyConfigurationProvider appConfigurationProvider, NotificationCompat.Builder notificationBuilder) {
    String thisMethodName = "setSmallIcon";
    Object[] args = new Object[]{appConfigurationProvider, notificationBuilder};
    return (Integer) invokeOriginalMethod(thisMethodName, 0, args);
  }

  public static void setSetShowWhen(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setSetShowWhen";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static boolean setLargeIconIfPresentAndSupported(Context context, AppboyConfigurationProvider appConfigurationProvider,
                                                          NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setLargeIconIfPresentAndSupported";
    Object[] args = new Object[]{context, appConfigurationProvider, notificationBuilder, notificationExtras};
    return (Boolean) invokeOriginalMethod(thisMethodName, false, args);
  }

  public static void setSoundIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setSoundIfPresentAndSupported";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setSummaryTextIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setSummaryTextIfPresentAndSupported";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setPriorityIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setPriorityIfPresentAndSupported";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setStyleIfSupported(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras, Bundle appboyExtras) {
    String thisMethodName = "setStyleIfSupported";
    Object[] args = new Object[]{context, notificationBuilder, notificationExtras, appboyExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setAccentColorIfPresentAndSupported(AppboyConfigurationProvider appConfigurationProvider,
                                                         NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setAccentColorIfPresentAndSupported";
    Object[] args = new Object[]{appConfigurationProvider, notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setCategoryIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setCategoryIfPresentAndSupported";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setVisibilityIfPresentAndSupported(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setVisibilityIfPresentAndSupported";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static void setPublicVersionIfPresentAndSupported(Context context, AppboyConfigurationProvider appboyConfigurationProvider,
                                                           NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setPublicVersionIfPresentAndSupported";
    Object[] args = new Object[]{context, appboyConfigurationProvider, notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#isValidNotificationVisibility(int)
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public static boolean isValidNotificationVisibility(int visibility) {
    return com.appboy.push.AppboyNotificationUtils.isValidNotificationVisibility(visibility);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#logBaiduNotificationClick(Context, String)
   */
  public static void logBaiduNotificationClick(Context context, String customContentString) {
    com.appboy.push.AppboyNotificationUtils.logBaiduNotificationClick(context, customContentString);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#handleCancelNotificationAction(Context, Intent)
   */
  public static void handleCancelNotificationAction(Context context, Intent intent) {
    com.appboy.push.AppboyNotificationUtils.handleCancelNotificationAction(context, intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#cancelNotification(Context, int)
   */
  public static void cancelNotification(Context context, int notificationId) {
    com.appboy.push.AppboyNotificationUtils.cancelNotification(context, notificationId);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#getNotificationReceiverClass()
   */
  public static Class<?> getNotificationReceiverClass() {
    return com.appboy.push.AppboyNotificationUtils.getNotificationReceiverClass();
  }

  /**
   * @deprecated The channel id should be set in {@link NotificationCompat.Builder#Builder(Context, String)}
   */
  @Deprecated
  public static void setNotificationChannelIfSupported(Context context, AppboyConfigurationProvider appConfigurationProvider,
                                                       NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setNotificationChannelIfSupported";
    Object[] args = new Object[]{context, appConfigurationProvider, notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  public static String getOrCreateNotificationChannelId(Context context,
                                                        AppboyConfigurationProvider appConfigurationProvider,
                                                        Bundle notificationExtras) {
    final String methodName = "getOrCreateNotificationChannelId";
    Object[] args = {context, appConfigurationProvider, notificationExtras};
    return (String) invokeOriginalMethod(methodName, null, args);
  }

  public static void setNotificationBadgeNumberIfPresent(NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    String thisMethodName = "setNotificationBadgeNumberIfPresent";
    Object[] args = new Object[]{notificationBuilder, notificationExtras};
    invokeOriginalMethod(thisMethodName, args);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#logPushDeliveryEvent(Context, Bundle)
   */
  public static void logPushDeliveryEvent(Context context, Bundle pushExtras) {
    com.appboy.push.AppboyNotificationUtils.logPushDeliveryEvent(context, pushExtras);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#handlePushStoryPageClicked(Context, Intent)
   */
  public static void handlePushStoryPageClicked(Context context, Intent intent) {
    com.appboy.push.AppboyNotificationUtils.handlePushStoryPageClicked(context, intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationUtils#handleContentCardsSerializedCardIfPresent(Context, Bundle)
   */
  public static void handleContentCardsSerializedCardIfPresent(Context context, Bundle fcmExtras) {
    com.appboy.push.AppboyNotificationUtils.handleContentCardsSerializedCardIfPresent(context, fcmExtras);
  }

  public static boolean isUninstallTrackingPush(Bundle notificationExtras) {
    return com.appboy.push.AppboyNotificationUtils.isUninstallTrackingPush(notificationExtras);
  }

  public static boolean isInAppMessageTestPush(@NonNull Intent intent) {
    return com.appboy.push.AppboyNotificationUtils.isInAppMessageTestPush(intent);
  }

  private static void invokeOriginalMethod(String methodName, Object[] args) {
    invokeOriginalMethod(methodName, null, args);
  }

  private static Object invokeOriginalMethod(String methodName, Object defaultValue, Object[] args) {
    // Get the corresponding method
    Method originalMethod = METHOD_MAPPING.get(methodName);
    if (originalMethod != null) {
      return ReflectionUtils.invokeMethod(null, originalMethod, args);
    } else {
      Log.w(TAG, "Original method with name: " + methodName + " was not found in method mapping");
    }
    return defaultValue;
  }
}
