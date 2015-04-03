package com.appboy.ui.inappmessage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.ui.R;

import java.util.ArrayList;
import java.util.List;

public class AppboyInAppMessageFullView extends AppboyInAppMessageImmersiveBaseView {

  public AppboyInAppMessageFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
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
    return (ImageView) findViewById(R.id.com_appboy_inappmessage_full_image);
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
