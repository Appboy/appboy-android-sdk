package com.appboy.ui.actions;

import android.content.Context;
import android.net.Uri;

import com.appboy.ui.support.StringUtils;

public class ActionFactory {
  public static IAction createUriAction(Context context, String url) {
    if (!StringUtils.isNullOrBlank(url)) {
      Uri uri = Uri.parse(url);
      if (WebAction.getSupportedSchemes().contains(uri.getScheme())) {
        return new WebAction(url);
      } else if ("intent".equals(uri.getScheme())) {
        return new ActivityAction(context.getPackageName(), uri);
      } else {
        return new ViewAction(uri);
      }
    }
    return null;
  }
}
