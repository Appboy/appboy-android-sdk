package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.appboy.ui.AppboyWebViewActivity;

/**
 * Action that opens the Google Play market to a specific app in either the Google Play store app
 * or via the AppboyWebViewActivity class. If it is opened in the AppboyWebViewActivity class, the
 * time spent viewing the app in the web view counts toward the session duration.
 */
public final class GooglePlayAppDetailsAction implements IAction {
  private static final String PLAY_STORE_APP_BASE = "market://details?id=";
  private static final String PLAY_STORE_WEB_BASE = "https://play.google.com/store/apps/details?id=";

  private final String mPackageName;
  private final boolean mUseAppboyWebView;

  public GooglePlayAppDetailsAction(String packageName, boolean useAppboyWebView) {
    mPackageName = packageName;
    mUseAppboyWebView = useAppboyWebView;
  }

  @Override
  public void execute(Context context) {
    if (mUseAppboyWebView) {
      Uri uri = Uri.parse(PLAY_STORE_WEB_BASE + mPackageName);
      Intent intent = new Intent(Intent.ACTION_VIEW, uri, context, AppboyWebViewActivity.class);
      context.startActivity(intent);
    } else {
      Uri uri = Uri.parse(PLAY_STORE_APP_BASE + mPackageName);
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      context.startActivity(intent);
    }
  }
}
