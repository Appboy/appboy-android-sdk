package com.appboy.sample;

import android.app.Activity;
import android.widget.Toast;

import com.appboy.models.IInAppMessage;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.listeners.IInAppMessageManagerListener;
import com.appboy.ui.inappmessage.InAppMessageCloser;
import com.appboy.ui.inappmessage.InAppMessageOperation;

public class CustomInAppMessageManagerListener implements IInAppMessageManagerListener {
  private final Activity mActivity;

  public CustomInAppMessageManagerListener(Activity activity) {
    mActivity = activity;
  }

  /**
   * see {@link IInAppMessageManagerListener#onInAppMessageReceived(IInAppMessage)}
   */
  @Override
  public boolean onInAppMessageReceived(IInAppMessage inAppMessage) {
    return false;
  }

  /**
   * see {@link IInAppMessageManagerListener#beforeInAppMessageDisplayed(IInAppMessage)}
   */
  @Override
  public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
    return InAppMessageOperation.DISPLAY_NOW;
  }

  /**
   * see {@link IInAppMessageManagerListener#onInAppMessageClicked(IInAppMessage, InAppMessageCloser)}
   */
  @Override
  public boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    Toast.makeText(mActivity, "The click was ignored.", Toast.LENGTH_LONG).show();
    
    // Closing should not be animated if transitioning to a new activity.
    // If remaining in the same activity, closing should be animated.
    inAppMessageCloser.close(true);
    return true;
  }

  /**
   * see {@link IInAppMessageManagerListener#onInAppMessageButtonClicked(MessageButton, InAppMessageCloser)}
   */
  @Override
  public boolean onInAppMessageButtonClicked(MessageButton button, InAppMessageCloser inAppMessageCloser) {
    Toast.makeText(mActivity, "The button click was ignored.", Toast.LENGTH_LONG).show();

    // Closing should not be animated if transitioning to a new activity.
    // If remaining in the same activity, closing should be animated.
    inAppMessageCloser.close(true);
    return true;
  }

  /**
   * see {@link IInAppMessageManagerListener#onInAppMessageDismissed(IInAppMessage)}
   */
  @Override
  public void onInAppMessageDismissed(IInAppMessage inAppMessage) {
    Toast.makeText(mActivity, "The in-app message was dismissed.", Toast.LENGTH_LONG).show();
  }
}
