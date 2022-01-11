@file:Suppress("DEPRECATION")
package com.braze.ui.inappmessage.listeners

import android.view.View
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.MessageButton
import com.braze.support.BrazeFunctionNotImplemented
import com.braze.ui.inappmessage.InAppMessageCloser
import com.braze.ui.inappmessage.InAppMessageOperation

/**
 * The IInAppMessageManagerListener returns the in-app message at specific
 * events in its control flow and gives the host app the option of
 * overriding Braze's default display handling and implementing its own custom behavior.
 *
 *
 * If you are implementing Unity, you must use IAppboyUnityInAppMessageListener instead.
 *
 *
 * See [BrazeInAppMessageManager]
 */
interface IInAppMessageManagerListener {
    /**
     * @param inAppMessage The in-app message that is currently requested for display.
     * @return InAppMessageOperation indicating how to handle the candidate in-app message.
     */
    fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage): InAppMessageOperation = InAppMessageOperation.DISPLAY_NOW

    /**
     * @param inAppMessage       The clicked in-app message.
     * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
     * If remaining in the same activity, closing should be animated.
     * @return boolean flag to indicate to Braze whether the click has been manually handled.
     * If true, Braze will only log a click and do nothing else. If false, Braze will
     * log a click and also close the in-app message automatically.
     */
    @Deprecated("InAppMessageCloser is deprecated", ReplaceWith("onInAppMessageClicked(inAppMessage)"))
    fun onInAppMessageClicked(inAppMessage: IInAppMessage, inAppMessageCloser: InAppMessageCloser?): Boolean = throw BrazeFunctionNotImplemented

    /**
     * @param inAppMessage       The clicked in-app message.
     * @return boolean flag to indicate to Braze whether the click has been manually handled.
     * If true, Braze will only log a click and do nothing else. If false, Braze will
     * log a click and also close the in-app message automatically.
     */
    fun onInAppMessageClicked(inAppMessage: IInAppMessage) = false

    /**
     * @param inAppMessage       The clicked in-app message.
     * @param button             The clicked message button.
     * @param inAppMessageCloser Closing should not be animated if transitioning to a new activity.
     * If remaining in the same activity, closing should be animated.
     * @return boolean flag to indicate to Braze whether the click has been manually handled.
     * If true, Braze will only log a click and do nothing else. If false, Braze will
     * log a click and also close the in-app message automatically.
     */
    @Deprecated("InAppMessageCloser is deprecated", ReplaceWith("onInAppMessageButtonClicked(inAppMessage, button)"))
    fun onInAppMessageButtonClicked(
        inAppMessage: IInAppMessage,
        button: MessageButton,
        inAppMessageCloser: InAppMessageCloser?
    ): Boolean = throw BrazeFunctionNotImplemented

    /**
     * @param inAppMessage       The clicked in-app message.
     * @param button             The clicked message button.
     * @return boolean flag to indicate to Braze whether the click has been manually handled.
     * If true, Braze will only log a click and do nothing else. If false, Braze will
     * log a click and also close the in-app message automatically.
     */
    fun onInAppMessageButtonClicked(inAppMessage: IInAppMessage, button: MessageButton) = false

    /**
     * @param inAppMessage the in-app message that was closed.
     */
    fun onInAppMessageDismissed(inAppMessage: IInAppMessage) {}

    /**
     * Called before the in-app message View is added to the layout.
     *
     *
     * Note that this is called before any default processing in
     * [DefaultInAppMessageViewLifecycleListener] takes place.
     *
     * @param inAppMessageView The [View] representing the [IInAppMessage].
     * @param inAppMessage     The [IInAppMessage] being displayed.
     */
    fun beforeInAppMessageViewOpened(inAppMessageView: View, inAppMessage: IInAppMessage) {}

    /**
     * Called after the in-app message View has been added to the layout
     * (and the appearing animation has completed).
     *
     *
     * Note that this is called after any default processing in
     * [DefaultInAppMessageViewLifecycleListener] takes place.
     *
     * @param inAppMessageView The [View] representing the [IInAppMessage].
     * @param inAppMessage     The [IInAppMessage] being displayed.
     */
    fun afterInAppMessageViewOpened(inAppMessageView: View, inAppMessage: IInAppMessage) {}

    /**
     * Called before the in-app message View is removed from the layout
     * (and before any closing animation starts).
     *
     *
     * Note that this is called before any default processing in
     * [DefaultInAppMessageViewLifecycleListener] takes place.
     *
     * @param inAppMessageView The [View] representing the [IInAppMessage].
     * @param inAppMessage     The [IInAppMessage] being displayed.
     */
    fun beforeInAppMessageViewClosed(inAppMessageView: View, inAppMessage: IInAppMessage) {}

    /**
     * Called after the in-app message View has been removed from the
     * layout (and the disappearing animation has completed).
     *
     *
     * Note that this is called after any default processing in
     * [DefaultInAppMessageViewLifecycleListener] takes place.
     *
     * @param inAppMessage The [IInAppMessage] being displayed.
     */
    fun afterInAppMessageViewClosed(inAppMessage: IInAppMessage) {}
}
