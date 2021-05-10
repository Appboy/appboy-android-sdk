package com.appboy.ui.support;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.appboy.IAppboyNavigator;
import com.appboy.support.StringUtils;
import com.appboy.ui.AppboyNavigator;
import com.braze.support.BrazeLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UriUtils {
  private static final String TAG = BrazeLogger.getBrazeLogTag(UriUtils.class);

  /**
   * Parses the query part of the uri and returns a mapping of the query keys to the
   * values. Empty keys or empty values will not be included in the mapping.
   */
  public static Map<String, String> getQueryParameters(Uri uri) {
    String encodedQuery = uri.getEncodedQuery();
    if (encodedQuery == null) {
      BrazeLogger.v(TAG, "Encoded query is null for Uri: " + uri + " Returning empty map for query parameters");
      return Collections.emptyMap();
    }

    Map<String, String> parameterValueMap = new HashMap<>();
    try {
      if (uri.isOpaque()) {
        // Convert the opaque uri into a parseable hierarchical one
        // This is basically copying the query from the original uri onto a new one
        uri = Uri.parse("://")
            .buildUpon()
            .encodedQuery(encodedQuery)
            .build();
      }

      final Set<String> queryParameterNames = uri.getQueryParameterNames();
      for (String queryParameterKey : queryParameterNames) {
        final String queryParameterValue = uri.getQueryParameter(queryParameterKey);

        if (!StringUtils.isNullOrEmpty(queryParameterKey) && !StringUtils.isNullOrEmpty(queryParameterValue)) {
          parameterValueMap.put(queryParameterKey, queryParameterValue);
        }
      }
    } catch (Exception e) {
      BrazeLogger.e(TAG, "Failed to map the query parameters of Uri: " + uri, e);
    }

    return parameterValueMap;
  }

  public static Intent getMainActivityIntent(Context context, Bundle extras) {
    Intent startActivityIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    startActivityIntent.setFlags(AppboyNavigator.getAppboyNavigator().getIntentFlags(IAppboyNavigator.IntentFlagPurpose.URI_UTILS_GET_MAIN_ACTIVITY_INTENT));
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
      BrazeLogger.w(TAG, "Could not find activity info for class with name: " + className, e);
      return false;
    }
  }
}
