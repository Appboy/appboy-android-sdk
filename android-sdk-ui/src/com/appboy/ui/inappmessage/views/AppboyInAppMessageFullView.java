package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.ui.R;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

public class AppboyInAppMessageFullView extends AppboyInAppMessageImmersiveBaseView {
  private ImageView mImageView;
  /**
   * @see AppboyInAppMessageBaseView#getMessageSimpleDraweeView()
   */
  private View mSimpleDraweeView;

  public AppboyInAppMessageFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void inflateStubViews() {
    if (mCanUseFresco) {
      mSimpleDraweeView = getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_full_drawee_stub);
      SimpleDraweeView castedSimpleDraweeView = (SimpleDraweeView) mSimpleDraweeView;

      // Since we can't set fresco attributes in the xml (inflation error), we'll do it here
      GenericDraweeHierarchy genericDraweeHierarchy = castedSimpleDraweeView.getHierarchy();
      genericDraweeHierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
    } else {
      mImageView = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_full_imageview_stub);
      mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
      mImageView.setAdjustViewBounds(true);
    }
  }

  public List<View> getMessageButtonViews() {
    List<View> buttonViews = new ArrayList<View>();
    if (findViewById(R.id.com_appboy_inappmessage_full_button_one) != null) {
      buttonViews.add(findViewById(R.id.com_appboy_inappmessage_full_button_one));
    }
    if (findViewById(R.id.com_appboy_inappmessage_full_button_two) != null) {
      buttonViews.add(findViewById(R.id.com_appboy_inappmessage_full_button_two));
    }
    return buttonViews;
  }

  public View getMessageButtonsView() {
    return findViewById(R.id.com_appboy_inappmessage_full_button_layout);
  }

  public TextView getMessageTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_full_message);
  }

  public TextView getMessageHeaderTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_full_header_text);
  }

  @Override
  public View getMessageCloseButtonView() {
    return findViewById(R.id.com_appboy_inappmessage_full_close_button);
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
    return null;
  }

  @Override
  public Object getMessageBackgroundObject() {
    return findViewById(R.id.com_appboy_inappmessage_full);
  }
}
