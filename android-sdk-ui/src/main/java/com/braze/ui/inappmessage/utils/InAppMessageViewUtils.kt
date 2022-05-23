package com.braze.ui.inappmessage.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.braze.enums.inappmessage.TextAlign
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.BrazeInAppMessageManager

object InAppMessageViewUtils {
    @JvmStatic
    fun setImage(bitmap: Bitmap?, imageView: ImageView) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        }
    }

    @JvmStatic
    fun setIcon(
        context: Context,
        icon: String?,
        iconColor: Int,
        iconBackgroundColor: Int,
        textView: TextView
    ) {
        if (icon != null) {
            try {
                textView.typeface =
                    Typeface.createFromAsset(context.assets, "fontawesome-webfont.ttf")
            } catch (e: Exception) {
                brazelog(E, e) { "Caught exception setting icon typeface. Not rendering icon." }
                return
            }
            textView.text = icon
            setTextViewColor(textView, iconColor)
            if (textView.background != null) {
                setDrawableColor(textView.background, iconBackgroundColor)
            } else {
                setViewBackgroundColor(textView, iconBackgroundColor)
            }
        }
    }

    @JvmStatic
    fun setFrameColor(view: View, color: Int?) {
        color?.let { view.setBackgroundColor(it) }
    }

    @JvmStatic
    fun setTextViewColor(textView: TextView, color: Int) {
        textView.setTextColor(color)
    }

    @JvmStatic
    fun setViewBackgroundColor(view: View, color: Int) {
        view.setBackgroundColor(color)
    }

    @JvmStatic
    fun setViewBackgroundColorFilter(view: View, @ColorInt color: Int) {
        setDrawableColorFilter(view.background, color)

        // The alpha needs to be set separately from the background color filter or else it won't apply
        view.background.alpha = Color.alpha(color)
    }

    @JvmStatic
    fun setDrawableColor(drawable: Drawable, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable is LayerDrawable) {
                // This layer drawable should have the GradientDrawable as the
                // 0th layer and the RippleDrawable as the 1st layer
                if (drawable.numberOfLayers > 0 && drawable.getDrawable(0) is GradientDrawable) {
                    setDrawableColor(drawable.getDrawable(0), color)
                } else {
                    brazelog {
                        "LayerDrawable for button background did not have the expected " +
                            "number of layers or the 0th layer was not a GradientDrawable."
                    }
                }
            }
        }
        if (drawable is GradientDrawable) {
            drawable.setColor(color)
        } else {
            setDrawableColorFilter(drawable, color)
        }
    }

    @JvmStatic
    fun resetMessageMarginsIfNecessary(messageView: TextView?, headerView: TextView?) {
        if (headerView == null && messageView != null) {
            // If header is not present but message is present, reset message margins to 0
            // Typically, the message's has a top margin to accommodate the header.
            val layoutParams =
                LinearLayout.LayoutParams(messageView.layoutParams.width, messageView.layoutParams.height)
            layoutParams.setMargins(0, 0, 0, 0)
            messageView.layoutParams = layoutParams
        }
    }

    @JvmStatic
    fun closeInAppMessageOnKeycodeBack() {
        brazelog { "Back button intercepted by in-app message view, closing in-app message." }
        BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true)
    }

    @JvmStatic
    fun setTextAlignment(textView: TextView, textAlign: TextAlign) {
        if (textAlign == TextAlign.START) {
            textView.gravity = Gravity.START
        } else if (textAlign == TextAlign.END) {
            textView.gravity = Gravity.END
        } else if (textAlign == TextAlign.CENTER) {
            textView.gravity = Gravity.CENTER
        }
    }

    @Suppress("deprecation")
    private fun setDrawableColorFilter(drawable: Drawable, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}
