@file:Suppress("DEPRECATION")

package com.braze.ui.inappmessage.listeners

import android.view.View
import com.braze.models.inappmessage.IInAppMessage
import com.braze.ui.inappmessage.InAppMessageCloser
import com.braze.models.inappmessage.MessageButton
import com.braze.models.inappmessage.IInAppMessageImmersive

/**
 * [IInAppMessageViewLifecycleListener] returns the in-app message view at specific events
 * in its display lifecycle for potential further processing and modification.
 *
 * For use cases unrelated to view customization, such as suppressing display or performing
 * custom click handling, see [IInAppMessageManagerListener]
 */
interface IInAppMessageViewLifecycleListener {
    /**
     * Called before the in-app message View is added to the root layout.
     * @param inAppMessageView
     * @param inAppMessage
     */
    fun beforeOpened(inAppMessageView: View, inAppMessage: IInAppMessage)

    /**
     * Called after the in-app message View has been added to the root layout
     * (and the appearing animation has completed).
     * @param inAppMessageView
     * @param inAppMessage
     */
    fun afterOpened(inAppMessageView: View, inAppMessage: IInAppMessage)

    /**
     * Called before the in-app message View is removed (and before any closing
     * animation starts).
     * @param inAppMessageView
     * @param inAppMessage
     */
    fun beforeClosed(inAppMessageView: View, inAppMessage: IInAppMessage)

    /**
     * Called after the in-app message View has been removed from the root
     * layout (and the disappearing animation has completed).
     * @param inAppMessage
     */
    fun afterClosed(inAppMessage: IInAppMessage)

    /**
     * Called when the in-app message View is clicked.
     * @param inAppMessageCloser
     * @param inAppMessageView
     * @param inAppMessage
     */
    fun onClicked(
        inAppMessageCloser: InAppMessageCloser,
        inAppMessageView: View,
        inAppMessage: IInAppMessage
    )

    /**
     * Called when an in-app message Button is clicked.
     * @param inAppMessageCloser
     * @param messageButton
     * @param inAppMessageImmersive
     */
    fun onButtonClicked(
        inAppMessageCloser: InAppMessageCloser,
        messageButton: MessageButton,
        inAppMessageImmersive: IInAppMessageImmersive
    )

    /**
     * Called when the in-app message View is dismissed.
     * @param inAppMessageView
     * @param inAppMessage
     */
    fun onDismissed(inAppMessageView: View, inAppMessage: IInAppMessage)
}
