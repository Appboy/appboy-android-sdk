package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import com.appboy.models.MessageButton;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class InAppMessageButtonViewUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(InAppMessageButtonViewUtils.class);
  private static final String[] REQUIRED_MATERIAL_DESIGN_BUTTON_CLASSES = {
      "com.google.android.material.button.MaterialButton"
  };

  /**
   * Handling only 1 state is ok here since the other button states default to enabled.
   */
  private static final int[][] MATERIAL_DESIGN_BUTTON_COLOR_STATE_LIST_STATES = {new int[]{android.R.attr.state_enabled}};

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
          .getDimensionPixelSize(R.dimen.com_appboy_in_app_message_button_border_stroke);
      final int strokeFocusedWidth = buttonView.getContext()
          .getResources()
          .getDimensionPixelSize(R.dimen.com_appboy_in_app_message_button_border_stroke_focused);
      if (messageButtons.size() <= i) {
        buttonView.setVisibility(View.GONE);
      } else {
        if (isMaterialDesignInClasspath() && buttonView instanceof MaterialButton) {
          setMaterialDesignButton((MaterialButton) buttonView, messageButton, strokeWidth);
        } else if (buttonView instanceof Button) {
          setStandardButton((Button) buttonView, messageButton, strokeWidth, strokeFocusedWidth);
        }
      }
    }
  }

  private static void setStandardButton(Button button, MessageButton messageButton, int strokeWidth, int strokeFocusedWidth) {
    AppboyLogger.v(TAG, "Using default Android button methods on in-app message");
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

  private static void setMaterialDesignButton(MaterialButton button, MessageButton messageButton, int strokeWidth) {
    AppboyLogger.v(TAG, "Using material design button methods on in-app message");
    // Material buttons come with ripple and stroke support out of the box
    // See https://github.com/material-components/material-components-android/blob/master/docs/components/MaterialButton.md#theme-attribute-mapping
    // for what fields should be used
    button.setText(messageButton.getText());
    button.setContentDescription(messageButton.getText());

    int[] borderColorArray = {messageButton.getBorderColor()};
    int[] backgroundColorArray = {messageButton.getBackgroundColor()};

    ColorStateList colorStateListBorder = new ColorStateList(MATERIAL_DESIGN_BUTTON_COLOR_STATE_LIST_STATES, borderColorArray);
    ColorStateList colorStateListBackground = new ColorStateList(MATERIAL_DESIGN_BUTTON_COLOR_STATE_LIST_STATES, backgroundColorArray);
    button.setStrokeWidth(strokeWidth);
    button.setStrokeColor(colorStateListBorder);
    button.setBackgroundTintList(colorStateListBackground);
    button.setBackgroundColor(messageButton.getBackgroundColor());
    button.setTextColor(messageButton.getTextColor());
  }

  /**
   * Checks if the views from Material Design (including {@link MaterialButton}) are on the classpath.
   */
  private static boolean isMaterialDesignInClasspath() {
    try {
      ClassLoader staticClassLoader = InAppMessageViewUtils.class.getClassLoader();
      for (String classPath : REQUIRED_MATERIAL_DESIGN_BUTTON_CLASSES) {
        if (Class.forName(classPath, false, staticClassLoader) == null) {
          // The class doesn't exist on the path
          return false;
        }
      }
    } catch (ClassNotFoundException c) {
      // This exception is expected so there's no
      // reason to log it
      return false;
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Caught error while checking for Material Design on the classpath", e);
      return false;
    }
    return true;
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
    Drawable buttonDrawable = getDrawable(context, R.drawable.com_appboy_inappmessage_button_background);
    buttonDrawable.mutate();

    GradientDrawable backgroundFillGradientDrawable;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // The drawable pulled from resources is a ripple drawable
      RippleDrawable rippleDrawable = (RippleDrawable) buttonDrawable;
      backgroundFillGradientDrawable = (GradientDrawable) rippleDrawable
          .findDrawableByLayerId(R.id.com_appboy_inappmessage_button_background_ripple_internal_gradient);
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
