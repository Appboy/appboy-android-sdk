package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.InAppMessageModal;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;
import com.appboy.ui.inappmessage.IInAppMessageImageView;
import com.appboy.ui.inappmessage.config.AppboyInAppMessageParams;
import com.appboy.ui.support.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class AppboyInAppMessageModalView extends AppboyInAppMessageImmersiveBaseView {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageModalView.class);
  private AppboyInAppMessageImageView mAppboyInAppMessageImageView;
  private InAppMessageModal mInAppMessage;

  public AppboyInAppMessageModalView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void applyInAppMessageParameters(Context context, InAppMessageModal inAppMessage) {
    mInAppMessage = inAppMessage;
    mAppboyInAppMessageImageView = findViewById(R.id.com_appboy_inappmessage_modal_imageview);
    setInAppMessageImageViewAttributes(context, inAppMessage, mAppboyInAppMessageImageView);
    resizeGraphicFrameIfAppropriate(context, inAppMessage);
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
    RelativeLayout imageLayout = findViewById(R.id.com_appboy_inappmessage_modal_image_layout);
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
    scrollViewChild.setOnClickListener(scrollView -> {
      AppboyLogger.d(TAG, "Passing scrollView click event to message clickable view.");
      getMessageClickableView().performClick();
    });
  }

  @Override
  public void setMessageBackgroundColor(int color) {
    InAppMessageViewUtils.setViewBackgroundColorFilter(findViewById(R.id.com_appboy_inappmessage_modal), color);
  }

  @Override
  public List<View> getMessageButtonViews(int numButtons) {
    List<View> buttonViews = new ArrayList<>();

    // Based on the number of buttons, make one of the button parent layouts visible
    if (numButtons == 1) {
      View singleButtonParent = findViewById(R.id.com_appboy_inappmessage_modal_button_layout_single);
      if (singleButtonParent != null) {
        singleButtonParent.setVisibility(VISIBLE);
      }

      View singleButton = findViewById(R.id.com_appboy_inappmessage_modal_button_single_one);
      if (singleButton != null) {
        buttonViews.add(singleButton);
      }
    } else if (numButtons == 2) {
      View dualButtonParent = findViewById(R.id.com_appboy_inappmessage_modal_button_layout_dual);
      if (dualButtonParent != null) {
        dualButtonParent.setVisibility(VISIBLE);
      }

      View dualButton1 = findViewById(R.id.com_appboy_inappmessage_modal_button_dual_one);
      View dualButton2 = findViewById(R.id.com_appboy_inappmessage_modal_button_dual_two);
      if (dualButton1 != null) {
        buttonViews.add(dualButton1);
      }
      if (dualButton2 != null) {
        buttonViews.add(dualButton2);
      }
    }
    return buttonViews;
  }

  @Override
  public TextView getMessageTextView() {
    return findViewById(R.id.com_appboy_inappmessage_modal_message);
  }

  @Override
  public TextView getMessageHeaderTextView() {
    return findViewById(R.id.com_appboy_inappmessage_modal_header_text);
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
    return findViewById(R.id.com_appboy_inappmessage_modal_icon);
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
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    resizeGraphicFrameIfAppropriate(this.getContext(), mInAppMessage);
  }

  /**
   * Programmatically set attributes on the image view classes inside the image ViewStubs.
   *
   * @param context
   * @param inAppMessage
   * @param inAppMessageImageView
   */
  private void setInAppMessageImageViewAttributes(Context context, IInAppMessageImmersive inAppMessage, IInAppMessageImageView inAppMessageImageView) {
    float pixelRadius = (float) ViewUtils.convertDpToPixels(context, AppboyInAppMessageParams.getModalizedImageRadiusDp());
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
   */
  private void resizeGraphicFrameIfAppropriate(final Context context, final InAppMessageModal inAppMessage) {
    if (inAppMessage == null || inAppMessage.getBitmap() == null) {
      return;
    }
    if (!inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC)) {
      return;
    }
    final double imageAspectRatio = (double) inAppMessage.getBitmap().getWidth() / inAppMessage.getBitmap().getHeight();
    Resources resources = context.getResources();
    final int marginPixels = resources.getDimensionPixelSize(R.dimen.com_appboy_in_app_message_modal_margin);
    final int maxModalWidth = resources.getDimensionPixelSize(R.dimen.com_appboy_in_app_message_modal_max_width);
    final int maxModalHeight = resources.getDimensionPixelSize(R.dimen.com_appboy_in_app_message_modal_max_height);

    // The measured width is only available after the draw phase, which
    // this runnable will draw after.
    this.post(() -> {
      double maxWidthPixelSize = Math.min(getMeasuredWidth() - marginPixels, maxModalWidth);
      double maxHeightPixelSize = Math.min(getMeasuredHeight() - marginPixels, maxModalHeight);
      double maxSizeAspectRatio = maxWidthPixelSize / maxHeightPixelSize;

      final View modalBoundView = findViewById(R.id.com_appboy_inappmessage_modal_graphic_bound);
      LayoutParams params = (LayoutParams) modalBoundView.getLayoutParams();
      if (imageAspectRatio >= maxSizeAspectRatio) {
        params.width = (int) maxWidthPixelSize;
        params.height = (int) (maxWidthPixelSize / imageAspectRatio);
      } else {
        params.width = (int) (maxHeightPixelSize * imageAspectRatio);
        params.height = (int) maxHeightPixelSize;
      }
      modalBoundView.setLayoutParams(params);
    });
  }
}
