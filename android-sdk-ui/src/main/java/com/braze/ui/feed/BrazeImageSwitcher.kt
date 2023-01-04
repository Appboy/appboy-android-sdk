package com.braze.ui.feed

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageSwitcher
import androidx.annotation.VisibleForTesting
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.R

class BrazeImageSwitcher(context: Context, attrs: AttributeSet?) : ImageSwitcher(context, attrs) {
    @set:VisibleForTesting
    var readIcon: Drawable? = null

    @set:VisibleForTesting
    var unReadIcon: Drawable? = null

    init {
        try {
            // Get the array of offset indices into the R value array defined for this view.
            // The R value array is at R.styleable.com_braze_ui_feed_BrazeImageSwitcher.
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.com_braze_ui_feed_BrazeImageSwitcher)

            // For all offsets defined on this view, if the offset is equal to the offset for the custom font file
            // defined at R.styleable.com_braze_ui_feed_BrazeImageSwitcher_brazeFeedCustomReadIcon or
            // R.styleable.com_braze_ui_feed_BrazeImageSwitcher_brazeFeedCustomUnReadIcon,
            // instruct the typed array to retrieve the data at that offset.
            for (i in 0 until typedArray.indexCount) {
                val offset = typedArray.getIndex(i)
                if (offset == R.styleable.com_braze_ui_feed_BrazeImageSwitcher_brazeFeedCustomReadIcon) {
                    val drawable = typedArray.getDrawable(offset)
                    if (drawable != null) {
                        readIcon = drawable
                    }
                } else if (typedArray.getIndex(i) == R.styleable.com_braze_ui_feed_BrazeImageSwitcher_brazeFeedCustomUnReadIcon) {
                    val drawable = typedArray.getDrawable(offset)
                    if (drawable != null) {
                        unReadIcon = drawable
                    }
                }
            }
            typedArray.recycle()
        } catch (e: Exception) {
            brazelog(W, e) { "Error while checking for custom drawable." }
        }
    }
}
