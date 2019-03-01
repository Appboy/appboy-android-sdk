package com.appboy.uix.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.appboy.uix.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AppboyNotificationActionUtils {
  private static final Map<String, Method> METHOD_MAPPING = new HashMap<String, Method>();

  static {
    Class clazz = com.appboy.push.AppboyNotificationActionUtils.class;
    Method[] classMethods = clazz.getMethods();

    for (Method classMethod : classMethods) {
      METHOD_MAPPING.put(classMethod.getName(), classMethod);
    }
  }

  public static void addNotificationActions(Context context, NotificationCompat.Builder notificationBuilder, Bundle notificationExtras) {
    Method originalMethod = METHOD_MAPPING.get("addNotificationActions");
    if (originalMethod != null) {
      ReflectionUtils.invokeMethod(null, originalMethod, context, notificationBuilder, notificationExtras);
    }
  }

  /**
   * @see com.appboy.push.AppboyNotificationActionUtils#handleNotificationActionClicked(Context, Intent)
   */
  public static void handleNotificationActionClicked(Context context, Intent intent) {
    com.appboy.push.AppboyNotificationActionUtils.handleNotificationActionClicked(context, intent);
  }

  /**
   * @see com.appboy.push.AppboyNotificationActionUtils#getActionFieldAtIndex(int, Bundle, String)
   */
  public static String getActionFieldAtIndex(int actionIndex, Bundle notificationExtras, String actionFieldKeyTemplate) {
    return com.appboy.push.AppboyNotificationActionUtils.getActionFieldAtIndex(actionIndex, notificationExtras, actionFieldKeyTemplate);
  }

  /**
   * @see com.appboy.push.AppboyNotificationActionUtils#getActionFieldAtIndex(int, Bundle, String, String)
   */
  public static String getActionFieldAtIndex(int actionIndex, Bundle notificationExtras, String actionFieldKeyTemplate, String defaultValue) {
    return com.appboy.push.AppboyNotificationActionUtils.getActionFieldAtIndex(actionIndex, notificationExtras, actionFieldKeyTemplate, defaultValue);
  }
}
