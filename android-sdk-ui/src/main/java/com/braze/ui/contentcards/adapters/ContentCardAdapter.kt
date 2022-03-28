package com.braze.ui.contentcards.adapters

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appboy.models.cards.Card
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.contentcards.handlers.IContentCardsViewBindingHandler
import com.braze.ui.contentcards.managers.BrazeContentCardsManager
import com.braze.ui.contentcards.recycler.ItemTouchHelperAdapter
import com.braze.ui.contentcards.view.ContentCardViewHolder
import kotlin.math.max
import kotlin.math.min

@Suppress("TooManyFunctions")
class ContentCardAdapter(
    private val context: Context,
    private val layoutManager: LinearLayoutManager,
    private val cardData: MutableList<Card>,
    private val contentCardsViewBindingHandler: IContentCardsViewBindingHandler
) : RecyclerView.Adapter<ContentCardViewHolder>(), ItemTouchHelperAdapter {

    // Handler is still used here instead of coroutines because it guarantees order
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var impressedCardIdsInternal = mutableSetOf<String>()

    /**
     * A list of the impressed card ids.
     */
    var impressedCardIds: List<String>
        get() = impressedCardIdsInternal.toList()
        set(impressedCardIds) {
            impressedCardIdsInternal = impressedCardIds.toMutableSet()
        }

    init {
        // We use stable ids to ensure that the same ViewHolder gets used with the same item.
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) =
        contentCardsViewBindingHandler.onCreateViewHolder(context, cardData, viewGroup, viewType)

    override fun onBindViewHolder(viewHolder: ContentCardViewHolder, position: Int) {
        contentCardsViewBindingHandler.onBindViewHolder(context, cardData, viewHolder, position)
    }

    override fun getItemViewType(position: Int) =
        contentCardsViewBindingHandler.getItemViewType(context, cardData, position)

    override fun getItemCount() =
        cardData.size

    override fun onItemDismiss(position: Int) {
        // Note that the ordering of these operations is important. We can't notify of item removal until the item has
        // actually been removed. Additionally, we can only call the card action listener after the card removal
        // as per the onContentCardDismissed() specification.
        val removedCard = cardData.removeAt(position)
        removedCard.isDismissed = true
        notifyItemRemoved(position)
        BrazeContentCardsManager.instance.contentCardsActionListener?.onContentCardDismissed(context, removedCard)
    }

    override fun isItemDismissable(position: Int): Boolean {
        return if (cardData.isEmpty()) {
            false
        } else {
            cardData[position].isDismissibleByUser
        }
    }

    override fun onViewAttachedToWindow(holder: ContentCardViewHolder) {
        // Note that onViewAttachedToWindow() is called right before a view is "visible".
        // I.e. the Layout Manager has not yet updated its "first/last visible item position"
        super.onViewAttachedToWindow(holder)

        // If the cards are empty just return
        if (cardData.isEmpty()) {
            return
        }
        val adapterPosition = holder.bindingAdapterPosition
        if (adapterPosition == RecyclerView.NO_POSITION || !isAdapterPositionOnScreen(
                adapterPosition
            )
        ) {
            brazelog(V) {
                "The card at position $adapterPosition isn't on screen or does not have a valid adapter position. Not logging impression."
            }
            return
        }
        logImpression(getCardAtIndex(adapterPosition))
    }

    override fun onViewDetachedFromWindow(holder: ContentCardViewHolder) {
        // Note that onViewDetachedFromWindow() is called right before a view is technically "non-visible".
        // I.e. once a view goes off-screen, it's first detached from the window, then the Layout Manager updates its "first/last visible item position"
        // Also note that when the RecyclerView is paused, onViewDetachedFromWindow() is called for all attached views.
        super.onViewDetachedFromWindow(holder)

        // If the cards are empty just return
        if (cardData.isEmpty()) {
            return
        }
        val adapterPosition = holder.bindingAdapterPosition

        // RecyclerView will attach some number of views above/below the visible views on screen.
        // However, when onViewDetachedFromWindow() is called for each of those views, regardless of
        // whether it was visible or not. We do not want to mistakenly mark some cards as
        // "read" if the user never actually saw them. E.g. if views [A B C] were visible on
        // screen, RecyclerView could have attached views ( A B C D E ).
        // Without this check, we would mistakenly mark views D & E as read.
        if (adapterPosition == RecyclerView.NO_POSITION ||
            !isAdapterPositionOnScreen(adapterPosition)
        ) {
            brazelog(V) { "The card at position $adapterPosition isn't on screen or does not have a valid adapter position. Not marking as read." }
            return
        }

        // Get the card at this adapter position
        // If the card is null, then there's nothing to notify or update
        val cardAtPosition = getCardAtIndex(adapterPosition) ?: return

        if (!cardAtPosition.isIndicatorHighlighted) {
            cardAtPosition.isIndicatorHighlighted = true

            // Mark as changed
            handler.post { notifyItemChanged(adapterPosition) }
        }
    }

    override fun getItemId(position: Int): Long {
        val card = getCardAtIndex(position)
        return card?.id?.hashCode()?.toLong() ?: 0
    }

    @Synchronized
    fun replaceCards(newCardData: List<Card>) {
        val diffCallback = CardListDiffCallback(cardData, newCardData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cardData.clear()
        cardData.addAll(newCardData)

        // The diff dispatch will call the adapter notify methods
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Marks every on-screen card as read.
     */
    fun markOnScreenCardsAsRead() {
        if (cardData.isEmpty()) {
            brazelog { "Card list is empty. Not marking on-screen cards as read." }
            return
        }
        val firstVisibleIndex = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleIndex = layoutManager.findLastVisibleItemPosition()

        // Either case could arise if there are no items in the adapter,
        // i.e. no cards are visible since none exist
        if (firstVisibleIndex < 0 || lastVisibleIndex < 0) {
            brazelog {
                "Not marking all on-screen cards as read. " +
                    "Either the first or last index is negative. First visible: " +
                    "$firstVisibleIndex . Last visible: $lastVisibleIndex"
            }
            return
        }

        // We want to mark all cards in the inclusive range of [first, last] as read
        for (i in firstVisibleIndex..lastVisibleIndex) {
            val card = getCardAtIndex(i)
            if (card != null) {
                card.isIndicatorHighlighted = true
            }
        }
        handler.post {
            // We add 1 since the number of items since if indices 0 & 1
            // were changed, then a total of 2 items were changed.
            val itemsChangedCount = lastVisibleIndex - firstVisibleIndex + 1
            notifyItemRangeChanged(firstVisibleIndex, itemsChangedCount)
        }
    }

    /**
     * Returns whether the card at the adapter position is a control card.
     *
     * @see Card.isControl
     * @param adapterPosition A valid adapter position for the card data. Invalid positions will return false.
     */
    fun isControlCardAtPosition(adapterPosition: Int): Boolean {
        val card = getCardAtIndex(adapterPosition)
        return card != null && card.isControl
    }

    @VisibleForTesting
    fun getCardAtIndex(index: Int): Card? {
        if (index < 0 || index >= cardData.size) {
            brazelog { "Cannot return card at index: $index in cards list of size: ${cardData.size}" }
            return null
        }
        return cardData[index]
    }

    /**
     * Gets whether the item at a position is visible on screen.
     */
    @VisibleForTesting
    fun isAdapterPositionOnScreen(adapterPosition: Int): Boolean {
        // At various points in the layout/scroll phase of the RecyclerView, the values
        // returned by "find*VisibleItem" and "find*CompletelyVisibleItem" will either
        // be RecyclerView.NO_POSITION (which is -1), differ by 1, or be equal.
        // Additionally, the "find*CompletelyVisible" value will sometimes update before
        // the "find*Visible" value. To accommodate each of these cases, we'll just take
        // the min of the "first" values and the max of the "last" values.
        val firstItemPosition = min(layoutManager.findFirstVisibleItemPosition(), layoutManager.findFirstCompletelyVisibleItemPosition())
        val lastItemPosition = max(layoutManager.findLastVisibleItemPosition(), layoutManager.findLastCompletelyVisibleItemPosition())
        return adapterPosition in firstItemPosition..lastItemPosition
    }

    /**
     * Logs an impression on the card. Performs a check against the known set previously impressed card ids.
     * Will also set the viewed state of the card to true.
     */
    @VisibleForTesting
    fun logImpression(card: Card?) {
        if (card == null) {
            return
        }
        if (!impressedCardIdsInternal.contains(card.id)) {
            card.logImpression()
            impressedCardIdsInternal.add(card.id)
            brazelog(V) { "Logged impression for card ${card.id}" }
        } else {
            brazelog(V) { "Already counted impression for card ${card.id}" }
        }
        if (!card.viewed) {
            card.viewed = true
        }
    }

    /**
     * A [Card] based implementation of the [DiffUtil.Callback]. This implementation assumes cards with the same id
     * are equivalent content-wise and visually.
     */
    private class CardListDiffCallback(private val oldCards: List<Card>, private val newCards: List<Card>) : DiffUtil.Callback() {
        override fun getOldListSize() =
            oldCards.size

        override fun getNewListSize() =
            newCards.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            doItemsShareIds(oldItemPosition, newItemPosition)

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            doItemsShareIds(oldItemPosition, newItemPosition)

        private fun doItemsShareIds(oldItemPosition: Int, newItemPosition: Int) =
            oldCards[oldItemPosition].id == newCards[newItemPosition].id
    }
}
