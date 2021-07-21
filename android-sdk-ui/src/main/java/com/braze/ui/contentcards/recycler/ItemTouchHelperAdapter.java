package com.braze.ui.contentcards.recycler;

public interface ItemTouchHelperAdapter {
  void onItemDismiss(int position);

  /**
   * @param position The adapter position of the item.
   * @return True if the item at the adapter position is dismissable, false if the item is not dismissable.
   */
  boolean isItemDismissable(int position);
}
