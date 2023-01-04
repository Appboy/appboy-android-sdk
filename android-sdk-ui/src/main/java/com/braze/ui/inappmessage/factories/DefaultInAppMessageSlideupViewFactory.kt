package com.braze.ui.inappmessage.factories

import android.app.Activity
import com.braze.ui.R
import com.braze.Braze
import com.braze.enums.BrazeViewBounds
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.InAppMessageSlideup
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.IInAppMessageViewFactory
import com.braze.ui.inappmessage.views.InAppMessageBaseView
import com.braze.ui.inappmessage.views.InAppMessageSlideupView
import com.braze.ui.support.isDeviceNotInTouchMode

open class DefaultInAppMessageSlideupViewFactory : IInAppMessageViewFactory {
    override fun createInAppMessageView(
        activity: Activity,
        inAppMessage: IInAppMessage
    ): InAppMessageSlideupView? {
        val view = activity.layoutInflater.inflate(
            R.layout.com_braze_inappmessage_slideup,
            null
        ) as InAppMessageSlideupView
        if (isDeviceNotInTouchMode(view)) {
            brazelog(W) { "The device is not currently in touch mode. This message requires user touch interaction to display properly." }
            return null
        }
        val inAppMessageSlideup = inAppMessage as InAppMessageSlideup
        val applicationContext = activity.applicationContext
        view.applyInAppMessageParameters(inAppMessage)
        val imageUrl = InAppMessageBaseView.getAppropriateImageUrl(inAppMessageSlideup)
        if (!imageUrl.isNullOrEmpty()) {
            val brazeImageLoader = Braze.getInstance(applicationContext).imageLoader
            view.messageImageView?.let {
                brazeImageLoader.renderUrlIntoInAppMessageView(
                    applicationContext,
                    inAppMessage,
                    imageUrl,
                    it,
                    BrazeViewBounds.IN_APP_MESSAGE_SLIDEUP
                )
            }
        }
        view.setMessageBackgroundColor(inAppMessageSlideup.backgroundColor)
        inAppMessageSlideup.message?.let { view.setMessage(it) }
        view.setMessageTextColor(inAppMessageSlideup.messageTextColor)
        view.setMessageTextAlign(inAppMessageSlideup.messageTextAlign)
        inAppMessageSlideup.icon?.let {
            view.setMessageIcon(
                it,
                inAppMessageSlideup.iconColor,
                inAppMessageSlideup.iconBackgroundColor
            )
        }
        view.setMessageChevron(inAppMessageSlideup.chevronColor, inAppMessageSlideup.clickAction)
        view.resetMessageMargins(inAppMessageSlideup.imageDownloadSuccessful)
        return view
    }
}
