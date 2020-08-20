package com.appboy.ui.contentcards;

import com.appboy.ui.contentcards.listeners.AppboyContentCardsActionListener;
import com.appboy.ui.contentcards.listeners.IContentCardsActionListener;

public class AppboyContentCardsManager {
  private static volatile AppboyContentCardsManager sInstance = null;

  private IContentCardsActionListener mCustomContentCardsActionListener;
  private final IContentCardsActionListener mDefaultContentCardsActionListener = new AppboyContentCardsActionListener();

  public static AppboyContentCardsManager getInstance() {
    if (sInstance == null) {
      synchronized (AppboyContentCardsManager.class) {
        if (sInstance == null) {
          sInstance = new AppboyContentCardsManager();
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
