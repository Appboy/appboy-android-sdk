package com.appboy.ui.inappmessage.factories;

import android.app.Activity;
import android.content.Context;

import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageBaseView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageSlideupView;
import com.appboy.ui.support.ViewUtils;
import com.braze.Braze;
import com.braze.enums.BrazeViewBounds;
import com.braze.images.IBrazeImageLoader;
import com.braze.support.BrazeLogger;

public class AppboySlideupViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = BrazeLogger.getBrazeLogTag(AppboySlideupViewFactory.class);

  @Override
  public AppboyInAppMessageSlideupView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    AppboyInAppMessageSlideupView view = (AppboyInAppMessageSlideupView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_slideup, null);
    if (ViewUtils.isDeviceNotInTouchMode(view)) {
      BrazeLogger.w(TAG, "The device is not currently in touch mode. This message requires user touch interaction to display properly.");
      return null;
    }

    InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
    Context applicationContext = activity.getApplicationContext();
    view.applyInAppMessageParameters(inAppMessage);

    String imageUrl = AppboyInAppMessageBaseView.getAppropriateImageUrl(inAppMessageSlideup);
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
