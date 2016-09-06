package com.appboy.ui.inappmessage.listeners;

import android.os.Bundle;

import com.appboy.models.IInAppMessage;

/**
 * The IInAppMessageWebViewClientListener is called at specific events during the display of an Html
 * In-App Message. Button clicks that occur inside an HTML In-App Message are routed to this listener
 * and not the IInAppMessageViewLifecycleListener. However, the display lifecycle of the HTML In-App Message is
 * still handled by the IInAppMessageViewLifecycleListener.
 *
 * See {@link com.appboy.ui.inappmessage.InAppMessageWebViewClient}.
 */
public interface IInAppMessageWebViewClientListener {

  /**
   * Called when a close URL (appboy://close) is followed in an HTML In App Message
   *
   * @param inAppMessage the inAppMessage
   * @param url          the url that triggered the close
   * @param queryBundle  a bundle of the query part of url
   */
  void onCloseAction(IInAppMessage inAppMessage, String url, Bundle queryBundle);

  /**
   * Called when a Newsfeed URL (appboy://newsfeed) is followed in an HTML In App Message
   *
   * @param inAppMessage the inAppMessage
   * @param url          the url that triggered the action
   * @param queryBundle  a bundle of the query part of url
   */
  void onNewsfeedAction(IInAppMessage inAppMessage, String url, Bundle queryBundle);

  /**
   * Called when the window location is set to a Custom Event URL (appboy://customEvent) in an HTML In App Message
   *
   * @param inAppMessage the inAppMessage
   * @param url          the url that triggered the action
   * @param queryBundle  a bundle of the query part of url
   */
  void onCustomEventAction(IInAppMessage inAppMessage, String url, Bundle queryBundle);

  /**
   * Called when a non Appboy scheme url is encountered.
   *
   * @param inAppMessage the inAppMessage
   * @param url          the url pressed
   * @param queryBundle  a bundle of the query part of url
   */
  void onOtherUrlAction(IInAppMessage inAppMessage, String url, Bundle queryBundle);
}
