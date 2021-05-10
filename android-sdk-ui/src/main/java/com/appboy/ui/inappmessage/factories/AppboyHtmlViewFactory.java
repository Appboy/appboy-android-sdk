package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageHtml;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;
import com.appboy.ui.inappmessage.jsinterface.AppboyInAppMessageHtmlJavascriptInterface;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlFullView;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlView;
import com.appboy.ui.support.ViewUtils;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;

/**
 * An {@link IInAppMessageViewFactory} for {@link InAppMessageHtml} messages.
 */
public class AppboyHtmlViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = AppboyHtmlViewFactory.class.getName();

  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;

  public AppboyHtmlViewFactory(IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
  }

  @SuppressLint("AddJavascriptInterface")
  @Override
  public AppboyInAppMessageHtmlView createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage message) {
    final Context context = activity.getApplicationContext();
    AppboyInAppMessageHtmlView view = (AppboyInAppMessageHtmlView) activity.getLayoutInflater()
        .inflate(R.layout.com_appboy_inappmessage_html, null);

    BrazeConfigurationProvider config = new BrazeConfigurationProvider(context);
    if (config.getIsTouchModeRequiredForHtmlInAppMessages() && ViewUtils.isDeviceNotInTouchMode(view)) {
      BrazeLogger.w(TAG, "The device is not currently in touch mode. This message requires user touch interaction to display properly.");
      return null;
    }
    final InAppMessageHtml inAppMessage = (InAppMessageHtml) message;
    final AppboyInAppMessageHtmlJavascriptInterface javascriptInterface = new AppboyInAppMessageHtmlJavascriptInterface(context, inAppMessage);
    view.setWebViewContent(inAppMessage.getMessage());
    view.setInAppMessageWebViewClient(new InAppMessageWebViewClient(activity.getApplicationContext(), inAppMessage, mInAppMessageWebViewClientListener));
    view.getMessageWebView().addJavascriptInterface(javascriptInterface, AppboyInAppMessageHtmlFullView.BRAZE_BRIDGE_PREFIX);
    return view;
  }
}
