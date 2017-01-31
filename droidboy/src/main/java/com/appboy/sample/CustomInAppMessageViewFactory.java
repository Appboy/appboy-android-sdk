package com.appboy.sample;

import android.app.Activity;
import android.view.View;

import com.appboy.models.IInAppMessage;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;

public class CustomInAppMessageViewFactory implements IInAppMessageViewFactory {
  @Override
  public View createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    CustomInAppMessageView inAppMessageView = (CustomInAppMessageView) activity.getLayoutInflater().inflate(R.layout.custom_inappmessage, null);

    inAppMessageView.setMessageBackgroundColor(inAppMessage.getBackgroundColor());
    inAppMessageView.setMessage(inAppMessage.getMessage());
    inAppMessageView.setMessageTextColor(inAppMessage.getMessageTextColor());
    inAppMessageView.setMessageIcon(inAppMessage.getIcon(), inAppMessage.getIconBackgroundColor(), inAppMessage.getIconColor());
    inAppMessageView.setMessageImage(inAppMessage.getBitmap());
    inAppMessageView.resetMessageMargins();

    return inAppMessageView;
  }
}
