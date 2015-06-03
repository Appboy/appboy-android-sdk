package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.appboy.ui.support.UriUtils;

import java.util.Map;

/**
 * Action that launches a new Activity.
 */
public final class ActivityAction implements IAction {
  private final Intent mIntent;

  /**
   * Constructs an ActivityAction given a package name and Uri. The Uri query parameters are parsed
   * and added to the Intent as extras.
   */
  public ActivityAction(String packageName, Uri uri) {
    this(packageName, uri, null);
  }

  /**
   * Constructs an ActivityAction given a package name and Uri. The Uri query parameters are parsed
   * and added to the Intent as extras. The bundle is used as the initial extras. For any collisions
   * in the keys, the uri will take priority.
   */
  public ActivityAction(String packageName, Uri uri, Bundle extras) {
    this(new Intent());
    mIntent.setClassName(packageName, uri.getHost());
    if (extras != null) {
      mIntent.putExtras(extras);
    }
    Map<String, String> parameters = UriUtils.getQueryParameters(uri);
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      mIntent.putExtra(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Constructs an ActivityAction given a fully initialized Intent. The intent will be passed as a
   * parameter to the {@link android.content.Context#startActivity(android.content.Intent)} method.
   */
  public ActivityAction(Intent intent) {
    mIntent = intent;
  }

  @Override
  public void execute(Context context) {
    if (mIntent.resolveActivity(context.getPackageManager()) != null) {
      context.startActivity(mIntent);
    }
  }
}
