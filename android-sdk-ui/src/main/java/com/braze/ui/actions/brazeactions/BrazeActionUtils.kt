@file:JvmName("BrazeActionUtils")

package com.braze.ui.actions.brazeactions

import android.net.Uri
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageImmersive
import com.braze.ui.actions.brazeactions.BrazeActionParser.ActionType
import com.braze.ui.actions.brazeactions.BrazeActionParser.getBrazeActionVersionAndJson
import com.braze.ui.actions.brazeactions.BrazeActionParser.isBrazeActionUri
import com.braze.ui.actions.brazeactions.steps.ContainerStep
import com.braze.ui.actions.brazeactions.steps.StepData
import org.json.JSONObject

/**
 * Determines whether any [Uri] in the message and/or buttons
 * contains a push prompt Braze Action.
 */
internal fun IInAppMessage.containsAnyPushPermissionBrazeActions(): Boolean =
    this.getAllUris()
        .filter { it.isBrazeActionUri() }
        .mapNotNull { it.getBrazeActionVersionAndJson()?.second }
        .flatMap { getAllBrazeActionStepTypes(it) }
        .any { it == ActionType.REQUEST_PUSH_PERMISSION }

/**
 * Retrieves all [Uri]'s from the main
 * message and any [MessageButton]'s present. Does not traverse any
 * [Uri] present in HTML messages.
 */
@JvmSynthetic
internal fun IInAppMessage?.getAllUris(): List<Uri> {
    if (this == null) return emptyList()
    val uris = mutableListOf<Uri>()

    // Add the main uri
    this.uri?.let { uris.add(it) }

    // Add all of the message button Uris
    if (this is IInAppMessageImmersive) {
        uris.addAll(
            this.messageButtons.mapNotNull { it.uri }
        )
    }
    return uris
}

@JvmSynthetic
internal fun getAllBrazeActionStepTypes(json: JSONObject): List<ActionType> {
    val allStepTypes = mutableListOf<ActionType>()
    val stepData = StepData(json)
    when (val actionType = BrazeActionParser.getActionType(stepData)) {
        // Break out the container steps and iterate them all
        ActionType.CONTAINER -> {
            ContainerStep.getChildStepIterator(stepData)
                .forEach { allStepTypes.addAll(getAllBrazeActionStepTypes(it)) }
        }

        // Do nothing if the type is invalid
        ActionType.INVALID -> {}

        // This is an individual/uncontained step
        else -> allStepTypes.add(actionType)
    }
    return allStepTypes
}
