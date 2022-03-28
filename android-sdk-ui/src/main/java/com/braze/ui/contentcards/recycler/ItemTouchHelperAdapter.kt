package com.braze.ui.contentcards.recycler

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position: Int)

    /**
     * @param position The adapter position of the item.
     * @return True if the item at the adapter position is dismissable, false if the item is not dismissable.
     */
    fun isItemDismissable(position: Int): Boolean
}
