package com.appboy.ui.support;

import android.net.Uri;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UriUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, UriUtils.class.getName());

  /**
   * Backport of the Uri.getQueryParameters method.
   *
   * Note: A Uri such as tel:+1-555-555-5555 is not hierarchical and does not accept a query
   * string, so an empty Map will be returned.
   */
  public static Map<String, String> getQueryParameters(Uri uri) {
    if (uri.isOpaque()) {
      AppboyLogger.d(TAG, "URI is not hierarchical. There are no query parameters to parse.");
      return Collections.emptyMap();
    }

    String query = uri.getEncodedQuery();
    if (query == null) {
      return Collections.emptyMap();
    }

    Map<String, String> parameters = new HashMap<String, String>();
    int start = 0;
    do {
      int next = query.indexOf('&', start);
      int end = (next == -1) ? query.length() : next;

      int separator = query.indexOf('=', start);
      if (separator > end || separator == -1) {
        separator = end;
      }

      if (end > start) {
        String name = query.substring(start, separator);
        String value = query.substring(separator + 1, end);
        parameters.put(Uri.decode(name), Uri.decode(value));
      }

      // Move start to end of name.
      start = end + 1;
    } while (start < query.length());
    return Collections.unmodifiableMap(parameters);
  }
}
