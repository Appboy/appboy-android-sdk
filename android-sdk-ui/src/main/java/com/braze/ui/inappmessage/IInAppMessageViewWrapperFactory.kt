package com.braze.ui.inappmessage

import android.view.View
import android.view.animation.Animation
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.models.inappmessage.IInAppMessage
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener

interface IInAppMessageViewWrapperFactory {
    /**
     * Factory interface for non [IInAppMessageImmersive] view wrappers.
     * Implementations should add click listeners to the in-app message view and
     * also add swipe functionality to [InAppMessageSlideup] in-app messages.
     *
     * @param inAppMessageView                  In-app message top level view visible to the user.
     * @param inAppMessage                      In-app message model.
     * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
     * @param configurationProvider       Configuration provider.
     * @param openingAnimation                  The [Animation] used when opening the [IInAppMessage]
     * and becoming visible to the user.
     * Should be called during [IInAppMessageViewWrapper.open].
     * @param closingAnimation                  The [Animation] used when closing the [IInAppMessage].
     * Should be called during [IInAppMessageViewWrapper.close].
     * @param clickableInAppMessageView         [View] for which click actions apply.
     */
    @Suppress("LongParameterList")
    fun createInAppMessageViewWrapper(
        inAppMessageView: View,
        inAppMessage: IInAppMessage,
        inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener,
        configurationProvider: BrazeConfigurationProvider,
        openingAnimation: Animation?,
        closingAnimation: Animation?,
        clickableInAppMessageView: View?
    ): IInAppMessageViewWrapper

    /**
     * Constructor for [IInAppMessageImmersive] in-app message view wrappers.
     * Implementations should add click listeners to the in-app message view and also
     * add listeners to an optional close button and message button views.
     *
     * @param inAppMessageView                  In-app message top level view visible to the user.
     * @param inAppMessage                      In-app message model.
     * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
     * @param configurationProvider       Configuration provider.
     * @param openingAnimation                  The [Animation] used when opening the [IInAppMessage]
     * and becoming visible to the user.
     * Should be called during [IInAppMessageViewWrapper.open].
     * @param closingAnimation                  The [Animation] used when closing the [IInAppMessage].
     * Should be called during [IInAppMessageViewWrapper.close].
     * @param clickableInAppMessageView         [View] for which click actions apply.
     * @param buttons                           List of views corresponding to [MessageButton]
     * objects stored in the in-app message model object.
     * These views should map one to one with the MessageButton objects.
     * @param closeButton                       The [View] responsible for closing the in-app message.
     */
    @Suppress("LongParameterList")
    fun createInAppMessageViewWrapper(
        inAppMessageView: View,
        inAppMessage: IInAppMessage,
        inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener,
        configurationProvider: BrazeConfigurationProvider,
        openingAnimation: Animation?,
        closingAnimation: Animation?,
        clickableInAppMessageView: View?,
        buttons: List<View>?,
        closeButton: View?
    ): IInAppMessageViewWrapper
}
