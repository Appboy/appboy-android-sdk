package com.appboy.ui.support;

public class ValidationUtils {
  // Regular expression that validates an email address. This is the same regular expression being used in the
  // Appboy iOS SDK. Adapted from http://www.cocoawithlove.com/2009/06/verifying-that-string-is-email-address.html
  private static String EMAIL_ADDRESS_REGEX =
    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
    "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[" +
    "\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z" +
    "0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9" +
    "]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z" +
    "0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\" +
    "x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

  public static boolean isValidEmailAddress(String emailAddress) {
    return emailAddress != null && emailAddress.toLowerCase().matches(EMAIL_ADDRESS_REGEX);
  }
}
