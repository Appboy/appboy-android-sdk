package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageHtml;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;
import com.appboy.ui.inappmessage.jsinterface.AppboyInAppMessageHtmlJavascriptInterface;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlFullView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlView;

/**
 * An {@link IInAppMessageViewFactory} for {@link InAppMessageHtml} messages.
 */
public class AppboyHtmlViewFactory implements IInAppMessageViewFactory {
  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;

  public AppboyHtmlViewFactory(IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
  }

  @SuppressLint("AddJavascriptInterface")
  @Override
  public AppboyInAppMessageHtmlView createInAppMessageView(Activity activity, IInAppMessage message) {
    final Context context = activity.getApplicationContext();
    AppboyInAppMessageHtmlView view = (AppboyInAppMessageHtmlView) activity.getLayoutInflater()
        .inflate(R.layout.com_appboy_inappmessage_html, null);

    final InAppMessageHtml inAppMessage = (InAppMessageHtml) message;
    final AppboyInAppMessageHtmlJavascriptInterface javascriptInterface = new AppboyInAppMessageHtmlJavascriptInterface(context, inAppMessage);
    view.setWebViewContent(inAppMessage.getMessage());
    view.setInAppMessageWebViewClient(new InAppMessageWebViewClient(activity.getApplicationContext(), inAppMessage, mInAppMessageWebViewClientListener));
    view.getMessageWebView().addJavascriptInterface(javascriptInterface, AppboyInAppMessageHtmlFullView.APPBOY_BRIDGE_PREFIX);
    return view;
  }
}
