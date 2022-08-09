package com.braze.ui.contentcards.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.appboy.models.cards.CaptionedImageCard
import com.appboy.models.cards.Card
import com.appboy.ui.R

open class CaptionedImageContentCardView(context: Context) : BaseContentCardView<CaptionedImageCard>(
    context
) {
    private inner class ViewHolder constructor(view: View) :
        ContentCardViewHolder(view, isUnreadIndicatorEnabled) {
        val title: TextView? = view.findViewById(R.id.com_braze_content_cards_captioned_image_title)
        val description: TextView? = view.findViewById(R.id.com_braze_content_cards_captioned_image_description)
        val imageView: ImageView? = view.findViewById(R.id.com_braze_content_cards_captioned_image_card_image)
    }

    override fun createViewHolder(viewGroup: ViewGroup): ContentCardViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.com_braze_captioned_image_content_card, viewGroup, false)
        setViewBackground(view)
        return ViewHolder(view)
    }

    override fun bindViewHolder(viewHolder: ContentCardViewHolder, card: Card) {
        if (card is CaptionedImageCard) {
            super.bindViewHolder(viewHolder, card)
            val captionedImageViewHolder = viewHolder as ViewHolder
            captionedImageViewHolder.title?.let { setOptionalTextView(it, card.title) }
            captionedImageViewHolder.description?.let { setOptionalTextView(it, card.description) }
            (if (card.domain.isNullOrBlank()) card.url else card.domain)?.let {
                captionedImageViewHolder.setActionHintText(
                    it
                )
            }
            setOptionalCardImage(
                captionedImageViewHolder.imageView,
                card.aspectRatio,
                card.imageUrl,
                card
            )
            viewHolder.itemView.contentDescription = "${card.title} .  ${card.description}"
        }
    }
}
