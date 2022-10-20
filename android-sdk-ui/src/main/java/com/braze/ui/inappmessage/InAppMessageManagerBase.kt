package com.braze.ui.inappmessage

import android.app.Activity
import android.content.Context
import androidx.annotation.RestrictTo
import com.braze.enums.inappmessage.MessageType
import com.braze.models.inappmessage.IInAppMessage
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.factories.DefaultInAppMessageAnimationFactory
import com.braze.ui.inappmessage.factories.DefaultInAppMessageFullViewFactory
import com.braze.ui.inappmessage.factories.DefaultInAppMessageHtmlFullViewFactory
import com.braze.ui.inappmessage.factories.DefaultInAppMessageHtmlViewFactory
import com.braze.ui.inappmessage.factories.DefaultInAppMessageModalViewFactory
import com.braze.ui.inappmessage.factories.DefaultInAppMessageSlideupViewFactory
import com.braze.ui.inappmessage.factories.DefaultInAppMessageViewWrapperFactory
import com.braze.ui.inappmessage.listeners.DefaultHtmlInAppMessageActionListener
import com.braze.ui.inappmessage.listeners.DefaultInAppMessageManagerListener
import com.braze.ui.inappmessage.listeners.DefaultInAppMessageWebViewClientListener
import com.braze.ui.inappmessage.listeners.IHtmlInAppMessageActionListener
import com.braze.ui.inappmessage.listeners.IInAppMessageManagerListener
import com.braze.ui.inappmessage.listeners.IInAppMessageWebViewClientListener

@Suppress("TooManyFunctions")
open class InAppMessageManagerBase {
    open val doesClickOutsideModalViewDismissInAppMessageView
        get() = doesClickOutsideModalViewDismissInAppMessageViewField

    private var doesClickOutsideModalViewDismissInAppMessageViewField = false

    /**
     * Determines whether the next call to
     * [BrazeInAppMessageManager.unregisterInAppMessageManager] will be ignored.
     */
    open var shouldNextUnregisterBeSkipped = false
        set(shouldSkip) {
            brazelog { "Setting setShouldNextUnregisterBeSkipped to $shouldSkip" }
            field = shouldSkip
        }

    open val doesBackButtonDismissInAppMessageView
        get() = doesBackButtonDismissInAppMessageViewField

    private var doesBackButtonDismissInAppMessageViewField = true

    // Since many clients have written code against this, we want to maintain backwards compatibility
    // as much as possible. That's why we have these names and present them as JvmFields.
    @JvmField
    @Suppress("VariableNaming")
    protected var mActivity: Activity? = null

    @JvmField
    @Suppress("VariableNaming")
    protected var mApplicationContext: Context? = null

    // These serve the purpose of allowing people to write more Kotlin-friendly code, but also provide
    // the getActivity() and getApplicationContext() Java generated code for backwards compatibility
    open val activity
        get() = mActivity

    open val applicationContext
        get() = mApplicationContext

    // view listeners
    private val inAppMessageWebViewClientListener: IInAppMessageWebViewClientListener =
        DefaultInAppMessageWebViewClientListener()

    // html action listeners
    private val defaultHtmlInAppMessageActionListener: IHtmlInAppMessageActionListener =
        DefaultHtmlInAppMessageActionListener()

    // factories
    private val inAppMessageSlideupViewFactory: IInAppMessageViewFactory =
        DefaultInAppMessageSlideupViewFactory()
    private val inAppMessageModalViewFactory: IInAppMessageViewFactory =
        DefaultInAppMessageModalViewFactory()
    private val inAppMessageFullViewFactory: IInAppMessageViewFactory =
        DefaultInAppMessageFullViewFactory()
    private val inAppMessageHtmlFullViewFactory: IInAppMessageViewFactory =
        DefaultInAppMessageHtmlFullViewFactory(inAppMessageWebViewClientListener)
    private val inAppMessageHtmlViewFactory: IInAppMessageViewFactory =
        DefaultInAppMessageHtmlViewFactory(inAppMessageWebViewClientListener)

    // animation factory
    private val inAppMessageAnimationFactoryField: IInAppMessageAnimationFactory =
        DefaultInAppMessageAnimationFactory()

    // manager listeners
    private val defaultInAppMessageManagerListener: IInAppMessageManagerListener =
        DefaultInAppMessageManagerListener()

    // view wrapper factory
    private val defaultInAppMessageViewWrapperFactory: IInAppMessageViewWrapperFactory =
        DefaultInAppMessageViewWrapperFactory()

    // custom listeners
    private var customInAppMessageViewFactory: IInAppMessageViewFactory? = null
    private var customInAppMessageAnimationFactory: IInAppMessageAnimationFactory? = null
    private var customInAppMessageManagerListener: IInAppMessageManagerListener? = null
    private var customInAppMessageViewWrapperFactory: IInAppMessageViewWrapperFactory? = null
    private var customHtmlInAppMessageActionListener: IHtmlInAppMessageActionListener? = null

    /**
     * A custom listener to be fired for control in-app messages.
     *
     *
     * see [IInAppMessage.isControl]
     */
    private var customControlInAppMessageManagerListener: IInAppMessageManagerListener? = null
    open val inAppMessageManagerListener: IInAppMessageManagerListener
        get() = customInAppMessageManagerListener ?: defaultInAppMessageManagerListener

    /**
     * A [IInAppMessageManagerListener] to be used only for control in-app messages.
     *
     * see [IInAppMessage.isControl]
     */
    open val controlInAppMessageManagerListener: IInAppMessageManagerListener
        get() = customControlInAppMessageManagerListener ?: defaultInAppMessageManagerListener
    open val htmlInAppMessageActionListener: IHtmlInAppMessageActionListener
        get() = customHtmlInAppMessageActionListener ?: defaultHtmlInAppMessageActionListener

    open val inAppMessageViewWrapperFactory: IInAppMessageViewWrapperFactory
        get() = customInAppMessageViewWrapperFactory ?: defaultInAppMessageViewWrapperFactory
    open val inAppMessageAnimationFactory: IInAppMessageAnimationFactory
        get() = customInAppMessageAnimationFactory ?: inAppMessageAnimationFactoryField

    @get:RestrictTo(RestrictTo.Scope.TESTS)
    open val isActivitySet: Boolean
        get() = activity != null

    /**
     * Gets the default [IInAppMessageViewFactory] as returned by the [BrazeInAppMessageManager]
     * for the given [IInAppMessage].
     *
     * @return The [IInAppMessageViewFactory] or null if the message type does not have a [IInAppMessageViewFactory].
     */
    open fun getDefaultInAppMessageViewFactory(inAppMessage: IInAppMessage): IInAppMessageViewFactory? {
        return when (inAppMessage.messageType) {
            MessageType.SLIDEUP -> inAppMessageSlideupViewFactory
            MessageType.MODAL -> inAppMessageModalViewFactory
            MessageType.FULL -> inAppMessageFullViewFactory
            MessageType.HTML_FULL -> inAppMessageHtmlFullViewFactory
            MessageType.HTML -> inAppMessageHtmlViewFactory
            else -> {
                brazelog(W) {
                    "Failed to find view factory for in-app message with type: ${inAppMessage.messageType}"
                }
                null
            }
        }
    }

    open fun getInAppMessageViewFactory(inAppMessage: IInAppMessage): IInAppMessageViewFactory? =
        customInAppMessageViewFactory ?: getDefaultInAppMessageViewFactory(inAppMessage)

    /**
     * Sets whether the hardware back button dismisses in-app messages. Defaults to true.
     * Note that the hardware back button default behavior will be used instead (i.e. the host [Activity]'s
     * [Activity.onKeyDown] method will be called).
     */
    open fun setBackButtonDismissesInAppMessageView(backButtonDismissesInAppMessageView: Boolean) {
        brazelog { "In-App Message back button dismissal set to $backButtonDismissesInAppMessageView" }
        doesBackButtonDismissInAppMessageViewField = backButtonDismissesInAppMessageView
    }

    /**
     * Sets whether the tapping outside the modal in-app message content dismiss the
     * message. Defaults to false.
     */
    open fun setClickOutsideModalViewDismissInAppMessageView(doesDismiss: Boolean) {
        brazelog { "Modal In-App Message outside tap dismissal set to $doesDismiss" }
        doesClickOutsideModalViewDismissInAppMessageViewField = doesDismiss
    }

    /**
     * Assigns a custom [IInAppMessageManagerListener] that will be used when displaying in-app messages. To revert
     * back to the default [IInAppMessageManagerListener], call this method with null.
     *
     *
     * see [IInAppMessage.isControl]
     *
     * @param inAppMessageManagerListener A custom [IInAppMessageManagerListener] or null (to revert back to the
     * default [IInAppMessageManagerListener]).
     */
    open fun setCustomInAppMessageManagerListener(inAppMessageManagerListener: IInAppMessageManagerListener?) {
        brazelog { "Custom InAppMessageManagerListener set" }
        customInAppMessageManagerListener = inAppMessageManagerListener
    }

    /**
     * Assigns a custom [IInAppMessageManagerListener] that will be used when displaying control in-app messages. To revert
     * back to the default [IInAppMessageManagerListener], call this method with null.
     *
     * @param inAppMessageManagerListener A custom [IInAppMessageManagerListener] for control in-app messages or null (to revert back to the
     * default [IInAppMessageManagerListener]).
     */
    open fun setCustomControlInAppMessageManagerListener(inAppMessageManagerListener: IInAppMessageManagerListener?) {
        brazelog {
            "Custom ControlInAppMessageManagerListener set. This listener will only be used for control in-app messages."
        }
        customControlInAppMessageManagerListener = inAppMessageManagerListener
    }

    /**
     * Assigns a custom IHtmlInAppMessageActionListener that will be used during the display of Html in-app messages.
     *
     * @param htmlInAppMessageActionListener A custom IHtmlInAppMessageActionListener or null (to revert back to the
     * default IHtmlInAppMessageActionListener).
     */
    open fun setCustomHtmlInAppMessageActionListener(htmlInAppMessageActionListener: IHtmlInAppMessageActionListener?) {
        brazelog { "Custom htmlInAppMessageActionListener set" }
        customHtmlInAppMessageActionListener = htmlInAppMessageActionListener
    }

    /**
     * Assigns a custom IInAppMessageAnimationFactory that will be used to animate the in-app message View. To revert
     * back to the default IInAppMessageAnimationFactory, call the setCustomInAppMessageAnimationFactory method with null.
     *
     * @param inAppMessageAnimationFactory A custom IInAppMessageAnimationFactory or null (to revert back to the default
     * IInAppMessageAnimationFactory).
     */
    open fun setCustomInAppMessageAnimationFactory(inAppMessageAnimationFactory: IInAppMessageAnimationFactory?) {
        brazelog { "Custom InAppMessageAnimationFactory set" }
        customInAppMessageAnimationFactory = inAppMessageAnimationFactory
    }

    /**
     * Assigns a custom IInAppMessageViewFactory that will be used to create the in-app message View. To revert
     * back to the default IInAppMessageViewFactory, call the setCustomInAppMessageViewFactory method with null.
     *
     * @param inAppMessageViewFactory A custom IInAppMessageViewFactory or null (to revert back to the default
     * IInAppMessageViewFactory).
     */
    open fun setCustomInAppMessageViewFactory(inAppMessageViewFactory: IInAppMessageViewFactory?) {
        brazelog { "Custom InAppMessageViewFactory set" }
        customInAppMessageViewFactory = inAppMessageViewFactory
    }

    /**
     * Sets a custom [IInAppMessageViewWrapperFactory] that will be used to
     * display an [IInAppMessage] to the user.
     */
    open fun setCustomInAppMessageViewWrapperFactory(inAppMessageViewWrapperFactory: IInAppMessageViewWrapperFactory?) {
        brazelog { "Custom IInAppMessageViewWrapperFactory set" }
        customInAppMessageViewWrapperFactory = inAppMessageViewWrapperFactory
    }
}
