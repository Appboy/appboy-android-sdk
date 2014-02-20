package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.appboy.Constants;

/**
 * Action that launches a new Activity.
 */
public final class ActivityAction implements IAction {
  private final Intent mIntent;

  public ActivityAction(Intent intent) {
  }

  public ActivityAction(Context ctx, Uri uri) {
    mIntent.setClassName(ctx, uri.getHost());
    for(String key : uri.getQueryParameterNames()) {
      mIntent.putExtra(key, uri.getQueryParameter(key));
    }
    mIntent = intent;
  }

  @Override
  public void execute(Context context) {
    context.startActivity(mIntent);
  }
}
