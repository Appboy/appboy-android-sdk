package com.braze.push

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.braze.Constants
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.toBundle

object BrazeHuaweiPushHandler {
    /**
     * Consumes an incoming data payload via Huawei if it originated from Braze. If the data payload did
     * not originate from Braze, then this method does nothing and returns false.
     *
     * @param context Application context
     * @param hmsRemoteMessageData The data payload map.
     * @return true iff the Huawei data payload originated from Braze and was consumed. Returns false
     * if the data payload did not originate from Braze or otherwise could not be forwarded or processed.
     */
    fun handleHmsRemoteMessageData(
        context: Context,
        hmsRemoteMessageData: Map<String, String>?
    ): Boolean {
        brazelog(V) { "Handling Huawei remote message: $hmsRemoteMessageData" }

        // This could be null because that's what Huawei's interface has and this maintains
        // backwards compatibility
        if (hmsRemoteMessageData.isNullOrEmpty()) {
            brazelog(W) { "Remote message data was null. Remote message did not originate from Braze." }
            return false
        }

        // Convert to a bundle for the intent passing
        val bundle: Bundle = hmsRemoteMessageData.toBundle()
        if (!bundle.containsKey(Constants.BRAZE_PUSH_BRAZE_KEY) || "true" != bundle[Constants.BRAZE_PUSH_BRAZE_KEY]) {
            brazelog(I) { "Remote message did not originate from Braze. Not consuming remote message" }
            return false
        }
        brazelog(I) { "Got remote message from Huawei: $bundle" }
        val pushIntent = Intent(BrazePushReceiver.HMS_PUSH_SERVICE_ROUTING_ACTION)
        pushIntent.putExtras(bundle)
        BrazePushReceiver.handleReceivedIntent(context, pushIntent)
        return true
    }
}
