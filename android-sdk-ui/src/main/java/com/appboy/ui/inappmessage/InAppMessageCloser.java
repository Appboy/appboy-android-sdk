package com.appboy.ui.inappmessage;

/**
 * A delegate method class used to close the currently displayed in-app message.
 */
public class InAppMessageCloser {
  private final IInAppMessageViewWrapper mInAppMessageViewWrapper;

  public InAppMessageCloser(IInAppMessageViewWrapper inAppMessageViewWrapper) {
    mInAppMessageViewWrapper = inAppMessageViewWrapper;
  }

  public void close(boolean animate) {
    if (animate) {
      mInAppMessageViewWrapper.getInAppMessage().setAnimateOut(true);
    } else {
      mInAppMessageViewWrapper.getInAppMessage().setAnimateOut(false);
    }
    mInAppMessageViewWrapper.close();
  }
}
