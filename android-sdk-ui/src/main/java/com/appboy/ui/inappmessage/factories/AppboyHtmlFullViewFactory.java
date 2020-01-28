package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageHtmlFull;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;
import com.appboy.ui.inappmessage.jsinterface.AppboyInAppMessageHtmlJavascriptInterface;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlFullView;

public class AppboyHtmlFullViewFactory implements IInAppMessageViewFactory {
  private IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;

  public AppboyHtmlFullViewFactory(IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
  }

  @SuppressLint("AddJavascriptInterface")
  @Override
  public AppboyInAppMessageHtmlFullView createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
    final Context context = activity.getApplicationContext();
    InAppMessageHtmlFull inAppMessageHtmlFull = (InAppMessageHtmlFull) inAppMessage;
    final AppboyInAppMessageHtmlJavascriptInterface javascriptInterface = new AppboyInAppMessageHtmlJavascriptInterface(context, inAppMessageHtmlFull);
    AppboyInAppMessageHtmlFullView view = (AppboyInAppMessageHtmlFullView) activity.getLayoutInflater()
        .inflate(R.layout.com_appboy_inappmessage_html_full, null);

    view.setWebViewContent(inAppMessage.getMessage(), inAppMessageHtmlFull.getLocalAssetsDirectoryUrl());
    view.setInAppMessageWebViewClient(new InAppMessageWebViewClient(context, inAppMessage, mInAppMessageWebViewClientListener));
    view.getMessageWebView().addJavascriptInterface(javascriptInterface, AppboyInAppMessageHtmlFullView.APPBOY_BRIDGE_PREFIX);
    return view;
  }
}
