package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.appboy.Appboy;
import com.appboy.IAppboyImageLoader;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.enums.inappmessage.Orientation;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageFull;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageFullView;
import com.appboy.ui.support.ViewUtils;

public class AppboyFullViewFactory implements IInAppMessageViewFactory {
  private static final int EMPTY_BUTTONS_SCROLLVIEW_EXCESS_HEIGHT_VALUE_IN_DP = 60;
  private static final int BUTTONS_PRESENT_SCROLLVIEW_EXCESS_HEIGHT_VALUE_IN_DP = 124;

  @Override
  public AppboyInAppMessageFullView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    final Context applicationContext = activity.getApplicationContext();
    final InAppMessageFull inAppMessageFull = (InAppMessageFull) inAppMessage;
    boolean isGraphic = inAppMessageFull.getImageStyle().equals(ImageStyle.GRAPHIC);
    final AppboyInAppMessageFullView view = getAppropriateFullView(activity, isGraphic);
    view.createAppropriateViews(activity, inAppMessageFull);

    // Since this image is the width of the screen, the view bounds are uncapped
    String imageUrl = view.getAppropriateImageUrl(inAppMessage);
    if (!StringUtils.isNullOrEmpty(imageUrl)) {
      IAppboyImageLoader appboyImageLoader = Appboy.getInstance(applicationContext).getAppboyImageLoader();
      appboyImageLoader.renderUrlIntoView(applicationContext, imageUrl, view.getMessageImageView(), AppboyViewBounds.NO_BOUNDS);
    }

    // modal frame should not be clickable.
    view.getFrameView().setOnClickListener(null);
    view.setMessageBackgroundColor(inAppMessageFull.getBackgroundColor());
    view.setFrameColor(inAppMessageFull.getFrameColor());
    view.setMessageButtons(inAppMessageFull.getMessageButtons());
    view.setMessageCloseButtonColor(inAppMessageFull.getCloseButtonColor());
    if (!isGraphic) {
      view.setMessage(inAppMessageFull.getMessage());
      view.setMessageTextColor(inAppMessageFull.getMessageTextColor());
      view.setMessageHeaderText(inAppMessageFull.getHeader());
      view.setMessageHeaderTextColor(inAppMessageFull.getHeaderTextColor());
      view.setMessageHeaderTextAlignment(inAppMessageFull.getHeaderTextAlign());
      view.setMessageTextAlign(inAppMessageFull.getMessageTextAlign());
      view.resetMessageMargins(inAppMessageFull.getImageDownloadSuccessful());

      // Only non-graphic full in-app messages should be capped to half the parent height
      ((AppboyInAppMessageImageView) view.getMessageImageView()).setToHalfParentHeight(true);
    }
    view.setLargerCloseButtonClickArea(view.getMessageCloseButtonView());
    resetLayoutParamsIfAppropriate(activity, inAppMessageFull, view);

    // Get the scrollView, if it exists. For graphic full, it will not
    final View scrollView = view.findViewById(R.id.com_appboy_inappmessage_full_scrollview);
    if (scrollView != null) {
      final View scrollViewParent = (View) scrollView.getParent();
      scrollView.post(new Runnable() {
        @Override
        public void run() {
          // Get the parent height
          int parentHeight = scrollViewParent.getHeight();

          // Half of that is the Image
          // So we have another half allotted for us + some margins + the buttons
          int halfHeight = parentHeight / 2;

          // Compute the rest of the height for the ScrollView + buttons + margins
          // 30dp top margin
          // 20dp margin between button / bottom of scrollview
          // 44dp height for buttons
          // 30dp bottom margin for buttons
          int excessHeight;
          if (inAppMessageFull.getMessageButtons() == null || inAppMessageFull.getMessageButtons().isEmpty()) {
            // No need to account for the button height / margin
            excessHeight = (int) ViewUtils.convertDpToPixels(applicationContext, EMPTY_BUTTONS_SCROLLVIEW_EXCESS_HEIGHT_VALUE_IN_DP);
          } else {
            // Account for all appropriate height / margins
            excessHeight = (int) ViewUtils.convertDpToPixels(applicationContext, BUTTONS_PRESENT_SCROLLVIEW_EXCESS_HEIGHT_VALUE_IN_DP);
          }

          // The remaining height is the MOST that the scrollView can take up
          int scrollViewAppropriateHeight = Math.min(scrollView.getHeight(), halfHeight - excessHeight);

          // Now set that height for the ScrollView
          ViewUtils.setHeightOnViewLayoutParams(scrollView, scrollViewAppropriateHeight);

          // Request another layout since we changed bounds for everything
          scrollView.requestLayout();
          view.getMessageImageView().requestLayout();
        }
      });
    }

    return view;
  }

  /**
   * For in-app messages that have a preferred orientation and are being displayed on tablet,
   * ensure the in-app message appears in the style of the preferred orientation regardless of
   * actual screen orientation.
   *
   * @param activity
   * @param inAppMessage
   * @param view
   * @return true if params were reset
   */
  boolean resetLayoutParamsIfAppropriate(Activity activity, IInAppMessage inAppMessage, AppboyInAppMessageFullView view) {
    if (!ViewUtils.isRunningOnTablet(activity)) {
      return false;
    }
    if (inAppMessage.getOrientation() == null || inAppMessage.getOrientation() == Orientation.ANY) {
      return false;
    }
    int longEdge = view.getLongEdge();
    int shortEdge = view.getShortEdge();
    if (longEdge > 0 && shortEdge > 0) {
      RelativeLayout.LayoutParams layoutParams;
      if (inAppMessage.getOrientation() == Orientation.LANDSCAPE) {
        layoutParams = new RelativeLayout.LayoutParams(longEdge, shortEdge);
      } else {
        layoutParams = new RelativeLayout.LayoutParams(shortEdge, longEdge);
      }
      layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      view.getMessageBackgroundObject().setLayoutParams(layoutParams);
      return true;
    }
    return false;
  }

  @SuppressLint("InflateParams")
  AppboyInAppMessageFullView getAppropriateFullView(Activity activity, boolean isGraphic) {
    if (isGraphic) {
      return (AppboyInAppMessageFullView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_full_graphic, null);
    } else {
      return (AppboyInAppMessageFullView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_full, null);
    }
  }
}
