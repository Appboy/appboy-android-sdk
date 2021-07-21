package com.braze.ui.contentcards.handlers;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.appboy.models.cards.Card;
import com.braze.events.ContentCardsUpdatedEvent;

import java.util.Collections;
import java.util.List;

public class DefaultContentCardsUpdateHandler implements IContentCardsUpdateHandler {

  // Interface that must be implemented and provided as a public CREATOR
  // field that generates instances of your Parcelable class from a Parcel.
  public static final Parcelable.Creator<DefaultContentCardsUpdateHandler> CREATOR = new Parcelable.Creator<DefaultContentCardsUpdateHandler>() {
    public DefaultContentCardsUpdateHandler createFromParcel(Parcel in) {
      return new DefaultContentCardsUpdateHandler();
    }

    public DefaultContentCardsUpdateHandler[] newArray(int size) {
      return new DefaultContentCardsUpdateHandler[size];
    }
  };

  @NonNull
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

  // Parcelable interface method
  @Override
  public int describeContents() {
    return 0;
  }

  // Parcelable interface method
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    // No state is kept in this class so the parcel is left unmodified
  }
}
