package com.braze.ui.contentcards.recycler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

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
  public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    // Since dragging is disabled, mask the drag flag to 0
    int dragFlags = 0;
    // Only let the item be swiped if the item is dismissable
    int swipeFlags = mAdapter.isItemDismissable(viewHolder.getAdapterPosition()) ? ItemTouchHelper.START : 0;
    return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override
  public boolean onMove(@NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder,
                        @NonNull RecyclerView.ViewHolder target) {
    // Since we don't support drag & drop, this method will never get called. Thus this return value is never used.
    return false;
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }
}
