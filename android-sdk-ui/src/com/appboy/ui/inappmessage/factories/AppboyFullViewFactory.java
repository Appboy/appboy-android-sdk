package com.appboy.ui.inappmessage.factories;

import android.app.Activity;

import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageFull;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageFullView;
import com.appboy.ui.support.FrescoLibraryUtils;

public class AppboyFullViewFactory implements IInAppMessageViewFactory {
  @Override
  public AppboyInAppMessageFullView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    InAppMessageFull inAppMessageFull = (InAppMessageFull) inAppMessage;
    AppboyInAppMessageFullView fullView = (AppboyInAppMessageFullView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_full, null);
    fullView.inflateStubViews();
    if (FrescoLibraryUtils.canUseFresco(activity.getApplicationContext())) {
      if (!StringUtils.isNullOrBlank(inAppMessage.getLocalImageUrl())) {
        fullView.setMessageSimpleDrawee(inAppMessage.getLocalImageUrl());
      } else {
        fullView.setMessageSimpleDrawee(inAppMessage.getRemoteImageUrl());
      }
    } else {
      fullView.setMessageImageView(inAppMessage.getBitmap());
    }

    fullView.setMessageBackgroundColor(inAppMessageFull.getBackgroundColor());
    fullView.setMessage(inAppMessageFull.getMessage());
    fullView.setMessageTextColor(inAppMessageFull.getMessageTextColor());
    fullView.setMessageHeaderText(inAppMessageFull.getHeader());
    fullView.setMessageHeaderTextColor(inAppMessageFull.getHeaderTextColor());
    fullView.setMessageButtons(inAppMessageFull.getMessageButtons());
    fullView.setMessageCloseButtonColor(inAppMessageFull.getCloseButtonColor());
    fullView.resetMessageMargins(inAppMessage.getImageDownloadSuccessful());

    return fullView;
  }
}
