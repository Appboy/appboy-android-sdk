package com.appboy.ui.inappmessage.factories;

import android.app.Activity;

import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageHtmlFull;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlFullView;

public class AppboyHtmlFullViewFactory implements IInAppMessageViewFactory {
  private IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;

  public AppboyHtmlFullViewFactory(IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
  }

  @Override
  public AppboyInAppMessageHtmlFullView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    InAppMessageHtmlFull inAppMessageHtmlFull = (InAppMessageHtmlFull) inAppMessage;
    AppboyInAppMessageHtmlFullView view = (AppboyInAppMessageHtmlFullView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_html_full, null);

    view.setWebViewContent(inAppMessage.getMessage(), inAppMessageHtmlFull.getLocalAssetsDirectoryUrl());
    view.setInAppMessageWebViewClient(new InAppMessageWebViewClient(inAppMessage, mInAppMessageWebViewClientListener));

    return view;
  }
}