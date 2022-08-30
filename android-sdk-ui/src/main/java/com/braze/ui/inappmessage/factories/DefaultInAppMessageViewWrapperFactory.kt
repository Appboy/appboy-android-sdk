package com.braze.ui.inappmessage.factories

import android.view.View
import android.view.animation.Animation
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.models.inappmessage.IInAppMessage
import com.braze.ui.inappmessage.DefaultInAppMessageViewWrapper
import com.braze.ui.inappmessage.IInAppMessageViewWrapper
import com.braze.ui.inappmessage.IInAppMessageViewWrapperFactory
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener

/**
 * The default [IInAppMessageViewWrapperFactory] that returns
 * an instance of [DefaultInAppMessageViewWrapper].
 */
class DefaultInAppMessageViewWrapperFactory : IInAppMessageViewWrapperFactory {
    override fun createInAppMessageViewWrapper(
        inAppMessageView: View,
        inAppMessage: IInAppMessage,
        inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener,
        configurationProvider: BrazeConfigurationProvider,
        openingAnimation: Animation?,
        closingAnimation: Animation?,
        clickableInAppMessageView: View?
    ): IInAppMessageViewWrapper {
        return DefaultInAppMessageViewWrapper(
            inAppMessageView,
            inAppMessage,
            inAppMessageViewLifecycleListener,
            configurationProvider,
            openingAnimation,
            closingAnimation,
            clickableInAppMessageView
        )
    }

    override fun createInAppMessageViewWrapper(
        inAppMessageView: View,
        inAppMessage: IInAppMessage,
        inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener,
        configurationProvider: BrazeConfigurationProvider,
        openingAnimation: Animation?,
        closingAnimation: Animation?,
        clickableInAppMessageView: View?,
        buttons: List<View>?,
        closeButton: View?
    ): IInAppMessageViewWrapper {
        return DefaultInAppMessageViewWrapper(
            inAppMessageView,
            inAppMessage,
            inAppMessageViewLifecycleListener,
            configurationProvider,
            openingAnimation,
            closingAnimation,
            clickableInAppMessageView,
            buttons,
            closeButton
        )
    }
}
