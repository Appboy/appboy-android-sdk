package com.braze.ui.inappmessage.listeners

import android.os.Bundle
import com.braze.models.inappmessage.IInAppMessage

/**
 * The [IHtmlInAppMessageActionListener] allows for the overriding of the default Braze display handling
 * and setting custom behavior during the display of HTML In-App Messages.
 */
interface IHtmlInAppMessageActionListener {
    /**
     * @param inAppMessage the In-App Message that was closed.
     * @param url the url clicked.
     * @param queryBundle a bundle of the query part of the url.
     */
    fun onCloseClicked(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle) {}

    /**
     * @param inAppMessage the In-App Message being displayed.
     * @param url the url clicked.
     * @param queryBundle a bundle of the query part of the url.
     * @return boolean flag to indicate to Braze whether the click has been manually handled. If
     * true, Braze will log a click and do nothing. If false, Braze will also navigate to the Newsfeed.
     */
    fun onNewsfeedClicked(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle): Boolean = false

    /**
     * @param inAppMessage the In-App Message being displayed.
     * @param url the url clicked.
     * @param queryBundle a bundle of the query part of the url.
     * @return boolean flag to indicate to Braze whether the click has been manually handled. If
     * true, Braze will do nothing. If false, Braze will log the custom event.
     */
    fun onCustomEventFired(
        inAppMessage: IInAppMessage,
        url: String,
        queryBundle: Bundle
    ): Boolean = false

    /**
     * @param inAppMessage the In-App Message being displayed.
     * @param url the url clicked.
     * @param queryBundle a bundle of the query part of the url.
     * @return boolean flag to indicate to Braze whether the click has been manually handled. If
     * true, Braze will log a click and do nothing. If false, Braze will also handle the URL.
     */
    fun onOtherUrlAction(inAppMessage: IInAppMessage, url: String, queryBundle: Bundle): Boolean = false
}
