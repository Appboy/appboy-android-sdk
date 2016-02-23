package com.appboy.wear.support;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {
  private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
  public static final String FORMAT_LONG = "yyyy-MM-dd kk:mm:ss";

  /**
   * Formats the date using the specified DateFormat pattern
   *
   * @param date object to format
   * @return string representation of the date in specified format
   * @throws NullPointerException if date is null
   */
  public static String formatDate(Date date) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    simpleDateFormat.setTimeZone(UTC_TIME_ZONE);
    simpleDateFormat.applyPattern(FORMAT_LONG);
    return simpleDateFormat.format(date);
  }
}
