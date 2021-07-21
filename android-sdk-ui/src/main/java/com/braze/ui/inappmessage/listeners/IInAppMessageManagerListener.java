package com.braze.ui.inappmessage.listeners;

import android.view.View;

import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.MessageButton;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.braze.ui.inappmessage.InAppMessageCloser;
import com.braze.ui.inappmessage.InAppMessageOperation;

/**
 * The IInAppMessageManagerListener returns the in-app message at specific
 * events in its control flow and gives the host app the option of
 * overriding Braze's default display handling and implementing its own custom behavior.
 * <p>
 * If you are implementing Unity, you must use IAppboyUnityInAppMessageListener instead.
 * <p>
 * See {@link BrazeInAppMessageManager}
 */
public interface IInAppMessageManagerListener {

  /**
   * @param inAppMessage The in-app message that is currently requested for display.
   * @return InAppMessageOperation indicating how to handle the candidate in-app message.
   */
  InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage);

  /**
   * @param inAppMessage       The clicked in-app message.
   * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
   *                           If remaining in the same activity, closing should be animated.
   * @return boolean flag to indicate to Braze whether the click has been manually handled.
   * If true, Braze will only log a click and do nothing else. If false, Braze will
   * log a click and also close the in-app message automatically.
   */
  boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser);

  /**
   * @param inAppMessage       The clicked in-app message.
   * @param button             The clicked message button.
   * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
   *                           If remaining in the same activity, closing should be animated.
   * @return boolean flag to indicate to Braze whether the click has been manually handled.
   * If true, Braze will only log a click and do nothing else. If false, Braze will
   * log a click and also close the in-app message automatically.
   */
  boolean onInAppMessageButtonClicked(IInAppMessage inAppMessage, MessageButton button, InAppMessageCloser inAppMessageCloser);

  /**
   * @param inAppMessage the in-app message that was closed.
   */
  void onInAppMessageDismissed(IInAppMessage inAppMessage);

  /**
   * Called before the in-app message View is added to the layout.
   * <p>
   * Note that this is called before any default processing in
   * {@link DefaultInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessageView The {@link View} representing the {@link IInAppMessage}.
   * @param inAppMessage     The {@link IInAppMessage} being displayed.
   */
  void beforeInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called after the in-app message View has been added to the layout
   * (and the appearing animation has completed).
   * <p>
   * Note that this is called after any default processing in
   * {@link DefaultInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessageView The {@link View} representing the {@link IInAppMessage}.
   * @param inAppMessage     The {@link IInAppMessage} being displayed.
   */
  void afterInAppMessageViewOpened(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called before the in-app message View is removed from the layout
   * (and before any closing animation starts).
   * <p>
   * Note that this is called before any default processing in
   * {@link DefaultInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessageView The {@link View} representing the {@link IInAppMessage}.
   * @param inAppMessage     The {@link IInAppMessage} being displayed.
   */
  void beforeInAppMessageViewClosed(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called after the in-app message View has been removed from the
   * layout (and the disappearing animation has completed).
   * <p>
   * Note that this is called after any default processing in
   * {@link DefaultInAppMessageViewLifecycleListener} takes place.
   *
   * @param inAppMessage The {@link IInAppMessage} being displayed.
   */
  void afterInAppMessageViewClosed(IInAppMessage inAppMessage);
}
