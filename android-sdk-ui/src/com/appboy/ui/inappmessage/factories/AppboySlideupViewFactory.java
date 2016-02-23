package com.appboy.ui.inappmessage.factories;

import android.app.Activity;

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
    InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
    AppboyInAppMessageSlideupView slideupView = (AppboyInAppMessageSlideupView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_slideup, null);
    slideupView.inflateStubViews();
    if (FrescoLibraryUtils.canUseFresco(activity.getApplicationContext())) {
      if (!StringUtils.isNullOrBlank(inAppMessage.getLocalImageUrl())) {
        slideupView.setMessageSimpleDrawee(inAppMessage.getLocalImageUrl());
      } else {
        slideupView.setMessageSimpleDrawee(inAppMessage.getRemoteImageUrl());
      }
    } else {
      slideupView.setMessageImageView(inAppMessageSlideup.getBitmap());
    }

    slideupView.setMessageBackgroundColor(inAppMessageSlideup.getBackgroundColor());
    slideupView.setMessage(inAppMessageSlideup.getMessage());
    slideupView.setMessageTextColor(inAppMessageSlideup.getMessageTextColor());
    slideupView.setMessageIcon(inAppMessageSlideup.getIcon(), inAppMessageSlideup.getIconColor(), inAppMessageSlideup.getIconBackgroundColor());
    slideupView.setMessageChevron(inAppMessageSlideup.getChevronColor(), inAppMessageSlideup.getClickAction());
    slideupView.resetMessageMargins(inAppMessage.getImageDownloadSuccessful());

    return slideupView;
  }
}