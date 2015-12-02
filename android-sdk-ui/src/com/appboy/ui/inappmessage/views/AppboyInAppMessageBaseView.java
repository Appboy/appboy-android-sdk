package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageView;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.appboy.ui.support.StringUtils;
import com.appboy.ui.support.ViewUtils;
import com.facebook.drawee.view.SimpleDraweeView;

public abstract class AppboyInAppMessageBaseView extends RelativeLayout implements IInAppMessageView {

  final boolean mCanUseFresco;

  public AppboyInAppMessageBaseView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mCanUseFresco = FrescoLibraryUtils.canUseFresco(context);
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

  public void setMessageImageView(Bitmap bitmap) {
    InAppMessageViewUtils.setImage(bitmap, getMessageImageView());
  }

  public void setMessageSimpleDrawee(String imageUrl) {
    FrescoLibraryUtils.setDraweeControllerHelper((SimpleDraweeView) getMessageSimpleDraweeView(), imageUrl, 0f, false);
  }

  public void setMessageIcon(String icon, int iconColor, int iconBackgroundColor) {
    InAppMessageViewUtils.setIcon(getContext(), icon, iconColor, iconBackgroundColor, getMessageIconView());
  }

  @Deprecated
  /**
   * Please use {@link AppboyInAppMessageBaseView#resetMessageMargins(boolean)} instead.
   */
  public void resetMessageMargins() {
    boolean successful = false;
    if (getMessageImageView() != null && getMessageImageView().getDrawable() != null) {
      successful = true;
    }

    resetMessageMargins(successful);
  }

  /**
   * Since SimpleDraweeViews use placeholders, we cannot directly check the nullity of its View Drawable
   * to check if the view should be removed.
   */
  public void resetMessageMargins(boolean imageRetrievalSuccessful) {
    // This view is either the SimpleDraweeView or an ImageView
    View viewContainingImage;
    // Since the ViewStubs containing the image layouts (i.e. for the SimpleDraweeView or ImageView)
    // are wrapped in a RelativeLayout, we must remove that layout as well. Simply setting the layout
    // bounds to wrap_content doesn't suffice.
    RelativeLayout layoutContainingImage;
    if (mCanUseFresco) {
      viewContainingImage = getMessageSimpleDraweeView();
      layoutContainingImage = (RelativeLayout) findViewById(R.id.com_appboy_stubbed_inappmessage_drawee_view_parent);
    } else {
      viewContainingImage = getMessageImageView();
      layoutContainingImage = (RelativeLayout) findViewById(R.id.com_appboy_stubbed_inappmessage_image_view_parent);
    }

    if (viewContainingImage != null) {
      if (!imageRetrievalSuccessful) {
        ViewUtils.removeViewFromParent(viewContainingImage);
        if (layoutContainingImage != null) {
          ViewUtils.removeViewFromParent(layoutContainingImage);
        }
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

  /**
   * In cases where the Fresco library isn't provided, we can't have the SimpleDraweeView class as
   * actual signature of this method. Thus, we return View for this method and cast it when needed.
   */
  public abstract View getMessageSimpleDraweeView();

  /**
   * Gets the view to display the correct card image after checking if it can use Fresco.
   * @param stubLayoutId The resource Id of the stub for inflation as returned by findViewById.
   * @return the view to display the image. This will either be an ImageView or DraweeView
   */
  View getProperViewFromInflatedStub(int stubLayoutId) {
    ViewStub imageStub = (ViewStub) findViewById(stubLayoutId);
    imageStub.inflate();

    if (mCanUseFresco) {
      return findViewById(R.id.com_appboy_stubbed_inappmessage_drawee_view);
    } else {
      return findViewById(R.id.com_appboy_stubbed_inappmessage_image_view);
    }
  }
}
