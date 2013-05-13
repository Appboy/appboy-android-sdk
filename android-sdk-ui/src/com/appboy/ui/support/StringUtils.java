package com.appboy.ui.support;

public final class StringUtils {
  public static final String EMPTY_STRING = "";

  public static String checkNotNullOrEmpty(String reference) {
    if (reference == null) {
      throw new NullPointerException("Provided String must be non-null.");
    }

    if (reference.length() == 0) {
      throw new IllegalArgumentException("Provided String must be non-empty.");
    }

    return reference;
  }

  public static boolean isNullOrEmpty(String reference) {
    return reference == null || reference.length() == 0;
  }

  /**
   * Checks if the string is null or only contains whitespace characters
   */
  public static boolean isNullOrBlank(String reference) {
    return reference == null || reference.trim().length() == 0;
  }
}
