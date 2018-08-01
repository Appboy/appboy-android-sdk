package com.appboy.ui.contentcards.recycler;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.appboy.ui.R;
import com.appboy.ui.contentcards.AppboyCardAdapter;

/**
 * Manages the divider logic of cards in the ContentCards. To center align cards, this sets a left padding on each view
 * based on the configured maximum width of the Content Cards and the size of the screen.
 * To add a divider between cards, sets a top padding on each card.
 *
 * Reads the item divider values from the "dimens" resource.
 *    For the item divider height: "R.dimen.com_appboy_content_cards_divider_height"
 *    For the item max width: "R.dimen.com_appboy_content_cards_max_width"
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
  public void getItemOffsets(Rect itemViewOutputRect, View view, RecyclerView parent, RecyclerView.State state) {
    super.getItemOffsets(itemViewOutputRect, view, parent, state);

    boolean isControlCard = false;
    if (parent.getAdapter() instanceof AppboyCardAdapter) {
      AppboyCardAdapter cardAdapter = (AppboyCardAdapter) parent.getAdapter();
      int childAdapterPosition = parent.getChildAdapterPosition(view);
      if (childAdapterPosition > 0) {
        isControlCard = cardAdapter.isControlCardAtPosition(childAdapterPosition);
      }
    }

    // Set the top of the divider item to the proper height in pixels, if not a control
    // If the card is a control, then don't set any extra divider on the card
    itemViewOutputRect.top = isControlCard ? 0 : mItemDividerHeight;

    // Now we have to center the view horizontally in the RecyclerView
    // by adding in a margin on to the left of the view
    itemViewOutputRect.left = getLeftPaddingValue(parent.getWidth());
  }

  /**
   * Retrieves the Content Cards dividerHeight from styles and returns its value in pixels after conversion from dp.
   */
  private int getItemDividerHeight() {
    return mContext.getResources().getDimensionPixelSize(R.dimen.com_appboy_content_cards_divider_height);
  }

  /**
   * Reads the width of the Content Cards from the dimens value.
   */
  private int getContentCardsItemMaxWidth() {
    return mContext.getResources().getDimensionPixelSize(R.dimen.com_appboy_content_cards_max_width);
  }

  /**
   * Calculates the left padding value in screen pixels using the width of the parent view and the predefined item
   * view max width.
   */
  private int getLeftPaddingValue(int parentWidth) {
    int leftPadding = (parentWidth - mItemDividerMaxWidth) / 2;
    return Math.max(leftPadding, 0);
  }
}
