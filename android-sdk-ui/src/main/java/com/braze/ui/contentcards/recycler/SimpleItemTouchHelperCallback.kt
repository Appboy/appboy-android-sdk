package com.braze.ui.contentcards.recycler

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

open class SimpleItemTouchHelperCallback(private val adapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled() =
        false

    override fun isItemViewSwipeEnabled() =
        true

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // Since dragging is disabled, mask the drag flag to 0
        val dragFlags = 0
        // Only let the item be swiped if the item is dismissable
        val swipeFlags = if (adapter.isItemDismissable(viewHolder.bindingAdapterPosition)) ItemTouchHelper.START else 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) =
        // Since we don't support drag & drop, this method will never get called. Thus this return value is never used.
        false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
        adapter.onItemDismiss(viewHolder.bindingAdapterPosition)
}
