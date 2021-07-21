package com.appboy.sample;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.MessageButton;
import com.braze.ui.inappmessage.InAppMessageCloser;
import com.braze.ui.inappmessage.InAppMessageOperation;
import com.braze.ui.inappmessage.listeners.IInAppMessageManagerListener;

import java.util.Map;

public class CustomInAppMessageManagerListener implements IInAppMessageManagerListener {
  private final Activity mActivity;

  public CustomInAppMessageManagerListener(Activity activity) {
    mActivity = activity;
  }

  @Override
  public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
    return InAppMessageOperation.DISPLAY_NOW;
  }

  @Override
  public boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    Toast.makeText(mActivity, "The click was ignored.", Toast.LENGTH_LONG).show();

    // Closing should not be animated if transitioning to a new activity.
    // If remaining in the same activity, closing should be animated.
    inAppMessageCloser.close(true);
    return true;
  }

  @Override
  public boolean onInAppMessageButtonClicked(IInAppMessage inAppMessage, MessageButton button, InAppMessageCloser inAppMessageCloser) {
    Toast.makeText(mActivity, "The button click was ignored.", Toast.LENGTH_LONG).show();

    // Closing should not be animated if transitioning to a new activity.
    // If remaining in the same activity, closing should be animated.
    inAppMessageCloser.close(true);
    return true;
  }

  @Override
  public void onInAppMessageDismissed(IInAppMessage inAppMessage) {
    if (inAppMessage.getExtras() != null && !inAppMessage.getExtras().isEmpty()) {
      Map<String, String> extras = inAppMessage.getExtras();
      StringBuilder keyValuePairs = new StringBuilder("Dismissed in-app message with extras payload containing [");
      for (String key : extras.keySet()) {
        keyValuePairs.append(" '").append(key).append(" = ").append(extras.get(key)).append('\'');
      }
      keyValuePairs.append(']');
      Toast.makeText(mActivity, keyValuePairs.toString(), Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(mActivity, "The in-app message was dismissed.", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void beforeInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage) { }

  @Override
  public void afterInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage) { }

  @Override
  public void beforeInAppMessageViewClosed(View inAppMessageView, IInAppMessage inAppMessage) { }

  @Override
  public void afterInAppMessageViewClosed(IInAppMessage inAppMessage) { }
}
