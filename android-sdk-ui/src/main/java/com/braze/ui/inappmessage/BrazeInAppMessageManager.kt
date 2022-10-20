package com.braze.ui.inappmessage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.annotation.VisibleForTesting
import com.braze.Braze.Companion.getInstance
import com.braze.BrazeInternal.retryInAppMessage
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.enums.inappmessage.InAppMessageFailureType
import com.braze.enums.inappmessage.Orientation
import com.braze.events.IEventSubscriber
import com.braze.events.InAppMessageEvent
import com.braze.events.SdkDataWipeEvent
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.InAppMessageImmersiveBase
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.getPrettyPrintedString
import com.braze.support.wouldPushPermissionPromptDisplay
import com.braze.ui.actions.brazeactions.containsAnyPushPermissionBrazeActions
import com.braze.ui.actions.brazeactions.containsInvalidBrazeAction
import com.braze.ui.inappmessage.listeners.DefaultInAppMessageViewLifecycleListener
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener
import com.braze.ui.inappmessage.utils.BackgroundInAppMessagePreparer.prepareInAppMessageForDisplay
import com.braze.ui.inappmessage.views.IInAppMessageImmersiveView
import com.braze.ui.inappmessage.views.IInAppMessageView
import com.braze.ui.inappmessage.views.InAppMessageHtmlBaseView
import com.braze.ui.support.isCurrentOrientationValid
import com.braze.ui.support.isRunningOnTablet
import com.braze.ui.support.removeViewFromParent
import com.braze.ui.support.setActivityRequestedOrientation
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * This class is used to display in-app messages that are either sent from Braze
 * or are created natively in the host app. It will only show one in-app message at a time and will
 * place all other in-app messages onto a stack. The [BrazeInAppMessageManager] will also keep track of in-app
 * impressions and clicks, which can be viewed on the dashboard.
 *
 * If there is already an in-app message being displayed, the new in-app message will be put onto the top of the
 * stack and can be displayed at a later time. If there is no in-app message being displayed, then the
 * [IInAppMessageManagerListener.beforeInAppMessageDisplayed]
 * will be called. The [InAppMessageOperation] return value can be used to
 * control when the in-app message should be displayed. A suggested usage of this method would be to delay
 * in-app message messages in certain parts of the app by returning [InAppMessageOperation.DISPLAY_LATER]
 * when in-app message would be distracting to the users app experience. If the method returns
 * [InAppMessageOperation.DISPLAY_NOW] then the in-app message will be displayed
 * immediately.
 *
 * The [IInAppMessageManagerListener.onInAppMessageClicked]
 * and [IInAppMessageManagerListener.onInAppMessageDismissed]
 * methods can be used to override the default click and dismiss behavior.
 *
 * By default, in-app messages fade in and out from view. The slideup type of in-app message slides in and out of view
 * can be dismissed by swiping the view horizontally. If the in-app message's DismissType is set to AUTO_DISMISS,
 * then the in-app message will animate out of view once the set duration time has elapsed.
 *
 * In order to use a custom view, you must create a custom view factory using the
 * [BrazeInAppMessageManager.setCustomInAppMessageViewFactory] method.
 *
 * A new in-app message [android.view.View] object is created when a in-app message is displayed and also
 * when the user navigates away to another [android.app.Activity]. This happens so that the
 * Activity can be garbage collected and does not create a memory leak. For that reason, the
 * [BrazeInAppMessageManager.registerInAppMessageManager]
 * and [BrazeInAppMessageManager.unregisterInAppMessageManager]
 * must be called in the Activity.onResume() and Activity.onPause()
 * methods of every Activity.
 */
// Static field leak doesn't apply to this singleton since the activity is nullified after the manager is unregistered.
@SuppressLint("StaticFieldLeak")
open class BrazeInAppMessageManager : InAppMessageManagerBase() {
    private val inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener =
        DefaultInAppMessageViewLifecycleListener()

    @JvmField
    @VisibleForTesting
    val displayingInAppMessage = AtomicBoolean(false)

    /**
     * The stack of In-App Messages waiting to be displayed.
     */
    @VisibleForTesting
    val inAppMessageStack = Stack<IInAppMessage>()
    val inAppMessageEventMap = mutableMapOf<IInAppMessage, InAppMessageEvent>()
    private var inAppMessageEventSubscriber: IEventSubscriber<InAppMessageEvent>? = null
    private var sdkDataWipeEventSubscriber: IEventSubscriber<SdkDataWipeEvent>? = null
    private var originalOrientation: Int? = null
    private var configurationProvider: BrazeConfigurationProvider? = null
    private var inAppMessageViewWrapper: IInAppMessageViewWrapper? = null

    /**
     * An In-App Message being carried over during the
     * [unregisterInAppMessageManager]
     * [registerInAppMessageManager] transition.
     */
    @VisibleForTesting
    var carryoverInAppMessage: IInAppMessage? = null

    /**
     * An In-App Message that could not display after a call to [requestDisplayInAppMessage]
     * due to no [Activity] being registered via [registerInAppMessageManager].
     */
    @VisibleForTesting
    var unregisteredInAppMessage: IInAppMessage? = null

    /**
     * Gets whether an in-app message is currently displaying on the device.
     */
    val isCurrentlyDisplayingInAppMessage: Boolean
        get() = displayingInAppMessage.get()

    /**
     * Ensures the InAppMessageManager is subscribed in-app message events if not already subscribed.
     * Before this method gets called, the InAppMessageManager is not subscribed to in-app message events
     * and cannot display them. Every call to registerInAppMessageManager() calls this method.
     *
     * If events with triggers are logged before the first call to registerInAppMessageManager(), then the
     * corresponding in-app message won't display. Thus, if logging events with triggers before the first call
     * to registerInAppMessageManager(), then call this method to ensure that in-app message events
     * are correctly handled by the BrazeInAppMessageManager.
     *
     * For example, if logging custom events with triggers in your first activity's onCreate(), be sure
     * to call this method manually beforehand so that the in-app message will get displayed by the time
     * registerInAppMessageManager() gets called.
     *
     * @param context The application context
     */
    open fun ensureSubscribedToInAppMessageEvents(context: Context) {
        if (inAppMessageEventSubscriber != null) {
            brazelog {
                "Removing existing in-app message event subscriber before subscribing a new one."
            }
            getInstance(context).removeSingleSubscription(
                inAppMessageEventSubscriber,
                InAppMessageEvent::class.java
            )
        }
        brazelog { "Subscribing in-app message event subscriber" }
        inAppMessageEventSubscriber = createInAppMessageEventSubscriber().also {
            getInstance(context).subscribeToNewInAppMessages(it)
        }

        if (sdkDataWipeEventSubscriber != null) {
            brazelog(V) { "Removing existing sdk data wipe event subscriber before subscribing a new one." }
            getInstance(context).removeSingleSubscription(
                sdkDataWipeEventSubscriber,
                SdkDataWipeEvent::class.java
            )
        }
        brazelog(V) { "Subscribing sdk data wipe subscriber" }
        sdkDataWipeEventSubscriber = IEventSubscriber<SdkDataWipeEvent> {
            inAppMessageStack.clear()
            carryoverInAppMessage = null
            unregisteredInAppMessage = null
        }.also {
            getInstance(context).addSingleSynchronousSubscription(
                it, SdkDataWipeEvent::class.java
            )
        }
    }

    /**
     * Registers the in-app message manager, which will listen to and display incoming in-app messages. The
     * current Activity is required in order to properly inflate and display the in-app message view.
     *
     * Important note: Every Activity must call [registerInAppMessageManager] in the onResume lifecycle
     * method, otherwise in-app messages may be lost!
     *
     * This method also calls [BrazeInAppMessageManager.ensureSubscribedToInAppMessageEvents].
     * To be sure that no in-app messages are lost, you should call [BrazeInAppMessageManager.ensureSubscribedToInAppMessageEvents] as early
     * as possible in your app, preferably in your [Application.onCreate].
     *
     * @param activity The current Activity.
     */
    open fun registerInAppMessageManager(activity: Activity?) {
        if (activity == null) {
            brazelog(W) { "Null Activity passed to registerInAppMessageManager. Doing nothing" }
            return
        } else {
            brazelog(V) { "Registering InAppMessageManager with activity: ${activity.localClassName}" }
        }

        // We need the current Activity so that we can inflate or programmatically create the in-app message
        // View for each Activity. We cannot share the View because doing so would create a memory leak.
        mActivity = activity
        if (mApplicationContext == null) {
            // Note, because this class is a singleton and doesn't have any dependencies passed in,
            // we cache the application context here because it's not available (as it normally would be
            // from Braze initialization).
            mApplicationContext = activity.applicationContext
            if (mApplicationContext == null) {
                brazelog(W) { "Activity had null applicationContext in registerInAppMessageManager. Doing Nothing." }
                return
            }
        }
        if (configurationProvider == null) {
            configurationProvider = mApplicationContext?.let { BrazeConfigurationProvider(it) }
        }

        // We have a special check to see if the host app switched to a different Activity (or recreated
        // the same Activity during an orientation change) so that we can redisplay the in-app message.
        if (carryoverInAppMessage != null) {
            carryoverInAppMessage?.let {
                brazelog { "Requesting display of carryover in-app message." }
                it.animateIn = false
                displayInAppMessage(it, true)
            }
            carryoverInAppMessage = null
        } else {
            unregisteredInAppMessage?.let {
                brazelog { "Adding previously unregistered in-app message." }
                addInAppMessage(it)
                unregisteredInAppMessage = null
            }
        }
        mApplicationContext?.let { ensureSubscribedToInAppMessageEvents(it) }
    }

    /**
     * Unregisters the in-app message manager.
     *
     * @param activity The current Activity.
     */
    open fun unregisterInAppMessageManager(activity: Activity?) {
        if (shouldNextUnregisterBeSkipped) {
            brazelog {
                "Skipping unregistration due to " +
                    "setShouldNextUnregisterBeSkipped being true. Activity: ${activity?.localClassName}"
            }
            shouldNextUnregisterBeSkipped = false
            return
        }
        if (activity == null) {
            // The activity is not needed to unregister so we can continue unregistration with it being null.
            brazelog(W) { "Null Activity passed to unregisterInAppMessageManager." }
        } else {
            brazelog(V) { "Unregistering InAppMessageManager from activity: ${activity.localClassName}" }
        }

        // If there is an in-app message being displayed when the host app transitions to another Activity (or
        // requests an orientation change), we save it in memory so that we can redisplay it when the
        // operation is done.
        val viewWrapper = inAppMessageViewWrapper
        if (viewWrapper != null) {
            val inAppMessageView = viewWrapper.inAppMessageView
            if (inAppMessageView is InAppMessageHtmlBaseView) {
                brazelog { "In-app message view includes HTML. Removing the page finished listener." }
                inAppMessageView.setHtmlPageFinishedListener(null)
            }
            inAppMessageView.removeViewFromParent()

            // Only continue if we're not animating a close
            carryoverInAppMessage = if (viewWrapper.isAnimatingClose) {
                // Note that mInAppMessageViewWrapper may be null after this call
                inAppMessageViewLifecycleListener.afterClosed(viewWrapper.inAppMessage)
                null
            } else {
                viewWrapper.inAppMessage
            }
            inAppMessageViewWrapper = null
        } else {
            carryoverInAppMessage = null
        }
        mActivity = null
        displayingInAppMessage.set(false)
    }

    /**
     * Provides a in-app message that will then be handled by the in-app message manager. If no in-app message is being
     * displayed, it will attempt to display the in-app message immediately.
     *
     * @param inAppMessage The in-app message to add.
     */
    open fun addInAppMessage(inAppMessage: IInAppMessage?) {
        if (inAppMessage != null) {
            inAppMessageStack.push(inAppMessage)
            requestDisplayInAppMessage()
        }
    }

    /**
     * Asks the InAppMessageManager to display the next in-app message if one is not currently being displayed.
     * If one is being displayed, this method will return false and will not display the next in-app message.
     *
     * @return A boolean value indicating whether an asynchronous task to display the in-app message display was executed.
     */
    @Suppress("LongMethod", "ReturnCount")
    open fun requestDisplayInAppMessage(): Boolean {
        return try {
            if (mActivity == null) {
                if (!inAppMessageStack.empty()) {
                    brazelog(W) {
                        "No activity is currently registered to receive in-app messages. Saving in-app " +
                            "message as unregistered in-app message. It will automatically be displayed " +
                            "when the next activity registers to receive in-app messages."
                    }
                    unregisteredInAppMessage = inAppMessageStack.pop()
                } else {
                    brazelog {
                        "No activity is currently registered to receive in-app messages and the in-app " +
                            "message stack is empty. Doing nothing."
                    }
                }
                return false
            }
            if (displayingInAppMessage.get()) {
                brazelog { "A in-app message is currently being displayed. Ignoring request to display in-app message." }
                return false
            }
            if (inAppMessageStack.isEmpty()) {
                brazelog { "The in-app message stack is empty. No in-app message will be displayed." }
                return false
            }
            val inAppMessage = inAppMessageStack.pop()
            val inAppMessageOperation: InAppMessageOperation = if (!inAppMessage.isControl) {
                inAppMessageManagerListener.beforeInAppMessageDisplayed(inAppMessage)
            } else {
                brazelog { "Using the control in-app message manager listener." }
                controlInAppMessageManagerListener.beforeInAppMessageDisplayed(inAppMessage)
            }
            when (inAppMessageOperation) {
                InAppMessageOperation.DISPLAY_NOW -> brazelog {
                    "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_NOW. The " +
                        "in-app message will be displayed."
                }
                InAppMessageOperation.DISPLAY_LATER -> {
                    brazelog {
                        "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_LATER. The " +
                            "in-app message will be pushed back onto the stack."
                    }
                    inAppMessageStack.push(inAppMessage)
                    return false
                }
                InAppMessageOperation.DISCARD -> {
                    brazelog {
                        "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISCARD. The " +
                            "in-app message will not be displayed and will not be put back on the stack."
                    }
                    return false
                }
            }
            prepareInAppMessageForDisplay(inAppMessage)
            true
        } catch (e: Exception) {
            brazelog(E, e) { "Error running requestDisplayInAppMessage" }
            false
        }
    }

    /**
     * Hides any currently displaying in-app message. Note that in-app message animation
     * is configurable on the in-app message model itself and should be configured there.
     *
     * @param dismissed whether the message was dismissed by the user. If dismissed is true,
     * IInAppMessageViewLifecycleListener.onDismissed() will be called on the current
     * IInAppMessageViewLifecycleListener.
     */
    open fun hideCurrentlyDisplayingInAppMessage(dismissed: Boolean) {
        shouldNextUnregisterBeSkipped = false
        val inAppMessageWrapperView = inAppMessageViewWrapper
        if (inAppMessageWrapperView != null) {
            if (dismissed) {
                inAppMessageViewLifecycleListener.onDismissed(
                    inAppMessageWrapperView.inAppMessageView,
                    inAppMessageWrapperView.inAppMessage
                )
            }
            inAppMessageWrapperView.close()
        }
    }

    /**
     * Resets the [BrazeInAppMessageManager] to its original state before the last in-app message
     * was displayed. This allows for a new in-app message to be displayed after calling this method.
     * [ViewUtils.setActivityRequestedOrientation] is called with the original
     * orientation before the last in-app message was displayed.
     */
    open fun resetAfterInAppMessageClose() {
        brazelog(V) { "Resetting after in-app message close." }
        inAppMessageViewWrapper = null
        val activity = mActivity
        val origOrientation = originalOrientation
        displayingInAppMessage.set(false)
        if (activity != null && origOrientation != null) {
            brazelog { "Setting requested orientation to original orientation $origOrientation" }
            activity.setActivityRequestedOrientation(origOrientation)
            originalOrientation = null
        }
    }

    // For backwards compatibility
    open fun getIsCurrentlyDisplayingInAppMessage() =
        displayingInAppMessage.get()

    /**
     * Internal method, do not call as part of an integration!
     *
     * Attempts to display an [IInAppMessage] to the user.
     *
     * @param inAppMessage The [IInAppMessage].
     * @param isCarryOver  If this [IInAppMessage] is "carried over" from an [Activity] transition.
     */
    @Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth", "ThrowsCount", "TooGenericExceptionThrown")
    // The TooGenericExceptionThrown is here because some clients may have written code that's dependent
    // on that very generic exception, so we want to stay backwards compatible.
    open fun displayInAppMessage(inAppMessage: IInAppMessage, isCarryOver: Boolean) {
        brazelog(V) {
            "Attempting to display in-app message with payload: ${inAppMessage.forJsonPut().getPrettyPrintedString()}"
        }

        // Note: for displayingInAppMessage to be accurate it requires this method does not exit
        // anywhere but the at the end of this try/catch when we know whether we are successfully
        // displaying the in-app message or not.
        if (!displayingInAppMessage.compareAndSet(false, true)) {
            brazelog {
                "A in-app message is currently being displayed. Adding in-app message back on the stack."
            }
            inAppMessageStack.push(inAppMessage)
            return
        }
        try {
            val activity = mActivity
            if (activity == null) {
                carryoverInAppMessage = inAppMessage
                throw Exception(
                    "No Activity is currently registered to receive in-app messages. Registering " +
                        "in-app message as carry-over in-app message. It will automatically be " +
                        "displayed when the next Activity registers to receive in-app messages."
                )
            }
            if (!isCarryOver) {
                val inAppMessageExpirationTimestamp = inAppMessage.expirationTimestamp
                if (inAppMessageExpirationTimestamp > 0) {
                    val currentTimeMillis = System.currentTimeMillis()
                    if (currentTimeMillis > inAppMessageExpirationTimestamp) {
                        throw Exception(
                            "In-app message is expired. Doing nothing. Expiration: " +
                                "$inAppMessageExpirationTimestamp. Current time: $currentTimeMillis"
                        )
                    }
                } else {
                    brazelog { "Expiration timestamp not defined. Continuing." }
                }
            } else {
                brazelog { "Not checking expiration status for carry-over in-app message." }
            }
            if (!verifyOrientationStatus(inAppMessage)) {
                // No display failure gets logged here since control in-app messages would also be affected.
                throw Exception("Current orientation did not match specified orientation for in-app message. Doing nothing.")
            }

            // At this point, the only factors that would inhibit in-app message display are view creation issues.
            // Since control in-app messages have no view, this is the end of execution for control in-app messages
            if (inAppMessage.isControl) {
                brazelog {
                    "Not displaying control in-app message. Logging impression and ending display execution."
                }
                inAppMessage.logImpression()
                resetAfterInAppMessageClose()
                return
            }
            if (inAppMessage.containsInvalidBrazeAction()) {
                val inAppMessageEvent = inAppMessageEventMap[inAppMessage]
                brazelog(I) { "Cannot show message containing an invalid Braze Action." }
                if (inAppMessageEvent != null) {
                    brazelog(I) { "Attempting to perform any fallback actions." }
                    retryInAppMessage(activity.applicationContext, inAppMessageEvent)
                }
                resetAfterInAppMessageClose()
                return
            }
            if (inAppMessage.containsAnyPushPermissionBrazeActions()
                && !activity.wouldPushPermissionPromptDisplay()
            ) {
                val inAppMessageEvent = inAppMessageEventMap[inAppMessage]
                brazelog(I) {
                    "Cannot show message containing a Braze Actions Push Prompt due to existing " +
                        "push prompt status, Android API version, or Target SDK level."
                }
                if (inAppMessageEvent != null) {
                    brazelog(I) { "Attempting to perform any fallback actions." }
                    retryInAppMessage(activity.applicationContext, inAppMessageEvent)
                }
                resetAfterInAppMessageClose()
                return
            }
            val inAppMessageViewFactory = getInAppMessageViewFactory(inAppMessage)
            if (inAppMessageViewFactory == null) {
                inAppMessage.logDisplayFailure(InAppMessageFailureType.DISPLAY_VIEW_GENERATION)
                throw Exception("ViewFactory from getInAppMessageViewFactory was null.")
            }
            val inAppMessageView = inAppMessageViewFactory.createInAppMessageView(
                activity, inAppMessage
            )
            if (inAppMessageView == null) {
                inAppMessage.logDisplayFailure(InAppMessageFailureType.DISPLAY_VIEW_GENERATION)
                throw Exception(
                    "The in-app message view returned from the IInAppMessageViewFactory was null. " +
                        "The in-app message will not be displayed and will not be put back on the stack."
                )
            }
            if (inAppMessageView.parent != null) {
                inAppMessage.logDisplayFailure(InAppMessageFailureType.DISPLAY_VIEW_GENERATION)
                throw Exception(
                    "The in-app message view returned from the IInAppMessageViewFactory already has a parent. This " +
                        "is a sign that the view is being reused. The IInAppMessageViewFactory method createInAppMessageView" +
                        "must return a new view without a parent. The in-app message will not be displayed and will not " +
                        "be put back on the stack."
                )
            }

            val configProvider = configurationProvider
                ?: throw Exception(
                    "configurationProvider is null. The in-app message will not be displayed and will not be" +
                        "put back on the stack."
                )
            val openingAnimation = inAppMessageAnimationFactory.getOpeningAnimation(inAppMessage)
            val closingAnimation = inAppMessageAnimationFactory.getClosingAnimation(inAppMessage)
            val viewWrapperFactory = inAppMessageViewWrapperFactory
            inAppMessageViewWrapper = when (inAppMessageView) {
                is IInAppMessageImmersiveView -> {
                    brazelog { "Creating view wrapper for immersive in-app message." }
                    val inAppMessageViewImmersive = inAppMessageView as IInAppMessageImmersiveView
                    val inAppMessageImmersiveBase = inAppMessage as InAppMessageImmersiveBase
                    val numButtons = inAppMessageImmersiveBase.messageButtons.size
                    viewWrapperFactory.createInAppMessageViewWrapper(
                        inAppMessageView,
                        inAppMessage,
                        inAppMessageViewLifecycleListener,
                        configProvider,
                        openingAnimation,
                        closingAnimation,
                        inAppMessageViewImmersive.messageClickableView,
                        inAppMessageViewImmersive.getMessageButtonViews(numButtons),
                        inAppMessageViewImmersive.messageCloseButtonView
                    )
                }
                is IInAppMessageView -> {
                    brazelog { "Creating view wrapper for base in-app message." }
                    val inAppMessageViewBase = inAppMessageView as IInAppMessageView
                    viewWrapperFactory.createInAppMessageViewWrapper(
                        inAppMessageView,
                        inAppMessage,
                        inAppMessageViewLifecycleListener,
                        configProvider,
                        openingAnimation,
                        closingAnimation,
                        inAppMessageViewBase.messageClickableView
                    )
                }
                else -> {
                    brazelog { "Creating view wrapper for in-app message." }
                    viewWrapperFactory.createInAppMessageViewWrapper(
                        inAppMessageView,
                        inAppMessage,
                        inAppMessageViewLifecycleListener,
                        configProvider,
                        openingAnimation,
                        closingAnimation,
                        inAppMessageView
                    )
                }
            }

            val viewWrapper = inAppMessageViewWrapper

            // If this message includes HTML, delay display until the content has finished loading
            if (inAppMessageView is InAppMessageHtmlBaseView) {
                brazelog {
                    "In-app message view includes HTML. Delaying display until the content has finished loading."
                }
                inAppMessageView.setHtmlPageFinishedListener {
                    try {
                        if (viewWrapper != null) {
                            brazelog {
                                "Page has finished loading. Opening in-app message view wrapper."
                            }
                            viewWrapper.open(activity)
                        }
                    } catch (e: Exception) {
                        brazelog(E, e) { "Failed to open view wrapper in page finished listener" }
                    }
                }
            } else {
                viewWrapper?.open(activity)
            }
        } catch (e: Throwable) {
            brazelog(E, e) {
                "Could not display in-app message with payload: ${inAppMessage.forJsonPut().getPrettyPrintedString()}"
            }
            resetAfterInAppMessageClose()
        }
    }

    private fun createInAppMessageEventSubscriber(): IEventSubscriber<InAppMessageEvent> {
        return IEventSubscriber { event: InAppMessageEvent ->
            val inAppMessage = event.inAppMessage
            inAppMessageEventMap[inAppMessage] = event
            addInAppMessage(inAppMessage)
        }
    }

    /**
     * For in-app messages that have a preferred orientation, locks the screen orientation and
     * returns true if the screen is currently in the preferred orientation. If the screen is not
     * currently in the preferred orientation, returns false.
     *
     * Always returns true for tablets, regardless of current orientation.
     *
     * Always returns true if the in-app message doesn't have a preferred orientation.
     */
    @SuppressLint("InlinedApi")
    @VisibleForTesting
    open fun verifyOrientationStatus(inAppMessage: IInAppMessage): Boolean {
        val activity = mActivity
        val preferredOrientation = inAppMessage.orientation

        if (activity == null) {
            brazelog(W) { "Cannot verify orientation status with null Activity." }
        } else if (activity.isRunningOnTablet()) {
            brazelog { "Running on tablet. In-app message can be displayed in any orientation." }
        } else if (preferredOrientation === Orientation.ANY) {
            brazelog { "Any orientation specified. In-app message can be displayed in any orientation." }
        } else {
            val currentScreenOrientation = activity.resources.configuration.orientation
            return if (isCurrentOrientationValid(currentScreenOrientation, preferredOrientation)) {
                if (originalOrientation == null) {
                    brazelog { "Requesting orientation lock." }
                    originalOrientation = activity.requestedOrientation
                    // This constant was introduced in API 18, so for devices pre 18 this will be a no-op
                    activity.setActivityRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                }
                true
            } else {
                false
            }
        }
        return true
    }

    companion object {
        private val instanceLock = ReentrantLock()

        @Volatile
        private var instance: BrazeInAppMessageManager? = null

        @JvmStatic
        fun getInstance(): BrazeInAppMessageManager {
            if (instance != null) {
                return instance as BrazeInAppMessageManager
            }
            instanceLock.withLock {
                if (instance == null) {
                    instance = BrazeInAppMessageManager()
                }
            }
            return instance as BrazeInAppMessageManager
        }
    }
}
