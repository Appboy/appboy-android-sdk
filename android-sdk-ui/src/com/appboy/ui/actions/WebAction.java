package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;

import com.appboy.ui.AppboyWebViewActivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Action that launches the AppboyWebViewActivity to a particular URL.
 */
public final class WebAction implements IAction {
  private final String mTargetUrl;
  private static final List<String> sSupportedSchemes = Collections.unmodifiableList(
      Arrays.asList("http", "https", "ftp", "ftps", "about", "javascript", "mailto", "tel"));

  public WebAction(String targetUrl) {
    mTargetUrl = targetUrl;
  }

  public void execute(Context context) {
    Intent intent = new Intent(context, AppboyWebViewActivity.class);
    intent.putExtra(AppboyWebViewActivity.URL_EXTRA, mTargetUrl);
    context.startActivity(intent);
  }

  public static List<String> getSupportedSchemes() {
    return sSupportedSchemes;
  }
}
