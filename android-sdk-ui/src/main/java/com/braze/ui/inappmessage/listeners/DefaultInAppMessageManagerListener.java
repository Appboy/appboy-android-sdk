package com.braze.ui.inappmessage.listeners;

import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.IInAppMessageThemeable;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
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

  // The rest of the functions take the defaults in IInAppMessageManagerListener
}
