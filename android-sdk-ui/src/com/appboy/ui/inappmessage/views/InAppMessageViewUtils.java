package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.enums.inappmessage.TextAlign;
import com.appboy.models.MessageButton;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.support.ViewUtils;

import java.util.List;

public class InAppMessageViewUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, InAppMessageViewUtils.class.getName());

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
        InAppMessageViewUtils.setDrawableColor(textView.getBackground(), iconBackgroundColor, context.getResources().getColor(R.color.com_appboy_inappmessage_icon_background));
      } else {
        setViewBackgroundColor(textView, iconBackgroundColor);
      }
    }
  }

  public static void setButtons(List<View> buttonViews, View buttonLayoutView, int defaultColor, List<MessageButton> messageButtons) {
    if (messageButtons == null || messageButtons.size() == 0) {
      ViewUtils.removeViewFromParent(buttonLayoutView);
      return;
    }
    for (int i = 0; i < buttonViews.size(); i++) {
      if (messageButtons.size() <= i) {
        buttonViews.get(i).setVisibility(View.GONE);
      } else {
        if (buttonViews.get(i) instanceof Button) {
          Button button = (Button) buttonViews.get(i);
          MessageButton messageButton = messageButtons.get(i);
          button.setText(messageButton.getText());
          setTextViewColor(button, messageButton.getTextColor());
          setDrawableColor(button.getBackground(), messageButton.getBackgroundColor(), defaultColor);
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
    if (isValidInAppMessageColor(color)) {
      textView.setTextColor(color);
    }
  }

  public static void setViewBackgroundColor(View view, int color) {
    if (isValidInAppMessageColor(color)) {
      view.setBackgroundColor(color);
    }
  }

  public static void setViewBackgroundColorFilter(View view, int color, int defaultColor) {
    if (isValidInAppMessageColor(color)) {
      view.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    } else {
      view.getBackground().setColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY);
    }
  }

  public static void setDrawableColor(Drawable drawable, int color, int defaultColor) {
    if (drawable instanceof GradientDrawable) {
      setDrawableColor((GradientDrawable) drawable, color, defaultColor);
    } else if (isValidInAppMessageColor(color)) {
      drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    } else {
      drawable.setColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY);
    }
  }

  public static void setDrawableColor(GradientDrawable gradientDrawable, int color, int defaultColor) {
    if (isValidInAppMessageColor(color)) {
      gradientDrawable.setColor(color);
    } else {
      gradientDrawable.setColor(defaultColor);
    }
  }

  public static boolean isValidInAppMessageColor(int color) {
    return color != 0;
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

  protected static void resetButtonSizesIfNecessary(List<View> buttonViews, List<MessageButton> messageButtons) {
    if (messageButtons != null &&  messageButtons.size() == 1) {
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
      buttonViews.get(0).setLayoutParams(layoutParams);
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
}
