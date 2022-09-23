package com.braze.ui.inappmessage.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.braze.enums.inappmessage.CropType
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import kotlin.math.min

/**
 * Extends ImageView with the ability to clip the view's corners by a defined radius on all image
 * types.
 */
@SuppressLint("AppCompatCustomView")
class InAppMessageImageView(context: Context?, attrs: AttributeSet?) :
    ImageView(context, attrs),
    IInAppMessageImageView {
    /**
     * Clip path that will be set to a closed round-rectangle contour based on the radii in
     * [.inAppRadii] and used to clip the image view.
     */
    var clipPath: Path = Path()

    /**
     * Represents the dimensions of the image view which will be used to create the clip path.
     */
    var rectf: RectF = RectF()

    /**
     * Array of 8 values, 4 pairs of [X,Y] radii. Each corner receives
     * two radius values [X, Y]. The corners are ordered top-left, top-right,
     * bottom-right, bottom-left
     */
    lateinit var inAppRadii: FloatArray
        private set
    private var aspectRatio = -1f
    @Suppress("BooleanPropertyNaming")
    private var setToHalfParentHeight = false

    init {
        // The view bounds need to be adjusted in order to scale to the full width available
        adjustViewBounds = true
    }

    override fun setCornersRadiiPx(
        topLeft: Float,
        topRight: Float,
        bottomLeft: Float,
        bottomRight: Float
    ) {
        inAppRadii = floatArrayOf(
            topLeft, topLeft,
            topRight, topRight,
            bottomLeft, bottomLeft,
            bottomRight, bottomRight
        )
    }

    override fun setCornersRadiusPx(cornersRadius: Float) {
        setCornersRadiiPx(cornersRadius, cornersRadius, cornersRadius, cornersRadius)
    }

    override fun setInAppMessageImageCropType(cropType: CropType?) {
        if (cropType == CropType.FIT_CENTER) {
            scaleType = ScaleType.FIT_CENTER
        } else if (cropType == CropType.CENTER_CROP) {
            scaleType = ScaleType.CENTER_CROP
        }
    }

    override fun setAspectRatio(aspectRatio: Float) {
        this.aspectRatio = aspectRatio
        requestLayout()
    }

    override fun setToHalfParentHeight(setToHalfHeight: Boolean) {
        setToHalfParentHeight = setToHalfHeight
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        clipCanvasToPath(canvas, width, height)
        super.onDraw(canvas)
    }

    @Suppress("MagicNumber")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // If the View isn't large enough, don't set the aspect ratio. This will prevent Glide from
        // applying any images to a View with 1 pixel dimensions.
        if (aspectRatio != -1f && measuredHeight > 0 && measuredWidth > 0) {
            val newWidth = measuredWidth
            val maxHeight = (newWidth / aspectRatio).toInt()
            // The +1 is necessary to ensure that the image hits the full width of the modal container.
            // Otherwise, images will have some "margin" on the left and right.
            val newHeight = min(measuredHeight, maxHeight) + 1
            setMeasuredDimension(newWidth, newHeight)
        } else {
            setMeasuredDimension(measuredWidth, measuredHeight)
        }
        if (setToHalfParentHeight) {
            val parentHeight = (parent as View).height
            setMeasuredDimension(measuredWidth, (parentHeight * 0.5).toInt())
        }
    }

    /**
     * Clips the input canvas to a rounded rectangle of the specified width and height, using the
     * radii set in [setCornersRadiiPx].
     *
     * @param canvas the canvas to be clipped
     * @param widthPx
     * @param heightPx
     * @return whether the canvas was successfully clipped
     */
    fun clipCanvasToPath(canvas: Canvas, widthPx: Int, heightPx: Int): Boolean {
        return try {
            clipPath.reset()
            rectf[0.0f, 0.0f, widthPx.toFloat()] = heightPx.toFloat()
            clipPath.addRoundRect(rectf, inAppRadii, Path.Direction.CW)
            canvas.clipPath(clipPath)
            true
        } catch (e: Exception) {
            brazelog(E, e) { "Encountered exception while trying to clip in-app message image" }
            false
        }
    }
}
