package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.appboy.Appboy;
import com.appboy.IAppboyImageLoader;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageModal;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.AppboyInAppMessageImageView;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageModalView;

public class AppboyModalViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyModalViewFactory.class);
  private static final float NON_GRAPHIC_ASPECT_RATIO = 290f / 100f;

  @Override
  public AppboyInAppMessageModalView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    Context applicationContext = activity.getApplicationContext();
    InAppMessageModal inAppMessageModal = (InAppMessageModal) inAppMessage;
    final boolean isGraphic = inAppMessageModal.getImageStyle().equals(ImageStyle.GRAPHIC);
    AppboyInAppMessageModalView view = getAppropriateModalView(activity, isGraphic);
    view.applyInAppMessageParameters(applicationContext, inAppMessageModal);

    String imageUrl = view.getAppropriateImageUrl(inAppMessageModal);
    if (!StringUtils.isNullOrEmpty(imageUrl)) {
      IAppboyImageLoader appboyImageLoader = Appboy.getInstance(applicationContext).getAppboyImageLoader();
      appboyImageLoader.renderUrlIntoInAppMessageView(applicationContext, inAppMessage, imageUrl, view.getMessageImageView(), AppboyViewBounds.IN_APP_MESSAGE_MODAL);
    }

    // Modal frame should only dismiss the message when configured.
    view.getFrameView().setOnClickListener(view1 -> {
      if (AppboyInAppMessageManager.getInstance().getDoesClickOutsideModalViewDismissInAppMessageView()) {
        AppboyLogger.i(TAG, "Dismissing modal after frame click");
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
      ((AppboyInAppMessageImageView) view.getMessageImageView()).setAspectRatio(NON_GRAPHIC_ASPECT_RATIO);
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
