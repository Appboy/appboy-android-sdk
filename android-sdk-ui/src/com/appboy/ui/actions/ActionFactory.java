package com.appboy.ui.actions;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.appboy.support.StringUtils;

public class ActionFactory {

  public static IAction createUriAction(Context context, String url) {
    return createUriAction(context, url, null);
  }

  public static IAction createUriAction(Context context, String url, Bundle extras) {
    if (!StringUtils.isNullOrBlank(url)) {
      Uri uri = Uri.parse(url);
      if (WebAction.getSupportedSchemes().contains(uri.getScheme())) {
        return new WebAction(url, extras);
      } else if ("intent".equals(uri.getScheme())) {
        return new ActivityAction(context.getPackageName(), uri, extras);
      } else {
        return new ViewAction(uri, extras);
      }
    }
    return null;
  }

  /**
   * Explicitly creates an Action to use the android.intent.action.VIEW action with the given URL.
   */
  public static IAction createViewUriAction(String url, Bundle extras) {
    if (!StringUtils.isNullOrBlank(url)) {
      Uri uri = Uri.parse(url);
      return new ViewAction(uri, extras);
    }
    return null;
  }
}
