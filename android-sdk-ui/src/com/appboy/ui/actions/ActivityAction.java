package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.appboy.Constants;

/**
 * Action that launches a new Activity.
 */
public final class ActivityAction implements IAction {
  private final Intent mIntent;
  private final Bundle mOptions;

  public ActivityAction(Intent intent) {
    this(intent, null);
  }

  public ActivityAction(Intent intent, Bundle options) {
    mIntent = intent;
    mOptions = options;
  }

  @Override
  public void execute(Context context) {
    context.startActivity(mIntent, mOptions);
  }
}
