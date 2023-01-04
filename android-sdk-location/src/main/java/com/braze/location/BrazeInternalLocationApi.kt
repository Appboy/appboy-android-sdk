package com.braze.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.braze.Constants
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.enums.LocationProviderName
import com.braze.models.IBrazeLocation
import com.braze.models.outgoing.BrazeLocation
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.IntentUtils
import com.braze.support.hasPermission
import com.braze.support.nowInMilliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.util.*

/**
 * An implementation of the location calls so that they are contained in a single, external
 * module that clients don't need to include if they don't want location services.
 */
class BrazeInternalLocationApi : IBrazeLocationApi {
    private lateinit var context: Context
    private lateinit var locationManager: LocationManager
    private lateinit var appConfigurationProvider: BrazeConfigurationProvider
    private lateinit var allowedLocationProviders: EnumSet<LocationProviderName>

    private val isLocationCollectionEnabled
        get() = if (appConfigurationProvider.isLocationCollectionEnabled) {
            brazelog(I) { "Location collection enabled via sdk configuration." }
            true
        } else {
            brazelog(I) { "Location collection disabled via sdk configuration." }
            false
        }

    /**
     * Initialize the object with some external variables. This function should be called immediately
     * after the object is created and should be called only once.
     */
    override fun initWithContext(
        context: Context,
        allowedProviders: EnumSet<LocationProviderName>,
        appConfigurationProvider: BrazeConfigurationProvider
    ) {
        this.context = context
        this.appConfigurationProvider = appConfigurationProvider
        this.allowedLocationProviders = allowedProviders
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    /**
     * Requests a location fix for the session on the device.
     */
    @Suppress("MissingPermission", "ReturnCount")
    override fun requestSingleLocationUpdate(manualLocationUpdateCallback: (location: IBrazeLocation) -> Unit): Boolean {
        if (!isLocationCollectionEnabled) {
            brazelog(I) { "Did not request single location update. Location collection is disabled." }
            return false
        }
        val hasFinePermission = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarsePermission = context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!(hasCoarsePermission || hasFinePermission)) {
            // Nothing we can do here without any permissions
            brazelog(I) { "Did not request single location update. Neither fine nor coarse location permissions found." }
            return false
        }

        // Check for a GPS location
        if (hasFinePermission) {
            val lastKnownGpsLocationIfValid =
                getLastKnownGpsLocationIfValid(locationManager)
            if (lastKnownGpsLocationIfValid != null) {
                brazelog { "Setting user location to last known GPS location: $lastKnownGpsLocationIfValid" }
                manualLocationUpdateCallback.invoke(BrazeLocation(lastKnownGpsLocationIfValid))
                return true
            }
        }
        val provider = getSuitableLocationProvider(
            locationManager,
            allowedLocationProviders,
            hasFinePermission,
            hasCoarsePermission
        )
        if (provider == null) {
            brazelog { "Could not request single location update. Could not find suitable location provider." }
            return false
        }
        brazelog { "Requesting single location update with provider: $provider" }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(
                    provider,
                    null,
                    Dispatchers.IO.asExecutor()
                ) { location: Location? ->
                    brazelog { "Location manager getCurrentLocation got location: $location" }
                    if (location != null) {
                        manualLocationUpdateCallback.invoke(BrazeLocation(location))
                    }
                }
            } else {
                requestSingleUpdateFromLocationManager(provider)
            }
            true
        } catch (se: SecurityException) {
            brazelog(E, se) {
                "Failed to request single location update due to security exception from insufficient permissions."
            }
            false
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to request single location update due to exception." }
            false
        }
    }

    /**
     * Determines the best location provider based on the permissions and list of providers allowed.
     */
    @Suppress("ComplexCondition")
    fun getSuitableLocationProvider(
        locationManager: LocationManager,
        allowedProviders: EnumSet<LocationProviderName>,
        hasFinePermission: Boolean,
        hasCoarsePermission: Boolean
    ): String? {
        var provider: String? = null
        // Check for our preferred providers in order.
        // Order set in accordance with https://stackoverflow.com/a/6775456/3745724
        if (hasFinePermission
            && allowedProviders.contains(LocationProviderName.GPS)
            && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        ) {
            provider = LocationManager.GPS_PROVIDER
        } else if ((hasCoarsePermission || hasFinePermission)
            && allowedProviders.contains(LocationProviderName.NETWORK)
            && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            provider = LocationManager.NETWORK_PROVIDER
        } else if (hasFinePermission
            && allowedProviders.contains(LocationProviderName.PASSIVE)
            && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)
        ) {
            provider = LocationManager.PASSIVE_PROVIDER
        }
        return provider
    }

    /**
     * Returns the last known location from the [LocationManager.GPS_PROVIDER], if
     * not older than [LAST_KNOWN_GPS_LOCATION_MAX_AGE_MS].
     * Assumes that the [Manifest.permission.ACCESS_FINE_LOCATION] permission is present.
     */
    @Suppress("MissingPermission")
    @VisibleForTesting
    fun getLastKnownGpsLocationIfValid(locationManager: LocationManager): Location? {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return null
        }
        val lastKnownGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: return null

        val ageMs = nowInMilliseconds() - lastKnownGpsLocation.time
        if (ageMs > LAST_KNOWN_GPS_LOCATION_MAX_AGE_MS) {
            brazelog(V) { "Last known GPS location is too old and will not be used. Age ms: $ageMs" }
            return null
        }
        brazelog { "Using last known GPS location: $lastKnownGpsLocation" }
        return lastKnownGpsLocation
    }

    @Suppress("deprecation", "MissingPermission")
    private fun requestSingleUpdateFromLocationManager(provider: String) {
        val locationUpdateIntent =
            Intent(Constants.BRAZE_ACTION_RECEIVER_SINGLE_LOCATION_UPDATE_INTENT_ACTION)
                .setClass(context, BrazeActionReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or IntentUtils.getMutablePendingIntentFlags()
        val locationUpdatePendingIntent =
            PendingIntent.getBroadcast(context, 0, locationUpdateIntent, flags)
        // Using this deprecated method because new method only supports API 30+
        locationManager.requestSingleUpdate(provider, locationUpdatePendingIntent)
    }

    companion object {
        /**
         * The oldest a location from [LocationManager.getLastKnownLocation] using [LocationManager.GPS_PROVIDER]
         * can be used before requesting a new fix another provider.
         */
        const val LAST_KNOWN_GPS_LOCATION_MAX_AGE_MS = 10 * 60 * 1000 // 10 Minutes
    }
}
