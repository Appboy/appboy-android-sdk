package com.appboy.ui.inappmessage.listeners;

import android.view.View;

import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.MessageButton;
import com.appboy.ui.inappmessage.InAppMessageCloser;

/**
 * IInAppMessageViewLifecycleListener returns the in-app message at specific events
 * in its display lifecycle for potential further processing, modification, and logging.
 */
public interface IInAppMessageViewLifecycleListener {

  /**
   * Called before the in-app message View is added to the root layout.
   * @param inAppMessageView
   * @param inAppMessage
   */
  void beforeOpened(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called after the in-app message View has been added to the root layout
   * (and the appearing animation has completed).
   * @param inAppMessageView
   * @param inAppMessage
   */
  void afterOpened(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called before the in-app message View is removed (and before any closing
   * animation starts).
   * @param inAppMessageView
   * @param inAppMessage
   */
  void beforeClosed(View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called after the in-app message View has been removed from the root
   * layout (and the disappearing animation has completed).
   * @param inAppMessage
   */
  void afterClosed(IInAppMessage inAppMessage);

  /**
   * Called when the in-app message View is clicked.
   * @param inAppMessageCloser
   * @param inAppMessageView
   * @param inAppMessage
   */
  void onClicked(InAppMessageCloser inAppMessageCloser, View inAppMessageView, IInAppMessage inAppMessage);

  /**
   * Called when an in-app message Button is clicked.
   * @param inAppMessageCloser
   * @param messageButton
   * @param inAppMessageImmersive
   */
  void onButtonClicked(InAppMessageCloser inAppMessageCloser, MessageButton messageButton, IInAppMessageImmersive inAppMessageImmersive);

  /**
   * Called when the in-app message View is dismissed.
   * @param inAppMessageView
   * @param inAppMessage
   */
  void onDismissed(View inAppMessageView, IInAppMessage inAppMessage);
}
