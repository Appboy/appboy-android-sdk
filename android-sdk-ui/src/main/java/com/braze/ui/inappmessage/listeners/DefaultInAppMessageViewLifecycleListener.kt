package com.braze.ui.inappmessage.listeners

import android.net.Uri
import android.view.View
import com.appboy.enums.Channel
import com.braze.coroutine.BrazeCoroutineScope
import com.braze.enums.inappmessage.ClickAction
import com.braze.models.inappmessage.IInAppMessage
import com.braze.models.inappmessage.IInAppMessageHtml
import com.braze.models.inappmessage.IInAppMessageImmersive
import com.braze.models.inappmessage.MessageButton
import com.braze.support.BrazeFunctionNotImplemented
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.WebContentUtils.getHtmlInAppMessageAssetCacheDirectory
import com.braze.support.deleteFileOrDirectory
import com.braze.support.toBundle
import com.braze.ui.BrazeDeeplinkHandler
import com.braze.ui.actions.NewsfeedAction
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import kotlinx.coroutines.launch

open class DefaultInAppMessageViewLifecycleListener : IInAppMessageViewLifecycleListener {
    private val inAppMessageManager: BrazeInAppMessageManager
        get() = BrazeInAppMessageManager.getInstance()

    override fun beforeOpened(inAppMessageView: View, inAppMessage: IInAppMessage) {
        // Note that the client method must be called before any default processing below
        inAppMessageManager.inAppMessageManagerListener.beforeInAppMessageViewOpened(inAppMessageView, inAppMessage)
        brazelog { "IInAppMessageViewLifecycleListener.beforeOpened called." }
        inAppMessage.logImpression()
    }

    override fun afterOpened(inAppMessageView: View, inAppMessage: IInAppMessage) {
        brazelog { "IInAppMessageViewLifecycleListener.afterOpened called." }

        // Note that the client method must be called after any default processing above
        inAppMessageManager.inAppMessageManagerListener.afterInAppMessageViewOpened(inAppMessageView, inAppMessage)
    }

    override fun beforeClosed(inAppMessageView: View, inAppMessage: IInAppMessage) {
        // Note that the client method must be called before any default processing below
        inAppMessageManager.inAppMessageManagerListener.beforeInAppMessageViewClosed(inAppMessageView, inAppMessage)
        brazelog { "IInAppMessageViewLifecycleListener.beforeClosed called." }
    }

    override fun afterClosed(inAppMessage: IInAppMessage) {
        brazelog { "IInAppMessageViewLifecycleListener.afterClosed called." }
        inAppMessageManager.resetAfterInAppMessageClose()
        if (inAppMessage is IInAppMessageHtml) {
            startClearHtmlInAppMessageAssetsThread()
        }
        inAppMessage.onAfterClosed()

        // Note that the client method must be called after any default processing above
        inAppMessageManager.inAppMessageManagerListener.afterInAppMessageViewClosed(inAppMessage)
    }

    @Suppress("DEPRECATION")
    override fun onClicked(
        inAppMessageCloser: com.braze.ui.inappmessage.InAppMessageCloser,
        inAppMessageView: View,
        inAppMessage: IInAppMessage
    ) {
        brazelog { "IInAppMessageViewLifecycleListener.onClicked called." }
        inAppMessage.logClick()

        // Perform the in-app message clicked listener action from the host application first. This give
        // the app the option to override the values that are sent from the server and handle the
        // in-app message differently depending on where the user is in the app.
        //
        // To modify the default in-app message clicked behavior, mutate the necessary in-app message members. As
        // an example, if the in-app message were to navigate to the news feed when it was clicked, the
        // behavior can be cancelled by setting the click action to NONE.
        @Suppress("SwallowedException")
        val wasHandled = try {
            val wasHandledLegacy = inAppMessageManager.inAppMessageManagerListener.onInAppMessageClicked(inAppMessage, inAppMessageCloser)
            brazelog { "Deprecated onInAppMessageClicked(inAppMessage, inAppMessageCloser) called." }
            wasHandledLegacy
        } catch (e: BrazeFunctionNotImplemented) {
            brazelog { "Using non-deprecated onInAppMessageClicked(inAppMessage)" }
            inAppMessageManager.inAppMessageManagerListener.onInAppMessageClicked(inAppMessage)
        }
        if (!wasHandled) {
            // Perform the default (or modified) in-app message clicked behavior.
            performInAppMessageClicked(inAppMessage, inAppMessageCloser)
        }
    }

    @Suppress("DEPRECATION")
    override fun onButtonClicked(
        inAppMessageCloser: com.braze.ui.inappmessage.InAppMessageCloser,
        messageButton: MessageButton,
        inAppMessageImmersive: IInAppMessageImmersive
    ) {
        brazelog { "IInAppMessageViewLifecycleListener.onButtonClicked called." }
        inAppMessageImmersive.logButtonClick(messageButton)
        @Suppress("SwallowedException")
        val wasHandled =
            try {
                inAppMessageManager.inAppMessageManagerListener.onInAppMessageButtonClicked(
                    inAppMessageImmersive,
                    messageButton,
                    inAppMessageCloser
                )
            } catch (e: BrazeFunctionNotImplemented) {
                inAppMessageManager.inAppMessageManagerListener.onInAppMessageButtonClicked(
                    inAppMessageImmersive,
                    messageButton
                )
            }
        if (!wasHandled) {
            // Perform the default (or modified) in-app message button clicked behavior.
            performInAppMessageButtonClicked(messageButton, inAppMessageImmersive, inAppMessageCloser)
        }
    }

    override fun onDismissed(inAppMessageView: View, inAppMessage: IInAppMessage) {
        brazelog { "IInAppMessageViewLifecycleListener.onDismissed called." }
        inAppMessageManager.inAppMessageManagerListener.onInAppMessageDismissed(inAppMessage)
    }

    @Suppress("DEPRECATION")
    private fun performInAppMessageButtonClicked(
        messageButton: MessageButton,
        inAppMessage: IInAppMessage,
        inAppMessageCloser: com.braze.ui.inappmessage.InAppMessageCloser
    ) {
        performClickAction(
            messageButton.clickAction,
            inAppMessage,
            inAppMessageCloser,
            messageButton.uri,
            messageButton.openUriInWebview
        )
    }

    @Suppress("DEPRECATION")
    private fun performInAppMessageClicked(inAppMessage: IInAppMessage, inAppMessageCloser: com.braze.ui.inappmessage.InAppMessageCloser) {
        performClickAction(
            inAppMessage.clickAction,
            inAppMessage,
            inAppMessageCloser,
            inAppMessage.uri,
            inAppMessage.openUriInWebView
        )
    }

    @Suppress("DEPRECATION")
    private fun performClickAction(
        clickAction: ClickAction,
        inAppMessage: IInAppMessage,
        inAppMessageCloser: com.braze.ui.inappmessage.InAppMessageCloser,
        clickUri: Uri?,
        openUriInWebview: Boolean
    ) {
        val activity = inAppMessageManager.activity
        if (activity == null) {
            brazelog(W) { "Can't perform click action because the cached activity is null." }
            return
        }
        when (clickAction) {
            ClickAction.NEWS_FEED -> {
                inAppMessageCloser.close(false)
                val newsfeedAction = NewsfeedAction(
                    inAppMessage.extras.toBundle(),
                    Channel.INAPP_MESSAGE
                )
                BrazeDeeplinkHandler.getInstance()
                    .gotoNewsFeed(activity, newsfeedAction)
            }
            ClickAction.URI -> {
                inAppMessageCloser.close(false)
                if (clickUri == null) {
                    brazelog { "clickUri is null, not performing click action" }
                    return
                }
                val uriAction = BrazeDeeplinkHandler.getInstance().createUriActionFromUri(
                    clickUri, inAppMessage.extras.toBundle(),
                    openUriInWebview, Channel.INAPP_MESSAGE
                )

                val appContext = inAppMessageManager.applicationContext
                if (appContext == null) {
                    brazelog { "appContext is null, not performing click action" }
                    return
                } else {
                    BrazeDeeplinkHandler.getInstance().gotoUri(appContext, uriAction)
                }
            }
            ClickAction.NONE -> inAppMessageCloser.close(inAppMessage.animateOut)
            else -> inAppMessageCloser.close(false)
        }
    }

    private fun startClearHtmlInAppMessageAssetsThread() {
        BrazeCoroutineScope.launch {
            val inAppMessageActivity = BrazeInAppMessageManager.getInstance().activity
            if (inAppMessageActivity != null) {
                val internalStorageCacheDirectory = getHtmlInAppMessageAssetCacheDirectory(inAppMessageActivity)
                deleteFileOrDirectory(internalStorageCacheDirectory)
            }
        }
    }
}
