package com.braze.ui.inappmessage.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.WindowInsetsCompat;

import com.braze.enums.inappmessage.TextAlign;
import com.braze.models.inappmessage.IInAppMessageWithImage;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils;
import com.braze.ui.support.ViewUtils;

import java.io.File;

public abstract class InAppMessageBaseView extends RelativeLayout implements IInAppMessageView {
  private static final String TAG = BrazeLogger.getBrazeLogTag(InAppMessageBaseView.class);

  protected boolean mHasAppliedWindowInsets = false;

  public InAppMessageBaseView(Context context, AttributeSet attrs) {
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

    if (getMessageIconView() != null && getMessageIconView().getText() != null && StringUtils.isNullOrBlank(getMessageIconView().getText().toString())) {
      ViewUtils.removeViewFromParent(getMessageIconView());
    }
  }

  public View getMessageClickableView() {
    return this;
  }

  @Override
  public void applyWindowInsets(@NonNull WindowInsetsCompat insets) {
    mHasAppliedWindowInsets = true;
  }

  @Override
  public boolean hasAppliedWindowInsets() {
    return mHasAppliedWindowInsets;
  }

  /**
   * @return return the local image Url, if present. Otherwise, return the remote image Url. Local
   * image Urls are Urls for images pre-fetched by the SDK for triggers.
   */
  public static String getAppropriateImageUrl(@NonNull IInAppMessageWithImage inAppMessage) {
    final String localImagePath = inAppMessage.getLocalImageUrl();
    if (!StringUtils.isNullOrBlank(localImagePath)) {
      File imageFile = new File(localImagePath);
      if (imageFile.exists()) {
        return localImagePath;
      } else {
        BrazeLogger.d(TAG, "Local bitmap file does not exist. Using remote url instead. Local path: " + localImagePath);
      }
    }

    return inAppMessage.getRemoteImageUrl();
  }

  public abstract TextView getMessageTextView();

  public abstract ImageView getMessageImageView();

  public abstract TextView getMessageIconView();

  public abstract Object getMessageBackgroundObject();
}
