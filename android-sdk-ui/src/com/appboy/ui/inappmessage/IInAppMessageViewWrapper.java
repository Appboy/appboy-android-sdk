package com.appboy.ui.inappmessage;

import android.app.Activity;
import android.view.View;

import com.appboy.models.IInAppMessage;

public interface IInAppMessageViewWrapper {

  void open(Activity activity);

  void close();

  View getInAppMessageView();

  IInAppMessage getInAppMessage();

  boolean getIsAnimatingClose();
}