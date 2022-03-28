package com.braze

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.braze.push.NotificationTrampolineActivity
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.BrazeInAppMessageManager

/**
 * Can be used to automatically handle Braze lifecycle methods.
 * Optionally, openSession() and closeSession() are called on onActivityStarted and onActivityStopped respectively.
 * The InAppMessageManager methods of registerInAppMessageManager() and unregisterInAppMessageManager() can be optionally
 * called here as well.
 * Note: This callback should not be set in any Activity. It must be set in the Application class of your app.
 *
 * @param sessionHandlingEnabled              When true, handles calling openSession and closeSession in onActivityStarted
 * and onActivityStopped respectively.
 * @param registerInAppMessageManager         When true, registers and unregisters the [BrazeInAppMessageManager] in
 * [Application.ActivityLifecycleCallbacks.onActivityResumed] and [Application.ActivityLifecycleCallbacks.onActivityPaused]
 * respectively.
 * @param inAppMessagingRegistrationBlocklist A set of [Activity]s for which in-app message registration will not occur.
 * Each class should be retrieved via [Activity.getClass]. If null, an empty set is used instead.
 * @param sessionHandlingBlocklist            A set of [Activity]s for which session handling
 * will not occur. Each class should be retrieved via [Activity.getClass].
 * If null, an empty set is used instead.
 */
open class BrazeActivityLifecycleCallbackListener @JvmOverloads constructor(
    private val sessionHandlingEnabled: Boolean = true,
    private val registerInAppMessageManager: Boolean = true,
    inAppMessagingRegistrationBlocklist: Set<Class<*>?>? = emptySet<Class<*>>(),
    sessionHandlingBlocklist: Set<Class<*>?>? = emptySet<Class<*>>()
) : ActivityLifecycleCallbacks {
    private var inAppMessagingRegistrationBlocklist: Set<Class<*>?>
    private var sessionHandlingBlocklist: Set<Class<*>?>

    init {
        this.inAppMessagingRegistrationBlocklist = inAppMessagingRegistrationBlocklist
            ?: emptySet<Class<*>>()
        this.sessionHandlingBlocklist = sessionHandlingBlocklist ?: emptySet<Class<*>>()
        brazelog(V) {
            "BrazeActivityLifecycleCallbackListener using in-app messaging blocklist: ${this.inAppMessagingRegistrationBlocklist}"
        }
        brazelog(V) {
            "BrazeActivityLifecycleCallbackListener using session handling blocklist: ${this.sessionHandlingBlocklist}"
        }
    }

    /**
     * Constructor that sets a blocklist for session handling and [BrazeInAppMessageManager] registration while also
     * enabling both features.
     *
     * @param inAppMessagingRegistrationBlocklist A set of [Activity]s for which in-app message registration will not
     * occur. Each class should be retrieved via [Activity.getClass].
     * @param sessionHandlingBlocklist            A set of [Activity]s for which session handling will not occur. Each
     * class should be retrieved via [Activity.getClass].
     */
    @JvmOverloads
    constructor(
        inAppMessagingRegistrationBlocklist: Set<Class<*>?>?,
        sessionHandlingBlocklist: Set<Class<*>?>? = emptySet<Class<*>>()
    ) : this(true, true, inAppMessagingRegistrationBlocklist, sessionHandlingBlocklist)

    /**
     * Sets the [Activity.getClass] blocklist for which in-app message registration will not occur.
     */
    fun setInAppMessagingRegistrationBlocklist(blocklist: Set<Class<*>?>) {
        brazelog(V) { "setInAppMessagingRegistrationBlocklist called with blocklist: $blocklist" }
        inAppMessagingRegistrationBlocklist = blocklist
    }

    /**
     * Sets the [Activity.getClass] blocklist for which session handling will not occur.
     */
    fun setSessionHandlingBlocklist(blocklist: Set<Class<*>?>) {
        brazelog(V) { "setSessionHandlingBlocklist called with blocklist: $blocklist" }
        sessionHandlingBlocklist = blocklist
    }

    override fun onActivityStarted(activity: Activity) {
        if (sessionHandlingEnabled && shouldHandleLifecycleMethodsInActivity(activity, true)) {
            brazelog(V) {
                "Automatically calling lifecycle method: openSession for class: ${activity.javaClass}"
            }
            Braze.getInstance(activity.applicationContext).openSession(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (sessionHandlingEnabled && shouldHandleLifecycleMethodsInActivity(activity, true)) {
            brazelog(V) {
                "Automatically calling lifecycle method: closeSession for class: ${activity.javaClass}"
            }
            Braze.getInstance(activity.applicationContext).closeSession(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (registerInAppMessageManager &&
            shouldHandleLifecycleMethodsInActivity(activity, false)
        ) {
            brazelog(V) {
                "Automatically calling lifecycle method: registerInAppMessageManager for class: ${activity.javaClass}"
            }
            BrazeInAppMessageManager.getInstance().registerInAppMessageManager(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (registerInAppMessageManager &&
            shouldHandleLifecycleMethodsInActivity(activity, false)
        ) {
            brazelog(V) {
                "Automatically calling lifecycle method: unregisterInAppMessageManager for class: ${activity.javaClass}"
            }
            BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (registerInAppMessageManager &&
            shouldHandleLifecycleMethodsInActivity(activity, false)
        ) {
            brazelog(V) {
                "Automatically calling lifecycle method: ensureSubscribedToInAppMessageEvents for class: ${activity.javaClass}"
            }
            BrazeInAppMessageManager.getInstance()
                .ensureSubscribedToInAppMessageEvents(activity.applicationContext)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    /**
     * Determines if this [Activity] should be ignored for the purposes of session tracking or in-app message registration.
     */
    @VisibleForTesting
    fun shouldHandleLifecycleMethodsInActivity(
        activity: Activity,
        forSessionHandling: Boolean
    ): Boolean {
        val activityClass: Class<out Activity> = activity.javaClass
        if (activityClass == NotificationTrampolineActivity::class.java) {
            brazelog(V) { "Skipping automatic registration for notification trampoline activity class." }
            // Always ignore
            return false
        }
        return if (forSessionHandling) {
            !sessionHandlingBlocklist.contains(activityClass)
        } else {
            !inAppMessagingRegistrationBlocklist.contains(activityClass)
        }
    }
}
