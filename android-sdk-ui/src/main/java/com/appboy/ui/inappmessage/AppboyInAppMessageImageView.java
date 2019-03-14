package com.appboy.ui.inappmessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.appboy.enums.inappmessage.CropType;
import com.appboy.support.AppboyLogger;

/**
 * Extends ImageView with the ability to clip the view's corners by a defined radius on all image
 * types.
 */
@SuppressLint("AppCompatCustomView")
public class AppboyInAppMessageImageView extends ImageView implements IInAppMessageImageView {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageImageView.class);
  /**
   * Clip path that will be set to a closed round-rectangle contour based on the radii in
   * {@link #mInAppRadii} and used to clip the image view.
   */
  private Path mClipPath;
  /**
   * Represents the dimensions of the image view which will be used to create the clip path.
   */
  private RectF mRect;
  /**
   * Array of 8 values, 4 pairs of [X,Y] radii. Each corner receives
   * two radius values [X, Y]. The corners are ordered top-left, top-right,
   * bottom-right, bottom-left
   */
  private float[] mInAppRadii;
  private float mAspectRatio = -1f;
  private boolean mSetToHalfParentHeight = false;

  public AppboyInAppMessageImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mClipPath = new Path();
    mRect = new RectF();
    // The view bounds need to be adjusted in order to scale to the full width available
    setAdjustViewBounds(true);
  }

  /**
   * See {@link IInAppMessageImageView#setCornersRadiiPx(float, float, float, float)}
   */
  @Override
  public void setCornersRadiiPx(float topLeft, float topRight, float bottomLeft, float bottomRight) {
    mInAppRadii = new float[]{
        topLeft, topLeft,
        topRight, topRight,
        bottomLeft, bottomLeft,
        bottomRight, bottomRight
    };
  }

  /**
   * See {@link IInAppMessageImageView#setCornersRadiusPx(float)}
   */
  @Override
  public void setCornersRadiusPx(float cornersRadius) {
    setCornersRadiiPx(cornersRadius, cornersRadius, cornersRadius, cornersRadius);
  }

  /**
   * See {@link IInAppMessageImageView#setInAppMessageImageCropType(CropType)}
   */
  @Override
  public void setInAppMessageImageCropType(CropType cropType) {
    if (cropType.equals(CropType.FIT_CENTER)) {
      setScaleType(ImageView.ScaleType.FIT_CENTER);
    } else if (cropType.equals(CropType.CENTER_CROP)) {
      setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
  }

  @Override
  public void setAspectRatio(float aspectRatio) {
    mAspectRatio = aspectRatio;
    requestLayout();
  }

  @Override
  public void setToHalfParentHeight(boolean setToHalfHeight) {
    mSetToHalfParentHeight = setToHalfHeight;
    requestLayout();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    clipCanvasToPath(canvas, getWidth(), getHeight());
    super.onDraw(canvas);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (mAspectRatio != -1) {
      int newWidth = getMeasuredWidth();
      int maxHeight = (int) (newWidth / mAspectRatio);
      // The +1 is necessary to ensure that the image hits the full width of the modal container.
      // Otherwise, images will have some "margin" on the left and right.
      int newHeight = Math.min(getMeasuredHeight(), maxHeight) + 1;
      setMeasuredDimension(newWidth, newHeight);
    }

    if (mSetToHalfParentHeight) {
      int parentHeight = ((View) getParent()).getHeight();
      setMeasuredDimension(getMeasuredWidth(), (int) (parentHeight * 0.5));
    }
  }

  /**
   * Clips the input canvas to a rounded rectangle of the specified width and height, using the
   * radii set in {@link #setCornersRadiiPx(float, float, float, float)}
   *
   * @param canvas the canvas to be clipped
   * @param widthPx
   * @param heightPx
   * @return whether the canvas was successfully clipped
   */
  boolean clipCanvasToPath(Canvas canvas, int widthPx, int heightPx) {
    if (mInAppRadii != null) {
      try {
        mClipPath.reset();
        mRect.set(0.0f, 0.0f, widthPx, heightPx);
        mClipPath.addRoundRect(mRect, mInAppRadii, Path.Direction.CW);
        canvas.clipPath(mClipPath);
        return true;
      } catch (Exception e) {
        AppboyLogger.e(TAG, "Encountered exception while trying to clip in-app message image", e);
        return false;
      }
    }
    return false;
  }

  // Utility package-private methods for unit tests
  void setClipPath(Path clipPath) {
    mClipPath = clipPath;
  }

  void setRectf(RectF rectF) {
    mRect = rectF;
  }

  Path getClipPath() {
    return mClipPath;
  }

  RectF getRectf() {
    return mRect;
  }

  float[] getInAppRadii() {
    return mInAppRadii;
  }
}
