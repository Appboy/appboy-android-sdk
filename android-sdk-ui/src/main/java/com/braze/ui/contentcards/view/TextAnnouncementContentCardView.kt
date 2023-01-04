package com.braze.ui.contentcards.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.braze.models.cards.Card
import com.braze.models.cards.TextAnnouncementCard
import com.braze.ui.R

open class TextAnnouncementContentCardView(context: Context) :
    BaseContentCardView<TextAnnouncementCard>(
        context
    ) {
    private inner class ViewHolder constructor(view: View) :
        ContentCardViewHolder(view, isUnreadIndicatorEnabled) {
        val title: TextView? = view.findViewById(R.id.com_braze_content_cards_text_announcement_card_title)
        val description: TextView? = view.findViewById(R.id.com_braze_content_cards_text_announcement_card_description)
    }

    override fun createViewHolder(viewGroup: ViewGroup): ContentCardViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.com_braze_text_announcement_content_card, viewGroup, false)
        setViewBackground(view)
        return ViewHolder(view)
    }

    override fun bindViewHolder(viewHolder: ContentCardViewHolder, card: Card) {
        if (card is TextAnnouncementCard) {
            super.bindViewHolder(viewHolder, card)
            val textAnnouncementViewHolder = viewHolder as ViewHolder
            textAnnouncementViewHolder.title?.let { setOptionalTextView(it, card.title) }
            textAnnouncementViewHolder.description?.let {
                setOptionalTextView(
                    it,
                    card.description
                )
            }
            val actionHintText = if (card.domain.isNullOrBlank()) card.url else card.domain
            actionHintText?.let { textAnnouncementViewHolder.setActionHintText(it) }
            viewHolder.itemView.contentDescription = "${card.title} . ${card.description}"
        }
    }
}
