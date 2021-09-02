package com.appboy.ui.actions;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.appboy.enums.Channel;

/**
 * @deprecated Please use {@link com.braze.ui.actions.UriAction} instead. Deprecated since 7/27/21
 */
@Deprecated
public class UriAction extends com.braze.ui.actions.UriAction implements IAction {
  public UriAction(@NonNull Uri uri, Bundle extras, boolean useWebView, @NonNull Channel channel) {
    super(uri, extras, useWebView, channel);
  }

  public UriAction(@NonNull com.appboy.ui.actions.UriAction originalUriAction) {
    super(originalUriAction);
  }
}
