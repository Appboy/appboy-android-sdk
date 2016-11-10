package com.appboy.ui.inappmessage.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;
import com.appboy.ui.inappmessage.AppboyInAppMessageSimpleDraweeView;
import com.appboy.ui.inappmessage.IInAppMessageImageView;
import com.appboy.ui.inappmessage.config.AppboyInAppMessageParams;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.appboy.ui.support.ViewUtils;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

import java.util.ArrayList;
import java.util.List;

public class AppboyInAppMessageModalView extends AppboyInAppMessageImmersiveBaseView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageModalView.class.getName());
  private AppboyInAppMessageImageView mAppboyInAppMessageImageView;
  /**
   * @see AppboyInAppMessageBaseView#getMessageSimpleDraweeView()
   */
  private View mSimpleDraweeView;

  public AppboyInAppMessageModalView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void inflateStubViews(Activity activity, IInAppMessageImmersive inAppMessage) {
    if (mCanUseFresco) {
      mSimpleDraweeView = getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_modal_drawee_stub);
      AppboyInAppMessageSimpleDraweeView castedSimpleDraweeView = (AppboyInAppMessageSimpleDraweeView) mSimpleDraweeView;
      setInAppMessageImageViewAttributes(activity, inAppMessage, castedSimpleDraweeView);
    } else {
      mAppboyInAppMessageImageView = (AppboyInAppMessageImageView) getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_modal_imageview_stub);
      setInAppMessageImageViewAttributes(activity, inAppMessage, mAppboyInAppMessageImageView);
      if (inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC) && inAppMessage.getBitmap() != null) {
        double aspectRatio = (double) inAppMessage.getBitmap().getWidth() / inAppMessage.getBitmap().getHeight();
        resizeGraphicFrameIfAppropriate(activity, inAppMessage, aspectRatio);
      }
    }
  }

  public View getFrameView() {
    return findViewById(R.id.com_appboy_inappmessage_modal_frame);
  }

  @Override
  public void resetMessageMargins(boolean imageRetrievalSuccessful) {
    super.resetMessageMargins(imageRetrievalSuccessful);
    // If the in-app message contains an image or icon, reset the image layout's margins to 0.
    // When there is no image or icon present, the layout has a top margin of 20 to create 20dp
    // of padding between the text content and the top of the message.
    RelativeLayout imageLayout = (RelativeLayout) findViewById(R.id.com_appboy_inappmessage_modal_image_layout);
    if (imageRetrievalSuccessful || getMessageIconView() != null) {
      if (imageLayout != null) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 0);
        imageLayout.setLayoutParams(layoutParams);
      }
    }

    // Make scrollView pass click events to message clickable view, so that clicking on the scrollView
    // dismisses the in-app message.
    View scrollViewChild = findViewById(R.id.com_appboy_inappmessage_modal_text_layout);
    scrollViewChild.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View scrollView) {
        AppboyLogger.d(TAG, "Passing scrollView click event to message clickable view.");
        getMessageClickableView().performClick();
      }
    });
  }

  @Override
  public void setMessageBackgroundColor(int color) {
    InAppMessageViewUtils.setViewBackgroundColorFilter(findViewById(R.id.com_appboy_inappmessage_modal),
        color, getContext().getResources().getColor(R.color.com_appboy_inappmessage_background_light));
  }

  @Override
  public List<View> getMessageButtonViews() {
    List<View> buttonViews = new ArrayList<View>();
    if (findViewById(R.id.com_appboy_inappmessage_modal_button_one) != null) {
      buttonViews.add(findViewById(R.id.com_appboy_inappmessage_modal_button_one));
    }
    if (findViewById(R.id.com_appboy_inappmessage_modal_button_two) != null) {
      buttonViews.add(findViewById(R.id.com_appboy_inappmessage_modal_button_two));
    }
    return buttonViews;
  }

  @Override
  public View getMessageButtonsView() {
    return findViewById(R.id.com_appboy_inappmessage_modal_button_layout);
  }

  @Override
  public TextView getMessageTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_modal_message);
  }

  @Override
  public TextView getMessageHeaderTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_modal_header_text);
  }

  @Override
  public View getMessageClickableView() {
    return findViewById(R.id.com_appboy_inappmessage_modal);
  }

  @Override
  public View getMessageCloseButtonView() {
    return findViewById(R.id.com_appboy_inappmessage_modal_close_button);
  }

  @Override
  public TextView getMessageIconView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_modal_icon);
  }

  @Override
  public Drawable getMessageBackgroundObject() {
    return getMessageClickableView().getBackground();
  }

  @Override
  public ImageView getMessageImageView() {
    return mAppboyInAppMessageImageView;
  }

  @Override
  public View getMessageSimpleDraweeView() {
    return mSimpleDraweeView;
  }

  public void setMessageSimpleDrawee(final IInAppMessageImmersive inAppMessage, final Activity activity) {
    if (inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC)) {
      ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
          if (imageInfo == null) {
            return;
          }
          final double imageAspectRatio = (double) imageInfo.getWidth() / imageInfo.getHeight();

          // If necessary, resize the graphic modal frame once the image aspect ratio is known.
          mSimpleDraweeView.post(new Runnable() {
            @Override
            public void run() {
              resizeGraphicFrameIfAppropriate(activity, inAppMessage, imageAspectRatio);
            }
          });
        }
      };
      FrescoLibraryUtils.setDraweeControllerHelper((AppboyInAppMessageSimpleDraweeView) getMessageSimpleDraweeView(), getAppropriateImageUrl(inAppMessage), 0f, false, controllerListener);
    } else {
      setMessageSimpleDrawee(inAppMessage);
    }
  }

  /**
   * Programmatically set attributes on the image view classes inside the image ViewStubs in a
   * fresco/native-agnostic manner.
   *
   * @param activity
   * @param inAppMessage
   * @param inAppMessageImageView
   */
  private void setInAppMessageImageViewAttributes(Activity activity, IInAppMessageImmersive inAppMessage, IInAppMessageImageView inAppMessageImageView) {
    float pixelRadius = (float) ViewUtils.convertDpToPixels(activity, AppboyInAppMessageParams.getModalizedImageRadiusDp());
    if (inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC)) {
      inAppMessageImageView.setCornersRadiusPx(pixelRadius);
    } else {
      inAppMessageImageView.setCornersRadiiPx(pixelRadius, pixelRadius, 0.0f, 0.0f);
    }
    inAppMessageImageView.setInAppMessageImageCropType(inAppMessage.getCropType());
  }

  /**
   * If displaying a graphic modal, resize its bounds based on the aspect ratio of the input image
   * and its maximum size.
   *
   * @param activity
   * @param inAppMessage
   * @param imageAspectRatio the aspect ratio of the image to be displayed in the graphic modal.
   */
  private void resizeGraphicFrameIfAppropriate(Activity activity, IInAppMessageImmersive inAppMessage, double imageAspectRatio) {
    if (!inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC)) {
      return;
    }
    double maxWidthDp = AppboyInAppMessageParams.getGraphicModalMaxWidthDp();
    double maxHeightDp = AppboyInAppMessageParams.getGraphicModalMaxHeightDp();
    double maxSizeAspectRatio = maxWidthDp / maxHeightDp;
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.com_appboy_inappmessage_modal_graphic_bound).getLayoutParams();
    if (imageAspectRatio >= maxSizeAspectRatio) {
      params.width = (int) ViewUtils.convertDpToPixels(activity, maxWidthDp);
      params.height = (int) (ViewUtils.convertDpToPixels(activity, maxWidthDp) / imageAspectRatio);
    } else {
      params.width = (int) (ViewUtils.convertDpToPixels(activity, maxHeightDp) * imageAspectRatio);
      params.height = (int) ViewUtils.convertDpToPixels(activity, maxHeightDp);
    }
    findViewById(R.id.com_appboy_inappmessage_modal_graphic_bound).setLayoutParams(params);
  }
}