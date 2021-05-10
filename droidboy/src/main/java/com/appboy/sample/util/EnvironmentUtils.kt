package com.appboy.sample.util

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.appboy.sample.DroidboyApplication
import com.braze.support.BrazeLogger
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class EnvironmentUtils {
  companion object {
    private val TAG = BrazeLogger.getBrazeLogTag(EnvironmentUtils::class.java)

    private const val BRAZE_ENVIRONMENT_DEEPLINK_SCHEME_PATH = "braze://environment"
    private const val BRAZE_ENVIRONMENT_DEEPLINK_ENDPOINT = "endpoint"
    private const val BRAZE_ENVIRONMENT_DEEPLINK_API_KEY = "api_key"

    @JvmStatic
    fun analyzeBitmapForEnvironmentBarcode(activity: Activity, bitmap: Bitmap) {
      // Build the barcode detector
      val options = BarcodeScannerOptions.Builder()
          .setBarcodeFormats(
              Barcode.FORMAT_QR_CODE)
          .build()
      val image = InputImage.fromBitmap(bitmap, 0)
      val scanner = BarcodeScanning.getClient(options)
      scanner.process(image)
          .addOnSuccessListener { barcodes: List<Barcode> ->
            if (barcodes.isEmpty()) {
              showToast(activity, "Couldn't find barcode. Please try again!")
            } else {
              for (barcode in barcodes) {
                val rawValue = barcode.rawValue
                if (rawValue.startsWith(BRAZE_ENVIRONMENT_DEEPLINK_SCHEME_PATH)) {
                  showToast(activity, "Found barcode: $rawValue")
                  setEnvironmentViaDeepLink(activity, rawValue)
                }
              }
            }
          }
          .addOnFailureListener { e: Exception? -> BrazeLogger.e(TAG, "Failed to parse barcode bitmap", e) }
          .addOnCompleteListener { bitmap.recycle() }
    }

    /**
     * Braze deep link in the form
     * braze://environment?endpoint=ENDPOINT_HERE&api_key=API_KEY_HERE
     */
    @SuppressLint("ApplySharedPref")
    private fun setEnvironmentViaDeepLink(context: Activity, environmentText: String) {
      val uri = Uri.parse(environmentText)
      val endpoint = uri.getQueryParameter(BRAZE_ENVIRONMENT_DEEPLINK_ENDPOINT)
      val apiKey = uri.getQueryParameter(BRAZE_ENVIRONMENT_DEEPLINK_API_KEY)

      BrazeLogger.i(TAG, "Using environment endpoint: $endpoint")
      BrazeLogger.i(TAG, "Using environment api key: $apiKey")
      val message = StringBuilder()
          .append("Looks correct? 👌")
          .append("\n\n")
          .append("New environment endpoint: ")
          .append("\n")
          .append(endpoint)
          .append("\n\n")
          .append("New environment api key: ")
          .append("\n")
          .append(apiKey)
      AlertDialog.Builder(context)
          .setTitle("Changing Droidboy environment")
          .setMessage(message.toString())
          .setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int ->
            val sharedPreferencesEditor: SharedPreferences.Editor = getPrefs(context).edit()
            sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_API_KEY_PREF_KEY, apiKey)
            sharedPreferencesEditor.putString(DroidboyApplication.OVERRIDE_ENDPOINT_PREF_KEY, endpoint)
            sharedPreferencesEditor.commit()
            LifecycleUtils.restartApp(context)
          } // A null listener allows the button to dismiss the dialog and take no further action.
          .setNegativeButton(R.string.no, null)
          .setIcon(R.drawable.ic_dialog_info)
          .show()
    }

    private fun getPrefs(context: Context): SharedPreferences {
      return context.getSharedPreferences(context.getString(com.appboy.sample.R.string.shared_prefs_location), Context.MODE_PRIVATE)
    }

    private fun showToast(context: Context, message: String) {
      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
  }
}
