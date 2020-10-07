package com.braze.hms_sample

import android.util.Log
import com.appboy.Appboy
import com.appboy.push.AppboyHuaweiPushHandler
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class CustomPushService: HmsMessageService() {

  companion object {
    val TAG: String = toString()
  }

  override fun onNewToken(token: String?) {
    super.onNewToken(token)

    val appId = AGConnectServicesConfig.fromContext(applicationContext).getString("client/app_id")
    val pushToken = HmsInstanceId.getInstance(applicationContext).getToken(appId, "HCM")
    Log.i(TAG, "Got Huawei push token $pushToken")
    Appboy.getInstance(applicationContext).registerAppboyPushMessages(token!!)
  }

  override fun onMessageReceived(hmsRemoteMessage: RemoteMessage?) {
    super.onMessageReceived(hmsRemoteMessage)

    if (AppboyHuaweiPushHandler.handleHmsRemoteMessageData(applicationContext, hmsRemoteMessage?.dataOfMap)) {
      Log.i(TAG, "Braze has handled Huawei push notification.")
    }
  }
}
