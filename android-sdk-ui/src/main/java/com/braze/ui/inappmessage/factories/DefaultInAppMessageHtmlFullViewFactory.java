package com.braze.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.appboy.ui.R;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.InAppMessageHtmlFull;
import com.braze.support.BrazeLogger;
import com.braze.ui.inappmessage.IInAppMessageViewFactory;
import com.braze.ui.inappmessage.jsinterface.InAppMessageJavascriptInterface;
import com.braze.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.braze.ui.inappmessage.utils.InAppMessageWebViewClient;
import com.braze.ui.inappmessage.views.InAppMessageHtmlFullView;
import com.braze.ui.support.ViewUtils;

public class DefaultInAppMessageHtmlFullViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = BrazeLogger.getBrazeLogTag(DefaultInAppMessageHtmlFullViewFactory.class);

  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;

  public DefaultInAppMessageHtmlFullViewFactory(IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
  }

  @SuppressLint("AddJavascriptInterface")
  @Override
  public InAppMessageHtmlFullView createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage inAppMessage) {
    InAppMessageHtmlFullView view = (InAppMessageHtmlFullView) activity.getLayoutInflater()
        .inflate(R.layout.com_braze_inappmessage_html_full, null);
    BrazeConfigurationProvider config = new BrazeConfigurationProvider(activity.getApplicationContext());
    if (config.getIsTouchModeRequiredForHtmlInAppMessages() && ViewUtils.isDeviceNotInTouchMode(view)) {
      BrazeLogger.w(TAG, "The device is not currently in touch mode. This message requires user touch interaction to display properly.");
      return null;
    }
    final Context context = activity.getApplicationContext();
    final InAppMessageHtmlFull inAppMessageHtmlFull = (InAppMessageHtmlFull) inAppMessage;
    final InAppMessageJavascriptInterface javascriptInterface = new InAppMessageJavascriptInterface(context, inAppMessageHtmlFull);
    view.setWebViewContent(inAppMessage.getMessage(), inAppMessageHtmlFull.getLocalAssetsDirectoryUrl());
    view.setInAppMessageWebViewClient(new InAppMessageWebViewClient(context, inAppMessage, mInAppMessageWebViewClientListener));
    view.getMessageWebView().addJavascriptInterface(javascriptInterface, InAppMessageHtmlFullView.BRAZE_BRIDGE_PREFIX);
    return view;
  }
}
