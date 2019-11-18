package com.appboy.ui.inappmessage.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.design.button.MaterialButton;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appboy.enums.inappmessage.TextAlign;
import com.appboy.models.MessageButton;
import com.appboy.support.AppboyImageUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

import java.util.List;

public class InAppMessageViewUtils {
  private static final String TAG = AppboyLogger.getAppboyLogTag(InAppMessageViewUtils.class);
  private static final String[] REQUIRED_MATERIAL_DESIGN_BUTTON_CLASSES = {
      "android.support.design.button.MaterialButton"
  };
  /**
   * Handling only 1 state is ok here since the other button states default to enabled.
   */
  private static final int[][] COLOR_STATE_LIST_STATES = new int[][]{new int[]{android.R.attr.state_enabled}};

  public static void setImage(Bitmap bitmap, ImageView imageView) {
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
    }
  }

  public static void setIcon(Context context, String icon, int iconColor, int iconBackgroundColor, TextView textView) {
    if (isValidIcon(icon)) {
      try {
        Typeface fontFamily = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        textView.setTypeface(fontFamily);
      } catch (Exception e) {
        AppboyLogger.e(TAG, "Caught exception setting icon typeface. Not rendering icon.", e);
        return;
      }
      textView.setText(icon);
      setTextViewColor(textView, iconColor);
      if (textView.getBackground() != null) {
        InAppMessageViewUtils.setDrawableColor(textView.getBackground(), iconBackgroundColor);
      } else {
        setViewBackgroundColor(textView, iconBackgroundColor);
      }
    }
  }

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

      final int strokeWidth = AppboyImageUtils.getPixelsFromDp(buttonView.getContext(), 1);
      if (messageButtons.size() <= i) {
        buttonView.setVisibility(View.GONE);
      } else {
        if (isMaterialDesignInClasspath() && buttonView instanceof MaterialButton) {
          AppboyLogger.v(TAG, "Using material design button methods on in-app message");
          // Material buttons come with ripple and stroke support out of the box
          // See https://github.com/material-components/material-components-android/blob/master/docs/components/MaterialButton.md#theme-attribute-mapping
          // for what fields should be used
          MaterialButton button = (MaterialButton) buttonView;
          button.setText(messageButton.getText());

          int[] borderColorArray = new int[]{messageButton.getBorderColor()};
          int[] backgroundColorArray = new int[]{messageButton.getBackgroundColor()};

          ColorStateList colorStateListBorder = new ColorStateList(COLOR_STATE_LIST_STATES, borderColorArray);
          ColorStateList colorStateListBackground = new ColorStateList(COLOR_STATE_LIST_STATES, backgroundColorArray);
          button.setStrokeWidth(strokeWidth);
          button.setStrokeColor(colorStateListBorder);
          button.setBackgroundTintList(colorStateListBackground);
          button.setBackgroundColor(messageButton.getBackgroundColor());
          button.setTextColor(messageButton.getTextColor());
        } else if (buttonView instanceof Button) {
          AppboyLogger.v(TAG, "Using default Android button methods on in-app message");
          // On supported API versions (21+), the button is a RippleDrawable with a GradientDrawable child
          // Below API 21, the button is just the GradientDrawable child
          Button button = (Button) buttonView;
          button.setText(messageButton.getText());

          Drawable drawable = button.getBackground();
          if (drawable instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
            // Mutating this drawable to guarantee that color changes to it
            // don't propagate to other instances of the button and be seen after
            // invalidating the in-app message view
            gradientDrawable.mutate();
            gradientDrawable.setStroke(strokeWidth, messageButton.getBorderColor());
            gradientDrawable.setColor(messageButton.getBackgroundColor());
          }

          // If above API 21, then change the actual drawable background to that of a RippleDrawable
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            safeSetLayerDrawableBackground(button);

            // The rounded corners in the background give a "shadow" that's actually a state list animator
            // See https://stackoverflow.com/questions/44527700/android-button-with-rounded-corners-ripple-effect-and-no-shadow
            button.setStateListAnimator(null);
          }
          setTextViewColor(button, messageButton.getTextColor());
        }
      }
    }
  }

  public static void setFrameColor(View view, Integer color) {
    if (color != null) {
      view.setBackgroundColor(color);
    }
  }

  public static void setTextViewColor(TextView textView, int color) {
    textView.setTextColor(color);
  }

  public static void setViewBackgroundColor(View view, int color) {
    view.setBackgroundColor(color);
  }

  public static void setViewBackgroundColorFilter(View view, @ColorInt int color) {
    view.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

    // The alpha needs to be set separately from the background color filter or else it won't apply
    view.getBackground().setAlpha(Color.alpha(color));
  }

  public static void setDrawableColor(Drawable drawable, int color) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (drawable instanceof LayerDrawable) {
        // This layer drawable should have the GradientDrawable as the 0th layer and the RippleDrawable as the 1st layer
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        if (layerDrawable.getNumberOfLayers() > 0 && layerDrawable.getDrawable(0) instanceof GradientDrawable) {
          setDrawableColor(layerDrawable.getDrawable(0), color);
        } else {
          AppboyLogger.d(TAG, "LayerDrawable for button background did not have the expected number of layers or the 0th layer was not a GradientDrawable.");
        }
      }
    }

    if (drawable instanceof GradientDrawable) {
      setDrawableColor((GradientDrawable) drawable, color);
    } else {
      drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
  }

  private static void setDrawableColor(GradientDrawable gradientDrawable, int color) {
    gradientDrawable.setColor(color);
  }

  public static boolean isValidIcon(String icon) {
    return icon != null;
  }

  protected static void resetMessageMarginsIfNecessary(TextView messageView, TextView headerView) {
    if (headerView == null && messageView != null) {
      // If header is not present but message is present, reset message margins to 0 (Typically, the message's has a top margin to accommodate the header.)
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(messageView.getLayoutParams().width, messageView.getLayoutParams().height);
      layoutParams.setMargins(0, 0, 0, 0);
      messageView.setLayoutParams(layoutParams);
    }
  }

  public static void closeInAppMessageOnKeycodeBack() {
    AppboyLogger.d(TAG, "Back button intercepted by in-app message view, closing in-app message.");
    AppboyInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true);
  }

  public static void setTextAlignment(TextView textView, TextAlign textAlign) {
    if (textAlign.equals(TextAlign.START)) {
      textView.setGravity(Gravity.START);
    } else if (textAlign.equals(TextAlign.END)) {
      textView.setGravity(Gravity.END);
    } else if (textAlign.equals(TextAlign.CENTER)) {
      textView.setGravity(Gravity.CENTER);
    }
  }

  /**
   * Sets a {@link LayerDrawable} as the background of the button. Any existing background is set
   * as the singular child of the new {@link LayerDrawable} background.
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static void safeSetLayerDrawableBackground(Button button) {
    ColorStateList ripplePressedColor = ColorStateList.valueOf(button.getContext().getResources().getColor(R.color.com_appboy_inappmessage_button_ripple));
    RippleDrawable rippleDrawable = new RippleDrawable(ripplePressedColor, null, button.getBackground());

    // Get the existing drawable background
    // Make that the "child" of the ripple
    LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{button.getBackground(), rippleDrawable});
    button.setBackground(layerDrawable);
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
}
