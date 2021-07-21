package com.braze.ui.contentcards.managers;

import com.braze.ui.contentcards.listeners.DefaultContentCardsActionListener;
import com.braze.ui.contentcards.listeners.IContentCardsActionListener;

public class BrazeContentCardsManager {
  private static volatile BrazeContentCardsManager sInstance = null;

  private IContentCardsActionListener mCustomContentCardsActionListener;
  private final IContentCardsActionListener mDefaultContentCardsActionListener = new DefaultContentCardsActionListener();

  public static BrazeContentCardsManager getInstance() {
    if (sInstance == null) {
      synchronized (BrazeContentCardsManager.class) {
        if (sInstance == null) {
          sInstance = new BrazeContentCardsManager();
        }
      }
    }
    return sInstance;
  }

  /**
   * Assigns a custom {@link IContentCardsActionListener} that will be used to handle user actions.
   *
   * @param customContentCardsActionListener A custom implementation of {@link IContentCardsActionListener}
   */
  public void setContentCardsActionListener(IContentCardsActionListener customContentCardsActionListener) {
    mCustomContentCardsActionListener = customContentCardsActionListener;
  }

  /**
   * @return The assigned implementation of the {@link IContentCardsActionListener} interface.
   */
  public IContentCardsActionListener getContentCardsActionListener() {
    return mCustomContentCardsActionListener != null ? mCustomContentCardsActionListener : mDefaultContentCardsActionListener;
  }
}
