package com.appboy.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.appboy.Constants;
import com.appboy.IAppboyNavigator;
import com.appboy.sample.activity.DroidBoyActivity;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.actions.NewsfeedAction;
import com.appboy.ui.actions.UriAction;

public class CustomAppboyNavigator implements IAppboyNavigator {
  private static final String TAG = AppboyLogger.getAppboyLogTag(CustomAppboyNavigator.class);

  @Override
  public void gotoNewsFeed(Context context, NewsfeedAction newsfeedAction) {
    Intent intent = new Intent(context, DroidBoyActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtras(newsfeedAction.getExtras());
    intent.putExtra(context.getResources().getString(R.string.source_key), Constants.APPBOY);
    intent.putExtra(context.getResources().getString(R.string.destination_view), context.getResources().getString(R.string.feed_key));
    context.startActivity(intent);
  }

  @Override
  public void gotoUri(Context context, UriAction uriAction) {
    String uri = uriAction.getUri().toString();
    if (!StringUtils.isNullOrBlank(uri) && uri.matches(context.getString(R.string.youtube_regex))) {
      uriAction.setUseWebView(false);
    }

    CustomUriAction customUriAction = new CustomUriAction(uriAction);
    customUriAction.execute(context);
  }

  public static class CustomUriAction extends UriAction {

    public CustomUriAction(@NonNull UriAction uriAction) {
      super(uriAction);
    }

    @Override
    protected void openUriWithActionView(Context context, Uri uri, Bundle extras) {
      Intent intent = getActionViewIntent(context, uri, extras);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      if (intent.resolveActivity(context.getPackageManager()) != null) {
        context.startActivity(intent);
      } else {
        AppboyLogger.w(TAG, "Could not find appropriate activity to open for deep link " + uri + ".");
      }
    }
  }
}

