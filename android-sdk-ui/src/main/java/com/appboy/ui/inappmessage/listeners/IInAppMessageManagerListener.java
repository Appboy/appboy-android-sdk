package com.appboy.ui.inappmessage.listeners;

import android.view.View;

import com.appboy.models.IInAppMessage;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.InAppMessageCloser;
import com.appboy.ui.inappmessage.InAppMessageOperation;

/**
 * The IInAppMessageManagerListener returns the in-app message at specific
 * events in its control flow and gives the host app the option of
 * overriding Braze's default display handling and implementing its own custom behavior.
 *
 * If you are implementing Unity, you must use IAppboyUnityInAppMessageListener instead.
 *
 * See {@link com.appboy.ui.inappmessage.AppboyInAppMessageManager}
 */
public interface IInAppMessageManagerListener {

  /**
   * @param inAppMessage the received in-app message.
   *
   * @deprecated with triggered in-app messages (introduced in Braze Android sdk 1.13.0), in-app
   * messages are pre-fetched. Only 'legacy' in-app messages will call this method.
   *
   * @return boolean flag to indicate to Braze whether the display of this message
   * has been manually handled. If true, Braze will do nothing with the in-app message.
   * If false, Braze will add the message to its internal stack of in-app messages and request
   * display.
   */
  @Deprecated
  boolean onInAppMessageReceived(IInAppMessage inAppMessage);

  /**
   * @param inAppMessage The in-app message that is currently requested for display.
   *
   * @return InAppMessageOperation indicating how to handle the candidate in-app message.
   */
  InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage);

  /**
   * @param inAppMessage The clicked in-app message.
   * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
   * If remaining in the same activity, closing should be animated.
   *
   * @return boolean flag to indicate to Braze whether the click has been manually handled.
   * If true, Braze will log a click and do nothing. If false, Braze will also close the in-app message.
   */
  boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser);

  /**
   * @param inAppMessage The clicked in-app message.
   * @param button The clicked message button.
   * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
   * If remaining in the same activity, closing should be animated.
   *
   * @return boolean flag to indicate to Braze whether the click has been manually handled.
   * If true, Braze will log a button click and do nothing. If false, Braze will also close the in-app message.
   */
  boolean onInAppMessageButtonClicked(IInAppMessage inAppMessage, MessageButton button, InAppMessageCloser inAppMessageCloser);

  /**
   * @param inAppMessage the in-app message that was closed.
   */
  void onInAppMessageDismissed(IInAppMessage inAppMessage);

  /**
   * Called before the in-app message View is added to the layout.
   *
   * Note that this is called before any default processing in
   * {@link AppboyInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessageView The {@link View} representing the {@link IInAppMessage}.
   * @param inAppMessage The {@link IInAppMessage} being displayed.
   */
  void beforeInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called after the in-app message View has been added to the layout
   * (and the appearing animation has completed).
   *
   * Note that this is called after any default processing in
   * {@link AppboyInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessageView The {@link View} representing the {@link IInAppMessage}.
   * @param inAppMessage The {@link IInAppMessage} being displayed.
   */
  void afterInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called before the in-app message View is removed from the layout
   * (and before any closing animation starts).
   *
   * Note that this is called before any default processing in
   * {@link AppboyInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessageView The {@link View} representing the {@link IInAppMessage}.
   * @param inAppMessage The {@link IInAppMessage} being displayed.
   */
  void beforeInAppMessageViewClosed(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called after the in-app message View has been removed from the
   * layout (and the disappearing animation has completed).
   *
   * Note that this is called after any default processing in
   * {@link AppboyInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessage The {@link IInAppMessage} being displayed.
   */
  void afterInAppMessageViewClosed(IInAppMessage inAppMessage);
}
