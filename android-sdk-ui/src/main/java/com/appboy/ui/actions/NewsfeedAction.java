package com.appboy.ui.actions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.appboy.enums.Channel;
import com.appboy.ui.activities.AppboyFeedActivity;
import com.braze.support.BrazeLogger;

import static android.content.ContentValues.TAG;

public class NewsfeedAction implements IAction {
  private final Bundle mExtras;
  private final Channel mChannel;

  public NewsfeedAction(Bundle extras, Channel channel) {
    mExtras = extras;
    mChannel = channel;
  }

  @Override
  public Channel getChannel() {
    return mChannel;
  }

  @Override
  public void execute(Context context) {
    try {
      Intent intent = new Intent(context, AppboyFeedActivity.class);
      if (mExtras != null) {
        intent.putExtras(mExtras);
      }
      context.startActivity(intent);
    } catch (Exception e) {
      BrazeLogger.e(TAG, "AppboyFeedActivity was not opened successfully.", e);
    }
  }

  public Bundle getExtras() {
    return mExtras;
  }
}
