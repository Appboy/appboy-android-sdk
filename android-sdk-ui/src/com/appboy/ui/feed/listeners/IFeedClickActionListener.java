package com.appboy.ui.feed.listeners;

import android.content.Context;

import com.appboy.models.cards.Card;
import com.appboy.ui.actions.IAction;

/**
 * The IFeedClickActionListener receives the news feed card when a
 * news feed click action is performed and gives the host app the ability to
 * override Appboy's default procedure when handling news feed card clicks.
 *
 * See {@link com.appboy.ui.feed.AppboyFeedManager} and {@link com.appboy.ui.widget.BaseCardView}
 */
public interface IFeedClickActionListener {

  /**
   * @param context the context of the news feed.
   * @param card the news feed card that has been clicked.
   * @param cardAction the action associated with the card being clicked
   *
   * @return boolean flag to indicate to Appboy whether the click is being handled by
   * the host app. If true Appboy will log a card click and do nothing. If false
   * Appboy will continue its typical handling of the news feed card click.
   */
  boolean onFeedCardClicked(Context context, Card card, IAction cardAction);
}
