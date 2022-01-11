package com.braze.ui.inappmessage;

import com.braze.models.inappmessage.IInAppMessage;

/**
 * @deprecated Please use {@link BrazeInAppMessageManager#hideCurrentlyDisplayingInAppMessage}
 *             and {@link IInAppMessage#setAnimateOut}
 * A delegate method class used to close the currently displayed in-app message.
 */
@Deprecated
public class InAppMessageCloser {
  private final IInAppMessageViewWrapper mInAppMessageViewWrapper;

  public InAppMessageCloser(IInAppMessageViewWrapper inAppMessageViewWrapper) {
    mInAppMessageViewWrapper = inAppMessageViewWrapper;
  }

  public void close(boolean animate) {
    mInAppMessageViewWrapper.getInAppMessage().setAnimateOut(animate);
    mInAppMessageViewWrapper.close();
  }
}
