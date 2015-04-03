package com.appboy.ui.inappmessage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.ui.support.StringUtils;
import com.appboy.ui.support.ViewUtils;

public abstract class AppboyInAppMessageBaseView extends RelativeLayout implements IInAppMessageView {

  public AppboyInAppMessageBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setMessageBackgroundColor(int color) {
    InAppMessageViewUtils.setViewBackgroundColor((View) getMessageBackgroundObject(), color);
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
    if (getMessageImageView() != null) {
      if (getMessageImageView().getDrawable() == null) {
        ViewUtils.removeViewFromParent(getMessageImageView());
      } else {
        // We prioritize the image view by removing the icon view if an image view is present.
        ViewUtils.removeViewFromParent(getMessageIconView());
      }
    }
    if (getMessageIconView() != null && StringUtils.isNullOrBlank((String) getMessageIconView().getText())) {
      ViewUtils.removeViewFromParent(getMessageIconView());
    }
  }

  public View getMessageClickableView() {
    return this;
  }

  public abstract TextView getMessageTextView();

  public abstract ImageView getMessageImageView();

  public abstract TextView getMessageIconView();

  public abstract Object getMessageBackgroundObject();
}
