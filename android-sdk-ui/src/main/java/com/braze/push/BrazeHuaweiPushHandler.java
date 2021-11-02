package com.braze.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.appboy.Constants;
import com.braze.support.BrazeLogger;
import com.braze.support.BundleUtils;

import java.util.Map;

public class BrazeHuaweiPushHandler {
  private static final String TAG = BrazeLogger.getBrazeLogTag(BrazeHuaweiPushHandler.class);

  /**
   * Consumes an incoming data payload via Huawei if it originated from Braze. If the data payload did
   * not originate from Braze, then this method does nothing and returns false.
   *
   * @param context Application context
   * @param hmsRemoteMessageData The data payload map.
   * @return true iff the Huawei data payload originated from Braze and was consumed. Returns false
   *         if the data payload did not originate from Braze or otherwise
   *         could not be forwarded or processed.
   */
  public static boolean handleHmsRemoteMessageData(Context context, Map<String, String> hmsRemoteMessageData) {
    BrazeLogger.v(TAG, "Handling Huawei remote message: " + hmsRemoteMessageData);
    if (hmsRemoteMessageData == null || hmsRemoteMessageData.isEmpty()) {
      BrazeLogger.w(TAG, "Remote message data was null. Remote message did not originate from Braze.");
      return false;
    }

    // Convert to a bundle for the intent passing
    final Bundle bundle = BundleUtils.toBundle(hmsRemoteMessageData);
    if (bundle == null) {
      BrazeLogger.w(TAG, "Converted bundle data was null. Not handling as Braze push.");
      return false;
    }

    if (!bundle.containsKey(Constants.APPBOY_PUSH_APPBOY_KEY) || !"true".equals(bundle.get(Constants.APPBOY_PUSH_APPBOY_KEY))) {
      BrazeLogger.i(TAG, "Remote message did not originate from Braze. Not consuming remote message");
      return false;
    }

    BrazeLogger.i(TAG, "Got remote message from Huawei: " + bundle);

    Intent pushIntent = new Intent(BrazePushReceiver.HMS_PUSH_SERVICE_ROUTING_ACTION);
    pushIntent.putExtras(bundle);
    BrazePushReceiver.handleReceivedIntent(context, pushIntent);
    return true;
  }
}
