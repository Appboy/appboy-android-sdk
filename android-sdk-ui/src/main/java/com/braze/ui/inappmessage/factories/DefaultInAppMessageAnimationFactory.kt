package com.braze.ui.inappmessage.factories

import android.content.res.Resources
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.braze.enums.inappmessage.SlideFrom
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.InAppMessageSlideup
import com.braze.ui.inappmessage.IInAppMessageAnimationFactory
import com.braze.ui.support.createVerticalAnimation
import com.braze.ui.support.setAnimationParams

open class DefaultInAppMessageAnimationFactory : IInAppMessageAnimationFactory {
    private val shortAnimationDurationMs =
        Resources.getSystem().getInteger(android.R.integer.config_shortAnimTime).toLong()

    override fun getOpeningAnimation(inAppMessage: IInAppMessage): Animation? {
        return if (inAppMessage is InAppMessageSlideup) {
            if (inAppMessage.slideFrom === SlideFrom.TOP) {
                createVerticalAnimation(-1f, 0f, shortAnimationDurationMs, false)
            } else {
                createVerticalAnimation(1f, 0f, shortAnimationDurationMs, false)
            }
        } else {
            setAnimationParams(AlphaAnimation(0f, 1f), shortAnimationDurationMs, true)
        }
    }

    override fun getClosingAnimation(inAppMessage: IInAppMessage): Animation? {
        return if (inAppMessage is InAppMessageSlideup) {
            if (inAppMessage.slideFrom === SlideFrom.TOP) {
                createVerticalAnimation(0f, -1f, shortAnimationDurationMs, false)
            } else {
                createVerticalAnimation(0f, 1f, shortAnimationDurationMs, false)
            }
        } else {
            setAnimationParams(AlphaAnimation(1f, 0f), shortAnimationDurationMs, false)
        }
    }
}
