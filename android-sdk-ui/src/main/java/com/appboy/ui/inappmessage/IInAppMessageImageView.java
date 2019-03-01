package com.appboy.ui.inappmessage;

import com.appboy.enums.inappmessage.CropType;

/**
 * IInAppMessageImageView is a unifying interface for {@link android.view.View} implementations
 * that hold in-app message images, defining the required radius and cropping behavior for in-app
 * messages images.
 *
 * All Known Implementing Classes:
 * {@link AppboyInAppMessageImageView}
 * {@link AppboyInAppMessageSimpleDraweeView}
 */
public interface IInAppMessageImageView {

  /**
   * Instruct the view to use the given radii for its corners.
   *
   * @param topLeft top-left corner radius in px
   * @param topRight top-right corner radius in px
   * @param bottomLeft bottom-left corner radius in px
   * @param bottomRight bottom-right corner radius in px
   */
  void setCornersRadiiPx(float topLeft, float topRight, float bottomLeft, float bottomRight);

  /**
   * Instruct the view to use the given radius for its corners.
   *
   * @param cornersRadius radius for all corners in px
   */
  void setCornersRadiusPx(float cornersRadius);

  /**
   * Instruct the view to use {@link android.widget.ImageView.ScaleType#CENTER_CROP} or equivalent.
   */
  void setInAppMessageImageCropType(CropType cropType);
}