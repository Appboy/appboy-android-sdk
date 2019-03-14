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
   * @param buttonViews The destination views for the attributes found in the {@link MessageButton} objects.
   * @param messageButtons The {@link MessageButton} source objects.
   */
  public static void setButtons(List<View> buttonViews, List<MessageButton> messageButtons) {
    for (int i = 0; i < buttonViews.size(); i++) {
      if (messageButtons.size() <= i) {
        buttonViews.get(i).setVisibility(View.GONE);
      } else {
        if (buttonViews.get(i) instanceof Button) {
          // On supported API versions (21+), the button is a RippleDrawable with a GradientDrawable child
          // Below API 21, the button is just the GradientDrawable child
          Button button = (Button) buttonViews.get(i);
          MessageButton messageButton = messageButtons.get(i);
          button.setText(messageButton.getText());

          Drawable drawable = button.getBackground();
          if (drawable instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
            int strokeWidth = AppboyImageUtils.getPixelsFromDp(button.getContext(), 1);
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
}
