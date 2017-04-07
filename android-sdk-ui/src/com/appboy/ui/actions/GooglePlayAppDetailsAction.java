package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;

import com.appboy.Constants;
import com.appboy.enums.AppStore;
import com.appboy.enums.Channel;
import com.appboy.support.AppboyLogger;

/**
 * Action that opens the Google Play market to a specific app in either the Google Play store app
 * or via the AppboyWebViewActivity class. If it is opened in the AppboyWebViewActivity class, the
 * time spent viewing the app in the web view counts toward the session duration.
 */
public final class GooglePlayAppDetailsAction implements IAction {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, GooglePlayAppDetailsAction.class.getName());
  private static final String PLAY_STORE_APP_BASE = "market://details?id=";
  private static final String PLAY_STORE_WEB_BASE = "https://play.google.com/store/apps/details?id=";
  private static final String AMAZON_STORE_APP_BASE = "amzn://apps/android?asin=";
  private static final String AMAZON_STORE_WEB_BASE = "http://www.amazon.com/gp/mas/dl/android?asin=";

  private final String mPackageName;
  private boolean mUseWebView;
  private final AppStore mAppStore;
  private final String mKindleId;
  private final Channel mChannel;

  public GooglePlayAppDetailsAction(String packageName, boolean useAppboyWebView, AppStore appStore,
                                    String kindleId, Channel channel) {
    mPackageName = packageName;
    mUseWebView = useAppboyWebView;
    mAppStore = appStore;
    mKindleId = kindleId;
    mChannel = channel;
  }

  @Override
  public Channel getChannel() {
    return mChannel;
  }

  @Override
  public void execute(Context context) {
    if (mAppStore != AppStore.KINDLE_STORE) {
      try {
        context.getPackageManager().getPackageInfo(("com.google.android.gsf"), 0);
      } catch (PackageManager.NameNotFoundException e) {
        AppboyLogger.i(TAG, "Google Play Store not found, launching Play Store with WebView");
        mUseWebView = true;
      } catch (Exception e) {
        AppboyLogger.e(TAG, "Unexpected exception while checking for com.google.android.gsf.");
        mUseWebView = true;
      }
    }

    String uriString;
    if (mUseWebView) {
      if (mAppStore == AppStore.KINDLE_STORE) {
        uriString = AMAZON_STORE_WEB_BASE + mKindleId;
      } else {
        uriString = PLAY_STORE_WEB_BASE + mPackageName;
      }
      Uri uri = Uri.parse(uriString);
      UriAction.openUriWithWebView(context, uri, null);
    } else {
      if (mAppStore == AppStore.KINDLE_STORE) {
        uriString = AMAZON_STORE_APP_BASE + mKindleId;
      } else {
        uriString = PLAY_STORE_APP_BASE + mPackageName;
      }
      Uri uri = Uri.parse(uriString);
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      context.startActivity(intent);
    }
  }

  @VisibleForTesting
  public boolean getUseWebView() {
    return mUseWebView;
  }
}
