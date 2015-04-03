package com.appboy.ui.inappmessage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.ui.R;

public class AppboyInAppMessageSlideupView extends AppboyInAppMessageBaseView {

  public AppboyInAppMessageSlideupView(Context context, AttributeSet attrs) {
    super(context, attrs);
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
    return (ImageView) findViewById(R.id.com_appboy_inappmessage_slideup_image);
  }

  @Override
  public TextView getMessageIconView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_slideup_icon);
  }

  public View getMessageChevronView() {
    return findViewById(R.id.com_appboy_inappmessage_slideup_chevron);
  }
}
