package com.braze.hms_sample

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.appboy.Appboy
import com.appboy.AppboyUser
import com.appboy.events.SimpleValueCallback
import com.appboy.support.AppboyLogger
import com.appboy.support.IntentUtils
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.aaid.HmsInstanceId
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

  companion object {
    private val TAG: String = this::class.java.name
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val userIdInput = findViewById<EditText>(R.id.etUserId)
    Appboy.getInstance(this).getCurrentUser(object : SimpleValueCallback<AppboyUser?>() {
      override fun onSuccess(value: AppboyUser) {
        userIdInput.post {
          userIdInput.setText(value.userId)
        }
      }
    })
    findViewById<Button>(R.id.bSubmitUserId).setOnClickListener {
      // Get the user id
      val userId: String = userIdInput.text.toString()
      Appboy.getInstance(this).changeUser(userId)
    }

    findViewById<Button>(R.id.bGetAndSendToken).setOnClickListener {
      getToken(this)
    }

    findViewById<Button>(R.id.bResetCustomEnvironment).setOnClickListener {
      deleteEnvPrefs()
      restartApp(this)
    }

    findViewById<Button>(R.id.bSetCustomEnvironment).setOnClickListener {
      val customApiKey: String = findViewById<EditText>(R.id.etSetCustomApiKey).text.toString()
      val customEndpoint: String = findViewById<EditText>(R.id.etSetCustomEndpoint).text.toString()
      setCustomEnv(customApiKey, customEndpoint)
      restartApp(this)
    }
  }

  @SuppressLint("ApplySharedPref")
  private fun setCustomEnv(apiKey: String, endpoint: String) {
    // Commit since we're restarting immediately afterwards
    this.getSharedPreferences(HmsApplication.CUSTOM_ENV_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(HmsApplication.CUSTOM_ENV_ENDPOINT_KEY, endpoint)
        .putString(HmsApplication.CUSTOM_ENV_API_KEY_KEY, apiKey)
        .commit()
  }

  @SuppressLint("ApplySharedPref")
  private fun deleteEnvPrefs() {
    // Commit since we're restarting immediately afterwards
    this.getSharedPreferences(HmsApplication.CUSTOM_ENV_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .clear()
        .commit()
  }

  /**
   * Obtain a token.
   */
  private fun getToken(context: Context) {
    thread(start = true) {
      try {
        val appId = AGConnectServicesConfig.fromContext(context).getString("client/app_id")
        val pushToken = HmsInstanceId.getInstance(context).getToken(appId, "HCM")
        Appboy.getInstance(context).registerAppboyPushMessages(pushToken!!)
        Log.i(TAG, "Got HMS push token $pushToken")
      } catch (e: Exception) {
        Log.e(TAG, "getToken failed, $e", e)
      }
    }
  }

  private fun restartApp(context: Context) {
    val startActivity = Intent(this, MainActivity::class.java)
    val pendingIntentId = 109829837
    val pendingIntent = PendingIntent.getActivity(context, pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT or IntentUtils.getDefaultPendingIntentFlags())
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager[AlarmManager.RTC, System.currentTimeMillis() + 1000] = pendingIntent
    AppboyLogger.i(TAG, "Restarting application to apply new environment values")
    System.exit(0)
  }
}
