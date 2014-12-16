package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Fires an android.intent.action.VIEW action with the given URI.
 */
public class ViewAction implements IAction {
  private final Intent mIntent;

  public ViewAction(Uri uri) {
    this(uri, null);
  }

  public ViewAction(Uri uri, Bundle extras) {
    mIntent = new Intent(Intent.ACTION_VIEW);
    mIntent.setData(uri);

    if (extras != null) {
      mIntent.putExtras(extras);
    }
  }

  @Override
  public void execute(Context context) {
    if (mIntent.resolveActivity(context.getPackageManager()) != null) {
      context.startActivity(mIntent);
    }
  }
}
