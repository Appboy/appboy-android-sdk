package com.braze.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.braze.BrazeInternal
import com.braze.Constants
import com.braze.enums.GeofenceTransitionType
import com.braze.models.outgoing.BrazeLocation
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Keep
class BrazeActionReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) {
            brazelog(W) { "BrazeActionReceiver received null intent. Doing nothing." }
            return
        } else if (context == null) {
            brazelog(W) { "BrazeActionReceiver received null context. Doing nothing." }
            return
        }
        val applicationContext = context.applicationContext
        // This pending result allows us to perform work off the main thread and receive 10 seconds from the system
        // to finish processing. By default, a BroadcastReceiver is allowed 5 seconds (due to the ANR limit)
        // for processing.
        val pendingResult = goAsync()
        val actionReceiver = ActionReceiver(applicationContext, intent)

        GlobalScope.launch(Dispatchers.IO) {
            actionReceiver.run()
            pendingResult.finish()
        }
    }

    @VisibleForTesting
    internal class ActionReceiver(
        private val applicationContext: Context,
        private val intent: Intent
    ) {
        private val action: String? = intent.action
        fun run() {
            try {
                performWork()
            } catch (e: Exception) {
                // If the action receiver encounters an error, we still have to mark the broadcast receiver's work as finished.
                brazelog(E, e) {
                    "Caught exception while performing the BrazeActionReceiver work. Action: $action Intent: $intent"
                }
            }
        }

        /**
         * Performs the work as specified by the intent action.
         *
         * @return True iff the work was perform successfully.
         */
        @VisibleForTesting
        fun performWork() {
            brazelog { "Received intent with action $action" }
            when (action) {
                null -> {
                    brazelog { "Received intent with null action. Doing nothing." }
                }
                Constants.BRAZE_ACTION_RECEIVER_GEOFENCE_UPDATE_INTENT_ACTION -> {
                    brazelog { "BrazeActionReceiver received intent with geofence transition: $action" }
                    GeofencingEvent.fromIntent(intent)?.let { handleGeofenceEvent(applicationContext, it) }
                }
                Constants.BRAZE_ACTION_RECEIVER_SINGLE_LOCATION_UPDATE_INTENT_ACTION -> {
                    brazelog { "BrazeActionReceiver received intent with single location update: $action" }
                    val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.extras?.getParcelable(LocationManager.KEY_LOCATION_CHANGED, Location::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.extras?.get(LocationManager.KEY_LOCATION_CHANGED) as Location?
                    }
                    location?.let { handleSingleLocationUpdate(applicationContext, it) }
                }
                else -> {
                    brazelog(W) { "Unknown intent received in BrazeActionReceiver with action: $action" }
                }
            }
        }

        companion object {
            private fun handleSingleLocationUpdate(applicationContext: Context, location: Location): Boolean {
                try {
                    BrazeInternal.logLocationRecordedEvent(applicationContext, BrazeLocation(location))
                } catch (e: Exception) {
                    brazelog(E, e) { "Exception while processing single location update" }
                    return false
                }
                return true
            }

            /**
             * Records all geofence transitions in the given geofence event.
             *
             * @param applicationContext The application context
             * @param geofenceEvent Google Play Services geofencing event
             * @return true if a geofence transition was recorded
             */
            @VisibleForTesting
            fun handleGeofenceEvent(applicationContext: Context, geofenceEvent: GeofencingEvent): Boolean {
                if (geofenceEvent.hasError()) {
                    val errorCode = geofenceEvent.errorCode
                    brazelog(W) { "Location Services error: $errorCode" }
                    return false
                }

                val transitionType = geofenceEvent.geofenceTransition
                val triggeringGeofences = geofenceEvent.triggeringGeofences
                return when {
                    Geofence.GEOFENCE_TRANSITION_ENTER == transitionType -> {
                        triggeringGeofences?.forEach { geofence ->
                            BrazeInternal.recordGeofenceTransition(
                                applicationContext,
                                geofence.requestId,
                                GeofenceTransitionType.ENTER
                            )
                        }
                        true
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT == transitionType -> {
                        triggeringGeofences?.forEach { geofence ->
                            BrazeInternal.recordGeofenceTransition(
                                applicationContext,
                                geofence.requestId,
                                GeofenceTransitionType.EXIT
                            )
                        }
                        true
                    }
                    else -> {
                        brazelog(W) { "Unsupported transition type received: $transitionType" }
                        false
                    }
                }
            }
        }
    }
}
