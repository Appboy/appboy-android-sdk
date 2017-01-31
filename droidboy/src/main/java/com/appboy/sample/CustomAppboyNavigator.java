package com.appboy.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.appboy.Constants;
import com.appboy.IAppboyNavigator;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;

public class CustomAppboyNavigator implements IAppboyNavigator {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, CustomAppboyNavigator.class.getName());

  @Override
  public void gotoNewsFeed(Context context, Bundle extras) {
    Intent intent = new Intent(context, DroidBoyActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtra(AppboyBroadcastReceiver.SOURCE_KEY, Constants.APPBOY);
    intent.putExtra(AppboyBroadcastReceiver.DESTINATION_VIEW, AppboyBroadcastReceiver.FEED);
    context.startActivity(intent);
  }

  @Override
  public void gotoURI(Context context, Uri uri, Bundle extras) {
    if (uri == null) {
      AppboyLogger.e(TAG, "IAppboyNavigator cannot open URI because the URI is null.");
      return;
    }
    if (!StringUtils.isNullOrBlank(uri.toString()) && uri.toString().matches(context.getString(R.string.youtube_regex))) {
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      if (extras != null) {
        intent.putExtras(extras);
      }
      context.startActivity(intent);
    } else {
      IAction action = ActionFactory.createUriAction(context, uri.toString(), extras);
      action.execute(context);
    }
  }
}

