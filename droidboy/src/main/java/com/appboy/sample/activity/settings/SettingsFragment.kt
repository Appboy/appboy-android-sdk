package com.appboy.sample.activity.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.appboy.BrazeInternal
import com.appboy.Constants
import com.appboy.events.SimpleValueCallback
import com.appboy.models.outgoing.AttributionData
import com.appboy.sample.*
import com.appboy.sample.imageloading.GlideAppboyImageLoader
import com.appboy.sample.logging.CustomEventDialog
import com.appboy.sample.logging.CustomPurchaseDialog
import com.appboy.sample.logging.CustomUserAttributeDialog
import com.appboy.sample.subscriptions.EmailSubscriptionStateDialog
import com.appboy.sample.subscriptions.PushSubscriptionStateDialog
import com.appboy.sample.util.ContentCardsTestingUtil.Companion.createRandomCards
import com.appboy.sample.util.LifecycleUtils
import com.appboy.sample.util.LogcatExportUtil.Companion.exportLogcatToFile
import com.appboy.sample.util.RuntimePermissionUtils
import com.appboy.ui.feed.AppboyFeedManager
import com.braze.Braze
import com.braze.BrazeUser
import com.braze.images.DefaultBrazeImageLoader
import com.braze.images.IBrazeImageLoader
import com.braze.support.BrazeLogger

@SuppressLint("ApplySharedPref")
class SettingsFragment : PreferenceFragmentCompat() {
  private lateinit var mGlideAppboyImageLoader: IBrazeImageLoader
  private lateinit var mImageLoader: IBrazeImageLoader
  private lateinit var mSharedPreferences: SharedPreferences

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.preferences, rootKey)
    val context = this.requireContext()

    mGlideAppboyImageLoader = GlideAppboyImageLoader()
    mImageLoader = DefaultBrazeImageLoader(context)
    mSharedPreferences = context.getSharedPreferences(getString(R.string.shared_prefs_location), Context.MODE_PRIVATE)

    setContentCardsPrefs(context)
    setSdkAuthPrefs(context)
    setNotchPrefs(context)
    setGdprPrefs(context)
    setNetworkPrefs(context)
    setImageDisplayPrefs(context)
    setSessionPrefs(context)
    setNewsFeedPrefs(context)
    setLocationPrefs(context)
    setMiscellaneousPrefs(context)
    setEnvironmentPrefs(context)
    setAboutInfo(context)
    setCustomLoggingSection(context)
  }

  private fun setSdkAuthPrefs(context: Context) {
    setClickPreference("enable_sdk_auth") {
      val sharedPreferencesEditor = mSharedPreferences.edit()
      sharedPreferencesEditor.putBoolean(DroidboyApplication.ENABLE_SDK_AUTH_PREF_KEY, true)
      sharedPreferencesEditor.commit()
      LifecycleUtils.restartApp(context)
    }
    setClickPreference("disable_sdk_auth") {
      val sharedPreferencesEditor = mSharedPreferences.edit()
      sharedPreferencesEditor.putBoolean(DroidboyApplication.ENABLE_SDK_AUTH_PREF_KEY, false)
      sharedPreferencesEditor.commit()
      LifecycleUtils.restartApp(context)
    }
  }

  private fun setCustomLoggingSection(context: Context) {
    showDialogOnClick("show_user_dialog", UserProfileDialog())
    showDialogOnClick("show_user_attribute_dialog", CustomUserAttributeDialog())
    showDialogOnClick("show_custom_event_dialog", CustomEventDialog())
    showDialogOnClick("show_log_purchase_dialog", CustomPurchaseDialog())
    showDialogOnClick("show_push_subscription_state_dialog", PushSubscriptionStateDialog())
    showDialogOnClick("show_email_subscription_state_dialog", EmailSubscriptionStateDialog())
  }

  private fun setAboutInfo(context: Context) {
    setSummary("sdk_version", Constants.APPBOY_SDK_VERSION)
    DroidboyApplication.getApiKeyInUse(context)?.let { setSummary("api_key", it) }
    setSummary("push_token", Braze.getInstance(context).registeredPushToken ?: "No push token registered")
    setSummary("build_type", BuildConfig.BUILD_TYPE)
    setSummary("version_code", BuildConfig.VERSION_CODE.toString())
    setSummary("build_name", BuildConfig.VERSION_NAME)
    setSummary("build_name", BuildConfig.VERSION_NAME)
    Braze.getInstance(context).runOnUser { user -> this@SettingsFragment.setSummary("current_user_id", user.userId) }
    setSummary("install_time", BuildConfig.BUILD_TIME)
    setSummary("device_id", Braze.getInstance(context).deviceId)
  }

  private fun setEnvironmentPrefs(context: Context) {
    setClickPreference("environment_barcode_picture_intent_key") {
      // Take a picture via intent
      val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      try {
        activity?.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
      } catch (e: Exception) {
        BrazeLogger.e(TAG, "Failed to handle image capture intent", e)
      }
    }
    setClickPreference("environment_reset_key") {
      val sharedPreferencesEditor = mSharedPreferences.edit()
      sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY)
      sharedPreferencesEditor.remove(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY)

      sharedPreferencesEditor.commit()
      LifecycleUtils.restartApp(context)
    }
    setClickPreference("environment_switch_dev") { changeEndpointToDevelopment() }
    showDialogOnClick("show_set_environment_dialog", SetEnvironmentPreference())
  }

  private fun setMiscellaneousPrefs(context: Context) {
    setClickPreference("anonymous_revert") {
      // Note that .commit() is used here since we're restarting the process and
      // thus need to immediately flush all shared prefs changes to disk
      val userSharedPreferences: SharedPreferences = context.getSharedPreferences("com.appboy.offline.storagemap", Context.MODE_PRIVATE)
      userSharedPreferences
          .edit()
          .clear()
          .commit()
      val droidboySharedPrefs: SharedPreferences = context.getSharedPreferences("droidboy", Context.MODE_PRIVATE)
      droidboySharedPrefs
          .edit()
          .remove(MainFragment.USER_ID_KEY)
          .commit()
      LifecycleUtils.restartApp(context)
    }
    setClickPreference("log_attribution") {
      Braze.getInstance(context).runOnUser { user ->
        val uniqueInt = SystemClock.currentThreadTimeMillis() % 1000
        user.setAttributionData(AttributionData("network_val_$uniqueInt",
            "campaign_val_$uniqueInt",
            "adgroup_val_$uniqueInt",
            "creative_val_$uniqueInt"))
        showToast("Attribution data sent to server")
      }
    }
    setClickPreference("logcat_export_file_key") {
      val logcatFileUri = exportLogcatToFile(context)
      val shareIntent = Intent(Intent.ACTION_SEND)
      shareIntent.type = "text/plain"
      shareIntent.putExtra(Intent.EXTRA_STREAM, logcatFileUri)

      // Grant temporary read permission to the content URI
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      startActivity(Intent.createChooser(shareIntent, "Export logcat as a big text file"))
    }
  }

  private fun setLocationPrefs(context: Context) {
    setClickPreference("set_manual_location") {
      Braze.getInstance(context).runOnUser { user ->
        user.setLastKnownLocation(1.0, 2.0, 3.0, 4.0)
        showToast("Manually set location to latitude 1.0d, longitude 2.0d, altitude 3.0m, accuracy 4.0m.")
      }
    }
    setClickPreference("location_runtime_permission_dialog") {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION)
      } else {
        showToast("Below Android M there is no need to check for runtime permissions.")
      }
    }
  }

  private fun setNewsFeedPrefs(context: Context) {
    setSwitchPreference("sort_feed") { newValue: Boolean ->
      val sharedPref: SharedPreferences = context.getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE)
      val editor = sharedPref.edit()
      editor.putBoolean(getString(R.string.sort_feed), newValue)
      editor.apply()
    }
    setSwitchPreference("set_custom_news_feed_card_click_action_listener") { newValue: Boolean ->
      AppboyFeedManager.getInstance().feedCardClickActionListener = if (newValue) CustomFeedClickActionListener() else null
    }
  }

  private fun setSessionPrefs(context: Context) {
    setClickPreference("open_session") { Braze.getInstance(context).openSession(this.activity) }
    setClickPreference("close_session") {
      Braze.getInstance(context).closeSession(this.activity)
      showToast(getString(R.string.close_session_toast))
    }
  }

  private fun setImageDisplayPrefs(context: Context) {
    setClickPreference("glide_image_loader_enable_setting_key") {
      Braze.getInstance(context).imageLoader = mGlideAppboyImageLoader
      showToast("Glide enabled")
    }
    setClickPreference("glide_image_loader_disable_setting_key") {
      Braze.getInstance(context).imageLoader = mImageLoader
      showToast("Glide disabled. Default Image loader in use.")
    }
  }

  private fun setNetworkPrefs(context: Context) {
    setClickPreference("disable_outbound_network_requests") {
      Braze.setOutboundNetworkRequestsOffline(true)
      showToast(getString(R.string.disabled_outbound_network_requests_toast))
    }
    setClickPreference("enable_outbound_network_requests") {
      Braze.setOutboundNetworkRequestsOffline(false)
      showToast(getString(R.string.enabled_outbound_network_requests_toast))
    }
    setClickPreference("data_flush") {
      Braze.getInstance(context).requestImmediateDataFlush()
      showToast(getString(R.string.data_flush_toast))
    }
  }

  private fun setGdprPrefs(context: Context) {
    setClickPreference("wipe_data_preference_key") { Braze.wipeData(context) }
    setClickPreference("enable_sdk_key") { Braze.enableSdk(context) }
    setClickPreference("disable_sdk_key") { Braze.disableSdk(context) }
  }

  private fun setContentCardsPrefs(context: Context) {
    setClickPreference("content_card_dismiss_all_cards_setting_key") {
      val cachedContentCards = Braze.getInstance(context).cachedContentCards
      if (cachedContentCards != null) {
        for (card in cachedContentCards) {
          card.setIsDismissed(true)
        }
      }
    }
    setClickPreference("content_card_populate_random_cards_setting_key") {
      val randomCards = createRandomCards(context, 3)
      val userId = Braze.getInstance(context).currentUser!!.userId
      for (card in randomCards) {
        BrazeInternal.addSerializedContentCardToStorage(context, card.forJsonPut().toString(), userId)
      }
    }
  }

  private fun setNotchPrefs(context: Context) {
    setSwitchPreference("display_in_full_cutout_setting_key") { newValue: Boolean ->
      // Restart the app to force onCreate() to re-run
      // Note that an app restart won't commit prefs changes so we have to do it manually
      PreferenceManager.getDefaultSharedPreferences(context).edit()
          .putBoolean("display_in_full_cutout_setting_key", newValue)
          .commit()
      LifecycleUtils.restartApp(context)
    }
    setSwitchPreference("display_no_limits_setting_key") { newValue: Boolean ->
      // Restart the app to force onCreate() to re-run
      // Note that an app restart won't commit prefs changes so we have to do it manually
      PreferenceManager.getDefaultSharedPreferences(context).edit()
          .putBoolean("display_no_limits_setting_key", newValue)
          .commit()
      LifecycleUtils.restartApp(context)
    }
  }

  private fun showToast(message: String) {
    Handler(this.requireActivity().mainLooper).post {
      Toast.makeText(this.requireContext(), message, Toast.LENGTH_LONG).show()
    }
  }

  private fun changeEndpointToDevelopment() {
    val sharedPreferencesEditor: SharedPreferences.Editor = mSharedPreferences.edit()
    if (Constants.IS_AMAZON) {
      sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, DEV_FIREOS_DROIDBOY_API_KEY)
    } else {
      sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, DEV_DROIDBOY_API_KEY)
    }
    sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, DEV_SDK_ENDPOINT)
    sharedPreferencesEditor.commit()
    LifecycleUtils.restartApp(this.requireContext())
  }

  private fun showDialogOnClick(key: String, dialog: DialogFragment) {
    setClickPreference(key) {
      dialog.show(childFragmentManager, "")
    }
  }

  companion object {
    private val TAG: String = BrazeLogger.getBrazeLogTag(SettingsFragment::class.java)

    const val REQUEST_IMAGE_CAPTURE = 271
    private const val DEV_DROIDBOY_API_KEY = "da8f263e-1483-4e9f-ac0c-7b40030c8f40"
    private const val DEV_FIREOS_DROIDBOY_API_KEY = "ecb81855-149f-465c-bab0-0254d6512133"
    private const val DEV_SDK_ENDPOINT = "https://elsa.braze.com/"

    /**
     * Extension function for preferences that are only clicked
     */
    fun PreferenceFragmentCompat.setClickPreference(key: String, block: () -> Unit) {
      this.findPreference<Preference>(key)?.setOnPreferenceClickListener {
        block.invoke()
        return@setOnPreferenceClickListener true
      }
    }

    fun PreferenceFragmentCompat.setSwitchPreference(key: String, block: (newValue: Boolean) -> Unit) {
      this.findPreference<Preference>(key)?.setOnPreferenceChangeListener { _, newValue ->
        block.invoke(newValue as @kotlin.ParameterName(name = "newValue") Boolean)
        return@setOnPreferenceChangeListener true
      }
    }

    fun PreferenceFragmentCompat.setSummary(key: String, summary: String) {
      this.findPreference<Preference>(key)?.summary = summary
    }

    fun Braze.runOnUser(block: (user: BrazeUser) -> Unit) {
      this.getCurrentUser(object : SimpleValueCallback<BrazeUser>() {
        override fun onSuccess(user: BrazeUser) {
          super.onSuccess(user)
          block(user)
        }
      })
    }
  }
}
