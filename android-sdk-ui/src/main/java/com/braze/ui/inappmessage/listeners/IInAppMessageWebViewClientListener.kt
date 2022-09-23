package com.braze.ui.inappmessage.listeners

import android.os.Bundle
import com.braze.models.inappmessage.IInAppMessage
import com.braze.ui.inappmessage.utils.InAppMessageWebViewClient

/**
 * The [IInAppMessageWebViewClientListener] is called at specific events during the display of an Html
 * In-App Message. Button clicks that occur inside an HTML In-App Message are routed to this listener
 * and not the [IInAppMessageViewLifecycleListener]. However, the display lifecycle of the HTML In-App Message is
 * still handled by the [IInAppMessageViewLifecycleListener].
 *
 * See [InAppMessageWebViewClient].
 */
interface IInAppMessageWebViewClientListener {
    /**
     * Called when a close URL (appboy://close) is followed in an HTML In App Message.
     *
     * @param inAppMessage the inAppMessage
     * @param url          the url that triggered the close
     * @param queryBundle a bundle of the query part of url
     */
    fun onCloseAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle)

    /**
     * Called when a Newsfeed URL (appboy://newsfeed) is followed in an HTML In App Message.
     *
     * @param inAppMessage the inAppMessage
     * @param url          the url that triggered the action
     * @param queryBundle a bundle of the query part of url
     */
    fun onNewsfeedAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle)

    /**
     * Called when the window location is set to a Custom Event URL (appboy://customEvent) in an HTML In App Message.
     *
     * @param inAppMessage the inAppMessage
     * @param url          the url that triggered the action
     * @param queryBundle a bundle of the query part of url
     */
    fun onCustomEventAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle)

    /**
     * Called when a non `appboy` scheme url is encountered.
     *
     * @param inAppMessage the inAppMessage
     * @param url          the url pressed
     * @param queryBundle a bundle of the query part of url
     */
    fun onOtherUrlAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle)
}
