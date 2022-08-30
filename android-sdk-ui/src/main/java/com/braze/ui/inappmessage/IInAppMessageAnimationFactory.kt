package com.braze.ui.inappmessage

import android.view.animation.Animation
import com.braze.models.inappmessage.IInAppMessage

interface IInAppMessageAnimationFactory {
    /**
     * This method returns the animation that will be used to animate the message as it enters the screen.
     * @return animation that will be applied to the in-app message view using
     * [android.view.View.setAnimation]
     */
    fun getOpeningAnimation(inAppMessage: IInAppMessage): Animation?

    /**
     * This method returns the animation that will be used to animate the message as it exits the screen.
     * @return animation that will be applied to the in-app message view using
     * [android.view.View.setAnimation]
     */
    fun getClosingAnimation(inAppMessage: IInAppMessage): Animation?
}
