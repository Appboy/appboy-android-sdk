package com.braze.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import com.braze.managers.BrazeGeofenceManager
import com.braze.managers.IBrazeGeofenceLocationUpdateListener
import com.braze.models.BrazeGeofence
import com.braze.models.outgoing.BrazeLocation
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
object GooglePlayLocationUtils {
    private const val REGISTERED_GEOFENCE_SHARED_PREFS_LOCATION = "com.appboy.support.geofences"

    /**
     * Requests to register the given list of geofences with Google Play Location Services.
     *
     * If a given geofence is already registered with Google Play Location Services, it will not be
     * needlessly re-registered. Geofences that are registered with Google Play Location Services but
     * not included in the given list of geofences will be un-registered.
     *
     * If the given geofence list is empty, geofences will be un-registered and deleted from local
     * storage.
     * @param context used by shared preferences
     * @param geofenceList list of [BrazeGeofence] objects
     * @param geofenceRequestIntent pending intent to fire when geofences transition events occur
     */
    @JvmStatic
    fun registerGeofencesWithGooglePlayIfNecessary(
        context: Context,
        geofenceList: List<BrazeGeofence>,
        geofenceRequestIntent: PendingIntent
    ) {
        try {
            val prefs = getRegisteredGeofenceSharedPrefs(context)
            val registeredGeofences = BrazeGeofenceManager.retrieveBrazeGeofencesFromLocalStorage(prefs)

            val newGeofencesToRegister = geofenceList.filter { newGeofence ->
                registeredGeofences.none { registeredGeofence ->
                    registeredGeofence.id == newGeofence.id && registeredGeofence.equivalentServerData(newGeofence)
                }
            }
            val obsoleteGeofenceIds = registeredGeofences.filter { registeredGeofence ->
                newGeofencesToRegister.none { newGeofence ->
                    newGeofence.id == registeredGeofence.id
                }
            }.map { it.id }

            if (obsoleteGeofenceIds.isNotEmpty()) {
                brazelog { "Un-registering ${obsoleteGeofenceIds.size} obsolete geofences from Google Play Services." }
                removeGeofencesRegisteredWithGeofencingClient(context, obsoleteGeofenceIds)
            } else {
                brazelog { "No obsolete geofences need to be unregistered from Google Play Services." }
            }
            if (newGeofencesToRegister.isNotEmpty()) {
                brazelog { "Registering ${newGeofencesToRegister.size} new geofences with Google Play Services." }
                registerGeofencesWithGeofencingClient(context, newGeofencesToRegister, geofenceRequestIntent)
            } else {
                brazelog { "No new geofences need to be registered with Google Play Services." }
            }
        } catch (e: Exception) {
            brazelog(E, e) { "Exception while adding geofences." }
        }
    }

    /**
     * Requests a single location update from Google Play Location Services for the given pending intent.
     *
     * @param resultListener A callback of type [IBrazeGeofenceLocationUpdateListener]
     * which will be informed of the result of location update.
     */
    @JvmStatic
    fun requestSingleLocationUpdateFromGooglePlay(
        context: Context,
        resultListener: IBrazeGeofenceLocationUpdateListener
    ) {
        try {
            brazelog { "Requesting single location update from Google Play Services." }
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener {
                    brazelog(V) { "Single location request from Google Play services was successful." }
                    resultListener.onLocationRequestComplete(BrazeLocation(it))
                }
                .addOnFailureListener { error: Exception? ->
                    brazelog(E, error) { "Failed to get single location update from Google Play services." }
                    resultListener.onLocationRequestComplete(null)
                }
        } catch (e: Exception) {
            brazelog(W, e) { "Failed to request location update due to exception." }
        }
    }

    /**
     * Delete the cache of registered geofences. This will cause any geofences passed to
     * [.registerGeofencesWithGooglePlayIfNecessary]
     * on the next call to that method to be registered.
     */
    @JvmStatic
    fun deleteRegisteredGeofenceCache(context: Context) {
        brazelog { "Deleting registered geofence cache." }
        getRegisteredGeofenceSharedPrefs(context).edit().clear().apply()
    }

    /**
     * Registers a list of [Geofence] with a [com.google.android.gms.location.GeofencingClient].
     *
     * @param newGeofencesToRegister List of [BrazeGeofence]s to register
     * @param geofenceRequestIntent A pending intent to fire on completion of adding the [Geofence]s with
     * the [com.google.android.gms.location.GeofencingClient].
     */
    private fun registerGeofencesWithGeofencingClient(
        context: Context,
        newGeofencesToRegister: List<BrazeGeofence>,
        geofenceRequestIntent: PendingIntent
    ) {
        val newGooglePlayGeofencesToRegister = newGeofencesToRegister.map { it.toGeofence() }
        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofences(newGooglePlayGeofencesToRegister) // no initial trigger
            .setInitialTrigger(0)
            .build()
        LocationServices.getGeofencingClient(context).addGeofences(geofencingRequest, geofenceRequestIntent)
            .addOnSuccessListener {
                brazelog { "Geofences successfully registered with Google Play Services." }
                storeGeofencesToSharedPrefs(context, newGeofencesToRegister)
            }
            .addOnFailureListener { geofenceError: Exception? ->
                if (geofenceError is ApiException) {
                    when (val statusCode = geofenceError.statusCode) {
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> brazelog(W) {
                            "Geofences not registered with Google Play Services due to GEOFENCE_TOO_MANY_GEOFENCES: $statusCode"
                        }
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> brazelog(W) {
                            "Geofences not registered with Google Play Services due to GEOFENCE_TOO_MANY_PENDING_INTENTS: $statusCode"
                        }
                        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> brazelog(W) {
                            "Geofences not registered with Google Play Services due to GEOFENCE_NOT_AVAILABLE: $statusCode"
                        }
                        GeofenceStatusCodes.SUCCESS ->
                            // Since we're in the failure listener, we don't expect this status code to appear. Nonetheless, it would
                            // be good to not surface this status code as unknown
                            brazelog {
                                "Received Geofence registration success code in failure block with Google Play Services."
                            }
                        else -> brazelog(W) { "Geofence pending result returned unknown status code: $statusCode" }
                    }
                } else {
                    brazelog(E, geofenceError) { "Geofence exception encountered while adding geofences." }
                }
            }
    }

    /**
     * Un-registers a list of [Geofence] with a [com.google.android.gms.location.GeofencingClient].
     *
     * @param obsoleteGeofenceIds List of [String]s containing Geofence IDs that needs to be un-registered
     */
    private fun removeGeofencesRegisteredWithGeofencingClient(context: Context, obsoleteGeofenceIds: List<String>) {
        LocationServices.getGeofencingClient(context).removeGeofences(obsoleteGeofenceIds)
            .addOnSuccessListener {
                brazelog { "Geofences successfully un-registered with Google Play Services." }
                removeGeofencesFromSharedPrefs(context, obsoleteGeofenceIds)
            }
            .addOnFailureListener { geofenceError: Exception? ->
                if (geofenceError is ApiException) {
                    when (val statusCode = geofenceError.statusCode) {
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> brazelog(W) {
                            "Geofences cannot be un-registered with Google Play Services due to GEOFENCE_TOO_MANY_GEOFENCES: $statusCode"
                        }
                        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> brazelog(W) {
                            "Geofences cannot be un-registered with Google Play Services due to GEOFENCE_TOO_MANY_PENDING_INTENTS: $statusCode"
                        }
                        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> brazelog(W) {
                            "Geofences cannot be un-registered with Google Play Services due to GEOFENCE_NOT_AVAILABLE: $statusCode"
                        }
                        GeofenceStatusCodes.SUCCESS ->
                            // Since we're in the failure listener, we don't expect this status code to appear. Nonetheless, it would
                            // be good to not surface this status code as unknown
                            brazelog {
                                "Received Geofence un-registration success code in failure block with Google Play Services."
                            }
                        else -> brazelog(W) { "Geofence pending result returned unknown status code: $statusCode" }
                    }
                } else {
                    brazelog(E, geofenceError) { "Geofence exception encountered while removing geofences." }
                }
            }
    }

    /**
     * Returns a [SharedPreferences] instance holding list of registered [BrazeGeofence]s.
     */
    private fun getRegisteredGeofenceSharedPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(REGISTERED_GEOFENCE_SHARED_PREFS_LOCATION, Context.MODE_PRIVATE)

    /**
     * Stores the list of [BrazeGeofence] which are successfully registered.
     *
     * @param newGeofencesToRegister List of [BrazeGeofence]s to store in SharedPreferences
     */
    private fun storeGeofencesToSharedPrefs(context: Context, newGeofencesToRegister: List<BrazeGeofence>) {
        val editor = getRegisteredGeofenceSharedPrefs(context).edit()
        for (brazeGeofence in newGeofencesToRegister) {
            editor.putString(brazeGeofence.id, brazeGeofence.forJsonPut().toString())
            brazelog(V) { "Geofence with id: ${brazeGeofence.id} added to shared preferences." }
        }
        editor.apply()
    }

    /**
     * Removes the list of [BrazeGeofence] which are now un-registered with Google Play Services.
     *
     * @param obsoleteGeofenceIds List of [String]s containing Geofence IDs that are un-registered
     */
    private fun removeGeofencesFromSharedPrefs(context: Context, obsoleteGeofenceIds: List<String>) {
        val editor = getRegisteredGeofenceSharedPrefs(context).edit()
        for (id in obsoleteGeofenceIds) {
            editor.remove(id)
            brazelog(V) { "Geofence with id: $id removed from shared preferences." }
        }
        editor.apply()
    }
}

/**
 * Creates a Google Play Location Services Geofence object from a BrazeGeofence.
 * @return A Geofence object.
 */
fun BrazeGeofence.toGeofence(): Geofence {
    val builder = Geofence.Builder()
    builder
        .setRequestId(id)
        .setCircularRegion(latitude, longitude, radiusMeter.toFloat())
        .setNotificationResponsiveness(notificationResponsivenessMs)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
    var transitionTypes = 0
    if (enterEvents) {
        transitionTypes = transitionTypes or Geofence.GEOFENCE_TRANSITION_ENTER
    }
    if (exitEvents) {
        transitionTypes = transitionTypes or Geofence.GEOFENCE_TRANSITION_EXIT
    }
    builder.setTransitionTypes(transitionTypes)
    return builder.build()
}
