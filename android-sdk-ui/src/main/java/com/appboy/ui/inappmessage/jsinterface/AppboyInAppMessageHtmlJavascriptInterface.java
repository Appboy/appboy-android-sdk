package com.appboy.ui.inappmessage.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.appboy.Appboy;
import com.appboy.models.IInAppMessageHtml;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.support.AppboyLogger;

import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Used to generate the javascript API in html in-app messages.
 */
public class AppboyInAppMessageHtmlJavascriptInterface {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageHtmlJavascriptInterface.class);

  private final Context mContext;
  private final AppboyInAppMessageHtmlUserJavascriptInterface mUserInterface;
  private final IInAppMessageHtml mInAppMessage;

  public AppboyInAppMessageHtmlJavascriptInterface(Context context, @NonNull IInAppMessageHtml inAppMessage) {
    mContext = context;
    mUserInterface = new AppboyInAppMessageHtmlUserJavascriptInterface(context);
    mInAppMessage = inAppMessage;
  }

  @JavascriptInterface
  public void requestImmediateDataFlush() {
    Appboy.getInstance(mContext).requestImmediateDataFlush();
  }

  @JavascriptInterface
  public void logCustomEventWithJSON(String eventName, String propertiesJSON) {
    AppboyProperties appboyProperties = parseProperties(propertiesJSON);
    Appboy.getInstance(mContext).logCustomEvent(eventName, appboyProperties);
  }

  @JavascriptInterface
  public void logPurchaseWithJSON(String productId, double price, String currencyCode, int quantity, String propertiesJSON) {
    AppboyProperties appboyProperties = parseProperties(propertiesJSON);
    Appboy.getInstance(mContext).logPurchase(productId, currencyCode, new BigDecimal(Double.toString(price)), quantity, appboyProperties);
  }

  @JavascriptInterface
  public void logButtonClick(String buttonId) {
    mInAppMessage.logButtonClick(buttonId);
  }

  @JavascriptInterface
  public void logClick() {
    mInAppMessage.logClick();
  }

  @JavascriptInterface
  public AppboyInAppMessageHtmlUserJavascriptInterface getUser() {
    return mUserInterface;
  }

  @VisibleForTesting
  AppboyProperties parseProperties(String propertiesJSON) {
    try {
      if (propertiesJSON != null && !propertiesJSON.equals("undefined")
          && !propertiesJSON.equals("null")) {
        return new AppboyProperties(new JSONObject(propertiesJSON));
      }
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Failed to parse properties JSON String: " + propertiesJSON, e);
    }

    return null;
  }
}
