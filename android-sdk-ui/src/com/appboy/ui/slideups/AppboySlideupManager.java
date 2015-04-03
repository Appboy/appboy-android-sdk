package com.appboy.ui.slideups;

import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

/**
 * @deprecated use {@link com.appboy.ui.inappmessage.AppboyInAppMessageManager} instead.
 */
@Deprecated
public class AppboySlideupManager {

  /**
   * @deprecated use {@link com.appboy.ui.inappmessage.AppboyInAppMessageManager#getInstance()} instead.
   */
  @Deprecated
  public static AppboyInAppMessageManager getInstance() {
    return AppboyInAppMessageManager.getInstance();
  }
}
