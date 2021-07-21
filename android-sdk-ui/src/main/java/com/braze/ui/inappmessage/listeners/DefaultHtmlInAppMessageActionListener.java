package com.braze.ui.inappmessage.listeners;

import android.os.Bundle;

import com.braze.models.inappmessage.IInAppMessage;

public class DefaultHtmlInAppMessageActionListener implements IHtmlInAppMessageActionListener {
  @Override
  public void onCloseClicked(IInAppMessage inAppMessage, String url, Bundle queryBundle) {}

  @Override
  public boolean onNewsfeedClicked(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    return false;
  }

  @Override
  public boolean onCustomEventFired(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    return false;
  }

  @Override
  public boolean onOtherUrlAction(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
    return false;
  }
}
