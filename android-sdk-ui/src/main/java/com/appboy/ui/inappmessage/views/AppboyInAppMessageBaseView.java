package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.enums.inappmessage.TextAlign;
import com.appboy.models.IInAppMessage;
import com.appboy.support.StringUtils;
import com.appboy.ui.inappmessage.IInAppMessageView;
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

  public void setMessageTextAlign(TextAlign textAlign) {
    InAppMessageViewUtils.setTextAlignment(getMessageTextView(), textAlign);
  }

  public void setMessage(String text) {
    getMessageTextView().setText(text);
  }

  public void setMessageImageView(Bitmap bitmap) {
    InAppMessageViewUtils.setImage(bitmap, getMessageImageView());
  }

  /**
   * @param inAppMessage
   * @return return the local image Url, if present. Otherwise, return the remote image Url. Local
   * image Urls are Urls for images pre-fetched by the SDK for triggers.
   */
  public String getAppropriateImageUrl(@NonNull IInAppMessage inAppMessage) {
    if (!StringUtils.isNullOrBlank(inAppMessage.getLocalImageUrl())) {
      return inAppMessage.getLocalImageUrl();
    } else {
      return inAppMessage.getRemoteImageUrl();
    }
  }

  public void setMessageIcon(String icon, int iconColor, int iconBackgroundColor) {
    if (getMessageIconView() != null) {
      InAppMessageViewUtils.setIcon(getContext(), icon, iconColor, iconBackgroundColor, getMessageIconView());
    }
  }

  public void resetMessageMargins(boolean imageRetrievalSuccessful) {
    View viewContainingImage = getMessageImageView();

    if (viewContainingImage != null) {
      if (!imageRetrievalSuccessful) {
        ViewUtils.removeViewFromParent(viewContainingImage);
      } else {
        // We prioritize the image by removing the icon view if the image is present.
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
