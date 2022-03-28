package com.braze.ui.contentcards.listeners

import android.content.Context
import com.appboy.models.cards.Card
import com.braze.ui.actions.IAction

/**
 * The [IContentCardsActionListener] receives the [Card] when a user action such as
 * clicking or dismissal is performed and gives the host app the ability to override
 * Braze's default procedure for the user action.
 */
interface IContentCardsActionListener {
    /**
     * @param context The context.
     * @param card The card that has been clicked.
     * @param cardAction The action associated with the card being clicked
     *
     * @return boolean Flag to indicate to Braze whether the click should be handled by
     * the host app. If true, Braze will log a card click and do nothing else. If false,
     * Braze will continue its typical handling of the click.
     */
    fun onContentCardClicked(context: Context, card: Card, cardAction: IAction?): Boolean = false

    /**
     * Note that the [Card] will be off-screen by the time this function is called.
     *
     * @param context The context.
     * @param card The card that has been dismissed.
     */
    fun onContentCardDismissed(context: Context, card: Card) {
        // The default implementation of this is "do nothing"
    }
}
