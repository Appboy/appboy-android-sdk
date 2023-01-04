package com.braze.ui.inappmessage.views

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import com.braze.ui.R
import com.braze.enums.inappmessage.ImageStyle
import com.braze.models.inappmessage.IInAppMessageImmersive
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.config.BrazeInAppMessageParams.modalizedImageRadiusDp
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setViewBackgroundColor
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setViewBackgroundColorFilter
import com.braze.ui.support.convertDpToPixels
import com.braze.ui.support.getMaxSafeBottomInset
import com.braze.ui.support.getMaxSafeLeftInset
import com.braze.ui.support.getMaxSafeRightInset
import com.braze.ui.support.getMaxSafeTopInset
import com.braze.ui.support.isRunningOnTablet

open class InAppMessageFullView(context: Context?, attrs: AttributeSet?) :
    InAppMessageImmersiveBaseView(context, attrs) {
    private var inAppMessageImageView: InAppMessageImageView? = null
    private var isGraphic = false

    override val messageTextView: TextView
        get() = findViewById(R.id.com_braze_inappmessage_full_message)
    override val messageHeaderTextView: TextView
        get() = findViewById(R.id.com_braze_inappmessage_full_header_text)
    override val frameView: View?
        get() = findViewById(R.id.com_braze_inappmessage_full_frame)
    override val messageCloseButtonView: View?
        get() = findViewById(R.id.com_braze_inappmessage_full_close_button)
    override val messageClickableView: View?
        get() = findViewById(R.id.com_braze_inappmessage_full)
    override val messageImageView: ImageView?
        get() = inAppMessageImageView
    override val messageIconView: TextView?
        get() = null
    override val messageBackgroundObject: View?
        get() = findViewById(R.id.com_braze_inappmessage_full)

    /**
     * @return the size in pixels of the long edge of a modalized full in-app messages, used to size
     * modalized in-app messages appropriately on tablets.
     */
    open val longEdge: Int
        get() {
            val inAppMessageFullView = findViewById<View>(R.id.com_braze_inappmessage_full)
            return inAppMessageFullView.layoutParams.height
        }

    /**
     * @return the size in pixels of the short edge of a modalized full in-app messages, used to size
     * modalized in-app messages appropriately on tablets.
     */
    open val shortEdge: Int
        get() {
            val inAppMessageFullView = findViewById<View>(R.id.com_braze_inappmessage_full)
            return inAppMessageFullView.layoutParams.width
        }

    open fun createAppropriateViews(
        activity: Activity,
        inAppMessage: IInAppMessageImmersive,
        isGraphic: Boolean
    ) {
        inAppMessageImageView = findViewById(R.id.com_braze_inappmessage_full_imageview)
        inAppMessageImageView?.let {
            setInAppMessageImageViewAttributes(activity, inAppMessage, it)
        }
        this.isGraphic = isGraphic
    }

    override fun setMessageBackgroundColor(color: Int) {
        val msgBackgroundObject = messageBackgroundObject
        if (msgBackgroundObject?.background is GradientDrawable) {
            setViewBackgroundColorFilter(msgBackgroundObject, color)
        } else {
            if (isGraphic) {
                super.setMessageBackgroundColor(color)
            } else {
                setViewBackgroundColor(
                    findViewById(R.id.com_braze_inappmessage_full_all_content_parent),
                    color
                )
                setViewBackgroundColor(
                    findViewById(R.id.com_braze_inappmessage_full_text_and_button_content_parent),
                    color
                )
            }
        }
    }

    override fun getMessageButtonViews(numButtons: Int): List<View> {
        val buttonViews = mutableListOf<View>()

        // Based on the number of buttons, make one of the button parent layouts visible
        if (numButtons == 1) {
            val singleButtonParent =
                findViewById<View>(R.id.com_braze_inappmessage_full_button_layout_single)
            if (singleButtonParent != null) {
                singleButtonParent.visibility = VISIBLE
            }
            val singleButton =
                findViewById<View>(R.id.com_braze_inappmessage_full_button_single_one)
            if (singleButton != null) {
                buttonViews.add(singleButton)
            }
        } else if (numButtons == 2) {
            val dualButtonParent =
                findViewById<View>(R.id.com_braze_inappmessage_full_button_layout_dual)
            if (dualButtonParent != null) {
                dualButtonParent.visibility = VISIBLE
            }
            val dualButton1 = findViewById<View>(R.id.com_braze_inappmessage_full_button_dual_one)
            val dualButton2 = findViewById<View>(R.id.com_braze_inappmessage_full_button_dual_two)
            if (dualButton1 != null) {
                buttonViews.add(dualButton1)
            }
            if (dualButton2 != null) {
                buttonViews.add(dualButton2)
            }
        }
        return buttonViews
    }

    override fun resetMessageMargins(imageRetrievalSuccessful: Boolean) {
        super.resetMessageMargins(imageRetrievalSuccessful)

        messageClickableView?.let { msgClickableView: View ->
            // Make scrollView pass click events to message clickable view, so that clicking on the scrollView
            // dismisses the in-app message.
            val scrollViewChild = findViewById<View>(R.id.com_braze_inappmessage_full_text_layout)
            scrollViewChild.setOnClickListener {
                brazelog { "Passing scrollView click event to message clickable view." }
                msgClickableView.performClick()
            }
        }
    }

    /**
     * Applies the [WindowInsetsCompat] by ensuring the close button and message text on the in-app message does not render
     * in the display cutout area.
     *
     * @param insets The [WindowInsetsCompat] object directly from
     * [androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener].
     */
    override fun applyWindowInsets(insets: WindowInsetsCompat) {
        super.applyWindowInsets(insets)
        // Attempt to fix the close button
        messageCloseButtonView?.let {
            applyDisplayCutoutMarginsToCloseButton(insets, it)
        }
        if (isGraphic) {
            // Fix the button layouts individually
            val singleButtonParent =
                findViewById<View>(R.id.com_braze_inappmessage_full_button_layout_single)
            if (singleButtonParent?.visibility == VISIBLE) {
                applyDisplayCutoutMarginsToContentArea(insets, singleButtonParent)
                return
            }
            val dualButtonParent =
                findViewById<View>(R.id.com_braze_inappmessage_full_button_layout_dual)
            if (dualButtonParent?.visibility == VISIBLE) {
                applyDisplayCutoutMarginsToContentArea(insets, dualButtonParent)
            }
        } else {
            // Fix the content area as well. The content area is the header, message, and buttons.
            val contentArea =
                findViewById<View>(R.id.com_braze_inappmessage_full_text_and_button_content_parent)
            contentArea?.let { applyDisplayCutoutMarginsToContentArea(insets, it) }
        }
    }

    /**
     * Programmatically set attributes on the image view classes inside the image ViewStubs.
     */
    private fun setInAppMessageImageViewAttributes(
        activity: Activity,
        inAppMessage: IInAppMessageImmersive,
        inAppMessageImageView: IInAppMessageImageView
    ) {
        inAppMessageImageView.setInAppMessageImageCropType(inAppMessage.cropType)
        if (activity.isRunningOnTablet()) {
            val radiusInPx = convertDpToPixels(activity, modalizedImageRadiusDp).toFloat()
            if (inAppMessage.imageStyle == ImageStyle.GRAPHIC) {
                // for graphic fulls, set the image radius at all four corners.
                inAppMessageImageView.setCornersRadiusPx(radiusInPx)
            } else {
                // for graphic fulls, set the image radius only at the top left and right corners, which
                // are at the edge of the in-app message.
                inAppMessageImageView.setCornersRadiiPx(radiusInPx, radiusInPx, 0.0f, 0.0f)
            }
        } else {
            inAppMessageImageView.setCornersRadiusPx(0.0f)
        }
    }

    /**
     * Shifts/margins the close button out of the display cutout area.
     */
    private fun applyDisplayCutoutMarginsToCloseButton(
        windowInsets: WindowInsetsCompat,
        closeButtonView: View
    ) {
        if (closeButtonView.layoutParams == null || closeButtonView.layoutParams !is MarginLayoutParams) {
            brazelog {
                "Close button layout params are null or not of the expected class. Not applying window insets."
            }
            return
        }

        // Offset the existing margin with whatever the inset margins safe area values are
        val layoutParams = closeButtonView.layoutParams as MarginLayoutParams
        layoutParams.setMargins(
            getMaxSafeLeftInset(windowInsets) + layoutParams.leftMargin,
            getMaxSafeTopInset(windowInsets) + layoutParams.topMargin,
            getMaxSafeRightInset(windowInsets) + layoutParams.rightMargin,
            getMaxSafeBottomInset(windowInsets) + layoutParams.bottomMargin
        )
    }

    /**
     * Shifts/margins the close button out of the display cutout area.
     */
    private fun applyDisplayCutoutMarginsToContentArea(
        windowInsets: WindowInsetsCompat,
        contentAreaView: View
    ) {
        if (contentAreaView.layoutParams !is MarginLayoutParams) {
            brazelog {
                "Content area layout params are null or not of the expected class. Not applying window insets."
            }
            return
        }

        // Offset the existing margin with whatever the inset margins safe area values are
        val layoutParams = contentAreaView.layoutParams as MarginLayoutParams
        layoutParams.setMargins(
            getMaxSafeLeftInset(windowInsets) + layoutParams.leftMargin,
            layoutParams.topMargin,
            getMaxSafeRightInset(windowInsets) + layoutParams.rightMargin,
            getMaxSafeBottomInset(windowInsets) + layoutParams.bottomMargin
        )
    }
}
