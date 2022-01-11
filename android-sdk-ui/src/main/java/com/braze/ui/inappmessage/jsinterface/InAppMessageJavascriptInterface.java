package com.braze.ui.inappmessage.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.braze.Braze;
import com.braze.models.inappmessage.IInAppMessageHtml;
import com.braze.models.outgoing.BrazeProperties;
import com.braze.support.BrazeLogger;

import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Used to generate the javascript API in html in-app messages.
 */
public class InAppMessageJavascriptInterface {
  private static final String TAG = BrazeLogger.getBrazeLogTag(InAppMessageJavascriptInterface.class);

  private final Context mContext;
  private final InAppMessageUserJavascriptInterface mUserInterface;
  private final IInAppMessageHtml mInAppMessage;

  public InAppMessageJavascriptInterface(Context context, @NonNull IInAppMessageHtml inAppMessage) {
    mContext = context;
    mUserInterface = new InAppMessageUserJavascriptInterface(context);
    mInAppMessage = inAppMessage;
  }

  @JavascriptInterface
  public void requestImmediateDataFlush() {
    Braze.getInstance(mContext).requestImmediateDataFlush();
  }

  @JavascriptInterface
  public void logCustomEventWithJSON(String eventName, String propertiesJSON) {
    BrazeProperties brazeProperties = parseProperties(propertiesJSON);
    Braze.getInstance(mContext).logCustomEvent(eventName, brazeProperties);
  }

  @JavascriptInterface
  public void logPurchaseWithJSON(String productId, double price, String currencyCode, int quantity, String propertiesJSON) {
    BrazeProperties brazeProperties = parseProperties(propertiesJSON);
    Braze.getInstance(mContext).logPurchase(productId, currencyCode, new BigDecimal(Double.toString(price)), quantity, brazeProperties);
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
  public InAppMessageUserJavascriptInterface getUser() {
    return mUserInterface;
  }

  @VisibleForTesting
  BrazeProperties parseProperties(String propertiesJSON) {
    try {
      if (propertiesJSON != null && !propertiesJSON.equals("undefined")
          && !propertiesJSON.equals("null")) {
        return new BrazeProperties(new JSONObject(propertiesJSON));
      }
    } catch (Exception e) {
      BrazeLogger.e(TAG, "Failed to parse properties JSON String: " + propertiesJSON, e);
    }

    return null;
  }
}
