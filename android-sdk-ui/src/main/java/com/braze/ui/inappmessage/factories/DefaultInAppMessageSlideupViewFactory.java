package com.braze.ui.inappmessage.factories;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.appboy.ui.R;
import com.braze.Braze;
import com.braze.enums.BrazeViewBounds;
import com.braze.images.IBrazeImageLoader;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.InAppMessageSlideup;
import com.braze.support.BrazeLogger;
import com.braze.support.StringUtils;
import com.braze.ui.inappmessage.IInAppMessageViewFactory;
import com.braze.ui.inappmessage.views.InAppMessageBaseView;
import com.braze.ui.inappmessage.views.InAppMessageSlideupView;
import com.braze.ui.support.ViewUtils;

public class DefaultInAppMessageSlideupViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = BrazeLogger.getBrazeLogTag(DefaultInAppMessageSlideupViewFactory.class);

  @Override
  public InAppMessageSlideupView createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage inAppMessage) {
    InAppMessageSlideupView view = (InAppMessageSlideupView) activity.getLayoutInflater().inflate(R.layout.com_braze_inappmessage_slideup, null);
    if (ViewUtils.isDeviceNotInTouchMode(view)) {
      BrazeLogger.w(TAG, "The device is not currently in touch mode. This message requires user touch interaction to display properly.");
      return null;
    }

    InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
    Context applicationContext = activity.getApplicationContext();
    view.applyInAppMessageParameters(inAppMessage);

    String imageUrl = InAppMessageBaseView.getAppropriateImageUrl(inAppMessageSlideup);
    if (!StringUtils.isNullOrEmpty(imageUrl)) {
      IBrazeImageLoader appboyImageLoader = Braze.getInstance(applicationContext).getImageLoader();
      appboyImageLoader.renderUrlIntoInAppMessageView(applicationContext, inAppMessage, imageUrl, view.getMessageImageView(), BrazeViewBounds.IN_APP_MESSAGE_SLIDEUP);
    }

    view.setMessageBackgroundColor(inAppMessageSlideup.getBackgroundColor());
    view.setMessage(inAppMessageSlideup.getMessage());
    view.setMessageTextColor(inAppMessageSlideup.getMessageTextColor());
    view.setMessageTextAlign(inAppMessageSlideup.getMessageTextAlign());
    view.setMessageIcon(inAppMessageSlideup.getIcon(), inAppMessageSlideup.getIconColor(), inAppMessageSlideup.getIconBackgroundColor());
    view.setMessageChevron(inAppMessageSlideup.getChevronColor(), inAppMessageSlideup.getClickAction());
    view.resetMessageMargins(inAppMessageSlideup.getImageDownloadSuccessful());
    return view;
  }

}
