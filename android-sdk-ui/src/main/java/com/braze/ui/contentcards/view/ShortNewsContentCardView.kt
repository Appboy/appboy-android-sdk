package com.braze.ui.contentcards.view

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.appboy.models.cards.Card
import com.appboy.models.cards.ShortNewsCard
import com.appboy.ui.R

open class ShortNewsContentCardView(context: Context) : BaseContentCardView<ShortNewsCard>(
    context
) {
    private inner class ViewHolder constructor(view: View) :
        ContentCardViewHolder(view, isUnreadIndicatorEnabled) {
        val title: TextView? = view.findViewById(R.id.com_braze_content_cards_short_news_card_title)
        val description: TextView? = view.findViewById(R.id.com_braze_content_cards_short_news_card_description)
        val imageView: ImageView? = view.findViewById(R.id.com_braze_content_cards_short_news_card_image)
    }

    override fun createViewHolder(viewGroup: ViewGroup): ContentCardViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.com_braze_short_news_content_card, viewGroup, false)
        setViewBackground(view)
        return ViewHolder(view)
    }

    override fun bindViewHolder(viewHolder: ContentCardViewHolder, card: Card) {
        if (card is ShortNewsCard) {
            super.bindViewHolder(viewHolder, card)
            val shortNewsCardViewHolder = viewHolder as ViewHolder
            shortNewsCardViewHolder.title?.let { setOptionalTextView(it, card.title) }
            shortNewsCardViewHolder.description?.let { setOptionalTextView(it, card.description) }
            val actionHintText = if (card.domain.isNullOrBlank()) card.url else card.domain
            actionHintText?.let { shortNewsCardViewHolder.setActionHintText(it) }

            setOptionalCardImage(
                shortNewsCardViewHolder.imageView,
                ASPECT_RATIO,
                card.imageUrl,
                card
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                safeSetClipToOutline(shortNewsCardViewHolder.imageView)
            }
            viewHolder.itemView.contentDescription = "${card.title} . ${card.description}"
        }
    }

    companion object {
        // This value will be the aspect ratio of the card on render.
        private const val ASPECT_RATIO = 1f
    }
}
