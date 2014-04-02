package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;

import com.appboy.ui.AppboyWebViewActivity;

/**
 * Action that launches the AppboyWebViewActivity to a particular URL.
 */
public final class WebAction implements IAction {
  private final String mTargetUrl;

  public WebAction(String targetUrl) {
    mTargetUrl = targetUrl;
  }

  public void execute(Context context) {
    Intent intent = new Intent(context, AppboyWebViewActivity.class);
    intent.putExtra(AppboyWebViewActivity.URL_EXTRA, mTargetUrl);
    context.startActivity(intent);
  }
}
