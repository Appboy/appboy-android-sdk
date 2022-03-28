package com.braze.ui.contentcards.handlers

import android.os.Parcel
import android.os.Parcelable
import com.appboy.models.cards.Card
import com.braze.events.ContentCardsUpdatedEvent
import java.util.*

class DefaultContentCardsUpdateHandler : IContentCardsUpdateHandler {
    override fun handleCardUpdate(event: ContentCardsUpdatedEvent): List<Card> {
        val sortedCards: List<Card> = event.allCards
        // Sort by pinned, then by the 'updated' timestamp descending
        // Pinned before non-pinned
        Collections.sort(sortedCards) { cardA: Card, cardB: Card ->
            return@sort when {
                // A displays above B since A is pinned and B isn't
                cardA.isPinned && !cardB.isPinned -> -1
                // B displays above A since B is pinned and A isn't
                !cardA.isPinned && cardB.isPinned -> 1
                // At this point, both A & B are pinned or both A & B are non-pinned
                // A displays above B if A is newer
                cardA.updated > cardB.updated -> -1
                // B displays above A if B is newer
                cardA.updated < cardB.updated -> 1
                // They're considered equal at this point
                else -> 0
            }
        }
        return sortedCards
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
