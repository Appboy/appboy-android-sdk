package com.appboy.sample

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.multidex.MultiDex
import com.appboy.events.BrazeSdkAuthenticationErrorEvent
import com.appboy.support.PackageUtils
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.configuration.BrazeConfig
import com.braze.enums.BrazeSdkMetadata
import com.braze.support.BrazeLogger
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.getPrettyPrintedString
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class DroidboyApplication : Application() {
    private var isSdkAuthEnabled: Boolean = false

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            activateStrictMode()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val logLevel = applicationContext.getSharedPreferences(getString(R.string.log_level_dialog_title), Context.MODE_PRIVATE)
            .getInt(getString(R.string.current_log_level), Log.VERBOSE)
        BrazeLogger.logLevel = logLevel
        val sharedPreferences = applicationContext.getSharedPreferences(getString(R.string.shared_prefs_location), Context.MODE_PRIVATE)
        Braze.configure(this, null)
        val brazeConfigBuilder = BrazeConfig.Builder()
        brazeConfigBuilder.setSdkMetadata(EnumSet.of(BrazeSdkMetadata.MANUAL))
        setOverrideApiKeyIfConfigured(sharedPreferences, brazeConfigBuilder)
        setOverrideEndpointIfConfigured(sharedPreferences, brazeConfigBuilder)
        isSdkAuthEnabled = setSdkAuthIfConfigured(sharedPreferences, brazeConfigBuilder)
        Braze.configure(this, brazeConfigBuilder.build())
        Braze.addSdkMetadata(this, EnumSet.of(BrazeSdkMetadata.BRANCH))

        registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())
        setupNotificationChannels()
        setupFirebaseCrashlytics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            setupChatDynamicShortcut()
        }

        if (isSdkAuthEnabled) {
            Braze.getInstance(applicationContext).subscribeToSdkAuthenticationFailures { message: BrazeSdkAuthenticationErrorEvent ->
                brazelog(TAG) { "Got sdk auth error message $message" }
                message.userId?.let { setNewSdkAuthToken(it) }
            }
            // Fire off an update to start off
            Braze.getInstance(applicationContext).currentUser?.userId?.let { setNewSdkAuthToken(it) }
        }
    }

    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            MultiDex.install(this)
        }
    }

    /**
     * Calls [Braze.changeUser] with a new SDK Auth token, if available. If no
     * token is available, just calls [Braze.changeUser] without a new SDK Auth token.
     */
    fun changeUserWithNewSdkAuthToken(userId: String) {
        runBlocking(Dispatchers.IO) {
            val token = getSdkAuthToken(userId)
            if (token != null) {
                Braze.getInstance(applicationContext).changeUser(userId, token)
            } else {
                Braze.getInstance(applicationContext).changeUser(userId)
            }
        }
    }

    private fun setNewSdkAuthToken(userId: String) {
        runBlocking(Dispatchers.IO) {
            val token = getSdkAuthToken(userId) ?: return@runBlocking
            Braze.getInstance(applicationContext).setSdkAuthenticationSignature(token)
        }
    }

    private suspend fun getSdkAuthToken(userId: String): String? {
        if (!isSdkAuthEnabled) return null

        return withContext(Dispatchers.IO) {
            brazelog(TAG) { "Making new SDK Auth token request for user: '$userId'" }

            val url = URL(BuildConfig.SDK_AUTH_ENDPOINT)
            val payload = JSONObject()
                .put(
                    "data",
                    JSONObject()
                        .put("user_id", userId)
                )

            with(url.openConnection() as HttpURLConnection) {
                TrafficStats.setThreadStatsTag(1337)
                requestMethod = "POST"
                addRequestProperty("Content-Type", "application/json")
                addRequestProperty("Accept", "application/json")

                OutputStreamWriter(outputStream).use { out -> out.write(payload.toString()) }

                val responseJson = JSONObject(inputStream.bufferedReader().readText())
                brazelog(TAG) { "SDK auth callback got response: ${responseJson.getPrettyPrintedString()}" }
                return@withContext responseJson.optJSONObject("data")?.optString("token")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun setupChatDynamicShortcut() {
        val builder = ShortcutInfo.Builder(this, "droidboy_dynamic_shortcut_chat_id")
            .setShortLabel("Braze Chat")
            .setLongLabel("Conversational Push")
            .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_send))
            .setIntent(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.braze.com?dynamicshortcut=true")
                )
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setLongLived(true)
        }

        val shortcutManager: ShortcutManager = getSystemService(ShortcutManager::class.java)
        shortcutManager.dynamicShortcuts = listOf(builder.build())
    }

    private fun setupNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationGroup(notificationManager, R.string.droidboy_notification_group_01_id, R.string.droidboy_notification_group_01_name)
        createNotificationChannel(
            notificationManager, R.string.droidboy_notification_channel_01_id, R.string.droidboy_notification_channel_messages_name,
            R.string.droidboy_notification_channel_messages_desc, R.string.droidboy_notification_group_01_id
        )
        createNotificationChannel(
            notificationManager, R.string.droidboy_notification_channel_02_id, R.string.droidboy_notification_channel_matches_name,
            R.string.droidboy_notification_channel_matches_desc, R.string.droidboy_notification_group_01_id
        )
        createNotificationChannel(
            notificationManager, R.string.droidboy_notification_channel_03_id, R.string.droidboy_notification_channel_offers_name,
            R.string.droidboy_notification_channel_offers_desc, R.string.droidboy_notification_group_01_id
        )
        createNotificationChannel(
            notificationManager, R.string.droidboy_notification_channel_04_id, R.string.droidboy_notification_channel_recommendations_name,
            R.string.droidboy_notification_channel_recommendations_desc, R.string.droidboy_notification_group_01_id
        )
    }

    @SuppressLint("NewApi")
    private fun createNotificationGroup(notificationManager: NotificationManager, idResource: Int, nameResource: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    getString(idResource),
                    getString(nameResource)
                )
            )
        }
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel(notificationManager: NotificationManager, idResource: Int, nameResource: Int, descResource: Int, groupResource: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(idResource),
                getString(nameResource),
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = getString(descResource)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.group = getString(groupResource)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("NewApi")
    private fun activateStrictMode() {
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()

        // We are explicitly not detecting detectLeakedClosableObjects(), detectLeakedSqlLiteObjects(), and detectUntaggedSockets()
        // The okhttp library used on most https calls trips the detectUntaggedSockets() check
        // com.google.android.gms.internal trips both the detectLeakedClosableObjects() and detectLeakedSqlLiteObjects() checks
        val vmPolicyBuilder = VmPolicy.Builder()
            .detectActivityLeaks()
            .penaltyLog()

        // Note that some detections require a specific sdk version or higher to enable.
        vmPolicyBuilder.detectLeakedRegistrationObjects()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            vmPolicyBuilder.detectFileUriExposure()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            vmPolicyBuilder.detectCleartextNetwork()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vmPolicyBuilder.detectContentUriWithoutPermission()
            vmPolicyBuilder.detectUntaggedSockets()
        }
        StrictMode.setThreadPolicy(threadPolicyBuilder.build())
        StrictMode.setVmPolicy(vmPolicyBuilder.build())
        StrictMode.allowThreadDiskReads()
        StrictMode.allowThreadDiskWrites()
    }

    private fun setOverrideApiKeyIfConfigured(sharedPreferences: SharedPreferences, config: BrazeConfig.Builder) {
        val overrideApiKey = sharedPreferences.getString(OVERRIDE_API_KEY_PREF_KEY, null)
        if (!overrideApiKey.isNullOrBlank()) {
            Log.i(TAG, String.format("Override API key found, configuring Braze with override key %s.", overrideApiKey))
            config.setApiKey(overrideApiKey)
            overrideApiKeyInUse = overrideApiKey
        }
    }

    private fun setOverrideEndpointIfConfigured(sharedPreferences: SharedPreferences, config: BrazeConfig.Builder) {
        val overrideEndpoint = sharedPreferences.getString(OVERRIDE_ENDPOINT_PREF_KEY, null)
        if (!overrideEndpoint.isNullOrBlank()) {
            Log.i(TAG, String.format("Override endpoint found, configuring Braze with override endpoint %s.", overrideEndpoint))
            config.setCustomEndpoint(overrideEndpoint)
        }
    }

    private fun setSdkAuthIfConfigured(sharedPreferences: SharedPreferences, config: BrazeConfig.Builder): Boolean {
        // Default to true for testing dogfood purposes
        val isOverridingSdkAuth = sharedPreferences.getBoolean(ENABLE_SDK_AUTH_PREF_KEY, true)
        config.setIsSdkAuthenticationEnabled(isOverridingSdkAuth)
        return isOverridingSdkAuth
    }

    private fun setupFirebaseCrashlytics() {
        // Only enable crash logging for the play store deployed released builds
        val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        firebaseCrashlytics.setCrashlyticsCollectionEnabled(BuildConfig.IS_DROIDBOY_RELEASE_BUILD)
        firebaseCrashlytics.setCustomKey("build_time", BuildConfig.BUILD_TIME)
        firebaseCrashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
        firebaseCrashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME)
    }

    companion object {
        private val TAG = BrazeLogger.getBrazeLogTag(DroidboyApplication::class.java)
        private var overrideApiKeyInUse: String? = null
        const val OVERRIDE_API_KEY_PREF_KEY = "override_api_key"
        const val OVERRIDE_ENDPOINT_PREF_KEY = "override_endpoint_url"
        const val ENABLE_SDK_AUTH_PREF_KEY = "enable_sdk_auth_if_present_pref_key"

        @JvmStatic
        fun getApiKeyInUse(context: Context): String? {
            return if (!overrideApiKeyInUse.isNullOrBlank()) {
                overrideApiKeyInUse
            } else {
                // Check if the api key is in resources
                readStringResourceValue(context, "com_appboy_api_key", "NO-API-KEY-SET")
            }
        }

        private fun readStringResourceValue(context: Context, key: String?, defaultValue: String): String {
            return try {
                if (key == null) {
                    return defaultValue
                }
                val resId = context.resources.getIdentifier(key, "string", PackageUtils.getResourcePackageName(context))
                if (resId == 0) {
                    brazelog {
                        "Unable to find the xml string value with key $key. " +
                            "Using default value '$defaultValue'."
                    }
                    defaultValue
                } else {
                    context.resources.getString(resId)
                }
            } catch (ignored: Exception) {
                brazelog {
                    "Unexpected exception retrieving the xml string configuration" +
                        " value with key $key. Using default value $defaultValue'."
                }
                defaultValue
            }
        }
    }
}
