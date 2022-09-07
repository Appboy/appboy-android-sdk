package com.braze.ui.inappmessage

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.enums.inappmessage.DismissType
import com.braze.enums.inappmessage.MessageType
import com.braze.enums.inappmessage.SlideFrom
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageImmersive
import com.braze.models.inappmessage.InAppMessageSlideup
import com.braze.models.inappmessage.MessageButton
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener
import com.braze.ui.inappmessage.listeners.SwipeDismissTouchListener.DismissCallbacks
import com.braze.ui.inappmessage.listeners.TouchAwareSwipeDismissTouchListener
import com.braze.ui.inappmessage.listeners.TouchAwareSwipeDismissTouchListener.ITouchListener
import com.braze.ui.inappmessage.views.IInAppMessageImmersiveView
import com.braze.ui.inappmessage.views.IInAppMessageView
import com.braze.ui.inappmessage.views.InAppMessageHtmlBaseView
import com.braze.ui.support.isDeviceNotInTouchMode
import com.braze.ui.support.removeViewFromParent
import com.braze.ui.support.setFocusableInTouchModeAndRequestFocus

/**
 * Constructor for base and slideup view wrappers. Adds click listeners to the in-app message view and
 * adds swipe functionality to slideup in-app messages.
 *
 * @param inAppMessageView                  In-app message top level view.
 * @param inAppMessage                      In-app message model.
 * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
 * @param configurationProvider       Configuration provider.
 * @param clickableInAppMessageView         View for which click actions apply.
 * @param buttonViews                       List of views corresponding to MessageButton objects stored in the in-app message model object.
 * These views should map one to one with the MessageButton objects.
 * @param closeButton                       The [View] responsible for closing the in-app message.
 */
open class DefaultInAppMessageViewWrapper @JvmOverloads constructor(
    override val inAppMessageView: View,
    override val inAppMessage: IInAppMessage,
    private val inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener,
    private val configurationProvider: BrazeConfigurationProvider,
    private val openingAnimation: Animation?,
    private val closingAnimation: Animation?,
    private var clickableInAppMessageView: View?,
    private var buttonViews: List<View>? = null,
    private var closeButton: View? = null
) : IInAppMessageViewWrapper {
    @Suppress("deprecation")
    private val inAppMessageCloser: InAppMessageCloser
    override var isAnimatingClose = false
    private var dismissRunnable: Runnable? = null

    /**
     * The [View] that previously held focus before a message is displayed as
     * given via [Activity.getCurrentFocus].
     */
    private var previouslyFocusedView: View? = null

    /**
     * A mapping of the view accessibility flags of views before overriding them.
     * Used in conjunction with [com.braze.configuration.BrazeConfig.Builder.setIsInAppMessageAccessibilityExclusiveModeEnabled]
     */
    private var viewAccessibilityFlagMap = HashMap<Int, Int>()

    /**
     * The [ViewGroup] parent of the in-app message.
     */
    private var contentViewGroupParentLayout: ViewGroup? = null

    init {
        clickableInAppMessageView = clickableInAppMessageView ?: inAppMessageView

        // Only slideup in-app messages can be swiped.
        if (inAppMessage is InAppMessageSlideup) {
            // Adds the swipe listener to the in-app message View. All slideup in-app messages should be dismissible via a swipe
            // (even auto close slideup in-app messages).
            val dismissCallbacks = createDismissCallbacks()
            val touchAwareSwipeListener = TouchAwareSwipeDismissTouchListener(
                inAppMessageView, dismissCallbacks
            )
            // We set a custom touch listener that cancel the auto close runnable when touched and adds
            // a new runnable when the touch ends.
            touchAwareSwipeListener.setTouchListener(createTouchAwareListener())
            clickableInAppMessageView?.setOnTouchListener(touchAwareSwipeListener)
        }
        clickableInAppMessageView?.setOnClickListener(createClickListener())
        @Suppress("deprecation")
        inAppMessageCloser = InAppMessageCloser(this)

        this.closeButton?.setOnClickListener(createCloseInAppMessageClickListener())
        this.buttonViews?.forEach { it.setOnClickListener(createButtonClickListener()) }
    }

    override fun open(activity: Activity) {
        brazelog(V) { "Opening in-app message view wrapper" }
        // Retrieve the ViewGroup which will display the in-app message
        val parentViewGroup = getParentViewGroup(activity)
        val parentViewGroupHeight = parentViewGroup.height
        if (configurationProvider.isInAppMessageAccessibilityExclusiveModeEnabled) {
            contentViewGroupParentLayout = parentViewGroup
            viewAccessibilityFlagMap.clear()
            setAllViewGroupChildrenAsNonAccessibilityImportant(
                contentViewGroupParentLayout,
                viewAccessibilityFlagMap
            )
        }
        previouslyFocusedView = activity.currentFocus

        // If the parent ViewGroup's height is 0, that implies it hasn't been drawn yet. We add a
        // ViewTreeObserver to wait until its drawn so we can get a proper measurement.
        if (parentViewGroupHeight == 0) {
            parentViewGroup.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    view: View,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    parentViewGroup.removeOnLayoutChangeListener(this)
                    brazelog { "Detected (bottom - top) of ${bottom - top} in OnLayoutChangeListener" }
                    parentViewGroup.removeView(inAppMessageView)
                    parentViewGroup.post {
                        addInAppMessageViewToViewGroup(
                            parentViewGroup,
                            inAppMessage,
                            inAppMessageView,
                            inAppMessageViewLifecycleListener
                        )
                    }
                }
            })
        } else {
            brazelog { "Detected root view height of $parentViewGroupHeight" }
            addInAppMessageViewToViewGroup(
                parentViewGroup,
                inAppMessage,
                inAppMessageView,
                inAppMessageViewLifecycleListener
            )
        }
    }

    override fun close() {
        if (configurationProvider.isInAppMessageAccessibilityExclusiveModeEnabled) {
            resetAllViewGroupChildrenToPreviousAccessibilityFlagOrAuto(
                contentViewGroupParentLayout,
                viewAccessibilityFlagMap
            )
        }
        inAppMessageView.removeCallbacks(dismissRunnable)
        inAppMessageViewLifecycleListener.beforeClosed(inAppMessageView, inAppMessage)
        if (inAppMessage.animateOut) {
            isAnimatingClose = true
            setAndStartAnimation(false)
        } else {
            closeInAppMessageView()
        }
    }

    /**
     * Gets the [ViewGroup] which will display the in-app message. Note that
     * if this implementation is overridden, then
     * [DefaultInAppMessageViewWrapper.getLayoutParams] should
     * also most likely be overridden to match the [ViewGroup] subclass
     * returned here.
     */
    private fun getParentViewGroup(activity: Activity): ViewGroup =
        // The android.R.id.content {@link FrameLayout} contains the
        // {@link Activity}'s top-level layout as its first child.
        activity.window.decorView.findViewById(android.R.id.content)

    /**
     * Creates the [ViewGroup.LayoutParams] used for adding the
     * [IInAppMessageView] to the [ViewGroup] returned by
     * [DefaultInAppMessageViewWrapper.getParentViewGroup].
     *
     * Note that the exact subclass of [ViewGroup.LayoutParams] should
     * match that of the [ViewGroup] returned by
     * [DefaultInAppMessageViewWrapper.getParentViewGroup].
     */
    private fun getLayoutParams(inAppMessage: IInAppMessage?): ViewGroup.LayoutParams {
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        if (inAppMessage is InAppMessageSlideup) {
            layoutParams.gravity =
                if (inAppMessage.slideFrom === SlideFrom.TOP) Gravity.TOP else Gravity.BOTTOM
        }
        return layoutParams
    }

    /**
     * Adds the [IInAppMessageView] to the parent [ViewGroup]. Also
     * calls [IInAppMessageViewLifecycleListener.beforeOpened] and
     * [IInAppMessageViewLifecycleListener.afterOpened].
     */
    private fun addInAppMessageViewToViewGroup(
        parentViewGroup: ViewGroup,
        inAppMessage: IInAppMessage,
        inAppMessageView: View,
        inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener
    ) {
        inAppMessageViewLifecycleListener.beforeOpened(inAppMessageView, inAppMessage)
        brazelog { "Adding In-app message view to parent view group." }
        parentViewGroup.addView(inAppMessageView, getLayoutParams(inAppMessage))
        if (inAppMessageView is IInAppMessageView) {
            ViewCompat.requestApplyInsets(parentViewGroup)
            ViewCompat.setOnApplyWindowInsetsListener(parentViewGroup) { _: View?, insets: WindowInsetsCompat? ->
                if (insets == null) {
                    // No margin fixing can be done with a null window inset
                    return@setOnApplyWindowInsetsListener insets
                }
                val castInAppMessageView = inAppMessageView as IInAppMessageView
                if (!castInAppMessageView.hasAppliedWindowInsets) {
                    brazelog(V) { "Calling applyWindowInsets on in-app message view." }
                    castInAppMessageView.applyWindowInsets(insets)
                } else {
                    brazelog { "Not reapplying window insets to in-app message view." }
                }
                insets
            }
        }
        if (inAppMessage.animateIn) {
            brazelog { "In-app message view will animate into the visible area." }
            setAndStartAnimation(true)
            // The afterOpened lifecycle method gets called when the opening animation ends.
        } else {
            brazelog { "In-app message view will be placed instantly into the visible area." }
            // There is no opening animation, so we call the afterOpened lifecycle method immediately.
            if (inAppMessage.dismissType === DismissType.AUTO_DISMISS) {
                addDismissRunnable()
            }
            finalizeViewBeforeDisplay(inAppMessage, inAppMessageView, inAppMessageViewLifecycleListener)
        }
    }

    /**
     * Calls [View.announceForAccessibility] with the [IInAppMessage.getMessage]
     * if the [IInAppMessageView] is [IInAppMessageImmersiveView] or the fallback message
     * [IInAppMessageView] is a [InAppMessageHtmlBaseView].
     */
    private fun announceForAccessibilityIfNecessary(fallbackAccessibilityMessage: String? = "In app message displayed.") {
        if (inAppMessageView is IInAppMessageImmersiveView) {
            val message = inAppMessage.message
            if (inAppMessage is IInAppMessageImmersive) {
                // Announce the header and message together with a brief pause between them
                val header = (inAppMessage as IInAppMessageImmersive).header
                inAppMessageView.announceForAccessibility("$header . $message")
            } else {
                inAppMessageView.announceForAccessibility(message)
            }
        } else if (inAppMessageView is InAppMessageHtmlBaseView) {
            inAppMessageView.announceForAccessibility(fallbackAccessibilityMessage)
        }
    }

    /**
     * Creates a [View.OnClickListener] that calls
     * [IInAppMessageViewLifecycleListener.onClicked].
     *
     * [IInAppMessageViewLifecycleListener.onClicked] is called and
     * can be used to turn off the close animation. Full and modal in-app messages can
     * only be clicked directly when they do not contain buttons.
     * Slideup in-app messages are always clickable.
     */
    private fun createClickListener(): View.OnClickListener {
        return View.OnClickListener {
            if ((inAppMessage as? IInAppMessageImmersive)?.messageButtons?.isEmpty() == true
                || inAppMessage !is IInAppMessageImmersive
            ) {
                inAppMessageViewLifecycleListener.onClicked(
                    inAppMessageCloser,
                    inAppMessageView,
                    inAppMessage
                )
            }
        }
    }

    /**
     * @return A click listener that calls [IInAppMessageViewLifecycleListener.onButtonClicked]
     * if the clicked [View.getId] matches that of a [MessageButton]'s [View].
     */
    private fun createButtonClickListener(): View.OnClickListener {
        return View.OnClickListener { view: View ->
            // The onClicked lifecycle method is called and it can be used to turn off the close animation.
            val inAppMessageImmersive = inAppMessage as IInAppMessageImmersive
            if (inAppMessageImmersive.messageButtons.isEmpty()) {
                brazelog {
                    "Cannot create button click listener since this in-app message does not have message buttons."
                }
                return@OnClickListener
            }
            var index = 0
            buttonViews?.let {
                while (index < it.size) {
                    if (view.id == it[index].id) {
                        val messageButton = inAppMessageImmersive.messageButtons[index]
                        inAppMessageViewLifecycleListener.onButtonClicked(
                            inAppMessageCloser,
                            messageButton,
                            inAppMessageImmersive
                        )
                        return@OnClickListener
                    }
                    index++
                }
            }
        }
    }

    private fun createCloseInAppMessageClickListener(): View.OnClickListener {
        return View.OnClickListener {
            BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true)
        }
    }

    private fun addDismissRunnable() {
        if (dismissRunnable == null) {
            dismissRunnable = Runnable {
                BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true)
            }
            inAppMessageView.postDelayed(
                dismissRunnable,
                inAppMessage.durationInMilliseconds.toLong()
            )
        }
    }

    /**
     * Instantiates and executes the correct animation for the current in-app message. Slideup-type
     * messages slide in from the top or bottom of the view. Other in-app messages fade in
     * and out of view.
     *
     */
    private fun setAndStartAnimation(opening: Boolean) {
        val animation: Animation? = if (opening) {
            openingAnimation
        } else {
            closingAnimation
        }
        animation?.setAnimationListener(createAnimationListener(opening))
        inAppMessageView.clearAnimation()
        inAppMessageView.animation = animation
        animation?.startNow()
        inAppMessageView.invalidate()
    }

    /**
     * Closes the in-app message view.
     * In this order, the following actions are performed:
     *
     * The view is removed from the parent.
     * Any WebViews are explicitly paused or frame execution finished in some way.
     * [IInAppMessageViewLifecycleListener.afterClosed] is called.
     */
    private fun closeInAppMessageView() {
        brazelog { "Closing in-app message view" }
        inAppMessageView.removeViewFromParent()
        // In the case of HTML in-app messages, we need to make sure the
        // WebView stops once the in-app message is removed.
        (inAppMessageView as? InAppMessageHtmlBaseView)?.finishWebViewDisplay()

        // Return the focus before closing the message
        if (previouslyFocusedView != null) {
            brazelog { "Returning focus to view after closing message. View: $previouslyFocusedView" }
            previouslyFocusedView?.requestFocus()
        }
        inAppMessageViewLifecycleListener.afterClosed(inAppMessage)
    }

    /**
     * Performs any last actions before calling
     * [IInAppMessageViewLifecycleListener.beforeOpened].
     */
    private fun finalizeViewBeforeDisplay(
        inAppMessage: IInAppMessage,
        inAppMessageView: View,
        inAppMessageViewLifecycleListener: IInAppMessageViewLifecycleListener
    ) {
        if (isDeviceNotInTouchMode(inAppMessageView)) {
            // Special behavior usual to TV environments

            // For views with defined directional
            // behavior, don't steal focus from them
            when (inAppMessage.messageType) {
                MessageType.MODAL, MessageType.FULL -> {}
                else -> inAppMessageView.setFocusableInTouchModeAndRequestFocus()
            }
        } else {
            inAppMessageView.setFocusableInTouchModeAndRequestFocus()
        }
        announceForAccessibilityIfNecessary()
        inAppMessageViewLifecycleListener.afterOpened(inAppMessageView, inAppMessage)
    }

    private fun createDismissCallbacks(): DismissCallbacks {
        return object : DismissCallbacks {
            override fun canDismiss(token: Any?): Boolean = true

            override fun onDismiss(view: View, token: Any?) {
                inAppMessage.animateOut = false
                BrazeInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(true)
            }
        }
    }

    private fun createTouchAwareListener(): ITouchListener {
        return object : ITouchListener {
            override fun onTouchStartedOrContinued() {
                inAppMessageView.removeCallbacks(dismissRunnable)
            }

            override fun onTouchEnded() {
                if (inAppMessage.dismissType === DismissType.AUTO_DISMISS) {
                    addDismissRunnable()
                }
            }
        }
    }

    private fun createAnimationListener(opening: Boolean): Animation.AnimationListener {
        return if (opening) {
            object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                // This lifecycle callback has been observed to not be called during slideup animations
                // on occasion. Do not add any code that *MUST* be executed here.
                override fun onAnimationEnd(animation: Animation?) {
                    if (inAppMessage.dismissType === DismissType.AUTO_DISMISS) {
                        addDismissRunnable()
                    }
                    brazelog { "In-app message animated into view." }
                    finalizeViewBeforeDisplay(
                        inAppMessage,
                        inAppMessageView,
                        inAppMessageViewLifecycleListener
                    )
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            }
        } else {
            object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    inAppMessageView.clearAnimation()
                    inAppMessageView.visibility = View.GONE
                    closeInAppMessageView()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            }
        }
    }

    companion object {
        /**
         * Sets all [View] children of the [ViewGroup]
         * as [ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS].
         */
        private fun setAllViewGroupChildrenAsNonAccessibilityImportant(
            viewGroup: ViewGroup?,
            viewAccessibilityFlagMap: MutableMap<Int, Int>
        ) {
            if (viewGroup == null) {
                brazelog(W) {
                    "In-app message ViewGroup was null. Not preparing in-app message accessibility for exclusive mode."
                }
                return
            }
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child != null) {
                    viewAccessibilityFlagMap[child.id] = child.importantForAccessibility
                    ViewCompat.setImportantForAccessibility(
                        child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                    )
                }
            }
        }

        /**
         * Sets all [View] children of the [ViewGroup] as their previously
         * mapped accessibility flag, or [ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO] if
         * not found in the mapping.
         */
        @Suppress("NestedBlockDepth", "FunctionMaxLength")
        private fun resetAllViewGroupChildrenToPreviousAccessibilityFlagOrAuto(
            viewGroup: ViewGroup?,
            viewAccessibilityFlagMap: Map<Int, Int>
        ) {
            if (viewGroup == null) {
                brazelog(W) {
                    "In-app message ViewGroup was null. Not resetting in-app message accessibility for exclusive mode."
                }
                return
            }
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child != null) {
                    val id = child.id
                    if (viewAccessibilityFlagMap.containsKey(id)) {
                        viewAccessibilityFlagMap[id]?.let {
                            ViewCompat.setImportantForAccessibility(
                                child,
                                it
                            )
                        }
                    } else {
                        ViewCompat.setImportantForAccessibility(
                            child,
                            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO
                        )
                    }
                }
            }
        }
    }
}
