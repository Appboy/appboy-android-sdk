package com.appboy.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;

import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.IInAppMessageWithImage;
import com.braze.ui.inappmessage.IInAppMessageViewFactory;

public class CustomInAppMessageViewFactory implements IInAppMessageViewFactory {
  @SuppressLint("InflateParams")
  @Override
  public View createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage inAppMessage) {
    CustomInAppMessageView inAppMessageView = (CustomInAppMessageView) activity.getLayoutInflater().inflate(R.layout.custom_inappmessage, null);

    inAppMessageView.setMessageBackgroundColor(inAppMessage.getBackgroundColor());
    inAppMessageView.setMessage(inAppMessage.getMessage());
    inAppMessageView.setMessageTextColor(inAppMessage.getMessageTextColor());
    inAppMessageView.setMessageIcon(inAppMessage.getIcon(), inAppMessage.getIconBackgroundColor(), inAppMessage.getIconColor());
    if (inAppMessage instanceof IInAppMessageWithImage) {
      inAppMessageView.setMessageImage(((IInAppMessageWithImage) inAppMessage).getBitmap());
    }
    inAppMessageView.resetMessageMargins();

    return inAppMessageView;
  }
}
