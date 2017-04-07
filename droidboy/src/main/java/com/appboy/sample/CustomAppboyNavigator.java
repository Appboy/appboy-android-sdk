package com.appboy.sample;

import android.content.Context;
import android.content.Intent;

import com.appboy.Constants;
import com.appboy.IAppboyNavigator;
import com.appboy.support.StringUtils;
import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.actions.NewsfeedAction;
import com.appboy.ui.actions.UriAction;

public class CustomAppboyNavigator implements IAppboyNavigator {

  @Override
  public void gotoNewsFeed(Context context, NewsfeedAction newsfeedAction) {
    Intent intent = new Intent(context, DroidBoyActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtras(newsfeedAction.getExtras());
    intent.putExtra(AppboyBroadcastReceiver.SOURCE_KEY, Constants.APPBOY);
    intent.putExtra(AppboyBroadcastReceiver.DESTINATION_VIEW, AppboyBroadcastReceiver.FEED);
    context.startActivity(intent);
  }

  @Override
  public void gotoUri(Context context, UriAction uriAction) {
    String uri = uriAction.getUri().toString();
    if (!StringUtils.isNullOrBlank(uri) && uri.matches(context.getString(R.string.youtube_regex))) {
      uriAction.setUseWebView(false);
    }
    AppboyNavigator.executeUriAction(context, uriAction);
  }
}

