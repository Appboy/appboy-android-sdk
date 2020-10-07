package com.braze.hms_sample

import android.app.Application
import android.content.Context
import android.util.Log

import com.appboy.Appboy
import com.appboy.AppboyLifecycleCallbackListener
import com.appboy.configuration.AppboyConfig
import com.appboy.support.AppboyLogger

class HmsApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    AppboyLogger.setLogLevel(Log.VERBOSE)

    // Clear it out
    Appboy.configure(this.applicationContext, null)

    // Get the custom env info, if it's there
    val prefs = this.applicationContext.getSharedPreferences(CUSTOM_ENV_PREFS_NAME, Context.MODE_PRIVATE)
    if (prefs.contains(CUSTOM_ENV_ENDPOINT_KEY) && prefs.contains(CUSTOM_ENV_API_KEY_KEY)) {
      val apiKey = prefs.getString(CUSTOM_ENV_API_KEY_KEY, "")
      val endpoint = prefs.getString(CUSTOM_ENV_ENDPOINT_KEY, "")

      Appboy.configure(this.applicationContext, AppboyConfig.Builder()
          .setApiKey(apiKey)
          .setCustomEndpoint(endpoint)
          .setHandlePushDeepLinksAutomatically(true)
          .build()
      )
    } else {
      Appboy.configure(this.applicationContext, AppboyConfig.Builder()
          .setApiKey("71125d96-05fb-4df2-80ff-fca220d9d33b")
          .setHandlePushDeepLinksAutomatically(true)
          .build()
      )
    }

    registerActivityLifecycleCallbacks(AppboyLifecycleCallbackListener())
  }

  companion object {
    val CUSTOM_ENV_PREFS_NAME = "custom_env_prefs"
    val CUSTOM_ENV_ENDPOINT_KEY = "custom_env_endpoint"
    val CUSTOM_ENV_API_KEY_KEY = "custom_env_api_key"
  }
}
