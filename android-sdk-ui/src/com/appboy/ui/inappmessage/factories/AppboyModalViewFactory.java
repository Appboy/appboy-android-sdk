package com.appboy.ui.inappmessage.factories;

import android.app.Activity;

import com.appboy.enums.inappmessage.ImageStyle;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageModal;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageModalView;
import com.appboy.ui.support.FrescoLibraryUtils;

public class AppboyModalViewFactory implements IInAppMessageViewFactory {
  @Override
  public AppboyInAppMessageModalView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    InAppMessageModal inAppMessageModal = (InAppMessageModal) inAppMessage;
    boolean isGraphic = inAppMessageModal.getImageStyle().equals(ImageStyle.GRAPHIC);
    AppboyInAppMessageModalView view = getAppropriateModalView(activity, isGraphic);
    view.inflateStubViews(activity, inAppMessageModal);
    if (FrescoLibraryUtils.canUseFresco(activity.getApplicationContext())) {
      view.setMessageSimpleDrawee(inAppMessageModal, activity);
    } else {
      view.setMessageImageView(inAppMessageModal.getBitmap());
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

  AppboyInAppMessageModalView getAppropriateModalView(Activity activity, boolean isGraphic) {
    if (isGraphic) {
      return (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal_graphic, null);
    } else {
      return (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal, null);
    }
  }
}