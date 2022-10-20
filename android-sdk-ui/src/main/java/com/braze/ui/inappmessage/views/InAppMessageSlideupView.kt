package com.braze.ui.inappmessage.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import com.appboy.ui.R
import com.braze.enums.inappmessage.ClickAction
import com.braze.models.inappmessage.IInAppMessage
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setViewBackgroundColorFilter
import com.braze.ui.support.getMaxSafeBottomInset
import com.braze.ui.support.getMaxSafeLeftInset
import com.braze.ui.support.getMaxSafeRightInset
import com.braze.ui.support.getMaxSafeTopInset
import com.braze.ui.support.removeViewFromParent

open class InAppMessageSlideupView(context: Context?, attrs: AttributeSet?) :
    InAppMessageBaseView(context, attrs) {
    private var inAppMessageImageView: InAppMessageImageView? = null

    override val messageTextView: TextView?
        get() = findViewById(R.id.com_braze_inappmessage_slideup_message)
    override val messageBackgroundObject: View?
        get() = findViewById(R.id.com_braze_inappmessage_slideup_container)
    override val messageImageView: ImageView?
        get() = inAppMessageImageView
    override val messageIconView: TextView?
        get() = findViewById(R.id.com_braze_inappmessage_slideup_icon)
    private val messageChevronView: View?
        get() = findViewById(R.id.com_braze_inappmessage_slideup_chevron)

    fun applyInAppMessageParameters(inAppMessage: IInAppMessage) {
        inAppMessageImageView = findViewById(R.id.com_braze_inappmessage_slideup_imageview)
        inAppMessageImageView?.setInAppMessageImageCropType(inAppMessage.cropType)
    }

    fun setMessageChevron(color: Int, clickAction: ClickAction) {
        if (clickAction == ClickAction.NONE) {
            messageChevronView?.visibility = GONE
        } else {
            messageChevronView?.let { setViewBackgroundColorFilter(it, color) }
        }
    }

    override fun setMessageBackgroundColor(color: Int) {
        if (messageBackgroundObject?.background is GradientDrawable) {
            messageBackgroundObject?.let { setViewBackgroundColorFilter(it, color) }
        } else {
            super.setMessageBackgroundColor(color)
        }
    }

    override fun resetMessageMargins(imageRetrievalSuccessful: Boolean) {
        super.resetMessageMargins(imageRetrievalSuccessful)
        val isIconBlank = messageIconView?.text?.isEmpty() != false

        if (!imageRetrievalSuccessful && isIconBlank) {
            // There's neither an icon nor an image
            // Remove the image container layout and reset our text's left margin
            val imageContainerLayout =
                findViewById<RelativeLayout>(R.id.com_braze_inappmessage_slideup_image_layout)
            imageContainerLayout?.removeViewFromParent()

            // Reset the margin for the message
            val slideupMessage = findViewById<TextView>(R.id.com_braze_inappmessage_slideup_message)
            val layoutParams = slideupMessage?.layoutParams as LayoutParams?
            layoutParams?.leftMargin =
                context.resources.getDimensionPixelSize(R.dimen.com_braze_inappmessage_slideup_left_message_margin_no_image)
            slideupMessage.layoutParams = layoutParams
        }
    }

    /**
     * Applies the [WindowInsetsCompat] by ensuring any part of the slideup does not render in the cutout area.
     *
     * @param insets The [WindowInsetsCompat] object directly from
     * [androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener].
     */
    override fun applyWindowInsets(insets: WindowInsetsCompat) {
        super.applyWindowInsets(insets)
        if (layoutParams == null || layoutParams !is MarginLayoutParams) {
            brazelog { "Close button view is null or not of the expected class. Not applying window insets." }
            return
        }

        // Offset the existing margin with whatever the inset margins safe area values are
        val layoutParams = layoutParams as MarginLayoutParams
        layoutParams.setMargins(
            getMaxSafeLeftInset(insets) + layoutParams.leftMargin,
            getMaxSafeTopInset(insets) + layoutParams.topMargin,
            getMaxSafeRightInset(insets) + layoutParams.rightMargin,
            getMaxSafeBottomInset(insets) + layoutParams.bottomMargin
        )
    }
}
