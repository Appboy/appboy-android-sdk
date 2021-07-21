package com.braze.ui.inappmessage.listeners;

import android.os.Bundle;

import com.braze.models.inappmessage.IInAppMessage;

/**
 * The {@link IHtmlInAppMessageActionListener} allows for the overriding of the default Braze display handling
 * and setting custom behavior during the display of HTML In-App Messages.
 */
public interface IHtmlInAppMessageActionListener {

  /**
   * @param inAppMessage the In-App Message that was closed.
   * @param url the url clicked.
   * @param queryBundle a bundle of the query part of the url.
   */
  void onCloseClicked(IInAppMessage inAppMessage, String url, Bundle queryBundle);

  /**
   * @param inAppMessage the In-App Message being displayed.
   * @param url the url clicked.
   * @param queryBundle a bundle of the query part of the url.
   * @return boolean flag to indicate to Braze whether the click has been manually handled. If
   * true, Braze will log a click and do nothing. If false, Braze will also navigate to the Newsfeed.
   */
  boolean onNewsfeedClicked(IInAppMessage inAppMessage, String url, Bundle queryBundle);

  /**
   * @param inAppMessage the In-App Message being displayed.
   * @param url the url clicked.
   * @param queryBundle a bundle of the query part of the url.
   * @return boolean flag to indicate to Braze whether the click has been manually handled. If
   * true, Braze will do nothing. If false, Braze will log the custom event.
   */
  boolean onCustomEventFired(IInAppMessage inAppMessage, String url, Bundle queryBundle);

  /**
   * @param inAppMessage the In-App Message being displayed.
   * @param url the url clicked.
   * @param queryBundle a bundle of the query part of the url.
   * @return boolean flag to indicate to Braze whether the click has been manually handled. If
   * true, Braze will log a click and do nothing. If false, Braze will also handle the URL.
   */
  boolean onOtherUrlAction(IInAppMessage inAppMessage, String url, Bundle queryBundle);
}
