package com.appboy.sample

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.appboy.enums.Gender
import com.appboy.enums.Month
import com.appboy.enums.NotificationSubscriptionType
import com.appboy.events.IValueCallback
import com.appboy.models.outgoing.AttributionData
import com.braze.Braze
import com.braze.BrazeUser
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.convertStringJsonArrayToList
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.math.BigDecimal
import java.util.*

class MainFragment : Fragment() {
    private lateinit var customEventTextView: AutoCompleteTextView
    private lateinit var customPurchaseTextView: AutoCompleteTextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var aliasEditText: EditText
    private lateinit var aliasLabelEditText: EditText
    private lateinit var adkAuthSignatureEditText: EditText
    private lateinit var customEventsAndPurchasesArrayAdapter: ArrayAdapter<String?>
    private val lastSeenCustomEventsAndPurchases: Queue<String?> = LinkedList()

    override fun onCreateView(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contentView = layoutInflater.inflate(R.layout.main_fragment, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("droidboy", Context.MODE_PRIVATE)
        customEventTextView =
            contentView.findViewById(R.id.com_appboy_sample_custom_event_autocomplete_text_view)
        customPurchaseTextView =
            contentView.findViewById(R.id.com_appboy_sample_purchase_autocomplete_text_view)
        customEventsAndPurchasesArrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            getLastSeenCustomEventsAndPurchasesFromLocalStorage()
        )
        customEventTextView.setAdapter(customEventsAndPurchasesArrayAdapter)
        customPurchaseTextView.setAdapter(customEventsAndPurchasesArrayAdapter)

        val userIdEditText: EditText =
            contentView.findViewById(R.id.com_appboy_sample_set_user_id_edit_text)
        userIdEditText.setText(sharedPreferences.getString(USER_ID_KEY, null))

        contentView.setOnButtonClick(R.id.com_appboy_sample_set_user_id_button) {
            val userId = userIdEditText.text.toString()
            if (userId.isNotBlank()) {
                (requireActivity().applicationContext as DroidboyApplication).changeUserWithNewSdkAuthToken(userId)
                Toast.makeText(requireContext(), "Set userId to: $userId", Toast.LENGTH_SHORT)
                    .show()
                val editor = sharedPreferences.edit()
                editor.putString(USER_ID_KEY, userId)
                editor.apply()
                FirebaseCrashlytics.getInstance().setUserId(userId)
            } else {
                Toast.makeText(requireContext(), "Please enter a userId.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        contentView.setOnButtonClick(R.id.com_appboy_sample_set_sdk_auth_signature_button) {
            val signature = adkAuthSignatureEditText.text.toString()
            if (signature.isNotBlank()) {
                Braze.getInstance(requireContext()).setSdkAuthenticationSignature(signature)
                Toast.makeText(requireContext(), "Set signature to: $signature", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Please enter a signature.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        contentView.setOnButtonClickWithEditableText(
            R.id.com_appboy_sample_set_sdk_auth_signature_button,
            R.id.com_appboy_sample_set_sdk_auth_signature_edit_text
        ) { _, signature ->
            if (signature.isNotBlank()) {
                Braze.getInstance(requireContext()).setSdkAuthenticationSignature(signature)
                Toast.makeText(requireContext(), "Set signature to: $signature", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Please enter a signature.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        aliasEditText = contentView.findViewById(R.id.com_appboy_sample_set_alias_edit_text)
        aliasLabelEditText =
            contentView.findViewById(R.id.com_appboy_sample_set_alias_label_edit_text)
        contentView.setOnButtonClick(R.id.com_appboy_sample_set_user_alias_button) { handleAliasClick() }

        contentView.setOnButtonClick(R.id.com_appboy_sample_log_custom_event_button) {
            val customEvent = customEventTextView.text.toString()
            if (customEvent.isNotBlank()) {
                Braze.getInstance(requireContext()).logCustomEvent(customEvent)
                Toast.makeText(
                    requireContext(),
                    String.format("Logged custom event %s.", customEvent),
                    Toast.LENGTH_SHORT
                ).show()
                onCustomEventOrPurchaseLogged(customEvent)
            } else {
                Toast.makeText(requireContext(), "Please enter a custom event.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        contentView.setOnButtonClick(R.id.com_appboy_sample_log_purchase_button) {
            val purchase = customPurchaseTextView.text.toString()
            if (purchase.isNotBlank()) {
                Braze.getInstance(requireContext()).logPurchase(purchase, "USD", BigDecimal.TEN)
                Toast.makeText(
                    requireContext(),
                    String.format("Logged purchase %s.", purchase),
                    Toast.LENGTH_SHORT
                ).show()
                onCustomEventOrPurchaseLogged(purchase)
            } else {
                Toast.makeText(requireContext(), "Please enter a purchase.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        contentView.setOnButtonClick(R.id.com_appboy_sample_set_user_attributes_button) {
            Braze.getInstance(requireContext()).getCurrentUser(object : IValueCallback<BrazeUser?> {
                override fun onSuccess(currentUser: BrazeUser) {
                    currentUser.setFirstName("first name least")
                    currentUser.setLastName("lastName")
                    currentUser.setEmail("email@test.com")
                    currentUser.setGender(Gender.FEMALE)
                    currentUser.setCountry("USA")
                    currentUser.setLanguage("cs")
                    currentUser.setHomeCity("New York")
                    currentUser.setPhoneNumber("1234567890")
                    currentUser.setDateOfBirth(1984, Month.AUGUST, 18)
                    currentUser.setAvatarImageUrl("https://raw.githubusercontent.com/Appboy/appboy-android-sdk/master/braze-logo.png")
                    currentUser.setPushNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN)
                    currentUser.setEmailNotificationSubscriptionType(NotificationSubscriptionType.OPTED_IN)
                    currentUser.setCustomUserAttribute(STRING_ATTRIBUTE_KEY, "stringValue")
                    currentUser.setCustomUserAttribute(FLOAT_ATTRIBUTE_KEY, 1.5f)
                    currentUser.setCustomUserAttribute(INT_ATTRIBUTE_KEY, 100)
                    currentUser.setCustomUserAttribute(BOOL_ATTRIBUTE_KEY, true)
                    currentUser.setCustomUserAttribute(LONG_ATTRIBUTE_KEY, 10L)
                    currentUser.setCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY, 1)
                    currentUser.setCustomUserAttribute(DOUBLE_ATTRIBUTE_KEY, 3.1)
                    currentUser.incrementCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY, 4)
                    currentUser.setCustomUserAttributeToSecondsFromEpoch(
                        DATE_ATTRIBUTE_KEY,
                        Date().time / 1000L
                    )
                    currentUser.setCustomAttributeArray(
                        STRING_ARRAY_ATTRIBUTE_KEY,
                        arrayOf("a", "b")
                    )
                    currentUser.addToCustomAttributeArray(ARRAY_ATTRIBUTE_KEY, "c")
                    currentUser.removeFromCustomAttributeArray(ARRAY_ATTRIBUTE_KEY, "b")
                    currentUser.addToCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "cat")
                    currentUser.addToCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "dog")
                    currentUser.removeFromCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "bird")
                    currentUser.removeFromCustomAttributeArray(PETS_ARRAY_ATTRIBUTE_KEY, "deer")
                    currentUser.setAttributionData(
                        AttributionData(
                            "network",
                            "campaign",
                            "ad group",
                            "creative"
                        )
                    )
                    currentUser.setLocationCustomAttribute(
                        "Favorite Location",
                        33.078883,
                        -116.603131
                    )
                    showToast("Set user attributes.")
                }

                override fun onError() {
                    showToast("Failed to set user attributes.")
                }
            })
        }

        contentView.setOnButtonClick(R.id.com_appboy_sample_unset_user_attributes_button) {
            Braze.getInstance(requireContext()).getCurrentUser(object : IValueCallback<BrazeUser?> {
                override fun onSuccess(currentUser: BrazeUser) {
                    // Unset current user default attributes
                    currentUser.setFirstName(null)
                    currentUser.setLastName(null)
                    currentUser.setEmail(null)
                    currentUser.setGender(Gender.UNKNOWN)
                    currentUser.setCountry(null)
                    currentUser.setLanguage(null)
                    currentUser.setHomeCity(null)
                    currentUser.setPhoneNumber(null)
                    currentUser.setDateOfBirth(1970, Month.JANUARY, 1)
                    currentUser.setAvatarImageUrl(null)
                    currentUser.setPushNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED)
                    currentUser.setEmailNotificationSubscriptionType(NotificationSubscriptionType.UNSUBSCRIBED)
                    // Unset current user custom attributes
                    currentUser.unsetCustomUserAttribute(STRING_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(FLOAT_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(INT_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(BOOL_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(LONG_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(DATE_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(ARRAY_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(STRING_ARRAY_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(PETS_ARRAY_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(INCREMENT_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(DOUBLE_ATTRIBUTE_KEY)
                    currentUser.unsetCustomUserAttribute(ATTRIBUTION_DATA_KEY)
                    currentUser.unsetLocationCustomAttribute("Mediocre Location")
                    showToast("Unset user attributes.")
                }

                override fun onError() {
                    showToast("Failed to unset user attributes.")
                }
            })
        }

        contentView.setOnButtonClick(R.id.com_appboy_sample_request_flush_button) {
            Braze.getInstance(requireContext()).requestImmediateDataFlush()
            Toast.makeText(requireContext(), "Requested data flush.", Toast.LENGTH_SHORT).show()
        }

        contentView.setOnButtonClick(R.id.com_appboy_sample_collect_and_flush_google_advertising_id_button) {
            this.lifecycleScope.launch(context = Dispatchers.IO) {
                try {
                    val advertisingIdInfo =
                        AdvertisingIdClient.getAdvertisingIdInfo(requireContext())
                    Braze.getInstance(requireContext()).setGoogleAdvertisingId(
                        advertisingIdInfo.id,
                        advertisingIdInfo.isLimitAdTrackingEnabled
                    )
                    Braze.getInstance(requireContext()).requestImmediateDataFlush()
                } catch (e: Exception) {
                    brazelog(E, e) { "Failed to collect Google Advertising ID information." }
                }
            }
        }
        return contentView
    }

    private fun getLastSeenCustomEventsAndPurchasesFromLocalStorage(): Array<String?> {
        val serializedEvents =
            sharedPreferences.getString(LAST_SEEN_CUSTOM_EVENTS_AND_PURCHASES_PREFERENCE_KEY, null)
        try {
            if (serializedEvents != null) {
                lastSeenCustomEventsAndPurchases.addAll(
                    JSONArray(serializedEvents).convertStringJsonArrayToList()
                )
            }
        } catch (e: JSONException) {
            brazelog(E, e) { "Failed to get recent events from storage" }
        }
        return lastSeenCustomEventsAndPurchases.toTypedArray()
    }

    private fun onCustomEventOrPurchaseLogged(eventOrPurchaseName: String) {
        if (lastSeenCustomEventsAndPurchases.contains(eventOrPurchaseName)) {
            return
        }
        lastSeenCustomEventsAndPurchases.add(eventOrPurchaseName)
        if (lastSeenCustomEventsAndPurchases.size > 5) {
            lastSeenCustomEventsAndPurchases.remove()
        }
        val editor = sharedPreferences.edit()
        editor.putString(
            LAST_SEEN_CUSTOM_EVENTS_AND_PURCHASES_PREFERENCE_KEY,
            JSONArray(lastSeenCustomEventsAndPurchases).toString()
        )
        editor.apply()
        customEventsAndPurchasesArrayAdapter.clear()
        customEventsAndPurchasesArrayAdapter.addAll(*lastSeenCustomEventsAndPurchases.toTypedArray())
    }

    private fun handleAliasClick() {
        val alias = aliasEditText.text.toString()
        val label = aliasLabelEditText.text.toString()
        Braze.getInstance(requireContext()).getCurrentUser(object : IValueCallback<BrazeUser?> {
            override fun onSuccess(value: BrazeUser) {
                value.addAlias(alias, label)
                Toast.makeText(
                    requireContext(),
                    "Added alias " + alias + " with label "
                        + label,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError() {
                Toast.makeText(requireContext(), "Failed to add alias", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Shows a toast on the activity's UI thread
     */
    private fun showToast(msg: String) {
        activity?.runOnUiThread { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        private const val STRING_ARRAY_ATTRIBUTE_KEY = "stringArrayAttribute"
        private const val ARRAY_ATTRIBUTE_KEY = "arrayAttribute"
        private const val DATE_ATTRIBUTE_KEY = "dateAttribute"
        private const val PETS_ARRAY_ATTRIBUTE_KEY = "arrayAttributePets"
        private const val FLOAT_ATTRIBUTE_KEY = "floatAttribute"
        private const val BOOL_ATTRIBUTE_KEY = "boolAttribute"
        private const val INT_ATTRIBUTE_KEY = "intAttribute"
        private const val LONG_ATTRIBUTE_KEY = "longAttribute"
        private const val STRING_ATTRIBUTE_KEY = "stringAttribute"
        private const val DOUBLE_ATTRIBUTE_KEY = "doubleAttribute"
        private const val INCREMENT_ATTRIBUTE_KEY = "incrementAttribute"
        private const val ATTRIBUTION_DATA_KEY = "ab_install_attribution"
        private const val LAST_SEEN_CUSTOM_EVENTS_AND_PURCHASES_PREFERENCE_KEY =
            "last_seen_custom_events_and_purchases"
        const val USER_ID_KEY = "user.id"

        fun View.setOnButtonClick(id: Int, block: (view: View) -> Unit) {
            val view = this.findViewById<Button>(id)
            view.setOnClickListener { block(view) }
        }

        /**
         * A combo of a button id and EditText id
         */
        fun View.setOnButtonClickWithEditableText(
            buttonId: Int,
            editTextId: Int,
            block: (view: View, textValue: String) -> Unit
        ) {
            val editTextValue = this.findViewById<EditText>(editTextId).text.toString()
            val view = this.findViewById<Button>(buttonId)
            view.setOnClickListener { block(view, editTextValue) }
        }
    }
}
