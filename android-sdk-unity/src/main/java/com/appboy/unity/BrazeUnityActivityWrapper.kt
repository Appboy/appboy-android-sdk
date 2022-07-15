package com.appboy.unity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.appboy.unity.configuration.UnityConfigurationProvider
import com.appboy.unity.enums.UnityInAppMessageManagerAction
import com.appboy.unity.utils.MessagingUtils
import com.appboy.unity.utils.MessagingUtils.BrazeInternalComponentMethod
import com.braze.Braze
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.MessageButton
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.activities.ContentCardsActivity
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.InAppMessageOperation
import com.braze.ui.inappmessage.listeners.DefaultHtmlInAppMessageActionListener
import com.braze.ui.inappmessage.listeners.DefaultInAppMessageManagerListener
import org.json.JSONArray

/**
 * This class allows UnityPlayerNativeActivity and UnityPlayerActivity instances to
 * integrate Braze by calling appropriate methods during each phase of the Android [Activity] lifecycle.
 */
class BrazeUnityActivityWrapper {
    private lateinit var unityConfigurationProvider: UnityConfigurationProvider
    private var nextInAppMessageDisplayOperation = InAppMessageOperation.DISPLAY_NOW

    /**
     * Call from [Activity.onCreate]
     */
    fun onCreateCalled(activity: Activity) {
        val unityConfigurationProvider = getUnityConfigurationProvider(activity)
        Braze.getInstance(activity).subscribeToNewInAppMessages(EventSubscriberFactory.createInAppMessageEventSubscriber(unityConfigurationProvider))
        Braze.getInstance(activity).subscribeToFeedUpdates(EventSubscriberFactory.createFeedUpdatedEventSubscriber(unityConfigurationProvider))
        Braze.getInstance(activity).subscribeToContentCardsUpdates(EventSubscriberFactory.createContentCardsEventSubscriber(unityConfigurationProvider))
        Braze.getInstance(activity).subscribeToSdkAuthenticationFailures(
            EventSubscriberFactory.createSdkAuthenticationFailureSubscriber(unityConfigurationProvider)
        )
        brazelog { "Finished onCreateCalled setup" }
    }

    /**
     * Call from [Activity.onStart]
     */
    fun onStartCalled(activity: Activity?) {
        if (activity != null) {
            Braze.getInstance(activity).openSession(activity)
        }
    }

    /**
     * Call from [Activity.onResume]
     */
    fun onResumeCalled(activity: Activity) {
        if (getUnityConfigurationProvider(activity).showInAppMessagesAutomaticallyKey) {
            BrazeInAppMessageManager.getInstance().registerInAppMessageManager(activity)
        }
    }

    /**
     * Call from [Activity.onPause]
     */
    fun onPauseCalled(activity: Activity) {
        if (getUnityConfigurationProvider(activity).showInAppMessagesAutomaticallyKey) {
            BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(activity)
        }
    }

    /**
     * Call from [Activity.onStop]
     */
    fun onStopCalled(activity: Activity?) {
        if (activity != null) {
            Braze.getInstance(activity).closeSession(activity)
        }
    }

    /**
     * Call from [Activity.onNewIntent]
     */
    fun onNewIntentCalled(intent: Intent?, activity: Activity) {
        // If the Activity is already open and we receive an intent to open the Activity again, we set
        // the new intent as the current one (which has the new intent extras).
        activity.intent = intent
    }

    fun onNewUnityInAppMessageManagerAction(actionEnumValue: Int) {
        when (val action = UnityInAppMessageManagerAction.getTypeFromValue(actionEnumValue)) {
            UnityInAppMessageManagerAction.IAM_DISPLAY_NOW,
            UnityInAppMessageManagerAction.IAM_DISPLAY_LATER,
            UnityInAppMessageManagerAction.IAM_DISCARD -> action.inAppMessageOperation?.let { nextInAppMessageDisplayOperation = it }
            UnityInAppMessageManagerAction.REQUEST_IAM_DISPLAY -> BrazeInAppMessageManager.getInstance().requestDisplayInAppMessage()
            else -> {
                // Do nothing
            }
        }
    }

    fun launchContentCardsActivity(activity: Activity) {
        activity.startActivity(Intent(activity, ContentCardsActivity::class.java))
    }

    fun setInAppMessageListener() {
        BrazeInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(
            object : DefaultInAppMessageManagerListener() {
                override fun beforeInAppMessageDisplayed(inAppMessage: IInAppMessage): InAppMessageOperation {
                    super.beforeInAppMessageDisplayed(inAppMessage)
                    MessagingUtils.sendToBrazeInternalComponent(
                        BrazeInternalComponentMethod.BEFORE_IAM_DISPLAYED,
                        inAppMessage.forJsonPut().toString()
                    )
                    return nextInAppMessageDisplayOperation
                }

                override fun onInAppMessageDismissed(inAppMessage: IInAppMessage) {
                    super.onInAppMessageDismissed(inAppMessage)
                    MessagingUtils.sendToBrazeInternalComponent(
                        BrazeInternalComponentMethod.ON_IAM_DISMISSED,
                        inAppMessage.forJsonPut().toString()
                    )
                }

                override fun onInAppMessageClicked(
                    inAppMessage: IInAppMessage,
                ): Boolean {
                    MessagingUtils.sendToBrazeInternalComponent(
                        BrazeInternalComponentMethod.ON_IAM_CLICKED,
                        inAppMessage.forJsonPut().toString()
                    )
                    return super.onInAppMessageClicked(inAppMessage)
                }

                override fun onInAppMessageButtonClicked(
                    inAppMessage: IInAppMessage,
                    button: MessageButton
                ): Boolean {
                    val jsonArray = JSONArray()
                        .put(inAppMessage.forJsonPut())
                        .put(button.forJsonPut())
                    MessagingUtils.sendToBrazeInternalComponent(
                        BrazeInternalComponentMethod.ON_IAM_BUTTON_CLICKED,
                        jsonArray.toString()
                    )
                    return super.onInAppMessageButtonClicked(inAppMessage, button)
                }
            }
        )

        BrazeInAppMessageManager.getInstance().setCustomHtmlInAppMessageActionListener(
            object : DefaultHtmlInAppMessageActionListener() {
                override fun onOtherUrlAction(
                    inAppMessage: IInAppMessage,
                    url: String,
                    queryBundle: Bundle
                ): Boolean {
                    val jsonArray = JSONArray()
                        .put(inAppMessage.forJsonPut())
                        .put(url)
                    MessagingUtils.sendToBrazeInternalComponent(
                        BrazeInternalComponentMethod.ON_IAM_HTML_CLICKED,
                        jsonArray.toString()
                    )
                    return super.onOtherUrlAction(inAppMessage, url, queryBundle)
                }
            }
        )
    }

    private fun getUnityConfigurationProvider(activity: Activity): UnityConfigurationProvider {
        if (!this::unityConfigurationProvider.isInitialized) {
            unityConfigurationProvider = UnityConfigurationProvider(activity.applicationContext)
        }
        return unityConfigurationProvider
    }
}
