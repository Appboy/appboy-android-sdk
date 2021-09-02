package com.appboy.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.Constants;
import com.appboy.enums.Channel;
import com.appboy.sample.activity.DroidBoyActivity;
import com.braze.IBrazeDeeplinkHandler;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;
import com.braze.ui.BrazeDeeplinkHandler;
import com.braze.ui.actions.NewsfeedAction;
import com.braze.ui.actions.UriAction;

public class CustomBrazeDeeplinkHandler implements IBrazeDeeplinkHandler {
  private static final String TAG = BrazeLogger.getBrazeLogTag(CustomBrazeDeeplinkHandler.class);

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

  @Override
  public int getIntentFlags(IntentFlagPurpose intentFlagPurpose) {
    return new BrazeDeeplinkHandler().getInstance().getIntentFlags(intentFlagPurpose);
  }

  @Nullable
  @Override
  public UriAction createUriActionFromUrlString(String url, Bundle extras, boolean openInWebView, Channel channel) {
    return BrazeDeeplinkHandler.getInstance().createUriActionFromUrlString(url, extras, openInWebView, channel);
  }

  @Nullable
  @Override
  public UriAction createUriActionFromUri(Uri uri, Bundle extras, boolean openInWebView, Channel channel) {
    return BrazeDeeplinkHandler.getInstance().createUriActionFromUri(uri, extras, openInWebView, channel);
  }

  public static class CustomUriAction extends UriAction {

    public CustomUriAction(@NonNull UriAction uriAction) {
      super(uriAction);
    }

    @Override
    protected void openUriWithActionView(Context context, Uri uri, Bundle extras) {
      Intent intent = getActionViewIntent(context, uri, extras);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      try {
        context.startActivity(intent);
      } catch (Exception e) {
        BrazeLogger.e(TAG, "Failed to handle uri " + uri + " with extras: " + extras, e);
      }
    }
  }
}

