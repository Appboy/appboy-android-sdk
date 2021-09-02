package com.appboy.ui;

import com.braze.IBrazeDeeplinkHandler;
import com.braze.ui.BrazeDeeplinkHandler;

/**
 * @deprecated Please use {@link BrazeDeeplinkHandler} instead. Deprecated since 7/27/21
 */
@Deprecated
public class AppboyNavigator extends BrazeDeeplinkHandler implements com.appboy.IAppboyNavigator {
  public static com.appboy.IAppboyNavigator getAppboyNavigator() {
    IBrazeDeeplinkHandler deepLinkHandler = getInstance();
    if (deepLinkHandler instanceof com.appboy.IAppboyNavigator) {
      return (com.appboy.IAppboyNavigator) deepLinkHandler;
    }
    return (com.appboy.IAppboyNavigator) new AppboyNavigator();
  }

  public static void setAppboyNavigator(com.appboy.IAppboyNavigator appboyNavigator) {
    setBrazeDeeplinkHandler(appboyNavigator);
  }
}
