package com.appboy.ui.inappmessage;

import android.view.View;

import java.util.List;

/**
 * IInAppMessageImmersiveView is the base view interface for all immersive in-app messages.
 *
 * An immersive in-app message is defined as an in-app message that takes up the entire screen
 * and/or 'blocks' the user from interacting with the app until the message is dismissed.
 * Immersive views extend the base in-app message view with header text, message buttons,
 * and a close button.
 *
 * Known implementing classes include
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageImmersiveBaseView}
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageModalView}
 * {@link com.appboy.ui.inappmessage.AppboyInAppMessageFullView}
 */
public interface IInAppMessageImmersiveView extends IInAppMessageView {

  /**
   * Gets the close button View so that Appboy can add click listeners to it.
   * @return the child View that displays the close button.
   */
  View getMessageCloseButtonView();

  /**
   * Gets the message button Views so that Appboy can add click listeners to them.
   * @return the child Views that display the message buttons. They should
   * be returned in the same order as the List<MessageButton> on the in-app message
   * object so that listeners are set correctly.
   */
  List<View> getMessageButtonViews();
}
