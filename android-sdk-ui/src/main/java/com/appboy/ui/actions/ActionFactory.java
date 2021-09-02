package com.appboy.ui.actions;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.appboy.enums.Channel;
import com.braze.support.StringUtils;

/**
 * @deprecated Please use {@link com.braze.IBrazeDeeplinkHandler} instead. Deprecated since 7/27/21
 */
@Deprecated
public class ActionFactory {
  @Nullable
  public static UriAction createUriActionFromUrlString(String url, Bundle extras, boolean openInWebView, Channel channel) {
    if (!StringUtils.isNullOrBlank(url)) {
      Uri uri = Uri.parse(url);
      return createUriActionFromUri(uri, extras, openInWebView, channel);
    }
    return null;
  }

  @Nullable
  public static UriAction createUriActionFromUri(Uri uri, Bundle extras, boolean openInWebView, Channel channel) {
    if (uri != null) {
      return new UriAction(uri, extras, openInWebView, channel);
    }
    return null;
  }
}
