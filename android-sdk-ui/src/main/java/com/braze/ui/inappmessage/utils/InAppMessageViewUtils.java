package com.braze.ui.inappmessage.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.braze.enums.inappmessage.TextAlign;
import com.braze.support.BrazeLogger;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;

public class InAppMessageViewUtils {
  private static final String TAG = BrazeLogger.getBrazeLogTag(InAppMessageViewUtils.class);

  public static void setImage(Bitmap bitmap, ImageView imageView) {
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
    }
  }

  public static void setIcon(Context context, String icon, int iconColor, int iconBackgroundColor, TextView textView) {
    if (icon != null) {
      try {
        Typeface fontFamily = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        textView.setTypeface(fontFamily);
      } catch (Exception e) {
        BrazeLogger.e(TAG, "Caught exception setting icon typeface. Not rendering icon.", e);
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
    setDrawableColorFilter(view.getBackground(), color);

    // The alpha needs to be set separately from the background color filter or else it won't apply
    view.getBackground().setAlpha(Color.alpha(color));
  }

  public static void setDrawableColor(Drawable drawable, @ColorInt int color) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (drawable instanceof LayerDrawable) {
        // This layer drawable should have the GradientDrawable as the
        // 0th layer and the RippleDrawable as the 1st layer
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        if (layerDrawable.getNumberOfLayers() > 0 && layerDrawable.getDrawable(0) instanceof GradientDrawable) {
          setDrawableColor(layerDrawable.getDrawable(0), color);
        } else {
          BrazeLogger.d(TAG, "LayerDrawable for button background did not have the expected "
              + "number of layers or the 0th layer was not a GradientDrawable.");
        }
      }
    }

    if (drawable instanceof GradientDrawable) {
      ((GradientDrawable) drawable).setColor(color);
    } else {
      setDrawableColorFilter(drawable, color);
    }
  }

  public static void resetMessageMarginsIfNecessary(TextView messageView, TextView headerView) {
    if (headerView == null && messageView != null) {
      // If header is not present but message is present, reset message margins to 0 (Typically, the message's has a top margin to accommodate the header.)
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(messageView.getLayoutParams().width, messageView.getLayoutParams().height);
      layoutParams.setMargins(0, 0, 0, 0);
      messageView.setLayoutParams(layoutParams);
    }
  }

  public static void closeInAppMessageOnKeycodeBack() {
    BrazeLogger.d(TAG, "Back button intercepted by in-app message view, closing in-app message.");
    BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true);
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

  private static void setDrawableColorFilter(Drawable drawable, @ColorInt int color) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
    } else {
      setDrawableColorFilterOld(drawable, color);
    }
  }

  /**
   * Calls {@link Drawable#setColorFilter(int, PorterDuff.Mode)}. Extracted out this call
   * since deprecation warning suppressions only work at the method level.
   */
  @SuppressWarnings("deprecation")
  private static void setDrawableColorFilterOld(Drawable drawable, @ColorInt int color) {
    drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
  }
}
