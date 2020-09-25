package com.appboy.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

import com.appboy.ui.inappmessage.IInAppMessageView;
import com.appboy.ui.inappmessage.views.InAppMessageViewUtils;
import com.appboy.ui.support.ViewUtils;

public class CustomInAppMessageView extends RelativeLayout implements IInAppMessageView {

  public CustomInAppMessageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setMessageBackgroundColor(int color) {
    LayerDrawable layerDrawable = (LayerDrawable) findViewById(R.id.inappmessage).getBackground();
    InAppMessageViewUtils.setDrawableColor(layerDrawable.findDrawableByLayerId(R.id.inappmessage_background), color);
  }

  public void setMessageTextColor(int color) {
    InAppMessageViewUtils.setTextViewColor(getMessageTextView(), color);
  }

  public void setMessage(String text) {
    getMessageTextView().setText(text);
  }

  public void setMessageImage(Bitmap bitmap) {
    InAppMessageViewUtils.setImage(bitmap, getMessageImageView());
  }

  public void setMessageIcon(String icon, int iconColor, int iconBackgroundColor) {
    InAppMessageViewUtils.setIcon(getContext(), icon, iconColor, iconBackgroundColor, getMessageIconView());
  }

  public void resetMessageMargins() {
    if (getMessageImageView().getDrawable() == null) {
      ViewUtils.removeViewFromParent(getMessageImageView());
    }
    if (getMessageIconView().getText().length() == 0) {
      ViewUtils.removeViewFromParent(getMessageIconView());
    }
  }

  public View getMessageClickableView() {
    return this;
  }

  @Override
  public void applyWindowInsets(@NonNull WindowInsetsCompat insets) {
    // Does nothing
  }

  @Override
  public boolean hasAppliedWindowInsets() {
    return false;
  }

  public TextView getMessageTextView() {
    return findViewById(R.id.inappmessage_message);
  }

  public ImageView getMessageImageView() {
    return findViewById(R.id.inappmessage_image);
  }

  public TextView getMessageIconView() {
    return findViewById(R.id.inappmessage_icon);
  }

}
