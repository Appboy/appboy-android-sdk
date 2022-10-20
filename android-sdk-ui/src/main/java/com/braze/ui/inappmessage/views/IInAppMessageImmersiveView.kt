package com.braze.ui.inappmessage.views

import android.view.View

/**
 * [IInAppMessageImmersiveView] is the base view interface for all immersive in-app messages.
 *
 *
 * An immersive in-app message is defined as an in-app message that takes up the entire screen
 * and/or 'blocks' the user from interacting with the app until the message is dismissed.
 * Immersive views extend the base in-app message view with header text, message buttons,
 * and a close button.
 */
interface IInAppMessageImmersiveView : IInAppMessageView {
    /**
     * Gets the close button View so that Braze can add click listeners to it.
     *
     * @return the child View that displays the close button.
     */
    val messageCloseButtonView: View?

    /**
     * Gets the message button Views so that Braze can add click listeners to them.
     *
     * @param numButtons The number of visible buttons
     * @return the child Views that display the message buttons. They should
     * be returned in the same order as the List<MessageButton> on the in-app message
     * object so that listeners are set correctly.
     */
    fun getMessageButtonViews(numButtons: Int): List<View>

    /**
     * Sets up the directional navigation pointers needed to support d-pad/TV-remote
     * navigation of the in-app message.
     *
     * See https://developer.android.com/training/keyboard-input/navigation#Direction
     *
     * @param numButtons The number of [MessageButton]'s
     * on this message.
     */
    fun setupDirectionalNavigation(numButtons: Int)
}
