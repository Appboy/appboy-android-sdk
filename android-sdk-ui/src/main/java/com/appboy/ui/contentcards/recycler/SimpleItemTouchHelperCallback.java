package com.appboy.ui.contentcards.recycler;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
  private final ItemTouchHelperAdapter mAdapter;

  public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
    mAdapter = adapter;
  }

  @Override
  public boolean isLongPressDragEnabled() {
    return false;
  }

  @Override
  public boolean isItemViewSwipeEnabled() {
    return true;
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    // Since dragging is disabled, mask the drag flag to 0
    int dragFlags = 0;
    // Only let the item be swiped if the item is dismissable
    int swipeFlags = mAdapter.isItemDismissable(viewHolder.getAdapterPosition()) ? ItemTouchHelper.START : 0;
    return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                        RecyclerView.ViewHolder target) {
    // Since we don't support drag & drop, this method will never get called. Thus this return value is never used.
    return false;
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }
}
