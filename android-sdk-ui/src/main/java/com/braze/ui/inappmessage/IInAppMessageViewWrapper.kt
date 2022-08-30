package com.braze.ui.inappmessage

import android.app.Activity
import android.view.View
import com.braze.models.inappmessage.IInAppMessage

interface IInAppMessageViewWrapper {
    /**
     * @return The [View] representing the [IInAppMessage]
     * that is visible to the user.
     */
    val inAppMessageView: View

    /**
     * @return The [IInAppMessage] being wrapped.
     */
    val inAppMessage: IInAppMessage

    /**
     * @return Whether the [IInAppMessage] view is
     * currently in the process of its close animation.
     */
    val isAnimatingClose: Boolean

    /**
     * Opens an [IInAppMessage] on the [Activity]. As a
     * result of this call, it is expected that an [IInAppMessage]
     * is visible and interactable by the user.
     *
     * Note that this method is expected to be called
     * on the main UI thread and should run synchronously.
     */
    fun open(activity: Activity)

    /**
     * Closes an [IInAppMessage]. As a
     * result of this call, it is expected that an [IInAppMessage]
     * is no longer visible and not interactable by the user.
     *
     * Note that this method is expected to be called
     * on the main UI thread and should run synchronously.
     */
    fun close()
}
