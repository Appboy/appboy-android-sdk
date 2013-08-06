package com.appboy.ui.actions;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import com.appboy.Constants;

import java.util.List;

/**
 * Action that opens the Google Play market to a specific app. If the Google Play market is not
 * installed on the device, it will launch an intent to open a browser to the app on the Google
 * Play web store.
 */
public final class MarketAction implements IAction {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, MarketAction.class.getName());
  private static final String PLAY_STORE_WEB_BASE = "https://play.google.com/store/apps/details?id=";

  private final Uri mMarketUri;
  private final String mAppPackageName;

  public MarketAction(String marketIntentAction, String appPackageName) {
    mMarketUri = Uri.parse(marketIntentAction);
    mAppPackageName = appPackageName;
  }

  @Override
  public void execute(Context context) {
    Intent marketIntent = new Intent(Intent.ACTION_VIEW, mMarketUri);
    List<ResolveInfo> marketIntentActivities = context.getPackageManager().queryIntentActivities(marketIntent, 0);
    if (marketIntentActivities.isEmpty()) {
      Uri playStoreWebUri = Uri.parse(PLAY_STORE_WEB_BASE + mAppPackageName);
      marketIntent = new Intent(Intent.ACTION_VIEW, playStoreWebUri);
    }
    try {
      context.startActivity(marketIntent);
    } catch (ActivityNotFoundException e) {
      Log.w(TAG, String.format("Unable to open %s.", marketIntent.getData()));
    }
  }
}
