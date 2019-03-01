package com.appboy.uix.push;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.support.AppboyLogger;
import com.appboy.uix.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AppboyNotificationStyleFactory {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyNotificationStyleFactory.class);
  private static final Map<String, Method> METHOD_MAPPING = new HashMap<String, Method>();

  static {
    Class clazz = com.appboy.push.AppboyNotificationStyleFactory.class;
    Method[] classMethods = clazz.getMethods();

    for (Method classMethod : classMethods) {
      METHOD_MAPPING.put(classMethod.getName(), classMethod);
    }
  }

  @Nullable
  public static NotificationCompat.Style getBigNotificationStyle(Context context, Bundle notificationExtras, Bundle appboyExtras,
                                                                 NotificationCompat.Builder notificationBuilder) {
    String thisMethodName = "getBigNotificationStyle";
    Object[] args = new Object[]{context, notificationExtras, appboyExtras, notificationBuilder};
    return (NotificationCompat.Style) invokeOriginalMethod(thisMethodName, null, args);
  }

  @Nullable
  public static NotificationCompat.BigTextStyle getBigTextNotificationStyle(AppboyConfigurationProvider appboyConfigurationProvider, Bundle notificationExtras) {
    String thisMethodName = "getBigTextNotificationStyle";
    Object[] args = new Object[]{appboyConfigurationProvider, notificationExtras};
    return (NotificationCompat.BigTextStyle) invokeOriginalMethod(thisMethodName, null, args);
  }

  @Nullable
  public static NotificationCompat.DecoratedCustomViewStyle getStoryStyle(Context context, Bundle notificationExtras, NotificationCompat.Builder notificationBuilder) {
    String thisMethodName = "getStoryStyle";
    Object[] args = new Object[]{context, notificationExtras, notificationBuilder};
    return (NotificationCompat.DecoratedCustomViewStyle) invokeOriginalMethod(thisMethodName, null, args);
  }

  @Nullable
  public static NotificationCompat.BigPictureStyle getBigPictureNotificationStyle(Context context, Bundle notificationExtras, Bundle appboyExtras) {
    String thisMethodName = "getBigPictureNotificationStyle";
    Object[] args = new Object[]{context, notificationExtras, appboyExtras};
    return (NotificationCompat.BigPictureStyle) invokeOriginalMethod(thisMethodName, null, args);
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
