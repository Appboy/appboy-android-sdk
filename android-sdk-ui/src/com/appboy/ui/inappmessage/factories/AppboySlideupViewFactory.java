package com.appboy.ui.inappmessage.factories;

import android.app.Activity;
import android.content.Context;

import com.appboy.Appboy;
import com.appboy.IAppboyImageLoader;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageSlideupView;
import com.appboy.ui.support.FrescoLibraryUtils;

public class AppboySlideupViewFactory implements IInAppMessageViewFactory {
  @Override
  public AppboyInAppMessageSlideupView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    Context applicationContext = activity.getApplicationContext();
    InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
    AppboyInAppMessageSlideupView view = (AppboyInAppMessageSlideupView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_slideup, null);
    view.inflateStubViews(inAppMessage);

    if (FrescoLibraryUtils.canUseFresco(applicationContext)) {
      view.setMessageSimpleDrawee(inAppMessage);
    } else {
      String imageUrl = view.getAppropriateImageUrl(inAppMessage);
      if (!StringUtils.isNullOrEmpty(imageUrl)) {
        IAppboyImageLoader appboyImageLoader = Appboy.getInstance(applicationContext).getAppboyImageLoader();
        appboyImageLoader.renderUrlIntoView(applicationContext, imageUrl, view.getMessageImageView(), AppboyViewBounds.IN_APP_MESSAGE_SLIDEUP);
      }
    }

    view.setMessageBackgroundColor(inAppMessageSlideup.getBackgroundColor());
    view.setMessage(inAppMessageSlideup.getMessage());
    view.setMessageTextColor(inAppMessageSlideup.getMessageTextColor());
    view.setMessageTextAlign(inAppMessageSlideup.getMessageTextAlign());
    view.setMessageIcon(inAppMessageSlideup.getIcon(), inAppMessageSlideup.getIconColor(), inAppMessageSlideup.getIconBackgroundColor());
    view.setMessageChevron(inAppMessageSlideup.getChevronColor(), inAppMessageSlideup.getClickAction());
    view.resetMessageMargins(inAppMessage.getImageDownloadSuccessful());

    return view;
  }
}
