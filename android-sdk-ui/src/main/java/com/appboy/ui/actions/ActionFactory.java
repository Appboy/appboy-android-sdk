package com.appboy.ui.actions;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.appboy.enums.Channel;
import com.appboy.support.StringUtils;

public class ActionFactory {

  /**
   * Convenience method for creating {@link UriAction} instances. Returns null if the supplied url
   * is null, blank, or can not be parsed into a valid Uri.
   */
  @Nullable
  public static UriAction createUriActionFromUrlString(String url, Bundle extras, boolean openInWebView, Channel channel) {
    if (!StringUtils.isNullOrBlank(url)) {
      Uri uri = Uri.parse(url);
      return createUriActionFromUri(uri, extras, openInWebView, channel);
    }
    return null;
  }

  /**
   * Convenience method for creating {@link UriAction} instances. Returns null if the supplied uri
   * is null.
   */
  @Nullable
  public static UriAction createUriActionFromUri(Uri uri, Bundle extras, boolean openInWebView, Channel channel) {
    if (uri != null) {
      return new UriAction(uri, extras, openInWebView, channel);
    }
    return null;
  }
}
