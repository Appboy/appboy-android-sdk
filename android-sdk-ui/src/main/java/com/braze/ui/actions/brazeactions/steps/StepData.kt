package com.braze.ui.actions.brazeactions.steps

import androidx.annotation.VisibleForTesting
import com.braze.enums.Channel
import com.braze.models.outgoing.BrazeProperties
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.getPrettyPrintedString
import com.braze.support.iterator
import org.json.JSONObject

/**
 * A data object for retrieving information on the enclosed
 * [IBrazeActionStep] in its [JSONObject] form.
 */
internal data class StepData(
    val srcJson: JSONObject,
    val channel: Channel = Channel.UNKNOWN
) {
    private val args: List<Any> by lazy {
        srcJson.optJSONArray(ARGS)
            .iterator<Any>()
            .asSequence()
            .toList()
    }

    val firstArg by lazy { getArg(0) }
    val secondArg by lazy { getArg(1) }

    @VisibleForTesting
    internal fun getArg(index: Int): Any? = args.getOrNull(index)

    /**
     * @return A [BrazeProperties] object of the argument [JSONObject] or null if
     * that [JSONObject] cannot be retrieved.
     */
    fun coerceArgToPropertiesOrNull(index: Int): BrazeProperties? {
        val props = args.getOrNull(index)
        return if (props != null && props is JSONObject) {
            BrazeProperties(props)
        } else {
            null
        }
    }

    /**
     * @return True if the number of arguments is within either
     * the fixed or variable length bounds specified.
     */
    fun isArgCountInBounds(
        fixedArgCount: Int = -1,
        rangedArgCount: IntRange? = null,
    ): Boolean {
        if (fixedArgCount != -1) {
            if (args.size != fixedArgCount) {
                brazelog { "Expected $fixedArgCount arguments. Got: $args" }
                return false
            }
        }
        if (rangedArgCount != null) {
            if (args.size !in rangedArgCount) {
                brazelog { "Expected $rangedArgCount arguments. Got: $args" }
                return false
            }
        }
        return true
    }

    fun isArgString(index: Int): Boolean {
        return if (getArg(index) is String) {
            true
        } else {
            brazelog { "Argument [$index] is not a String. Source: $srcJson" }
            false
        }
    }

    /**
     * @return True if the argument is either null or [JSONObject].
     */
    fun isArgOptionalJsonObject(index: Int): Boolean {
        val arg = getArg(index)
        return if (arg == null || arg is JSONObject) {
            true
        } else {
            brazelog { "Argument [$index] is not a JSONObject. Source: $srcJson" }
            false
        }
    }

    override fun toString(): String = "Channel $channel and json\n${srcJson.getPrettyPrintedString()}"

    companion object {
        internal const val ARGS = "args"
    }
}
