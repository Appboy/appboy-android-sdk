package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageModal;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageBaseView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageModalView;
import com.appboy.ui.inappmessage.views.InAppMessageImageView;
import com.braze.Braze;
import com.braze.enums.BrazeViewBounds;
import com.braze.images.IBrazeImageLoader;
import com.braze.support.BrazeLogger;

public class AppboyModalViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = BrazeLogger.getBrazeLogTag(AppboyModalViewFactory.class);
  private static final float NON_GRAPHIC_ASPECT_RATIO = 290f / 100f;

  @Override
  public AppboyInAppMessageModalView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    Context applicationContext = activity.getApplicationContext();
    InAppMessageModal inAppMessageModal = (InAppMessageModal) inAppMessage;
    final boolean isGraphic = inAppMessageModal.getImageStyle().equals(ImageStyle.GRAPHIC);
    AppboyInAppMessageModalView view = getAppropriateModalView(activity, isGraphic);
    view.applyInAppMessageParameters(applicationContext, inAppMessageModal);

    String imageUrl = AppboyInAppMessageBaseView.getAppropriateImageUrl(inAppMessageModal);
    if (!StringUtils.isNullOrEmpty(imageUrl)) {
      IBrazeImageLoader appboyImageLoader = Braze.getInstance(applicationContext).getImageLoader();
      appboyImageLoader.renderUrlIntoInAppMessageView(applicationContext, inAppMessage, imageUrl, view.getMessageImageView(), BrazeViewBounds.IN_APP_MESSAGE_MODAL);
    }

    // Modal frame should only dismiss the message when configured.
    view.getFrameView().setOnClickListener(view1 -> {
      if (AppboyInAppMessageManager.getInstance().getDoesClickOutsideModalViewDismissInAppMessageView()) {
        BrazeLogger.i(TAG, "Dismissing modal after frame click");
        AppboyInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true);
      }
    });
    view.setMessageBackgroundColor(inAppMessage.getBackgroundColor());
    view.setFrameColor(inAppMessageModal.getFrameColor());
    view.setMessageButtons(inAppMessageModal.getMessageButtons());
    view.setMessageCloseButtonColor(inAppMessageModal.getCloseButtonColor());
    if (!isGraphic) {
      view.setMessage(inAppMessage.getMessage());
      view.setMessageTextColor(inAppMessage.getMessageTextColor());
      view.setMessageHeaderText(inAppMessageModal.getHeader());
      view.setMessageHeaderTextColor(inAppMessageModal.getHeaderTextColor());
      view.setMessageIcon(inAppMessage.getIcon(), inAppMessage.getIconColor(), inAppMessage.getIconBackgroundColor());
      view.setMessageHeaderTextAlignment(inAppMessageModal.getHeaderTextAlign());
      view.setMessageTextAlign(inAppMessageModal.getMessageTextAlign());
      view.resetMessageMargins(inAppMessageModal.getImageDownloadSuccessful());
      ((InAppMessageImageView) view.getMessageImageView()).setAspectRatio(NON_GRAPHIC_ASPECT_RATIO);
    }
    view.setLargerCloseButtonClickArea(view.getMessageCloseButtonView());
    view.setupDirectionalNavigation(inAppMessageModal.getMessageButtons().size());
    return view;
  }

  @SuppressLint("InflateParams")
  private AppboyInAppMessageModalView getAppropriateModalView(Activity activity, boolean isGraphic) {
    if (isGraphic) {
      return (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal_graphic, null);
    } else {
      return (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal, null);
    }
  }
}
