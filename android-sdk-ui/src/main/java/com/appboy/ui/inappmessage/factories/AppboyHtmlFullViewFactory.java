package com.appboy.ui.inappmessage.factories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageHtmlFull;
import com.appboy.ui.R;
import com.appboy.ui.inappmessage.IInAppMessageViewFactory;
import com.appboy.ui.inappmessage.InAppMessageWebViewClient;
import com.appboy.ui.inappmessage.jsinterface.AppboyInAppMessageHtmlJavascriptInterface;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlFullView;
import com.appboy.ui.support.ViewUtils;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;

public class AppboyHtmlFullViewFactory implements IInAppMessageViewFactory {
  private static final String TAG = BrazeLogger.getBrazeLogTag(AppboyHtmlFullViewFactory.class);

  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener;

  public AppboyHtmlFullViewFactory(IInAppMessageWebViewClientListener inAppMessageWebViewClientListener) {
    mInAppMessageWebViewClientListener = inAppMessageWebViewClientListener;
  }

  @SuppressLint("AddJavascriptInterface")
  @Override
  public AppboyInAppMessageHtmlFullView createInAppMessageView(@NonNull Activity activity, @NonNull IInAppMessage inAppMessage) {
    AppboyInAppMessageHtmlFullView view = (AppboyInAppMessageHtmlFullView) activity.getLayoutInflater()
        .inflate(R.layout.com_appboy_inappmessage_html_full, null);
    BrazeConfigurationProvider config = new BrazeConfigurationProvider(activity.getApplicationContext());
    if (config.getIsTouchModeRequiredForHtmlInAppMessages() && ViewUtils.isDeviceNotInTouchMode(view)) {
      BrazeLogger.w(TAG, "The device is not currently in touch mode. This message requires user touch interaction to display properly.");
      return null;
    }
    final Context context = activity.getApplicationContext();
    final InAppMessageHtmlFull inAppMessageHtmlFull = (InAppMessageHtmlFull) inAppMessage;
    final AppboyInAppMessageHtmlJavascriptInterface javascriptInterface = new AppboyInAppMessageHtmlJavascriptInterface(context, inAppMessageHtmlFull);
    view.setWebViewContent(inAppMessage.getMessage(), inAppMessageHtmlFull.getLocalAssetsDirectoryUrl());
    view.setInAppMessageWebViewClient(new InAppMessageWebViewClient(context, inAppMessage, mInAppMessageWebViewClientListener));
    view.getMessageWebView().addJavascriptInterface(javascriptInterface, AppboyInAppMessageHtmlFullView.BRAZE_BRIDGE_PREFIX);
    return view;
  }
}
