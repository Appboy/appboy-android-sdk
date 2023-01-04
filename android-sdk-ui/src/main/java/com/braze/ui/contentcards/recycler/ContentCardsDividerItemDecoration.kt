package com.braze.ui.contentcards.recycler

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.braze.ui.R
import com.braze.ui.contentcards.adapters.ContentCardAdapter

/**
 * Manages the divider logic of cards in the ContentCards. To center align cards, this sets a left padding on each view
 * based on the configured maximum width of the Content Cards and the size of the screen.
 * To add a divider between cards, sets a top padding on each card.
 *
 * Reads the item divider values from the "dimens" resource.
 * For the item divider height: "R.dimen.com_braze_content_cards_divider_height"
 * For the item max width: "R.dimen.com_braze_content_cards_max_width"
 */
open class ContentCardsDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val appContext: Context = context.applicationContext

    /**
     * The Content Cards dividerHeight from styles in pixels after conversion from dp.
     */
    private val itemDividerHeight = appContext.resources.getDimensionPixelSize(R.dimen.com_braze_content_cards_divider_height)

    /**
     * The width of the Content Cards from the dimens value in pixels after conversion from dp.
     */
    private val contentCardsItemMaxWidth = appContext.resources.getDimensionPixelSize(R.dimen.com_braze_content_cards_max_width)

    override fun getItemOffsets(
        itemViewOutputRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(itemViewOutputRect, view, parent, state)
        val childAdapterPosition = parent.getChildAdapterPosition(view)
        var isControlCard = false
        if (parent.adapter is ContentCardAdapter) {
            val cardAdapter = parent.adapter as ContentCardAdapter
            isControlCard = cardAdapter.isControlCardAtPosition(childAdapterPosition)
        }

        // The goal here is to ensure that:
        // * The first visible card in the has a margin to the top
        // * All cards have the same margin between them
        // * The last visible card has a margin to the bottom

        // Only the first card in the list gets a top divider. All other non-control cards get a bottom divider.
        itemViewOutputRect.top = if (childAdapterPosition == 0) itemDividerHeight else 0
        itemViewOutputRect.bottom = if (isControlCard) 0 else itemDividerHeight

        // Now we have to center the view horizontally in the RecyclerView
        // by adding in a margin on to the left & right of the view
        val sidePadding = getSidePaddingValue(parent.width)
        itemViewOutputRect.left = sidePadding
        itemViewOutputRect.right = sidePadding
    }

    /**
     * Calculates the padding value in screen pixels using the width of the parent view and the predefined item
     * view max width.
     */
    private fun getSidePaddingValue(parentWidth: Int): Int {
        val padding = (parentWidth - contentCardsItemMaxWidth) / 2
        return padding.coerceAtLeast(0)
    }
}
