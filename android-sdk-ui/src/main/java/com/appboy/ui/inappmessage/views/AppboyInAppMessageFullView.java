package com.appboy.ui.inappmessage.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;
import com.appboy.ui.inappmessage.IInAppMessageImageView;
import com.appboy.ui.inappmessage.config.AppboyInAppMessageParams;
import com.appboy.ui.support.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class AppboyInAppMessageFullView extends AppboyInAppMessageImmersiveBaseView {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageFullView.class);
  private AppboyInAppMessageImageView mAppboyInAppMessageImageView;
  private boolean mIsGraphic;

  public AppboyInAppMessageFullView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void createAppropriateViews(Activity activity, IInAppMessageImmersive inAppMessage, boolean isGraphic) {
    mAppboyInAppMessageImageView = findViewById(R.id.com_appboy_inappmessage_full_imageview);
    setInAppMessageImageViewAttributes(activity, inAppMessage, mAppboyInAppMessageImageView);
    mIsGraphic = isGraphic;
  }

  @Override
  public void setMessageBackgroundColor(int color) {
    if (getMessageBackgroundObject().getBackground() instanceof GradientDrawable) {
      InAppMessageViewUtils.setViewBackgroundColorFilter(getMessageBackgroundObject(), color);
    } else {
      if (mIsGraphic) {
        super.setMessageBackgroundColor(color);
      } else {
        InAppMessageViewUtils.setViewBackgroundColor(findViewById(R.id.com_appboy_inappmessage_full_all_content_parent), color);
        InAppMessageViewUtils.setViewBackgroundColor(findViewById(R.id.com_appboy_inappmessage_full_text_and_button_content_parent), color);
      }
    }
  }

  @Override
  public List<View> getMessageButtonViews(int numButtons) {
    List<View> buttonViews = new ArrayList<View>();

    // Based on the number of buttons, make one of the button parent layouts visible
    if (numButtons == 1) {
      View singleButtonParent = findViewById(R.id.com_appboy_inappmessage_full_button_layout_single);
      if (singleButtonParent != null) {
        singleButtonParent.setVisibility(VISIBLE);
      }

      View singleButton = findViewById(R.id.com_appboy_inappmessage_full_button_single_one);
      if (singleButton != null) {
        buttonViews.add(singleButton);
      }
    } else if (numButtons == 2) {
      View dualButtonParent = findViewById(R.id.com_appboy_inappmessage_full_button_layout_dual);
      if (dualButtonParent != null) {
        dualButtonParent.setVisibility(VISIBLE);
      }

      View dualButton1 = findViewById(R.id.com_appboy_inappmessage_full_button_dual_one);
      View dualButton2 = findViewById(R.id.com_appboy_inappmessage_full_button_dual_two);
      if (dualButton1 != null) {
        buttonViews.add(dualButton1);
      }
      if (dualButton2 != null) {
        buttonViews.add(dualButton2);
      }
    }
    return buttonViews;
  }

  @Override
  public TextView getMessageTextView() {
    return findViewById(R.id.com_appboy_inappmessage_full_message);
  }

  @Override
  public TextView getMessageHeaderTextView() {
    return findViewById(R.id.com_appboy_inappmessage_full_header_text);
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
   * Applies the {@link WindowInsetsCompat} by ensuring the close button and message text on the in-app message does not render
   * in the display cutout area.
   *
   * @param insets The {@link WindowInsetsCompat} object directly from
   * {@link android.support.v4.view.ViewCompat#setOnApplyWindowInsetsListener(View, OnApplyWindowInsetsListener)}.
   */
  @Override
  public void applyWindowInsets(@NonNull WindowInsetsCompat insets) {
    // Attempt to fix the close button
    View closeButtonView = getMessageCloseButtonView();
    if (closeButtonView != null) {
      applyDisplayCutoutMarginsToCloseButton(insets, closeButtonView);
    }

    if (mIsGraphic) {
      // Fix the button layouts individually
      View singleButtonParent = findViewById(R.id.com_appboy_inappmessage_full_button_layout_single);
      if (singleButtonParent != null && singleButtonParent.getVisibility() == VISIBLE) {
        applyDisplayCutoutMarginsToContentArea(insets, singleButtonParent);
        return;
      }
      View dualButtonParent = findViewById(R.id.com_appboy_inappmessage_full_button_layout_dual);
      if (dualButtonParent != null && dualButtonParent.getVisibility() == VISIBLE) {
        applyDisplayCutoutMarginsToContentArea(insets, dualButtonParent);
      }
    } else {
      // Fix the content area as well. The content area is the header, message, and buttons.
      View contentArea = findViewById(R.id.com_appboy_inappmessage_full_text_and_button_content_parent);
      if (contentArea != null) {
        applyDisplayCutoutMarginsToContentArea(insets, contentArea);
      }
    }
  }

  /**
   * @return the size in pixels of the long edge of a modalized full in-app messages, used to size
   * modalized in-app messages appropriately on tablets.
   */
  public int getLongEdge() {
    View inAppMessageFullView = findViewById(R.id.com_appboy_inappmessage_full);
    return inAppMessageFullView.getLayoutParams().height;
  }

  /**
   * @return the size in pixels of the short edge of a modalized full in-app messages, used to size
   * modalized in-app messages appropriately on tablets.
   */
  public int getShortEdge() {
    View inAppMessageFullView = findViewById(R.id.com_appboy_inappmessage_full);
    return inAppMessageFullView.getLayoutParams().width;
  }

  /**
   * Programmatically set attributes on the image view classes inside the image ViewStubs.
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

  /**
   * Shifts/margins the close button out of the display cutout area
   */
  private void applyDisplayCutoutMarginsToCloseButton(@NonNull WindowInsetsCompat windowInsets, @NonNull View closeButtonView) {
    if (closeButtonView.getLayoutParams() == null || !(closeButtonView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
      AppboyLogger.d(TAG, "Close button layout params are null or not of the expected class. Not applying window insets.");
      return;
    }

    // Offset the existing margin with whatever the inset margins safe area values are
    final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) closeButtonView.getLayoutParams();
    layoutParams.setMargins(
        ViewUtils.getMaxSafeLeftInset(windowInsets) + layoutParams.leftMargin,
        ViewUtils.getMaxSafeTopInset(windowInsets) + layoutParams.topMargin,
        ViewUtils.getMaxSafeRightInset(windowInsets) + layoutParams.rightMargin,
        ViewUtils.getMaxSafeBottomInset(windowInsets) + layoutParams.bottomMargin);
  }

  /**
   * Shifts/margins the close button out of the display cutout area
   */
  private void applyDisplayCutoutMarginsToContentArea(@NonNull WindowInsetsCompat windowInsets, @NonNull View contentAreaView) {
    if (contentAreaView.getLayoutParams() == null || !(contentAreaView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
      AppboyLogger.d(TAG, "Content area layout params are null or not of the expected class. Not applying window insets.");
      return;
    }

    // Offset the existing margin with whatever the inset margins safe area values are
    final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) contentAreaView.getLayoutParams();
    layoutParams.setMargins(
        ViewUtils.getMaxSafeLeftInset(windowInsets) + layoutParams.leftMargin,
        layoutParams.topMargin,
        ViewUtils.getMaxSafeRightInset(windowInsets) + layoutParams.rightMargin,
        ViewUtils.getMaxSafeBottomInset(windowInsets) + layoutParams.bottomMargin);
  }
}
