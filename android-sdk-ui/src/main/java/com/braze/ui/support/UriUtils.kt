@file:JvmName("UriUtils")

package com.braze.ui.support

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.braze.IBrazeDeeplinkHandler
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag
import com.braze.ui.BrazeDeeplinkHandler

private val TAG = "UriUtils".getBrazeLogTag()

/**
 * Parses the query part of the uri and returns a mapping of the query keys to the
 * values. Empty keys or empty values will not be included in the mapping.
 */
fun Uri.getQueryParameters(): Map<String, String> {
    var uri = this
    val encodedQuery = uri.encodedQuery
    if (encodedQuery == null) {
        brazelog(TAG, V) { "Encoded query is null for Uri: $uri Returning empty map for query parameters" }
        return emptyMap()
    }
    val parameterValues = mutableMapOf<String, String>()
    try {
        if (uri.isOpaque) {
            // Convert the opaque uri into a parseable hierarchical one
            // This is basically copying the query from the original uri onto a new one
            uri = Uri.parse("://")
                .buildUpon()
                .encodedQuery(encodedQuery)
                .build()
        }
        val queryParameterNames = uri.queryParameterNames.filter { !it.isNullOrEmpty() }
        for (queryParameterKey in queryParameterNames) {
            val queryParameterValue = uri.getQueryParameter(queryParameterKey)
            if (!queryParameterValue.isNullOrEmpty()) {
                parameterValues[queryParameterKey] = queryParameterValue
            }
        }
    } catch (e: Exception) {
        brazelog(TAG, E, e) { "Failed to map the query parameters of Uri: $uri" }
    }
    return parameterValues
}

fun getMainActivityIntent(context: Context, extras: Bundle? = null): Intent? {
    val startActivityIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    startActivityIntent?.flags = BrazeDeeplinkHandler.getInstance()
        .getIntentFlags(IBrazeDeeplinkHandler.IntentFlagPurpose.URI_UTILS_GET_MAIN_ACTIVITY_INTENT)
    if (extras != null) {
        startActivityIntent?.putExtras(extras)
    }
    return startActivityIntent
}

/**
 * @param context The context used to create the checked component identifier.
 * @param className The class name for a registered activity with the given context
 * @return true if the class name matches a registered activity in the Android Manifest.
 */
fun isActivityRegisteredInManifest(context: Context, className: String): Boolean {
    return try {
        // If the activity is registered, then a non-null ActivityInfo is returned by the package manager.
        // If unregistered, then an exception is thrown by the package manager.
        context.packageManager.getActivityInfo(ComponentName(context, className), 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        brazelog(TAG, W, e) { "Could not find activity info for class with name: $className" }
        false
    }
}
