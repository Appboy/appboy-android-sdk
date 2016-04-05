package com.appboy.ui.inappmessage.listeners;

import com.appboy.models.IInAppMessage;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.InAppMessageCloser;
import com.appboy.ui.inappmessage.InAppMessageOperation;

/**
 * The IInAppMessageManagerListener returns the in-app message at specific
 * events in its control flow and gives the host app the option of
 * overriding Appboy's default display handling and implementing its own custom behavior.
 *
 * If you are implementing Unity, you must use IAppboyUnityInAppMessageListener instead.
 *
 * See {@link com.appboy.ui.inappmessage.AppboyInAppMessageManager}
 */
public interface IInAppMessageManagerListener {

  /**
   * @param inAppMessage the received in-app message.
   *
   * @deprecated with triggered in-app messages (introduced in Appboy Android sdk 1.13.0), in-app
   * messages are pre-fetched. Only 'legacy' in-app messages will call this method.
   *
   * @return boolean flag to indicate to Appboy whether the display of this message
   * has been manually handled. If true, Appboy will do nothing with the in-app message.
   * If false, Appboy will add the message to its internal stack of in-app messages and request
   * display.
   */
  @Deprecated
  boolean onInAppMessageReceived(IInAppMessage inAppMessage);

  /**
   * @param inAppMessage the in-app message that is currently requested for display.
   *
   * @return InAppMessageOperation indicating how to handle the candidate in-app message.
   */
  InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage);

  /**
   * @param inAppMessage the clicked in-app message.
   * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
   * If remaining in the same activity, closing should be animated.
   *
   * @return boolean flag to indicate to Appboy whether the click has been manually handled.
   * If true, Appboy will log a click and do nothing. If false, Appboy will also close the in-app message.
   */
  boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser);

  /**
   * @param button the clicked message button.
   * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
   * If remaining in the same activity, closing should be animated.
   *
   * @return boolean flag to indicate to Appboy whether the click has been manually handled.
   * If true, Appboy will log a button click and do nothing. If false, Appboy will also close the in-app message.
   */
  boolean onInAppMessageButtonClicked(MessageButton button, InAppMessageCloser inAppMessageCloser);

  /**
   * @param inAppMessage the in-app message that was closed.
   */
  void onInAppMessageDismissed(IInAppMessage inAppMessage);
}
