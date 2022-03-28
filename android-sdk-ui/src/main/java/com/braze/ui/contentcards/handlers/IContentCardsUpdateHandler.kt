package com.braze.ui.contentcards.handlers

import android.os.Parcelable
import com.appboy.models.cards.Card
import com.braze.events.ContentCardsUpdatedEvent

/**
 * An interface to handle card updates for the [Card]. Handles the
 * sorting for [ContentCardsUpdatedEvent]'s in the [ContentCardsFragment].
 */
interface IContentCardsUpdateHandler : Parcelable {
    /**
     * Handles a [ContentCardsUpdatedEvent] and returns a list of [Card] for rendering.
     * Each [ContentCardsUpdatedEvent] will contain
     * the full list of cards from either the cache or from a network request.
     *
     * @param event the [ContentCardsUpdatedEvent] update.
     * @return a list of [Card] to be rendered in the Content Cards from this [ContentCardsUpdatedEvent]
     */
    fun handleCardUpdate(event: ContentCardsUpdatedEvent): List<Card>
}
