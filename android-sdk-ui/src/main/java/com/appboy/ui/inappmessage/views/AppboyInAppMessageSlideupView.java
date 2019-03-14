package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.models.IInAppMessage;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;
import com.appboy.ui.support.ViewUtils;

public class AppboyInAppMessageSlideupView extends AppboyInAppMessageBaseView {
  private AppboyInAppMessageImageView mAppboyInAppMessageImageView;

  public AppboyInAppMessageSlideupView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void inflateStubViews(IInAppMessage inAppMessage) {
    mAppboyInAppMessageImageView = findViewById(R.id.com_appboy_inappmessage_slideup_imageview);
    mAppboyInAppMessageImageView.setInAppMessageImageCropType(inAppMessage.getCropType());
  }

  public void setMessageChevron(int color, ClickAction clickAction) {
    switch (clickAction) {
      case NONE:
        getMessageChevronView().setVisibility(View.GONE);
        break;
      default:
        InAppMessageViewUtils.setViewBackgroundColorFilter(getMessageChevronView(),
            color);
    }
  }

  @Override
  public void setMessageBackgroundColor(int color) {
    if (getMessageBackgroundObject().getBackground() instanceof GradientDrawable) {
      InAppMessageViewUtils.setViewBackgroundColorFilter(getMessageBackgroundObject(), color);
    } else {
      super.setMessageBackgroundColor(color);
    }
  }

  @Override
  public void resetMessageMargins(boolean imageRetrievalSuccessful) {
    super.resetMessageMargins(imageRetrievalSuccessful);

    boolean isIconBlank = getMessageIconView() == null || getMessageIconView().getText() == null || getMessageIconView().getText().length() == 0;
    if (!imageRetrievalSuccessful && isIconBlank) {
      // There's neither an icon nor an image
      // Remove the image container layout and reset our text's left margin
      RelativeLayout imageContainerLayout = findViewById(R.id.com_appboy_inappmessage_slideup_image_layout);
      if (imageContainerLayout != null) {
        ViewUtils.removeViewFromParent(imageContainerLayout);
      }

      // Reset the margin for the message
      TextView slideupMessage = findViewById(R.id.com_appboy_inappmessage_slideup_message);
      RelativeLayout.LayoutParams layoutParams = (LayoutParams) slideupMessage.getLayoutParams();
      layoutParams.leftMargin = getContext().getResources().getDimensionPixelSize(R.dimen.com_appboy_in_app_message_slideup_left_message_margin_no_image);
      slideupMessage.setLayoutParams(layoutParams);
    }
  }

  @Override
  public TextView getMessageTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_slideup_message);
  }

  @Override
  public View getMessageBackgroundObject() {
    return findViewById(R.id.com_appboy_inappmessage_slideup_container);
  }

  @Override
  public ImageView getMessageImageView() {
    return mAppboyInAppMessageImageView;
  }

  @Override
  public TextView getMessageIconView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_slideup_icon);
  }

  public View getMessageChevronView() {
    return findViewById(R.id.com_appboy_inappmessage_slideup_chevron);
  }
}
