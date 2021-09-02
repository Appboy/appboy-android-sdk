package com.braze.ui.contentcards.listeners;

import android.content.Context;

import com.appboy.models.cards.Card;
import com.braze.ui.actions.IAction;

public class DefaultContentCardsActionListener implements IContentCardsActionListener {
  @Override
  public boolean onContentCardClicked(Context context, Card card, IAction cardAction) {
    return false;
  }

  @Override
  public void onContentCardDismissed(Context context, Card card) {}
}
