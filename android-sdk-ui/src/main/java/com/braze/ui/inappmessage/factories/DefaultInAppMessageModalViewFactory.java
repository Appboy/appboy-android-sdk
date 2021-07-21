package com.braze.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.appboy.ui.R;
import com.braze.Braze;
import com.braze.enums.BrazeViewBounds;
import com.braze.enums.inappmessage.ImageStyle;
import com.braze.images.IBrazeImageLoader;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.InAppMessageModal;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.braze.ui.inappmessage.IInAppMessageViewFactory;
import com.braze.ui.inappmessage.views.InAppMessageBaseView;
import com.braze.ui.inappmessage.views.InAppMessageImageView;
import com.braze.ui.inappmessage.views.InAppMessageModalView;

public class DefaultInAppMessageModalViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = BrazeLogger.getBrazeLogTag(DefaultInAppMessageModalViewFactory.class);
  private static final float NON_GRAPHIC_ASPECT_RATIO = 290f / 100f;

  @Override
  public InAppMessageModalView createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage inAppMessage) {
    Context applicationContext = activity.getApplicationContext();
    InAppMessageModal inAppMessageModal = (InAppMessageModal) inAppMessage;
    final boolean isGraphic = inAppMessageModal.getImageStyle().equals(ImageStyle.GRAPHIC);
    InAppMessageModalView view = getAppropriateModalView(activity, isGraphic);
    view.applyInAppMessageParameters(applicationContext, inAppMessageModal);

    String imageUrl = InAppMessageBaseView.getAppropriateImageUrl(inAppMessageModal);
    if (!StringUtils.isNullOrEmpty(imageUrl)) {
      IBrazeImageLoader appboyImageLoader = Braze.getInstance(applicationContext).getImageLoader();
      appboyImageLoader.renderUrlIntoInAppMessageView(applicationContext, inAppMessage, imageUrl, view.getMessageImageView(), BrazeViewBounds.IN_APP_MESSAGE_MODAL);
    }

    // Modal frame should only dismiss the message when configured.
    view.getFrameView().setOnClickListener(view1 -> {
      if (BrazeInAppMessageManager.getInstance().getDoesClickOutsideModalViewDismissInAppMessageView()) {
        BrazeLogger.i(TAG, "Dismissing modal after frame click");
        BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true);
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
  private InAppMessageModalView getAppropriateModalView(Activity activity, boolean isGraphic) {
    if (isGraphic) {
      return (InAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_braze_inappmessage_modal_graphic, null);
    } else {
      return (InAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_braze_inappmessage_modal, null);
    }
  }
}
