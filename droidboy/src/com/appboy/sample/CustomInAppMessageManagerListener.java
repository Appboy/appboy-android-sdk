package com.appboy.sample;

import android.app.Activity;
import android.widget.Toast;

import com.appboy.models.IInAppMessage;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.IInAppMessageManagerListener;
import com.appboy.ui.inappmessage.InAppMessageCloser;
import com.appboy.ui.inappmessage.InAppMessageOperation;

public class CustomInAppMessageManagerListener implements IInAppMessageManagerListener {
  private final Activity mActivity;

  public CustomInAppMessageManagerListener(Activity activity) {
    mActivity = activity;
  }

  @Override
  public boolean onInAppMessageReceived(IInAppMessage inAppMessage) {
    return false;
  }

  @Override
  public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
    return InAppMessageOperation.DISPLAY_NOW;
  }

  @Override
  public boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    Toast.makeText(mActivity, "The click was ignored.", Toast.LENGTH_LONG).show();
    inAppMessageCloser.close(true);
    return true;
  }

  @Override
  public boolean onInAppMessageButtonClicked(MessageButton button, InAppMessageCloser inAppMessageCloser) {
    Toast.makeText(mActivity, "The button click was ignored.", Toast.LENGTH_LONG).show();
    inAppMessageCloser.close(true);
    return true;
  }

  @Override
  public void onInAppMessageDismissed(IInAppMessage inAppMessage) {
    Toast.makeText(mActivity, "The in-app message was dismissed.", Toast.LENGTH_LONG).show();
  }
}
