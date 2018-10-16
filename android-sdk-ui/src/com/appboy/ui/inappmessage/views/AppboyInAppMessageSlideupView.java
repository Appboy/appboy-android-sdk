package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.models.IInAppMessage;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;

public class AppboyInAppMessageSlideupView extends AppboyInAppMessageBaseView {
  private AppboyInAppMessageImageView mAppboyInAppMessageImageView;

  public AppboyInAppMessageSlideupView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void inflateStubViews(IInAppMessage inAppMessage) {
    mAppboyInAppMessageImageView = (AppboyInAppMessageImageView) getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_slideup_imageview_stub);
    mAppboyInAppMessageImageView.setInAppMessageImageCropType(inAppMessage.getCropType());
  }

  public void setMessageChevron(int color, ClickAction clickAction) {
    switch (clickAction) {
      case NONE:
        getMessageChevronView().setVisibility(View.GONE);
        break;
      default:
        InAppMessageViewUtils.setViewBackgroundColorFilter(getMessageChevronView(),
            color, getContext().getResources().getColor(R.color.com_appboy_inappmessage_chevron));
    }
  }

  @Override
  public TextView getMessageTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_slideup_message);
  }

  @Override
  public View getMessageBackgroundObject() {
    return findViewById(R.id.com_appboy_inappmessage_slideup);
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
