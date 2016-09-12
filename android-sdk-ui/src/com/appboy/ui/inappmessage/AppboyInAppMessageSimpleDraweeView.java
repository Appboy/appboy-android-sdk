package com.appboy.ui.inappmessage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.appboy.Constants;
import com.appboy.enums.inappmessage.CropType;
import com.appboy.support.AppboyLogger;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Extends SimpleDraweeView with the ability to clip the view's corners by a defined radius on all
 * image types.
 */
public class AppboyInAppMessageSimpleDraweeView extends SimpleDraweeView implements IInAppMessageImageView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageSimpleDraweeView.class.getName());
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
   * Array of 8 values, 4 pairs of [X,Y] radii.  Each corner receives
   * two radius values [X, Y]. The corners are ordered top-left, top-right,
   * bottom-right, bottom-left
   */
  private float[] mInAppRadii;

  public AppboyInAppMessageSimpleDraweeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mClipPath = new Path();
    mRect = new RectF();
  }

  /**
   * See {@link IInAppMessageImageView#setCornersRadiiPx(float, float, float, float)}
   */
  @Override
  public void setCornersRadiiPx(float topLeft, float topRight, float bottomLeft, float bottomRight) {
    float[] inappRadii = new float[]{
        topLeft, topLeft,
        topRight, topRight,
        bottomLeft, bottomLeft,
        bottomRight, bottomRight
    };
    mInAppRadii = inappRadii;
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
      getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
    } else if (cropType.equals(CropType.CENTER_CROP)) {
      getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    clipCanvasToPath(canvas, getWidth(), getHeight());
    super.onDraw(canvas);
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
}