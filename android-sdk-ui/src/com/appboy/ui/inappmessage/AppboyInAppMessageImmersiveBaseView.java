package com.appboy.ui.inappmessage;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.models.MessageButton;
import com.appboy.ui.R;
import com.appboy.ui.support.StringUtils;
import com.appboy.ui.support.ViewUtils;

import java.util.List;

public abstract class AppboyInAppMessageImmersiveBaseView extends AppboyInAppMessageBaseView implements IInAppMessageImmersiveView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageImmersiveBaseView.class.getName());

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

  public void resetMessageMargins() {
    super.resetMessageMargins();
    if (StringUtils.isNullOrBlank(getMessageTextView().getText().toString())) {
      ViewUtils.removeViewFromParent(getMessageTextView());
    }
    if (StringUtils.isNullOrBlank(getMessageHeaderTextView().getText().toString())) {
      ViewUtils.removeViewFromParent(getMessageHeaderTextView());
    }
    InAppMessageViewUtils.resetMessageMarginsIfNecessary(getMessageTextView(), getMessageHeaderTextView());
  }

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
    if(keyCode == KeyEvent.KEYCODE_BACK) {
      Log.d(TAG, "Back button intercepted by in-app message view, closing in-app message.");
      AppboyInAppMessageManager.getInstance().hideCurrentInAppMessage(true);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
