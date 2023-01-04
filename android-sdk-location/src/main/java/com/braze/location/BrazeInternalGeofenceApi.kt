package com.braze.location

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.braze.Constants
import com.braze.models.BrazeGeofence
import com.braze.support.IntentUtils
import com.google.android.gms.location.LocationServices

/**
 * An implementation of the geofence calls so that they are contained in a single, external
 * module that clients don't need to include if they don't want location services.
 */
class BrazeInternalGeofenceApi : IBrazeGeofenceApi {

    /**
     * @return the PendingIntent that should be fired when a geofence is triggered.
     */
    override fun getGeofenceTransitionPendingIntent(context: Context): PendingIntent {
        val geofenceIntent = Intent(Constants.BRAZE_ACTION_RECEIVER_GEOFENCE_UPDATE_INTENT_ACTION)
            .setClass(context, BrazeActionReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or IntentUtils.getMutablePendingIntentFlags()
        return PendingIntent.getBroadcast(context, 0, geofenceIntent, flags)
    }

    /**
     * Teardown all geofences associated with the given intent.
     */
    override fun teardownGeofences(applicationContext: Context, intent: PendingIntent) {
        LocationServices.getGeofencingClient(applicationContext)
            .removeGeofences(intent)
    }

    /**
     * Register a list of geofences.
     *
     * @param context Application context.
     * @param geofenceList List of [BrazeGeofence] to be registered.
     * @param geofenceRequestIntent The intent to fire when geofence is triggered.
     */
    override fun registerGeofences(
        context: Context,
        geofenceList: List<BrazeGeofence>,
        geofenceRequestIntent: PendingIntent
    ) {
        GooglePlayLocationUtils.registerGeofencesWithGooglePlayIfNecessary(context, geofenceList, geofenceRequestIntent)
    }

    /**
     * Deletes the geofence cache.
     */
    override fun deleteRegisteredGeofenceCache(context: Context) {
        GooglePlayLocationUtils.deleteRegisteredGeofenceCache(context)
    }
}
