package com.braze.ui.inappmessage.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.braze.ui.R
import com.braze.enums.inappmessage.ImageStyle
import com.braze.models.inappmessage.IInAppMessageImmersive
import com.braze.models.inappmessage.InAppMessageModal
import com.braze.ui.inappmessage.config.BrazeInAppMessageParams.modalizedImageRadiusDp
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setViewBackgroundColorFilter
import com.braze.ui.support.convertDpToPixels
import com.braze.support.BrazeLogger.brazelog
import kotlin.math.min

open class InAppMessageModalView(context: Context?, attrs: AttributeSet?) :
    InAppMessageImmersiveBaseView(context, attrs) {

    protected var inAppMessageImageView: InAppMessageImageView? = null
    protected var inAppMessage: InAppMessageModal? = null

    override val frameView: View?
        get() = findViewById(R.id.com_braze_inappmessage_modal_frame)
    override val messageTextView: TextView?
        get() = findViewById(R.id.com_braze_inappmessage_modal_message)
    override val messageHeaderTextView: TextView?
        get() = findViewById(R.id.com_braze_inappmessage_modal_header_text)
    override val messageClickableView: View?
        get() = findViewById(R.id.com_braze_inappmessage_modal)
    override val messageCloseButtonView: View?
        get() = findViewById(R.id.com_braze_inappmessage_modal_close_button)
    override val messageIconView: TextView?
        get() = findViewById(R.id.com_braze_inappmessage_modal_icon)
    override val messageBackgroundObject: Drawable?
        get() = messageClickableView?.background
    override val messageImageView: ImageView?
        get() = inAppMessageImageView

    override fun resetMessageMargins(imageRetrievalSuccessful: Boolean) {
        super.resetMessageMargins(imageRetrievalSuccessful)
        // If the in-app message contains an image or icon, reset the image layout's margins to 0.
        // When there is no image or icon present, the layout has a top margin of 20 to create 20dp
        // of padding between the text content and the top of the message.
        val imageLayout =
            findViewById<RelativeLayout>(R.id.com_braze_inappmessage_modal_image_layout)
        if (imageRetrievalSuccessful || messageIconView != null) {
            if (imageLayout != null) {
                val layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0, 0, 0, 0)
                imageLayout.layoutParams = layoutParams
            }
        }

        // Make scrollView pass click events to message clickable view, so that clicking on the scrollView
        // dismisses the in-app message.
        val scrollViewChild = findViewById<View>(R.id.com_braze_inappmessage_modal_text_layout)
        scrollViewChild?.setOnClickListener {
            brazelog { "Passing scrollView click event to message clickable view." }
            messageClickableView?.performClick()
        }
    }

    override fun setMessageBackgroundColor(color: Int) {
        setViewBackgroundColorFilter(findViewById(R.id.com_braze_inappmessage_modal), color)
    }

    override fun getMessageButtonViews(numButtons: Int): List<View> {
        val buttonViews = mutableListOf<View>()

        // Based on the number of buttons, make one of the button parent layouts visible
        if (numButtons == 1) {
            val singleButtonParent =
                findViewById<View>(R.id.com_braze_inappmessage_modal_button_layout_single)
            singleButtonParent?.visibility = VISIBLE
            val singleButton =
                findViewById<View>(R.id.com_braze_inappmessage_modal_button_single_one)
            if (singleButton != null) {
                buttonViews.add(singleButton)
            }
        } else if (numButtons == 2) {
            val dualButtonParent =
                findViewById<View>(R.id.com_braze_inappmessage_modal_button_layout_dual)
            dualButtonParent?.visibility = VISIBLE
            val dualButton1 = findViewById<View>(R.id.com_braze_inappmessage_modal_button_dual_one)
            val dualButton2 = findViewById<View>(R.id.com_braze_inappmessage_modal_button_dual_two)
            if (dualButton1 != null) {
                buttonViews.add(dualButton1)
            }
            if (dualButton2 != null) {
                buttonViews.add(dualButton2)
            }
        }
        return buttonViews
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        resizeGraphicFrameIfAppropriate(this.context, inAppMessage)
    }

    open fun applyInAppMessageParameters(context: Context, inAppMessage: InAppMessageModal) {
        this.inAppMessage = inAppMessage
        inAppMessageImageView = findViewById(R.id.com_braze_inappmessage_modal_imageview)
        inAppMessageImageView?.let { setInAppMessageImageViewAttributes(context, inAppMessage, it) }
        resizeGraphicFrameIfAppropriate(context, inAppMessage)
    }

    /**
     * Programmatically set attributes on the image view classes inside the image ViewStubs.
     */
    protected open fun setInAppMessageImageViewAttributes(
        context: Context,
        inAppMessage: IInAppMessageImmersive,
        inAppMessageImageView: IInAppMessageImageView
    ) {
        val pixelRadius = convertDpToPixels(context, modalizedImageRadiusDp).toFloat()
        if (inAppMessage.imageStyle == ImageStyle.GRAPHIC) {
            inAppMessageImageView.setCornersRadiusPx(pixelRadius)
        } else {
            inAppMessageImageView.setCornersRadiiPx(pixelRadius, pixelRadius, 0.0f, 0.0f)
        }
        inAppMessageImageView.setInAppMessageImageCropType(inAppMessage.cropType)
    }

    /**
     * If displaying a graphic modal, resize its bounds based on the aspect ratio of the input image
     * and its maximum size.
     */
    protected open fun resizeGraphicFrameIfAppropriate(
        context: Context,
        inAppMessage: InAppMessageModal?
    ) {
        val bitmap = inAppMessage?.bitmap ?: return
        if (inAppMessage.imageStyle != ImageStyle.GRAPHIC) {
            return
        }
        val imageAspectRatio = bitmap.width.toDouble() / bitmap.height
        val resources = context.resources
        val marginPixels =
            resources.getDimensionPixelSize(R.dimen.com_braze_inappmessage_modal_margin)
        val maxModalWidth =
            resources.getDimensionPixelSize(R.dimen.com_braze_inappmessage_modal_max_width)
        val maxModalHeight =
            resources.getDimensionPixelSize(R.dimen.com_braze_inappmessage_modal_max_height)

        // The measured width is only available after the draw phase, which
        // this runnable will draw after.
        post {
            val maxWidthPixelSize = min(measuredWidth - marginPixels, maxModalWidth).toDouble()
            val maxHeightPixelSize =
                min(measuredHeight - marginPixels, maxModalHeight).toDouble()
            val maxSizeAspectRatio = maxWidthPixelSize / maxHeightPixelSize
            val modalBoundView = findViewById<View>(R.id.com_braze_inappmessage_modal_graphic_bound)
            if (modalBoundView != null) {
                val params = modalBoundView.layoutParams as LayoutParams
                if (imageAspectRatio >= maxSizeAspectRatio) {
                    params.width = maxWidthPixelSize.toInt()
                    params.height = (maxWidthPixelSize / imageAspectRatio).toInt()
                } else {
                    params.width = (maxHeightPixelSize * imageAspectRatio).toInt()
                    params.height = maxHeightPixelSize.toInt()
                }
                modalBoundView.layoutParams = params
            }
        }
    }
}
