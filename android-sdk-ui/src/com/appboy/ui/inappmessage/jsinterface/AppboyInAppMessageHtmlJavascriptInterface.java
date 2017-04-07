package com.appboy.ui.inappmessage.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.support.AppboyLogger;
import com.facebook.common.internal.VisibleForTesting;

import org.json.JSONObject;

import java.math.BigDecimal;

// Used to generate the javascript API in html in-app messages.  See https://documentation.appboy.com for more information.
public class AppboyInAppMessageHtmlJavascriptInterface {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageHtmlJavascriptInterface.class.getName());

  private Context mContext;
  private AppboyInAppMessageHtmlUserJavascriptInterface mUserInterface;

  public AppboyInAppMessageHtmlJavascriptInterface(Context context) {
    mContext = context;
    mUserInterface = new AppboyInAppMessageHtmlUserJavascriptInterface(context);
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