package com.appboy.ui.inappmessage.factories;

import android.app.Activity;

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
    AppboyInAppMessageModalView modalView = (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal, null);
    modalView.inflateStubViews();
    if (FrescoLibraryUtils.canUseFresco(activity.getApplicationContext())) {
      modalView.setMessageSimpleDrawee(inAppMessage.getImageUrl());
    } else {
      modalView.setMessageImageView(inAppMessageModal.getBitmap());
    }
    // modal frame should not be clickable.
    modalView.getModalFrameView().setOnClickListener(null);
    modalView.setMessageBackgroundColor(inAppMessage.getBackgroundColor());
    modalView.setMessage(inAppMessage.getMessage());
    modalView.setMessageTextColor(inAppMessage.getMessageTextColor());
    modalView.setMessageHeaderText(inAppMessageModal.getHeader());
    modalView.setMessageHeaderTextColor(inAppMessageModal.getHeaderTextColor());
    modalView.setModalFrameColor(inAppMessageModal.getModalFrameColor());
    modalView.setMessageIcon(inAppMessage.getIcon(), inAppMessage.getIconColor(), inAppMessage.getIconBackgroundColor());
    modalView.setMessageButtons(inAppMessageModal.getMessageButtons());
    modalView.setMessageCloseButtonColor(inAppMessageModal.getCloseButtonColor());
    modalView.resetMessageMargins(inAppMessage.getImageDownloadSuccessful());

    return modalView;
  }
}