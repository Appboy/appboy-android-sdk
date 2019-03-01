package com.appboy.uix.push;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.appboy.IAppboyNotificationFactory;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.uix.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AppboyNotificationFactory implements IAppboyNotificationFactory {
  private static final Map<String, Method> METHOD_MAPPING = new HashMap<String, Method>();

  static {
    Class clazz = com.appboy.push.AppboyNotificationFactory.class;
    Method[] classMethods = clazz.getMethods();

    for (Method classMethod : classMethods) {
      METHOD_MAPPING.put(classMethod.getName(), classMethod);
    }
  }

  private static volatile AppboyNotificationFactory sInstance = null;

  /**
   * @see com.appboy.push.AppboyNotificationFactory#getInstance()
   */
  public static AppboyNotificationFactory getInstance() {
    if (sInstance == null) {
      synchronized (AppboyNotificationFactory.class) {
        if (sInstance == null) {
          sInstance = new AppboyNotificationFactory();
        }
      }
    }
    return sInstance;
  }

  /**
   * @see com.appboy.push.AppboyNotificationFactory#populateNotificationBuilder(AppboyConfigurationProvider, Context, Bundle, Bundle)
   */
  public NotificationCompat.Builder populateNotificationBuilder(AppboyConfigurationProvider appConfigurationProvider,
                                                                Context context, Bundle notificationExtras, Bundle appboyExtras) {
    Method originalMethod = METHOD_MAPPING.get("populateNotificationBuilder");
    if (originalMethod != null) {
      return (NotificationCompat.Builder) ReflectionUtils.invokeMethod(com.appboy.push.AppboyNotificationFactory.getInstance(), originalMethod, appConfigurationProvider,
          context, notificationExtras, appboyExtras);
    } else {
      return null;
    }
  }

  /**
   * @see com.appboy.push.AppboyNotificationFactory#createNotification(AppboyConfigurationProvider, Context, Bundle, Bundle)
   */
  @Override
  public Notification createNotification(AppboyConfigurationProvider appConfigurationProvider,
                                                Context context, Bundle notificationExtras, Bundle appboyExtras) {
    Method originalMethod = METHOD_MAPPING.get("createNotification");
    if (originalMethod != null) {
      return (Notification) ReflectionUtils.invokeMethod(com.appboy.push.AppboyNotificationFactory.getInstance(), originalMethod, appConfigurationProvider,
          context, notificationExtras, appboyExtras);
    } else {
      return null;
    }
  }
}
