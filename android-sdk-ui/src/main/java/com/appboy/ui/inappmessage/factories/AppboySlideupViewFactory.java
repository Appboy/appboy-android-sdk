package com.appboy.ui.inappmessage.factories;

import android.app.Activity;
import android.content.Context;

import com.appboy.Appboy;
import com.appboy.IAppboyImageLoader;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageBaseView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageSlideupView;
import com.appboy.ui.support.ViewUtils;

public class AppboySlideupViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = AppboyLogger.getBrazeLogTag(AppboySlideupViewFactory.class);

  @Override
  public AppboyInAppMessageSlideupView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    AppboyInAppMessageSlideupView view = (AppboyInAppMessageSlideupView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_slideup, null);
    if (ViewUtils.isDeviceNotInTouchMode(view)) {
      AppboyLogger.w(TAG, "The device is not currently in touch mode. This message requires user touch interaction to display properly.");
      return null;
    }

    InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
    Context applicationContext = activity.getApplicationContext();
    view.applyInAppMessageParameters(inAppMessage);

    String imageUrl = AppboyInAppMessageBaseView.getAppropriateImageUrl(inAppMessageSlideup);
    if (!StringUtils.isNullOrEmpty(imageUrl)) {
      IAppboyImageLoader appboyImageLoader = Appboy.getInstance(applicationContext).getAppboyImageLoader();
      appboyImageLoader.renderUrlIntoInAppMessageView(applicationContext, inAppMessage, imageUrl, view.getMessageImageView(), AppboyViewBounds.IN_APP_MESSAGE_SLIDEUP);
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
