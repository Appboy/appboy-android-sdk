package com.appboy.ui.contentcards.listeners;

import android.content.Context;
import android.support.annotation.Nullable;

import com.appboy.models.cards.Card;
import com.appboy.ui.actions.IAction;

/**
 * The {@link IContentCardsActionListener} receives the ContentCard when a user action such as
 * clicking or dismissal is performed and gives the host app the ability to override
 * Braze's default procedure for the user action.
 */
public interface IContentCardsActionListener {

  /**
   * @param context The context.
   * @param card The card that has been clicked.
   * @param cardAction The action associated with the card being clicked
   *
   * @return boolean flag to indicate to Braze whether the click is being handled by
   * the host app. If true Braze will log a card click and do nothing. If false
   * Braze will continue its typical handling of the ContentCard click.
   */
  boolean onContentCardClicked(Context context, Card card, @Nullable IAction cardAction);

  /**
   * Note that the {@link Card} will be off-screen by the time this function is called.
   *
   * @param context The context.
   * @param card The card that has been dismissed.
   */
  void onContentCardDismissed(Context context, Card card);
}
