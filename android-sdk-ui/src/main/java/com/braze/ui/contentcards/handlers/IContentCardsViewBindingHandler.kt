package com.braze.ui.contentcards.handlers

import android.content.Context
import android.os.Parcelable
import android.view.ViewGroup
import com.braze.models.cards.Card
import com.braze.ui.contentcards.view.ContentCardViewHolder

/**
 * An interface to define how the cards display in the [ContentCardsFragment]. The methods here
 * closely mirror those of [RecyclerView.Adapter] and are called as part of those methods in
 * the [ContentCardAdapter].
 */
interface IContentCardsViewBindingHandler : Parcelable {
    /**
     * Creates an [ContentCardViewHolder] of the given type to represent an item in the ContentCards. You can create
     * a new View manually or inflate it from an XML layout file.
     *
     * The new [ContentCardViewHolder] will be used to display adapter items
     * using [IContentCardsViewBindingHandler.onBindViewHolder].
     *
     * @see RecyclerView.Adapter.onCreateViewHolder
     * @param context The application context
     * @param cards The collection of card items in the adapter. Should not be modified.
     * @param viewGroup The [ViewGroup] into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [ContentCardViewHolder] that holds a View of the given view type.
     */
    fun onCreateViewHolder(
        context: Context,
        cards: List<Card>,
        viewGroup: ViewGroup,
        viewType: Int
    ): ContentCardViewHolder

    /**
     * Called to display the data at the specified adapter position. This method should update the contents of the
     * [ContentCardViewHolder.itemView] to reflect the item at the given adapter position.
     *
     * @see RecyclerView.Adapter.onBindViewHolder
     * @param context The application context.
     * @param cards The collection of card items in the adapter. Should not be modified.
     * @param viewHolder The [ContentCardViewHolder] which should be updated to represent the contents
     * of the item at the given adapter position.
     * @param adapterPosition The position of the item within the adapter's card items.
     */
    fun onBindViewHolder(
        context: Context,
        cards: List<Card>,
        viewHolder: ContentCardViewHolder,
        adapterPosition: Int
    )

    /**
     * Returns the view type of the item at the given position for the purposes of view recycling purposes.
     *
     * @param context The application context.
     * @param cards The collection of card items in the adapter. Should not be modified.
     * @param adapterPosition The position of the item within the adapter's card items.
     * @return A value identifying the type of the view needed to represent the item at position. Type values need not be contiguous.
     */
    fun getItemViewType(
        context: Context,
        cards: List<Card>,
        adapterPosition: Int
    ): Int
}
