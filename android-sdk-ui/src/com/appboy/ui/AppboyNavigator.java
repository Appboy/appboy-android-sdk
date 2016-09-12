package com.appboy.ui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.appboy.Constants;
import com.appboy.IAppboyNavigator;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.ActivityAction;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.activities.AppboyFeedActivity;

public class AppboyNavigator implements IAppboyNavigator {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyNavigator.class.getName());

  @SuppressLint("WrongConstant")
  @Override
  public void gotoNewsFeed(Context context, Bundle extras) {
    // Checks to see if the AppboyFeedActivity is registered in the manifest. If it is, we can
    // open up the Appboy news feed in a new Activity. Otherwise, we just ignore the request.
    ComponentName componentName = new ComponentName(context, AppboyFeedActivity.class);
    try {
      context.getPackageManager().getActivityInfo(componentName, PackageManager.GET_ACTIVITIES);
      Intent intent = new Intent(context, AppboyFeedActivity.class);
      ActivityAction activityAction = new ActivityAction(intent);
      activityAction.execute(context);
    } catch (PackageManager.NameNotFoundException e) {
      AppboyLogger.d(TAG, "The AppboyFeedActivity is not registered in the manifest. Ignoring request "
          + "to display the news feed.");
    }
  }

  @Override
  public void gotoURI(Context context, Uri uri, Bundle extras) {
    if (uri == null) {
      AppboyLogger.e(TAG, "IAppboyNavigator cannot open URI because the URI is null.");
      return;
    }
    IAction action = ActionFactory.createUriAction(context, uri.toString(), extras);
    action.execute(context);
  }
}
