package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.ui.R;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

public class AppboyInAppMessageSlideupView extends AppboyInAppMessageBaseView {
  private ImageView mImageView;
  /**
   * @see AppboyInAppMessageBaseView#getMessageSimpleDraweeView()
   */
  private View mSimpleDraweeView;

  public AppboyInAppMessageSlideupView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void inflateStubViews() {
    if (mCanUseFresco) {
      mSimpleDraweeView = getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_slideup_drawee_stub);
      SimpleDraweeView castedSimpleDraweeView = (SimpleDraweeView) mSimpleDraweeView;

      // Since we can't set fresco attributes in the xml (inflation error), we'll do it here
      GenericDraweeHierarchy genericDraweeHierarchy = castedSimpleDraweeView.getHierarchy();
      genericDraweeHierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
    } else {
      mImageView = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_slideup_imageview_stub);
    }
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
    return mImageView;
  }

  @Override
  public View getMessageSimpleDraweeView() {
    return mSimpleDraweeView;
  }

  @Override
  public TextView getMessageIconView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_slideup_icon);
  }

  public View getMessageChevronView() {
    return findViewById(R.id.com_appboy_inappmessage_slideup_chevron);
  }
}
