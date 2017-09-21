package com.appboy.ui.support;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.appboy.support.AppboyLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UriUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(UriUtils.class);

  /**
   * Backport of the Uri.getQueryParameters method.
   *
   * Note: A Uri such as tel:+1-555-555-5555 is not hierarchical and does not accept a query
   * string, so an empty Map will be returned.
   */
  @SuppressWarnings("checkstyle:rightcurly")
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

  public static Intent getMainActivityIntent(Context context, Bundle extras) {
    // get main activity intent.
    Intent startActivityIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    if (extras != null) {
      startActivityIntent.putExtras(extras);
    }
    return startActivityIntent;
  }

  /**
   * @param context The context used to create the checked component identifier.
   * @param className The class name for a registered activity with the given context
   * @return true if the class name matches a registered activity in the Android Manifest.
   */
  public static boolean isActivityRegisteredInManifest(Context context, String className) {
    try {
      // If the activity is registered, then a non-null ActivityInfo is returned by the package manager.
      // If unregistered, then an exception is thrown by the package manager.
      ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(new ComponentName(context, className), 0);
      return activityInfo != null;
    } catch (PackageManager.NameNotFoundException e) {
      AppboyLogger.w(TAG, "Could not find activity info for class with name: " + className, e);
      return false;
    }
  }
}
