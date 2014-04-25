package com.appboy.ui.support;

import android.content.res.Resources;

public final class StringUtils {
  public static final String EMPTY_STRING = "";

  public static boolean isNullOrEmpty(String reference) {
    return reference == null || reference.length() == 0;
  }

  /**
   * Checks if the string is null or only contains whitespace characters
   */
  public static boolean isNullOrBlank(String reference) {
    return reference == null || reference.trim().length() == 0;
  }

  public static String getOptionalStringResource(Resources resources, int stringResourceId, String defaultString) {
    try {
      return resources.getString(stringResourceId);
    } catch (Resources.NotFoundException e) {
      return defaultString;
    }
  }
}
