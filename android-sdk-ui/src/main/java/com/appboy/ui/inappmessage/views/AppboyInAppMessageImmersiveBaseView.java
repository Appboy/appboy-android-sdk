package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.appboy.enums.inappmessage.TextAlign;
import com.appboy.models.MessageButton;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.IInAppMessageImmersiveView;
import com.appboy.ui.support.ViewUtils;

import java.util.List;

public abstract class AppboyInAppMessageImmersiveBaseView extends AppboyInAppMessageBaseView implements IInAppMessageImmersiveView {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageImmersiveBaseView.class);

  public AppboyInAppMessageImmersiveBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
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

  @Override
  public void setupDirectionalNavigation(int numButtons) {
    // Buttons should focus to each other and the close button
    final List<View> messageButtonViews = getMessageButtonViews(numButtons);
    final View closeButton = getMessageCloseButtonView();
    final int closeButtonId = closeButton.getId();
    // If the user happens to leave touch mode while the IAM is already on-screen,
    // we need to specify what View will receive that initial focus.
    int defaultFocusId = closeButtonId;
    View defaultFocusView = closeButton;
    View primaryButton;
    View secondaryButton;
    int primaryId;
    int secondaryId;

    switch (numButtons) {
      case 2:
        primaryButton = messageButtonViews.get(1);
        secondaryButton = messageButtonViews.get(0);
        primaryId = primaryButton.getId();
        secondaryId = secondaryButton.getId();
        defaultFocusId = primaryId;
        defaultFocusView = primaryButton;

        // Primary points to close and secondary button
        primaryButton.setNextFocusLeftId(secondaryId);
        primaryButton.setNextFocusRightId(secondaryId);
        primaryButton.setNextFocusUpId(closeButtonId);
        primaryButton.setNextFocusDownId(closeButtonId);

        // Secondary also points to close and secondary button
        secondaryButton.setNextFocusLeftId(primaryId);
        secondaryButton.setNextFocusRightId(primaryId);
        secondaryButton.setNextFocusUpId(closeButtonId);
        secondaryButton.setNextFocusDownId(closeButtonId);

        // Close button points to primary, then secondary
        closeButton.setNextFocusUpId(primaryId);
        closeButton.setNextFocusDownId(primaryId);
        closeButton.setNextFocusRightId(primaryId);
        closeButton.setNextFocusLeftId(secondaryId);
        break;
      case 1:
        primaryButton = messageButtonViews.get(0);
        primaryId = primaryButton.getId();
        defaultFocusId = primaryId;
        defaultFocusView = primaryButton;

        // Primary points to close
        primaryButton.setNextFocusLeftId(closeButtonId);
        primaryButton.setNextFocusRightId(closeButtonId);
        primaryButton.setNextFocusUpId(closeButtonId);
        primaryButton.setNextFocusDownId(closeButtonId);

        // Close button points to primary
        closeButton.setNextFocusUpId(primaryId);
        closeButton.setNextFocusDownId(primaryId);
        closeButton.setNextFocusRightId(primaryId);
        closeButton.setNextFocusLeftId(primaryId);
        break;
      default:
        AppboyLogger.w(TAG, "Cannot setup directional navigation. Got unsupported number of buttons: " + numButtons);
    }

    // The entire view should focus back to the close
    // button and not allow for backwards navigation.
    this.setNextFocusUpId(defaultFocusId);
    this.setNextFocusDownId(defaultFocusId);
    this.setNextFocusRightId(defaultFocusId);
    this.setNextFocusLeftId(defaultFocusId);

    // Request focus for the default view
    defaultFocusView.requestFocus();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      defaultFocusView.setFocusedByDefault(true);
    }
  }

  public void setMessageButtons(@NonNull List<MessageButton> messageButtons) {
    InAppMessageButtonViewUtils.setButtons(getMessageButtonViews(messageButtons.size()), messageButtons);
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

  /**
   * Returns a list of all button views for this {@link IInAppMessageImmersiveView}. The default views
   * for in-app messages can contain multiple layouts depending on the number of buttons.
   *
   * @param numButtons The number of buttons used for this layout.
   */
  public abstract List<View> getMessageButtonViews(int numButtons);

  public abstract View getFrameView();

  public abstract TextView getMessageTextView();

  public abstract TextView getMessageHeaderTextView();

  /**
   * Immersive messages can alternatively be closed by the back button.
   *
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && AppboyInAppMessageManager.getInstance().getDoesBackButtonDismissInAppMessageView()) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  /**
   * Immersive messages can alternatively be closed by the back button.
   *
   * @return If the button pressed was the back button, close the in-app message
   * and return true to indicate that the event was handled.
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (!isInTouchMode() && event.getKeyCode() == KeyEvent.KEYCODE_BACK && AppboyInAppMessageManager.getInstance().getDoesBackButtonDismissInAppMessageView()) {
      InAppMessageViewUtils.closeInAppMessageOnKeycodeBack();
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  /**
   * Sets a rectangular click area for the close button. This is necessary to provide a larger click
   * area than the close button drawable and to ensure that the click area is not a mask of the drawable
   * and is instead an easy to tap rectangle.
   *
   * @param closeButtonView The close button view.
   */
  public void setLargerCloseButtonClickArea(final View closeButtonView) {
    if (closeButtonView == null || closeButtonView.getParent() == null) {
      AppboyLogger.w(TAG, "Cannot increase click area for view if view and/or parent are null.");
      return;
    }

    if (closeButtonView.getParent() instanceof View) {
      ((View) closeButtonView.getParent()).post(() -> {
        Rect delegateArea = new Rect();

        // The hit rectangle for the ImageButton
        closeButtonView.getHitRect(delegateArea);

        // Extend the touch area of the ImageButton beyond its bounds
        final int desiredCloseButtonClickAreaWidth = getContext().getResources().getDimensionPixelSize(R.dimen.com_appboy_in_app_message_close_button_click_area_width);
        final int desiredCloseButtonClickAreaHeight = getContext().getResources().getDimensionPixelSize(R.dimen.com_appboy_in_app_message_close_button_click_area_height);
        int extraHorizontalPadding = (desiredCloseButtonClickAreaWidth - delegateArea.width()) / 2;
        int extraVerticalPadding = (desiredCloseButtonClickAreaHeight - delegateArea.height()) / 2;

        delegateArea.top -= extraVerticalPadding;
        delegateArea.bottom += extraVerticalPadding;
        delegateArea.left -= extraHorizontalPadding;
        delegateArea.right += extraHorizontalPadding;

        // Instantiate a TouchDelegate.
        // "delegateArea" is the bounds in local coordinates of
        // the containing view to be mapped to the delegate view.
        TouchDelegate touchDelegate = new TouchDelegate(delegateArea, closeButtonView);

        // Sets the TouchDelegate on the parent view, such that touches
        // within the touch delegate bounds are routed to the child.
        ((View) closeButtonView.getParent()).setTouchDelegate(touchDelegate);
      });
    }
  }
}
