package com.appboy.ui.feed;

import com.appboy.ui.feed.listeners.AppboyDefaultFeedClickActionListener;
import com.appboy.ui.feed.listeners.IFeedClickActionListener;

public final class AppboyFeedManager {
  private static volatile AppboyFeedManager sInstance = null;

  // card click listeners
  private IFeedClickActionListener mCustomFeedCardClickActionListener;
  private IFeedClickActionListener mDefaultFeedCardClickActionListener = new AppboyDefaultFeedClickActionListener();

  public static AppboyFeedManager getInstance() {
    if (sInstance == null) {
      synchronized (AppboyFeedManager.class) {
        if (sInstance == null) {
          sInstance = new AppboyFeedManager();
        }
      }
    }
    return sInstance;
  }

  /**
   * Assigns a custom IFeedClickActionListener that will be used to handle news feed card click actions.
   *
   * @param customNewsFeedCardClickActionListener A custom implementation of IFeedClickActionListener
   */
  public void setFeedCardClickActionListener(IFeedClickActionListener customNewsFeedCardClickActionListener) {
    mCustomFeedCardClickActionListener = customNewsFeedCardClickActionListener;
  }

  /**
   * @return the assigned implementation of the INewFeedCardClickActionListener interface.
   */
  public IFeedClickActionListener getFeedCardClickActionListener() {
    return mCustomFeedCardClickActionListener != null ? mCustomFeedCardClickActionListener : mDefaultFeedCardClickActionListener;
  }
}
