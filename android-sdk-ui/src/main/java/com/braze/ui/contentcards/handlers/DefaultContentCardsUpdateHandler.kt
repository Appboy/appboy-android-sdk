package com.braze.ui.contentcards.handlers

import android.os.Parcel
import android.os.Parcelable
import com.braze.events.ContentCardsUpdatedEvent
import com.braze.models.cards.Card
import com.braze.ui.actions.brazeactions.containsInvalidBrazeAction

class DefaultContentCardsUpdateHandler : IContentCardsUpdateHandler {
    override fun handleCardUpdate(event: ContentCardsUpdatedEvent): List<Card> {
        // Sort by pinned, then by the 'updated' timestamp descending
        // Pinned before non-pinned
        val cardComparator = Comparator { cardA: Card, cardB: Card ->
            when {
                // A displays above B since A is pinned and B isn't
                cardA.isPinned && !cardB.isPinned -> -1
                // B displays above A since B is pinned and A isn't
                !cardA.isPinned && cardB.isPinned -> 1
                // At this point, both A & B are pinned or both A & B are non-pinned
                // A displays above B if A is newer
                cardA.created > cardB.created -> -1
                // B displays above A if B is newer
                cardA.created < cardB.created -> 1
                // They're considered equal at this point
                else -> 0
            }
        }
        return event.allCards.filter { card -> !card.containsInvalidBrazeAction() }
            .sortedWith(cardComparator)
    }

    // Parcelable interface method
    override fun describeContents() = 0

    // Parcelable interface method
    override fun writeToParcel(dest: Parcel, flags: Int) {
        // No state is kept in this class so the parcel is left unmodified
    }

    companion object {
        // Interface that must be implemented and provided as a public CREATOR
        // field that generates instances of your Parcelable class from a Parcel.
        @JvmField
        val CREATOR: Parcelable.Creator<DefaultContentCardsUpdateHandler> = object : Parcelable.Creator<DefaultContentCardsUpdateHandler> {
            override fun createFromParcel(source: Parcel) =
                DefaultContentCardsUpdateHandler()

            override fun newArray(size: Int): Array<DefaultContentCardsUpdateHandler?> =
                arrayOfNulls(size)
        }
    }
}
