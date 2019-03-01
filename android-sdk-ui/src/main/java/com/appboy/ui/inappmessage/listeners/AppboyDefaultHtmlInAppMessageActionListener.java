package com.appboy.ui.inappmessage.listeners;

import android.os.Bundle;

import com.appboy.models.IInAppMessage;

public class AppboyDefaultHtmlInAppMessageActionListener implements IHtmlInAppMessageActionListener {
  @Override
  public void onCloseClicked(IInAppMessage inAppMessage, String url, Bundle queryBundle) {
  }

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