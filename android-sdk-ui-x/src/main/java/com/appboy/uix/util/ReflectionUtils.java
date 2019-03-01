package com.appboy.uix.util;

import com.appboy.support.AppboyLogger;

import java.lang.reflect.Method;

public class ReflectionUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(ReflectionUtils.class);

  public static Object invokeMethod(Object receiver, Method method, Object... args) {
    try {
      return method.invoke(receiver, args);
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to invoke method: " + method, e);
      return null;
    }
  }
}
