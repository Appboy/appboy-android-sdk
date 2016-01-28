package com.appboy.ui.inappmessage;

import android.view.View;
import android.widget.FrameLayout;

import com.appboy.models.IInAppMessage;

public interface IInAppMessageViewWrapper {
  void open(FrameLayout root);
  void close();
  View getInAppMessageView();
  IInAppMessage getInAppMessage();
  boolean getIsAnimatingClose();
}