package com.braze.ui.inappmessage.views;

import android.view.View;

import com.braze.models.inappmessage.MessageButton;

import java.util.List;

/**
 * {@link IInAppMessageImmersiveView} is the base view interface for all immersive in-app messages.
 * <p>
 * An immersive in-app message is defined as an in-app message that takes up the entire screen
 * and/or 'blocks' the user from interacting with the app until the message is dismissed.
 * Immersive views extend the base in-app message view with header text, message buttons,
 * and a close button.
 */
public interface IInAppMessageImmersiveView extends IInAppMessageView {

  /**
   * Gets the close button View so that Braze can add click listeners to it.
   *
   * @return the child View that displays the close button.
   */
  View getMessageCloseButtonView();

  /**
   * Gets the message button Views so that Braze can add click listeners to them.
   *
   * @param numButtons The number of visible buttons
   * @return the child Views that display the message buttons. They should
   * be returned in the same order as the List<MessageButton> on the in-app message
   * object so that listeners are set correctly.
   */
  List<View> getMessageButtonViews(int numButtons);

  /**
   * Sets up the directional navigation pointers needed to support d-pad/TV-remote
   * navigation of the in-app message.
   *
   * See https://developer.android.com/training/keyboard-input/navigation#Direction
   *
   * @param numButtons The number of {@link MessageButton}'s
   *                   on this message.
   */
  void setupDirectionalNavigation(int numButtons);
}
