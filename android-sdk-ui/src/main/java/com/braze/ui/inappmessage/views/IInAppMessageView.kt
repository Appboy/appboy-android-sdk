package com.braze.ui.inappmessage.views

import android.view.View
import androidx.core.view.WindowInsetsCompat

/**
 * [IInAppMessageView] is the base view interface for all in-app messages.
 */
interface IInAppMessageView {
    /**
     * Gets the clickable portion of the in-app message so that Braze can add click listeners to it.
     *
     * @return the View that displays the clickable portion of the in-app message.
     * If the entire message is clickable, return this.
     */
    val messageClickableView: View?

    /**
     * Variable to prevent [WindowInsetsCompat] from getting applied
     * multiple times on the same in-app message view.
     *
     * @see .applyWindowInsets
     * @return Whether [WindowInsetsCompat] has been applied to this in-app message.
     */
    var hasAppliedWindowInsets: Boolean

    /**
     * Called when the [WindowInsetsCompat] information should be applied to this
     * in-app message. [WindowInsetsCompat] will typically only be applied on notched
     * devices and on Activities displaying inside the screen cutout area.
     * Implementations of this method are expected to modify any necessary margins to ensure
     * compatibility with the argument notch dimensions. For example, full screen in-app messages
     * should have their close buttons moved to not be obscured by the status bar, slideups should not
     * render behind the notch, and modal in-app messages will have no changes since they do not render
     * in the cutout area.
     * The screen has a notch if [WindowInsetsCompat.getDisplayCutout] is non-null.
     * The system window insets (e.g. [WindowInsetsCompat.getSystemWindowInsetTop]
     * will be present if the status bar is translucent or the status/navigation bars are otherwise
     * non-occluding of the root Activity content.
     *
     * @param insets The [WindowInsetsCompat] object directly from
     * [androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener].
     */
    fun applyWindowInsets(insets: WindowInsetsCompat)
}
