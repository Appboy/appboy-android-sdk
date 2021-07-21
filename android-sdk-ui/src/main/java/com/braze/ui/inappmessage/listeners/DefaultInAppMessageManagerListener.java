package com.braze.ui.inappmessage.listeners;

import android.view.View;

import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.IInAppMessageThemeable;
import com.braze.models.inappmessage.MessageButton;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.braze.ui.inappmessage.InAppMessageCloser;
import com.braze.ui.inappmessage.InAppMessageOperation;
import com.braze.ui.support.ViewUtils;

public class DefaultInAppMessageManagerListener implements IInAppMessageManagerListener {

  @Override
  public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
    if (inAppMessage instanceof IInAppMessageThemeable && ViewUtils.isDeviceInNightMode(BrazeInAppMessageManager.getInstance().getApplicationContext())) {
      ((IInAppMessageThemeable) inAppMessage).enableDarkTheme();
    }
    return InAppMessageOperation.DISPLAY_NOW;
  }

  @Override
  public boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    return false;
  }

  @Override
  public boolean onInAppMessageButtonClicked(IInAppMessage inAppMessage, MessageButton button, InAppMessageCloser inAppMessageCloser) {
    return false;
  }

  @Override
  public void onInAppMessageDismissed(IInAppMessage inAppMessage) {}

  @Override
  public void beforeInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage) {}

  @Override
  public void afterInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage) {}

  @Override
  public void beforeInAppMessageViewClosed(View inAppMessageView, IInAppMessage inAppMessage) {}

  @Override
  public void afterInAppMessageViewClosed(IInAppMessage inAppMessage) {}
}
