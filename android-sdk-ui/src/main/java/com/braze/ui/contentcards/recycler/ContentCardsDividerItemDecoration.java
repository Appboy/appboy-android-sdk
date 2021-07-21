package com.braze.ui.contentcards.recycler;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appboy.ui.R;
import com.braze.ui.contentcards.adapters.ContentCardAdapter;

/**
 * Manages the divider logic of cards in the ContentCards. To center align cards, this sets a left padding on each view
 * based on the configured maximum width of the Content Cards and the size of the screen.
 * To add a divider between cards, sets a top padding on each card.
 *
 * Reads the item divider values from the "dimens" resource.
 *    For the item divider height: "R.dimen.com_braze_content_cards_divider_height"
 *    For the item max width: "R.dimen.com_braze_content_cards_max_width"
 */
public class ContentCardsDividerItemDecoration extends RecyclerView.ItemDecoration {
  private final Context mContext;
  private final int mItemDividerHeight;
  private final int mItemDividerMaxWidth;

  public ContentCardsDividerItemDecoration(Context context) {
    mContext = context.getApplicationContext();
    mItemDividerHeight = getItemDividerHeight();
    mItemDividerMaxWidth = getContentCardsItemMaxWidth();
  }

  @Override
  public void getItemOffsets(@NonNull Rect itemViewOutputRect,
                             @NonNull View view,
                             @NonNull RecyclerView parent,
                             @NonNull RecyclerView.State state) {
    super.getItemOffsets(itemViewOutputRect, view, parent, state);

    int childAdapterPosition = parent.getChildAdapterPosition(view);

    boolean isControlCard = false;
    if (parent.getAdapter() instanceof ContentCardAdapter) {
      ContentCardAdapter cardAdapter = (ContentCardAdapter) parent.getAdapter();
      isControlCard = cardAdapter.isControlCardAtPosition(childAdapterPosition);
    }

    // The goal here is to ensure that:
    // * The first visible card in the has a margin to the top
    // * All cards have the same margin between them
    // * The last visible card has a margin to the bottom

    // Only the first card in the list gets a top divider. All other non-control cards get a bottom divider.
    itemViewOutputRect.top = childAdapterPosition == 0 ? mItemDividerHeight : 0;
    itemViewOutputRect.bottom = isControlCard ? 0 : mItemDividerHeight;

    // Now we have to center the view horizontally in the RecyclerView
    // by adding in a margin on to the left & right of the view
    itemViewOutputRect.left = getSidePaddingValue(parent.getWidth());
    itemViewOutputRect.right = getSidePaddingValue(parent.getWidth());
  }

  /**
   * Retrieves the Content Cards dividerHeight from styles and returns its value in pixels after conversion from dp.
   */
  private int getItemDividerHeight() {
    return mContext.getResources().getDimensionPixelSize(R.dimen.com_braze_content_cards_divider_height);
  }

  /**
   * Reads the width of the Content Cards from the dimens value.
   */
  private int getContentCardsItemMaxWidth() {
    return mContext.getResources().getDimensionPixelSize(R.dimen.com_braze_content_cards_max_width);
  }

  /**
   * Calculates the padding value in screen pixels using the width of the parent view and the predefined item
   * view max width.
   */
  private int getSidePaddingValue(int parentWidth) {
    int padding = (parentWidth - mItemDividerMaxWidth) / 2;
    return Math.max(padding, 0);
  }
}
