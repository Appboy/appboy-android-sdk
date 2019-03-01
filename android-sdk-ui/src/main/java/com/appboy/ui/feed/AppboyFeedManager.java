package com.appboy.ui.feed;

import com.appboy.ui.feed.listeners.AppboyDefaultFeedClickActionListener;
import com.appboy.ui.feed.listeners.IFeedClickActionListener;

public class AppboyFeedManager {
  private static volatile AppboyFeedManager sInstance = null;

  // card click listeners
  private IFeedClickActionListener mCustomFeedClickActionListener;
  private IFeedClickActionListener mDefaultFeedClickActionListener = new AppboyDefaultFeedClickActionListener();

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
   * Assigns a custom {@link IFeedClickActionListener} that will be used to handle news feed card
   * click actions.
   *
   * @param customNewsFeedClickActionListener A custom implementation of
   * {@link IFeedClickActionListener}
   */
  public void setFeedCardClickActionListener(IFeedClickActionListener customNewsFeedClickActionListener) {
    mCustomFeedClickActionListener = customNewsFeedClickActionListener;
  }

  /**
   * @return the assigned implementation of the {@link IFeedClickActionListener} interface.
   */
  public IFeedClickActionListener getFeedCardClickActionListener() {
    return mCustomFeedClickActionListener != null ? mCustomFeedClickActionListener : mDefaultFeedClickActionListener;
  }
}
