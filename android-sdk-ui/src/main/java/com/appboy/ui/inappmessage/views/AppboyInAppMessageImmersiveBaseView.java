package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.TextView;

import com.appboy.enums.inappmessage.TextAlign;
import com.appboy.models.MessageButton;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.inappmessage.IInAppMessageImmersiveView;
import com.appboy.ui.support.ViewUtils;

import java.util.List;

public abstract class AppboyInAppMessageImmersiveBaseView extends AppboyInAppMessageBaseView implements IInAppMessageImmersiveView {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageImmersiveBaseView.class);

  public AppboyInAppMessageImmersiveBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Returns a list of all button views for this {@link IInAppMessageImmersiveView}. The default views
   * for in-app messages can contain multiple layouts depending on the number of buttons.
   *
   * @param numButtons The number of buttons used for this layout.
   */
  public abstract List<View> getMessageButtonViews(int numButtons);

  public void setMessageButtons(List<MessageButton> messageButtons) {
    // Change the weight sum if only a single button is present. This
    // makes the single button sit in the center of the parent view.
    int numButtons = messageButtons != null ? messageButtons.size() : 0;
    InAppMessageViewUtils.setButtons(getMessageButtonViews(numButtons), messageButtons);
  }

  public void setMessageCloseButtonColor(int color) {
    InAppMessageViewUtils.setViewBackgroundColorFilter(getMessageCloseButtonView(), color);
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

    setLargerCloseButtonClickArea(getMessageCloseButtonView());
  }

  public abstract View getFrameView();

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

  private void setLargerCloseButtonClickArea(final View target) {
    if (target == null || target.getParent() == null) {
      AppboyLogger.w(TAG, "Cannot increase click area for view if view and/or parent are null.");
      return;
    }

    if (target.getParent() instanceof View) {
      ((View) target.getParent()).post(new Runnable() {
        @Override
        public void run() {
          Rect delegateArea = new Rect();

          // The hit rectangle for the ImageButton
          target.getHitRect(delegateArea);

          // Extend the touch area of the ImageButton beyond its bounds
          int rightAndTopPadding = (int) ViewUtils.convertDpToPixels(getContext(), 15);
          int leftAndBottomPadding = (int) ViewUtils.convertDpToPixels(getContext(), 10);

          delegateArea.right += rightAndTopPadding;
          delegateArea.bottom += leftAndBottomPadding;
          delegateArea.left -= leftAndBottomPadding;
          delegateArea.top -= rightAndTopPadding;

          // Instantiate a TouchDelegate.
          // "delegateArea" is the bounds in local coordinates of
          // the containing view to be mapped to the delegate view.
          TouchDelegate touchDelegate = new TouchDelegate(delegateArea, target);

          // Sets the TouchDelegate on the parent view, such that touches
          // within the touch delegate bounds are routed to the child.
          ((View) target.getParent()).setTouchDelegate(touchDelegate);
        }
      });
    }
  }
}
