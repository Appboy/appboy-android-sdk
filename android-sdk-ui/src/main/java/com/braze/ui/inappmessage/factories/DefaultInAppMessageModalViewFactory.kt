package com.braze.ui.inappmessage.factories

import android.annotation.SuppressLint
import android.app.Activity
import com.appboy.ui.R
import com.braze.Braze
import com.braze.enums.BrazeViewBounds
import com.braze.enums.inappmessage.ImageStyle
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.InAppMessageModal
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.IInAppMessageViewFactory
import com.braze.ui.inappmessage.views.InAppMessageBaseView
import com.braze.ui.inappmessage.views.InAppMessageImageView
import com.braze.ui.inappmessage.views.InAppMessageModalView

class DefaultInAppMessageModalViewFactory : IInAppMessageViewFactory {
    override fun createInAppMessageView(
        activity: Activity,
        inAppMessage: IInAppMessage
    ): InAppMessageModalView {
        val applicationContext = activity.applicationContext
        val inAppMessageModal = inAppMessage as InAppMessageModal
        val isGraphic = inAppMessageModal.imageStyle == ImageStyle.GRAPHIC
        val view = getAppropriateModalView(activity, isGraphic)
        view.applyInAppMessageParameters(applicationContext, inAppMessageModal)
        val imageUrl = InAppMessageBaseView.getAppropriateImageUrl(inAppMessageModal)
        if (!imageUrl.isNullOrEmpty()) {
            val brazeImageLoader = Braze.getInstance(applicationContext).imageLoader
            brazeImageLoader.renderUrlIntoInAppMessageView(
                applicationContext,
                inAppMessage,
                imageUrl,
                view.messageImageView,
                BrazeViewBounds.IN_APP_MESSAGE_MODAL
            )
        }

        // Modal frame should only dismiss the message when configured.
        view.frameView.setOnClickListener {
            if (BrazeInAppMessageManager.getInstance().doesClickOutsideModalViewDismissInAppMessageView) {
                brazelog(I) { "Dismissing modal after frame click" }
                BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true)
            }
        }
        view.setMessageBackgroundColor(inAppMessage.backgroundColor)
        view.setFrameColor(inAppMessageModal.frameColor)
        view.setMessageButtons(inAppMessageModal.messageButtons)
        view.setMessageCloseButtonColor(inAppMessageModal.closeButtonColor)
        if (!isGraphic) {
            view.setMessage(inAppMessage.message)
            view.setMessageTextColor(inAppMessage.messageTextColor)
            view.setMessageHeaderText(inAppMessageModal.header)
            view.setMessageHeaderTextColor(inAppMessageModal.headerTextColor)
            view.setMessageIcon(
                inAppMessage.icon,
                inAppMessage.iconColor,
                inAppMessage.iconBackgroundColor
            )
            view.setMessageHeaderTextAlignment(inAppMessageModal.headerTextAlign)
            view.setMessageTextAlign(inAppMessageModal.messageTextAlign)
            view.resetMessageMargins(inAppMessageModal.imageDownloadSuccessful)
            (view.messageImageView as InAppMessageImageView).setAspectRatio(NON_GRAPHIC_ASPECT_RATIO)
        }
        view.setLargerCloseButtonClickArea(view.messageCloseButtonView)
        view.setupDirectionalNavigation(inAppMessageModal.messageButtons.size)
        return view
    }

    @SuppressLint("InflateParams")
    private fun getAppropriateModalView(
        activity: Activity,
        isGraphic: Boolean
    ): InAppMessageModalView {
        return if (isGraphic) {
            activity.layoutInflater.inflate(
                R.layout.com_braze_inappmessage_modal_graphic,
                null
            ) as InAppMessageModalView
        } else {
            activity.layoutInflater.inflate(
                R.layout.com_braze_inappmessage_modal,
                null
            ) as InAppMessageModalView
        }
    }

    companion object {
        private const val NON_GRAPHIC_ASPECT_RATIO = 290f / 100f
    }
}
