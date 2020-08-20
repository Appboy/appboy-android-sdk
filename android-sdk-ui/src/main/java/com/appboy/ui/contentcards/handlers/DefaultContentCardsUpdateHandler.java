package com.appboy.ui.contentcards.handlers;

import com.appboy.events.ContentCardsUpdatedEvent;
import com.appboy.models.cards.Card;

import java.util.Collections;
import java.util.List;

public class DefaultContentCardsUpdateHandler implements IContentCardsUpdateHandler {
  @Override
  public List<Card> handleCardUpdate(ContentCardsUpdatedEvent event) {
    List<Card> sortedCards = event.getAllCards();
    // Sort by pinned, then by the 'updated' timestamp descending
    // Pinned before non-pinned
    Collections.sort(sortedCards, (cardA, cardB) -> {
      // A displays above B
      if (cardA.getIsPinned() && !cardB.getIsPinned()) {
        return -1;
      }

      // B displays above A
      if (!cardA.getIsPinned() && cardB.getIsPinned()) {
        return 1;
      }

      // At this point, both A & B are pinned or both A & B are non-pinned
      // A displays above B since A is newer
      if (cardA.getUpdated() > cardB.getUpdated()) {
        return -1;
      }

      // B displays above A since A is newer
      if (cardA.getUpdated() < cardB.getUpdated()) {
        return 1;
      }

      // At this point, every sortable field matches so keep the natural ordering
      return 0;
    });

    return sortedCards;
  }
}
