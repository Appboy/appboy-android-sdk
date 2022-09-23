package com.braze.ui.inappmessage.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import com.braze.enums.inappmessage.TextAlign
import com.braze.models.inappmessage.IInAppMessageWithImage
import com.braze.support.BrazeLogger.Priority.D
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setIcon
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setImage
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setTextAlignment
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setTextViewColor
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setViewBackgroundColor
import com.braze.ui.support.removeViewFromParent
import java.io.File

abstract class InAppMessageBaseView(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs), IInAppMessageView {
    override val messageClickableView: View
        get() = this

    override var hasAppliedWindowInsets: Boolean = false

    abstract val messageTextView: TextView?
    abstract val messageImageView: ImageView?
    abstract val messageIconView: TextView?
    abstract val messageBackgroundObject: Any?

    open fun setMessageBackgroundColor(color: Int) {
        setViewBackgroundColor(messageBackgroundObject as View, color)
    }

    open fun setMessageTextColor(color: Int) {
        messageTextView?.let { setTextViewColor(it, color) }
    }

    open fun setMessageTextAlign(textAlign: TextAlign) {
        messageTextView?.let { setTextAlignment(it, textAlign) }
    }

    open fun setMessage(text: String) {
        messageTextView?.text = text
    }

    open fun setMessageImageView(bitmap: Bitmap) {
        messageImageView?.let { setImage(bitmap, it) }
    }

    open fun setMessageIcon(icon: String, iconColor: Int, iconBackgroundColor: Int) {
        messageIconView?.let { setIcon(context, icon, iconColor, iconBackgroundColor, it) }
    }

    open fun resetMessageMargins(imageRetrievalSuccessful: Boolean) {
        messageImageView?.let {
            if (!imageRetrievalSuccessful) {
                it.removeViewFromParent()
            } else {
                messageIconView.removeViewFromParent()
            }
        }
        if (messageIconView?.text?.toString()?.isBlank() == true) {
            messageIconView.removeViewFromParent()
        }
    }

    override fun applyWindowInsets(insets: WindowInsetsCompat) {
        hasAppliedWindowInsets = true
    }

    companion object {
        /**
         * @return return the local image Url, if present. Otherwise, return the remote image Url. Local
         * image Urls are Urls for images pre-fetched by the SDK for triggers.
         */
        @JvmStatic
        fun getAppropriateImageUrl(inAppMessage: IInAppMessageWithImage): String? {
            val localImagePath = inAppMessage.localImageUrl
            if (!localImagePath.isNullOrBlank()) {
                val imageFile = File(localImagePath)
                if (imageFile.exists()) {
                    return localImagePath
                } else {
                    brazelog(D) {
                        "Local bitmap file does not exist. Using remote url instead. Local path: $localImagePath"
                    }
                }
            }
            return inAppMessage.remoteImageUrl
        }
    }
}
