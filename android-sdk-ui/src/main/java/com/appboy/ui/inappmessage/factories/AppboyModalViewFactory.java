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
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageModalView;

public class AppboyModalViewFactory implements IInAppMessageViewFactory {
  @Override
  public AppboyInAppMessageModalView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    Context applicationContext = activity.getApplicationContext();
    InAppMessageModal inAppMessageModal = (InAppMessageModal) inAppMessage;
    boolean isGraphic = inAppMessageModal.getImageStyle().equals(ImageStyle.GRAPHIC);
    AppboyInAppMessageModalView view = getAppropriateModalView(activity, isGraphic);
    view.inflateStubViews(activity, inAppMessageModal);

    String imageUrl = view.getAppropriateImageUrl(inAppMessage);
    if (!StringUtils.isNullOrEmpty(imageUrl)) {
      IAppboyImageLoader appboyImageLoader = Appboy.getInstance(applicationContext).getAppboyImageLoader();
      appboyImageLoader.renderUrlIntoView(applicationContext, imageUrl, view.getMessageImageView(), AppboyViewBounds.IN_APP_MESSAGE_MODAL);
    }

    // modal frame should not be clickable.
    view.getFrameView().setOnClickListener(null);
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
      view.resetMessageMargins(inAppMessage.getImageDownloadSuccessful());
    }
    return view;
  }

  @SuppressLint("InflateParams")
  AppboyInAppMessageModalView getAppropriateModalView(Activity activity, boolean isGraphic) {
    if (isGraphic) {
      return (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal_graphic, null);
    } else {
      return (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal, null);
    }
  }
}
