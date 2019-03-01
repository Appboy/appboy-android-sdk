package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.appboy.enums.inappmessage.TextAlign;
import com.appboy.models.MessageButton;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageImmersiveView;
import com.appboy.ui.support.ViewUtils;

import java.util.List;

public abstract class AppboyInAppMessageImmersiveBaseView extends AppboyInAppMessageBaseView implements IInAppMessageImmersiveView {

  public AppboyInAppMessageImmersiveBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public abstract List<View> getMessageButtonViews();

  public void setMessageButtons(List<MessageButton> messageButtons) {
    View buttonLayout = getMessageButtonsView();
    int backgroundColor = getContext().getResources().getColor(R.color.com_appboy_inappmessage_button_bg_light);
    InAppMessageViewUtils.setButtons(getMessageButtonViews(), buttonLayout, backgroundColor, messageButtons);
    InAppMessageViewUtils.resetButtonSizesIfNecessary(getMessageButtonViews(), messageButtons);
  }

  public void setMessageCloseButtonColor(int color) {
    InAppMessageViewUtils.setViewBackgroundColorFilter(getMessageCloseButtonView(),
        color, getContext().getResources().getColor(R.color.com_appboy_inappmessage_button_close_light));
  }

  public void setMessageHeaderTextColor(int color) {
    InAppMessageViewUtils.setTextViewColor(getMessageHeaderTextView(), color);
  }

  public void setMessageHeaderText(String text) {
    getMessageHeaderTextView().setText(text);
  }

  public void setMessageHeaderTextAlignment(TextAlign textAlign) {
    InAppMessageViewUtils.setTextAlignment(getMessageHeaderTextView(), textAlign);
  }

  public void setFrameColor(Integer color) {
    InAppMessageViewUtils.setFrameColor(getFrameView(), color);
  }

  public void resetMessageMargins() {
    boolean successful = false;
    if (getMessageImageView() != null && getMessageImageView().getDrawable() != null) {
      successful = true;
    }

    resetMessageMargins(successful);
  }

  @Override
  public void resetMessageMargins(boolean imageRetrievalSuccessful) {
    super.resetMessageMargins(imageRetrievalSuccessful);
    if (StringUtils.isNullOrBlank(getMessageTextView().getText().toString())) {
      ViewUtils.removeViewFromParent(getMessageTextView());
    }
    if (StringUtils.isNullOrBlank(getMessageHeaderTextView().getText().toString())) {
      ViewUtils.removeViewFromParent(getMessageHeaderTextView());
    }
    InAppMessageViewUtils.resetMessageMarginsIfNecessary(getMessageTextView(), getMessageHeaderTextView());
  }

  public abstract View getFrameView();

  public abstract View getMessageButtonsView();

  public abstract TextView getMessageTextView();

  public abstract TextView getMessageHeaderTextView();

  /**
   * Immersive messages can alternatively be closed by the back button.
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
