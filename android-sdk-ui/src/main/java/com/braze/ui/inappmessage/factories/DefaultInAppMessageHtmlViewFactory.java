package com.braze.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.appboy.ui.R;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.InAppMessageHtml;
import com.braze.support.BrazeLogger;
import com.braze.ui.inappmessage.IInAppMessageViewFactory;
import com.braze.ui.inappmessage.jsinterface.InAppMessageJavascriptInterface;
import com.braze.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.braze.ui.inappmessage.utils.InAppMessageWebViewClient;
import com.braze.ui.inappmessage.views.InAppMessageHtmlFullView;
import com.braze.ui.inappmessage.views.InAppMessageHtmlView;
import com.braze.ui.support.ViewUtils;

/**
 * An {@link IInAppMessageViewFactory} for {@link InAppMessageHtml} messages.
 */
public class DefaultInAppMessageHtmlViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = DefaultInAppMessageHtmlViewFactory.class.getName();

  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;

  public DefaultInAppMessageHtmlViewFactory(IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
  }

  @SuppressLint("AddJavascriptInterface")
  @Override
  public InAppMessageHtmlView createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage message) {
    final Context context = activity.getApplicationContext();
    InAppMessageHtmlView view = (InAppMessageHtmlView) activity.getLayoutInflater()
        .inflate(R.layout.com_braze_inappmessage_html, null);

    BrazeConfigurationProvider config = new BrazeConfigurationProvider(context);
    if (config.getIsTouchModeRequiredForHtmlInAppMessages() && ViewUtils.isDeviceNotInTouchMode(view)) {
      BrazeLogger.w(TAG, "The device is not currently in touch mode. This message requires user touch interaction to display properly.");
      return null;
    }
    final InAppMessageHtml inAppMessage = (InAppMessageHtml) message;
    final InAppMessageJavascriptInterface javascriptInterface = new InAppMessageJavascriptInterface(context, inAppMessage);
    view.setWebViewContent(inAppMessage.getMessage());
    view.setInAppMessageWebViewClient(new InAppMessageWebViewClient(activity.getApplicationContext(), inAppMessage, mInAppMessageWebViewClientListener));
    view.getMessageWebView().addJavascriptInterface(javascriptInterface, InAppMessageHtmlFullView.BRAZE_BRIDGE_PREFIX);
    return view;
  }
}
