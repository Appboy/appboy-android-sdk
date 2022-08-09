package com.braze.ui.contentcards.handlers

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import com.appboy.enums.CardType
import com.appboy.enums.CardType.Companion.fromValue
import com.appboy.models.cards.Card
import com.braze.ui.contentcards.view.BannerImageContentCardView
import com.braze.ui.contentcards.view.BaseContentCardView
import com.braze.ui.contentcards.view.CaptionedImageContentCardView
import com.braze.ui.contentcards.view.ContentCardViewHolder
import com.braze.ui.contentcards.view.DefaultContentCardView
import com.braze.ui.contentcards.view.ShortNewsContentCardView
import com.braze.ui.contentcards.view.TextAnnouncementContentCardView
import java.util.*

class DefaultContentCardsViewBindingHandler : IContentCardsViewBindingHandler {
    /**
     * A cache for the views used in binding the items in the [RecyclerView].
     */
    private val contentCardViewCache: MutableMap<CardType, BaseContentCardView<*>> = mutableMapOf()

    override fun onCreateViewHolder(
        context: Context,
        cards: List<Card>,
        viewGroup: ViewGroup,
        viewType: Int
    ): ContentCardViewHolder {
        val cardType = fromValue(viewType)
        return getContentCardsViewFromCache(context, cardType).createViewHolder(viewGroup)
    }

    override fun onBindViewHolder(
        context: Context,
        cards: List<Card>,
        viewHolder: ContentCardViewHolder,
        adapterPosition: Int
    ) {
        if (adapterPosition < 0 || adapterPosition >= cards.size) {
            return
        }
        val cardAtPosition = cards[adapterPosition]
        val contentCardView = getContentCardsViewFromCache(
            context,
            cardAtPosition.cardType
        )
        contentCardView.bindViewHolder(viewHolder, cardAtPosition)
    }

    override fun getItemViewType(
        context: Context,
        cards: List<Card>,
        adapterPosition: Int
    ): Int {
        if (adapterPosition < 0 || adapterPosition >= cards.size) {
            return -1
        }
        val card = cards[adapterPosition]
        return card.cardType.value
    }

    /**
     * Gets a cached instance of a [BaseContentCardView] for view creation/binding for a given [CardType].
     * If the [CardType] is not found in the cache, then a view binding implementation for that [CardType]
     * is created and added to the cache.
     */
    @VisibleForTesting
    fun getContentCardsViewFromCache(context: Context, cardType: CardType): BaseContentCardView<*> {
        if (!contentCardViewCache.containsKey(cardType) || contentCardViewCache[cardType] == null) {
            // Create the view here
            val contentCardView: BaseContentCardView<*> = when (cardType) {
                CardType.BANNER -> BannerImageContentCardView(context)
                CardType.CAPTIONED_IMAGE -> CaptionedImageContentCardView(context)
                CardType.SHORT_NEWS -> ShortNewsContentCardView(context)
                CardType.TEXT_ANNOUNCEMENT -> TextAnnouncementContentCardView(context)
                else -> DefaultContentCardView(context)
            }
            contentCardViewCache[cardType] = contentCardView
        }
        return contentCardViewCache[cardType] ?: DefaultContentCardView(context)
    }

    // Parcelable interface method
    override fun describeContents(): Int = 0

    // Parcelable interface method
    override fun writeToParcel(dest: Parcel, flags: Int) {
        // Retaining views across a transition could lead to a
        // resource leak so the parcel is left unmodified
    }

    companion object {
        // Interface that must be implemented and provided as a public CREATOR
        // field that generates instances of your Parcelable class from a Parcel.
        @JvmField
        val CREATOR: Parcelable.Creator<DefaultContentCardsViewBindingHandler> =
            object : Parcelable.Creator<DefaultContentCardsViewBindingHandler> {
                override fun createFromParcel(source: Parcel) =
                    DefaultContentCardsViewBindingHandler()

                override fun newArray(size: Int): Array<DefaultContentCardsViewBindingHandler?> =
                    arrayOfNulls(size)
            }
    }
}
