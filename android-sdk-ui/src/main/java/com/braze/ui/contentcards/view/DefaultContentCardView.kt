package com.braze.ui.contentcards.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.braze.models.cards.Card
import com.braze.ui.R

/**
 * A view for when the card type is unknown or otherwise can't be rendered.
 */
open class DefaultContentCardView(context: Context) : BaseContentCardView<Card>(
    context
) {
    override fun createViewHolder(viewGroup: ViewGroup): ContentCardViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.com_braze_default_content_card, viewGroup, false)
        return ContentCardViewHolder(view, false)
    }

    override fun bindViewHolder(viewHolder: ContentCardViewHolder, card: Card) {
        // Do nothing here since default cards are not meant to be displayed.
    }
}
