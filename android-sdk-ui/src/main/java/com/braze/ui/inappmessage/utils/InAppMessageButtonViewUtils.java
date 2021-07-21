package com.braze.ui.inappmessage.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import com.appboy.ui.R;
import com.braze.models.inappmessage.MessageButton;

import java.util.List;

public class InAppMessageButtonViewUtils {
  /**
   * Sets the appropriate colors for the button text, background, and border.
   *
   * @param buttonViews    The destination views for the attributes found in the {@link MessageButton} objects.
   * @param messageButtons The {@link MessageButton} source objects.
   */
  public static void setButtons(List<View> buttonViews, List<MessageButton> messageButtons) {
    for (int i = 0; i < buttonViews.size(); i++) {
      View buttonView = buttonViews.get(i);
      MessageButton messageButton = messageButtons.get(i);

      final int strokeWidth = buttonView.getContext()
          .getResources()
          .getDimensionPixelSize(R.dimen.com_braze_inappmessage_button_border_stroke);
      final int strokeFocusedWidth = buttonView.getContext()
          .getResources()
          .getDimensionPixelSize(R.dimen.com_braze_inappmessage_button_border_stroke_focused);
      if (messageButtons.size() <= i) {
        buttonView.setVisibility(View.GONE);
      } else {
        if (buttonView instanceof Button) {
          setButton((Button) buttonView, messageButton, strokeWidth, strokeFocusedWidth);
        }
      }
    }
  }

  private static void setButton(Button button, MessageButton messageButton, int strokeWidth, int strokeFocusedWidth) {
    button.setText(messageButton.getText());
    button.setContentDescription(messageButton.getText());
    InAppMessageViewUtils.setTextViewColor(button, messageButton.getTextColor());

    // StateListDrawable is the background, holding everything else
    StateListDrawable stateListDrawableBackground = new StateListDrawable();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // The rounded corners in the background give a "shadow" that's actually a state list animator
      // See https://stackoverflow.com/questions/44527700/android-button-with-rounded-corners-ripple-effect-and-no-shadow
      button.setStateListAnimator(null);
    }

    Drawable defaultButtonDrawable = getButtonDrawable(button.getContext(), messageButton, strokeWidth, strokeFocusedWidth, false);
    Drawable focusedButtonDrawable = getButtonDrawable(button.getContext(), messageButton, strokeWidth, strokeFocusedWidth, true);

    // The focused state MUST be added before the enabled state to work properly
    stateListDrawableBackground.addState(new int[] { android.R.attr.state_focused }, focusedButtonDrawable);
    stateListDrawableBackground.addState(new int[] { android.R.attr.state_enabled }, defaultButtonDrawable);
    button.setBackground(stateListDrawableBackground);
  }

  @SuppressWarnings("deprecation") // getDrawable() is deprecated but the alternatives are above our min SDK version Build.VERSION_CODES.JELLY_BEAN
  private static Drawable getDrawable(Context context, int drawableId) {
    return context.getResources().getDrawable(drawableId);
  }

  private static Drawable getButtonDrawable(Context context,
                                            MessageButton messageButton,
                                            int strokeWidth,
                                            int strokeFocusedWidth,
                                            boolean isFocused) {
    Drawable buttonDrawable = getDrawable(context, R.drawable.com_braze_inappmessage_button_background);
    buttonDrawable.mutate();

    GradientDrawable backgroundFillGradientDrawable;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // The drawable pulled from resources is a ripple drawable
      RippleDrawable rippleDrawable = (RippleDrawable) buttonDrawable;
      backgroundFillGradientDrawable = (GradientDrawable) rippleDrawable
          .findDrawableByLayerId(R.id.com_braze_inappmessage_button_background_ripple_internal_gradient);
    } else {
      // It's just the GradientDrawable as the only element since no ripple exists
      backgroundFillGradientDrawable = (GradientDrawable) buttonDrawable;
    }

    if (isFocused) {
      strokeWidth = strokeFocusedWidth;
    }
    backgroundFillGradientDrawable.setStroke(strokeWidth, messageButton.getBorderColor());
    backgroundFillGradientDrawable.setColor(messageButton.getBackgroundColor());

    return buttonDrawable;
  }
}
