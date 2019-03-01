package com.appboy.ui.inappmessage.listeners;

import com.appboy.models.IInAppMessage;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.InAppMessageCloser;
import com.appboy.ui.inappmessage.InAppMessageOperation;

public class AppboyDefaultInAppMessageManagerListener implements IInAppMessageManagerListener {
  @Override
  @Deprecated
  public boolean onInAppMessageReceived(IInAppMessage inAppMessage) {
    return false;
  }

  @Override
  public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
    return InAppMessageOperation.DISPLAY_NOW;
  }

  @Override
  public boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    return false;
  }

  @Override
  public boolean onInAppMessageButtonClicked(MessageButton button, InAppMessageCloser inAppMessageCloser) {
    return false;
  }

  @Override
  public void onInAppMessageDismissed(IInAppMessage inAppMessage) {
  }
}