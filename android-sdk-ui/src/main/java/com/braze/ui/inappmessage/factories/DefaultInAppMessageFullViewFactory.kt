package com.braze.ui.inappmessage.factories

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.RelativeLayout
import com.appboy.ui.R
import com.braze.Braze
import com.braze.enums.BrazeViewBounds
import com.braze.enums.inappmessage.ImageStyle
import com.braze.enums.inappmessage.Orientation
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.InAppMessageFull
import com.braze.ui.inappmessage.IInAppMessageViewFactory
import com.braze.ui.inappmessage.views.InAppMessageBaseView
import com.braze.ui.inappmessage.views.InAppMessageFullView
import com.braze.ui.inappmessage.views.InAppMessageImageView
import com.braze.ui.support.convertDpToPixels
import com.braze.ui.support.isRunningOnTablet
import com.braze.ui.support.setHeightOnViewLayoutParams
import kotlin.math.min

open class DefaultInAppMessageFullViewFactory : IInAppMessageViewFactory {
    @Suppress("LongMethod")
    override fun createInAppMessageView(
        activity: Activity,
        inAppMessage: IInAppMessage
    ): InAppMessageFullView {
        val applicationContext = activity.applicationContext
        val inAppMessageFull = inAppMessage as InAppMessageFull
        val isGraphic = inAppMessageFull.imageStyle == ImageStyle.GRAPHIC
        val view = getAppropriateFullView(activity, isGraphic)
        view.createAppropriateViews(activity, inAppMessageFull, isGraphic)

        // Since this image is the width of the screen, the view bounds are uncapped
        val imageUrl = InAppMessageBaseView.getAppropriateImageUrl(inAppMessageFull)
        if (!imageUrl.isNullOrEmpty()) {
            val brazeImageLoader = Braze.getInstance(applicationContext).imageLoader
            view.messageImageView?.let {
                brazeImageLoader.renderUrlIntoInAppMessageView(
                    applicationContext,
                    inAppMessage,
                    imageUrl,
                    it,
                    BrazeViewBounds.NO_BOUNDS
                )
            }
        }

        // modal frame should not be clickable.
        view.frameView?.setOnClickListener(null)
        view.setMessageBackgroundColor(inAppMessageFull.backgroundColor)
        inAppMessageFull.frameColor?.let { view.setFrameColor(it) }
        view.setMessageButtons(inAppMessageFull.messageButtons)
        view.setMessageCloseButtonColor(inAppMessageFull.closeButtonColor)
        if (!isGraphic) {
            inAppMessageFull.message?.let { view.setMessage(it) }
            view.setMessageTextColor(inAppMessageFull.messageTextColor)
            inAppMessageFull.header?.let { view.setMessageHeaderText(it) }
            view.setMessageHeaderTextColor(inAppMessageFull.headerTextColor)
            view.setMessageHeaderTextAlignment(inAppMessageFull.headerTextAlign)
            view.setMessageTextAlign(inAppMessageFull.messageTextAlign)
            view.resetMessageMargins(inAppMessageFull.imageDownloadSuccessful)

            // Only non-graphic full in-app messages should be capped to half the parent height
            (view.messageImageView as InAppMessageImageView).setToHalfParentHeight(true)
        }
        view.setLargerCloseButtonClickArea(view.messageCloseButtonView)
        resetLayoutParamsIfAppropriate(activity, inAppMessageFull, view)
        view.setupDirectionalNavigation(inAppMessageFull.messageButtons.size)

        // Get the scrollView, if it exists. For graphic full, it will not
        val scrollView = view.findViewById<View>(R.id.com_braze_inappmessage_full_scrollview)
        if (scrollView != null) {
            val allContentParent = view.findViewById<View>(R.id.com_braze_inappmessage_full_all_content_parent)
            scrollView.post {
                // Get the parent height
                val parentHeight = allContentParent.height

                // Half of that is the Image
                // So we have another half allotted for us + some margins + the buttons
                val halfHeight = parentHeight / 2

                // Compute the rest of the height for the ScrollView + buttons + margins
                val contentView = view.findViewById<View>(R.id.com_braze_inappmessage_full_text_and_button_content_parent)
                val layoutParams = contentView.layoutParams as MarginLayoutParams
                var nonScrollViewHeight = layoutParams.bottomMargin + layoutParams.topMargin
                if (inAppMessageFull.messageButtons.isNotEmpty()) {
                    // Account for all appropriate height / margins
                    nonScrollViewHeight += convertDpToPixels(
                        applicationContext,
                        BUTTONS_PRESENT_SCROLLVIEW_EXCESS_HEIGHT_VALUE_IN_DP.toDouble()
                    ).toInt()
                }

                // The remaining height is the MOST that the scrollView can take up
                val scrollViewAppropriateHeight =
                    min(scrollView.height, halfHeight - nonScrollViewHeight)

                // Now set that height for the ScrollView
                setHeightOnViewLayoutParams(scrollView, scrollViewAppropriateHeight)

                // Request another layout since we changed bounds for everything
                scrollView.requestLayout()
                view.messageImageView?.requestLayout()
            }
        }
        return view
    }

    /**
     * For in-app messages that have a preferred orientation and are being displayed on tablet,
     * ensure the in-app message appears in the style of the preferred orientation regardless of
     * actual screen orientation.
     *
     * @param activity
     * @param inAppMessage
     * @param view
     * @return true if params were reset
     */
    private fun resetLayoutParamsIfAppropriate(
        activity: Activity,
        inAppMessage: IInAppMessage,
        view: InAppMessageFullView
    ): Boolean {
        if (!activity.isRunningOnTablet()) {
            return false
        }
        if (inAppMessage.orientation === Orientation.ANY) {
            return false
        }
        val longEdge = view.longEdge
        val shortEdge = view.shortEdge
        if (longEdge > 0 && shortEdge > 0) {
            val layoutParams: RelativeLayout.LayoutParams = if (inAppMessage.orientation === Orientation.LANDSCAPE) {
                RelativeLayout.LayoutParams(longEdge, shortEdge)
            } else {
                RelativeLayout.LayoutParams(shortEdge, longEdge)
            }
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            view.messageBackgroundObject?.layoutParams = layoutParams
            return true
        }
        return false
    }

    @SuppressLint("InflateParams")
    fun getAppropriateFullView(activity: Activity, isGraphic: Boolean): InAppMessageFullView {
        return if (isGraphic) {
            activity.layoutInflater.inflate(
                R.layout.com_braze_inappmessage_full_graphic,
                null
            ) as InAppMessageFullView
        } else {
            activity.layoutInflater.inflate(
                R.layout.com_braze_inappmessage_full,
                null
            ) as InAppMessageFullView
        }
    }

    companion object {
        /**
         * 20dp margin between button / bottom of scrollview.
         * 44dp height for buttons.
         */
        private const val BUTTONS_PRESENT_SCROLLVIEW_EXCESS_HEIGHT_VALUE_IN_DP = 64
    }
}
