package com.appboy.ui.inappmessage;

import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.WindowInsetsCompat;
import android.view.View;

/**
 * InAppMessageBase is the base view interface for all in-app messages.
 *
 * It implicitly defines policy for in-app message views that can be
 * manipulated from outside the object.
 */
public interface IInAppMessageView {

  /**
   * Gets the clickable portion of the in-app message so that Braze can add click listeners to it.
   *
   * @return the View that displays the clickable portion of the in-app message.
   * If the entire message is clickable, return this.
   */
  View getMessageClickableView();

  /**
   * Called when the {@link WindowInsetsCompat} information should be applied to this
   * in-app message. {@link WindowInsetsCompat} will typically only be applied on notched
   * devices and on Activities displaying inside the screen cutout area.
   * <br>
   * <br>
   * Implementations of this method are expected to modify any necessary margins to ensure
   * compatibility with the argument notch dimensions. For example, full screen in-app messages
   * should have their close buttons moved to not be obscured by the status bar, slideups should not
   * render behind the notch, and modal in-app messages will have no changes since they do not render
   * in the cutout area.
   *
   * @param insets The {@link WindowInsetsCompat} object directly from {@link android.support.v4.view.ViewCompat#setOnApplyWindowInsetsListener(View, OnApplyWindowInsetsListener)}.
   */
  void applyWindowInsets(WindowInsetsCompat insets);
}
