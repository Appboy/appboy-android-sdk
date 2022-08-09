package com.braze.ui.contentcards.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.appboy.models.cards.BannerImageCard
import com.appboy.models.cards.Card
import com.appboy.ui.R

open class BannerImageContentCardView(context: Context) : BaseContentCardView<BannerImageCard>(
    context
) {
    private inner class ViewHolder(view: View) :
        ContentCardViewHolder(view, isUnreadIndicatorEnabled) {
        val imageView: ImageView? = view.findViewById(R.id.com_braze_content_cards_banner_image_card_image)
    }

    override fun createViewHolder(viewGroup: ViewGroup): ContentCardViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.com_braze_banner_image_content_card, viewGroup, false)
        setViewBackground(view)
        return ViewHolder(view)
    }

    override fun bindViewHolder(viewHolder: ContentCardViewHolder, card: Card) {
        if (card is BannerImageCard) {
            super.bindViewHolder(viewHolder, card)
            val bannerImageViewHolder = viewHolder as ViewHolder
            setOptionalCardImage(
                bannerImageViewHolder.imageView,
                card.aspectRatio,
                card.imageUrl,
                card
            )
        }
    }
}
