package com.appboy.ui.inappmessage.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;
import com.appboy.ui.inappmessage.AppboyInAppMessageSimpleDraweeView;
import com.appboy.ui.inappmessage.IInAppMessageImageView;
import com.appboy.ui.inappmessage.config.AppboyInAppMessageParams;
import com.appboy.ui.support.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class AppboyInAppMessageFullView extends AppboyInAppMessageImmersiveBaseView {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageFullView.class.getName());
  private AppboyInAppMessageImageView mAppboyInAppMessageImageView;
  /**
   * @see AppboyInAppMessageBaseView#getMessageSimpleDraweeView()
   */
  private View mSimpleDraweeView;

  public AppboyInAppMessageFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void inflateStubViews(Activity activity, IInAppMessageImmersive inAppMessage) {
    if (mCanUseFresco) {
      mSimpleDraweeView = getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_full_drawee_stub);
      AppboyInAppMessageSimpleDraweeView castedSimpleDraweeView = (AppboyInAppMessageSimpleDraweeView) mSimpleDraweeView;
      setInAppMessageImageViewAttributes(activity, inAppMessage, castedSimpleDraweeView);
    } else {
      mAppboyInAppMessageImageView = (AppboyInAppMessageImageView) getProperViewFromInflatedStub(R.id.com_appboy_inappmessage_full_imageview_stub);
      setInAppMessageImageViewAttributes(activity, inAppMessage, mAppboyInAppMessageImageView);
    }
  }

  @Override
  public void setMessageBackgroundColor(int color) {
    if (getMessageBackgroundObject().getBackground() instanceof GradientDrawable) {
      InAppMessageViewUtils.setViewBackgroundColorFilter(findViewById(R.id.com_appboy_inappmessage_full),
          color, getContext().getResources().getColor(R.color.com_appboy_inappmessage_background_light));
    } else {
      super.setMessageBackgroundColor(color);
    }
  }

  @Override
  public List<View> getMessageButtonViews() {
    List<View> buttonViews = new ArrayList<View>();
    if (findViewById(R.id.com_appboy_inappmessage_full_button_one) != null) {
      buttonViews.add(findViewById(R.id.com_appboy_inappmessage_full_button_one));
    }
    if (findViewById(R.id.com_appboy_inappmessage_full_button_two) != null) {
      buttonViews.add(findViewById(R.id.com_appboy_inappmessage_full_button_two));
    }
    return buttonViews;
  }

  @Override
  public View getMessageButtonsView() {
    return findViewById(R.id.com_appboy_inappmessage_full_button_layout);
  }

  @Override
  public TextView getMessageTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_full_message);
  }

  @Override
  public TextView getMessageHeaderTextView() {
    return (TextView) findViewById(R.id.com_appboy_inappmessage_full_header_text);
  }

  @Override
  public View getFrameView() {
    return findViewById(R.id.com_appboy_inappmessage_full_frame);
  }

  @Override
  public View getMessageCloseButtonView() {
    return findViewById(R.id.com_appboy_inappmessage_full_close_button);
  }

  @Override
  public View getMessageClickableView() {
    return findViewById(R.id.com_appboy_inappmessage_full);
  }

  @Override
  public ImageView getMessageImageView() {
    return mAppboyInAppMessageImageView;
  }

  @Override
  public View getMessageSimpleDraweeView() {
    return mSimpleDraweeView;
  }

  @Override
  public TextView getMessageIconView() {
    return null;
  }

  @Override
  public View getMessageBackgroundObject() {
    return findViewById(R.id.com_appboy_inappmessage_full);
  }

  @Override
  public void resetMessageMargins(boolean imageRetrievalSuccessful) {
    super.resetMessageMargins(imageRetrievalSuccessful);

    // Make scrollView pass click events to message clickable view, so that clicking on the scrollView
    // dismisses the in-app message.
    View scrollViewChild = findViewById(R.id.com_appboy_inappmessage_full_text_layout);
    scrollViewChild.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View scrollView) {
        AppboyLogger.d(TAG, "Passing scrollView click event to message clickable view.");
        getMessageClickableView().performClick();
      }
    });
  }

  /**
   * @return the size in pixels of the long edge of a modalized full in-app messages, used to size
   * modalized in-app messages appropriately on tablets.
   */
  public int getLongEdge() {
    return findViewById(R.id.com_appboy_inappmessage_full).getLayoutParams().height;
  }

  /**
   * @return the size in pixels of the short edge of a modalized full in-app messages, used to size
   * modalized in-app messages appropriately on tablets.
   */
  public int getShortEdge() {
    return findViewById(R.id.com_appboy_inappmessage_full).getLayoutParams().width;
  }

  /**
   * Programmatically set attributes on the image view classes inside the image ViewStubs in a
   * fresco/native-agnostic manner.
   *
   * @param activity
   * @param inAppMessage
   * @param inAppMessageImageView
   */
  private void setInAppMessageImageViewAttributes(Activity activity, IInAppMessageImmersive inAppMessage, IInAppMessageImageView inAppMessageImageView) {
    inAppMessageImageView.setInAppMessageImageCropType(inAppMessage.getCropType());
    if (ViewUtils.isRunningOnTablet(activity)) {
      float radiusInPx = (float) ViewUtils.convertDpToPixels(activity, AppboyInAppMessageParams.getModalizedImageRadiusDp());
      if (inAppMessage.getImageStyle().equals(ImageStyle.GRAPHIC)) {
        // for graphic fulls, set the image radius at all four corners.
        inAppMessageImageView.setCornersRadiusPx(radiusInPx);
      } else {
        // for graphic fulls, set the image radius only at the top left and right corners, which
        // are at the edge of the in-app message.
        inAppMessageImageView.setCornersRadiiPx(radiusInPx, radiusInPx, 0.0f, 0.0f);
      }
    } else {
      inAppMessageImageView.setCornersRadiusPx(0.0f);
    }
  }
}
