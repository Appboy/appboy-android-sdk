package com.braze.hms_sample

import android.app.Application
import android.content.Context
import android.util.Log
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.configuration.BrazeConfig
import com.braze.support.BrazeLogger

class HmsApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    BrazeLogger.setLogLevel(Log.VERBOSE)

    // Clear it out
    Braze.configure(this.applicationContext, null)

    // Get the custom env info, if it's there
    val prefs = this.applicationContext.getSharedPreferences(CUSTOM_ENV_PREFS_NAME, Context.MODE_PRIVATE)
    if (prefs.contains(CUSTOM_ENV_ENDPOINT_KEY) && prefs.contains(CUSTOM_ENV_API_KEY_KEY)) {
      val apiKey = prefs.getString(CUSTOM_ENV_API_KEY_KEY, "")
      val endpoint = prefs.getString(CUSTOM_ENV_ENDPOINT_KEY, "")

      Braze.configure(this.applicationContext, BrazeConfig.Builder()
          .setApiKey(apiKey)
          .setCustomEndpoint(endpoint)
          .setHandlePushDeepLinksAutomatically(true)
          .build()
      )
    } else {
      Braze.configure(this.applicationContext, BrazeConfig.Builder()
          .setApiKey("71125d96-05fb-4df2-80ff-fca220d9d33b")
          .setHandlePushDeepLinksAutomatically(true)
          .build()
      )
    }

    registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())
  }

  companion object {
    val CUSTOM_ENV_PREFS_NAME = "custom_env_prefs"
    val CUSTOM_ENV_ENDPOINT_KEY = "custom_env_endpoint"
    val CUSTOM_ENV_API_KEY_KEY = "custom_env_api_key"
  }
}
