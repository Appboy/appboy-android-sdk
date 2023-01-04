package com.braze.ui.feed.listeners;

import android.content.Context;

import com.braze.models.cards.Card;
import com.braze.ui.actions.IAction;

public class BrazeDefaultFeedClickActionListener implements IFeedClickActionListener {
  @Override
  public boolean onFeedCardClicked(Context context, Card card, IAction cardAction) {
    return false;
  }
}
