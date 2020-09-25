package com.appboy.ui.inappmessage;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.WindowInsetsCompat;

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
   * <br>
   * <br>
   * The screen has a notch if {@link WindowInsetsCompat#getDisplayCutout()} is non-null.
   * The system window insets (e.g. {@link WindowInsetsCompat#getSystemWindowInsetTop()}
   * will be present if the status bar is translucent or the status/navigation bars are otherwise
   * non-occluding of the root Activity content.
   *
   * @param insets The {@link WindowInsetsCompat} object directly from
   * {@link androidx.core.view.ViewCompat#setOnApplyWindowInsetsListener(View, OnApplyWindowInsetsListener)}.
   */
  void applyWindowInsets(@NonNull WindowInsetsCompat insets);

  /**
   * Helper method to prevent {@link WindowInsetsCompat} from getting applied
   * multiple times on the same in-app message view.
   *
   * @see #applyWindowInsets(WindowInsetsCompat)
   * @return Whether {@link WindowInsetsCompat} has been applied to this in-app message.
   */
  boolean hasAppliedWindowInsets();
}
