package com.braze.ui.actions.brazeactions

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.appboy.enums.Channel
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.getOptionalString
import com.braze.ui.actions.brazeactions.steps.AddToCustomAttributeArrayStep
import com.braze.ui.actions.brazeactions.steps.AddToSubscriptionGroupStep
import com.braze.ui.actions.brazeactions.steps.ContainerStep
import com.braze.ui.actions.brazeactions.steps.IBrazeActionStep
import com.braze.ui.actions.brazeactions.steps.LogCustomEventStep
import com.braze.ui.actions.brazeactions.steps.NoOpStep
import com.braze.ui.actions.brazeactions.steps.OpenLinkExternallyStep
import com.braze.ui.actions.brazeactions.steps.OpenLinkInWebViewStep
import com.braze.ui.actions.brazeactions.steps.RemoveFromCustomAttributeArrayStep
import com.braze.ui.actions.brazeactions.steps.RequestPushPermissionStep
import com.braze.ui.actions.brazeactions.steps.SetCustomUserAttributeStep
import com.braze.ui.actions.brazeactions.steps.SetEmailSubscriptionStep
import com.braze.ui.actions.brazeactions.steps.SetPushNotificationSubscriptionStep
import com.braze.ui.actions.brazeactions.steps.StepData
import org.json.JSONObject

object BrazeActionParser {
    private const val BRAZE_ACTIONS_V1 = "v1"
    internal const val TYPE = "type"
    internal const val BRAZE_ACTIONS_SCHEME = "brazeActions"

    internal enum class ActionType(val key: String, val impl: IBrazeActionStep) {
        CONTAINER("container", ContainerStep),
        LOG_CUSTOM_EVENT("logCustomEvent", LogCustomEventStep),
        SET_CUSTOM_ATTRIBUTE("setCustomUserAttribute", SetCustomUserAttributeStep),
        REQUEST_PUSH_PERMISSION("requestPushPermission", RequestPushPermissionStep),
        ADD_TO_SUBSCRIPTION_GROUP("addToSubscriptionGroup", AddToSubscriptionGroupStep),
        REMOVE_FROM_SUBSCRIPTION_GROUP("removeFromSubscriptionGroup", AddToSubscriptionGroupStep),
        ADD_TO_CUSTOM_ATTRIBUTE_ARRAY("addToCustomAttributeArray", AddToCustomAttributeArrayStep),
        REMOVE_FROM_CUSTOM_ATTRIBUTE_ARRAY("removeFromCustomAttributeArray", RemoveFromCustomAttributeArrayStep),
        SET_EMAIL_SUBSCRIPTION("setEmailNotificationSubscriptionType", SetEmailSubscriptionStep),
        SET_PUSH_NOTIFICATION_SUBSCRIPTION("setPushNotificationSubscriptionType", SetPushNotificationSubscriptionStep),
        OPEN_LINK_IN_WEBVIEW("openLinkInWebview", OpenLinkInWebViewStep),
        OPEN_LINK_EXTERNALLY("openLink", OpenLinkExternallyStep),
        INVALID("", NoOpStep);

        companion object {
            private val map = values().associateBy { it.key }

            @JvmStatic
            fun fromValue(value: String?): ActionType = map.getOrElse(value.orEmpty()) { INVALID }
        }
    }

    fun Uri?.isBrazeActionUri(): Boolean = this?.scheme == BRAZE_ACTIONS_SCHEME

    /**
     * Parses a Braze Actions [Uri].
     */
    fun execute(context: Context, uri: Uri, channel: Channel = Channel.UNKNOWN) {
        brazelog(V) { "Attempting to parse Braze Action with channel $channel and uri:\n'$uri'" }
        try {
            val components = uri.getBrazeActionVersionAndJson()
            if (components == null) {
                brazelog(I) {
                    "Failed to decode Braze Action into both " +
                        "version and json components. Doing nothing."
                }
                return
            }

            val (version, json) = components
            if (version != BRAZE_ACTIONS_V1) {
                brazelog {
                    "Braze Actions version $version is " +
                        "unsupported. Version must be $BRAZE_ACTIONS_V1"
                }
                return
            }

            parse(context, StepData(json, channel))
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to parse uri as a Braze Action.\n'$uri'" }
        }
        brazelog(V) { "Done handling Braze uri\n'$uri'" }
    }

    /**
     * Evaluates the Braze Action json for validity before running it.
     */
    @JvmSynthetic
    internal fun parse(
        context: Context,
        data: StepData
    ) {
        try {
            val actionType = getActionType(data)
            if (actionType == ActionType.INVALID) {
                return
            }
            brazelog(V) {
                "Performing Braze Action type $actionType with data $data"
            }
            actionType.impl.run(context, data)
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to run with data $data" }
        }
    }

    /**
     * Parses an encoded URL-SAFE BASE64 input to back to UTF-16. Note
     * that the base64 input is skip encoded into 8 bits due to
     * fit Base64 (UTF-8).
     */
    @JvmSynthetic
    internal fun parseEncodedActionToJson(action: String): JSONObject {
        // Convert the base64 input into an array of 8-bit words
        val bytes8: ByteArray = Base64.decode(action, Base64.URL_SAFE)

        // Combine every pair of 8-bit words into a single 16-bit word
        val bit16 = IntArray(bytes8.size / 2)
        for (i in bytes8.indices step 2) {
            @Suppress("MagicNumber")
            val lowerByte = bytes8[i].toInt() and 0xFF

            @Suppress("UnnecessaryParentheses", "MagicNumber")
            val upperByte = (bytes8[i + 1].toInt() and 0xFF) shl 8
            val combined = upperByte or lowerByte
            bit16[i / 2] = combined
        }

        // Convert each 16-bit word into a UTF-16 character
        // and combine each into a String
        val result = StringBuilder()
        for (code in bit16) {
            result.append(Char(code))
        }
        return JSONObject(result.toString())
    }

    /**
     * Extracts the version from the Braze Action [Uri] and
     * decodes the Base64 portion of the action to [JSONObject].
     * Assumes that the [Uri] is a valid Braze Action.
     *
     * @see isBrazeActionUri
     */
    @JvmSynthetic
    internal fun Uri.getBrazeActionVersionAndJson(): Pair<String, JSONObject>? {
        // Extract the version and encoded action
        val version = this.host
        val encodedAction = this.lastPathSegment

        if (version == null || encodedAction == null) {
            brazelog { "Failed to parse version and encoded action from uri: $this" }
            return null
        }

        val json = try {
            parseEncodedActionToJson(encodedAction)
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to decode action into json. Action:\n'$encodedAction'" }
            null
        } ?: return null

        return Pair(version, json)
    }

    /**
     * Parses the data for the step type and checks validity.
     *
     * @return The parsed [ActionType] or [ActionType.INVALID]
     * if the [IBrazeActionStep.isValid] returns false.
     */
    @JvmSynthetic
    internal fun getActionType(data: StepData): ActionType {
        val type = ActionType.fromValue(data.srcJson.getOptionalString(TYPE))
        val isValid = type.impl.isValid(data)
        if (!isValid) {
            brazelog {
                "Cannot parse invalid action of " +
                    "type $type and data $data"
            }
            return ActionType.INVALID
        }
        return type
    }
}
