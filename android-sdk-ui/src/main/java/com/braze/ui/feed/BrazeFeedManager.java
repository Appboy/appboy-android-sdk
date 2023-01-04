package com.braze.ui.feed;

import com.braze.ui.feed.listeners.BrazeDefaultFeedClickActionListener;
import com.braze.ui.feed.listeners.IFeedClickActionListener;

public class BrazeFeedManager {
  private static volatile BrazeFeedManager sInstance = null;

  // card click listeners
  private IFeedClickActionListener mCustomFeedClickActionListener;
  private final IFeedClickActionListener mDefaultFeedClickActionListener = new BrazeDefaultFeedClickActionListener();

  public static BrazeFeedManager getInstance() {
    if (sInstance == null) {
      synchronized (BrazeFeedManager.class) {
        if (sInstance == null) {
          sInstance = new BrazeFeedManager();
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
