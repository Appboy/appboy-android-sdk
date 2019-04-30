package com.appboy.ui.contentcards.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.ui.R;

public class ContentCardViewHolder extends RecyclerView.ViewHolder {
  @Nullable
  private final View mUnreadBar;
  @Nullable
  private final ImageView mPinnedIcon;
  @Nullable
  private final TextView mActionHint;

  public ContentCardViewHolder(final View view, boolean showUnreadIndicator) {
    super(view);

    mUnreadBar = view.findViewById(R.id.com_appboy_content_cards_unread_bar);
    if (mUnreadBar != null) {
      if (showUnreadIndicator) {
        mUnreadBar.setVisibility(View.VISIBLE);
        // Round the edges at the bottom
        mUnreadBar.setBackground(view.getContext().getResources().getDrawable(R.drawable.com_appboy_content_cards_unread_bar_background));
      } else {
        mUnreadBar.setVisibility(View.GONE);
      }
    }

    mPinnedIcon = view.findViewById(R.id.com_appboy_content_cards_pinned_icon);
    mActionHint = view.findViewById(R.id.com_appboy_content_cards_action_hint);
  }

  /**
   * Sets the pinned icon to {@link View#VISIBLE} when true, or {@link View#GONE} otherwise.
   *
   * @param isVisible Should the pinned icon be visible on the card.
   */
  public void setPinnedIconVisible(boolean isVisible) {
    if (mPinnedIcon != null) {
      mPinnedIcon.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
  }

  /**
   * Sets the unread bar to {@link View#VISIBLE} when true, or {@link View#GONE} otherwise.
   *
   * @param isVisible Should the unread bar be visible on the card.
   */
  public void setUnreadBarVisible(boolean isVisible) {
    if (mUnreadBar != null) {
      mUnreadBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
  }

  /**
   * Sets the action hint to {@link View#VISIBLE} when true, or {@link View#GONE} otherwise.
   *
   * @param isVisible Should the action hint be visible on the card.
   */
  public void setActionHintVisible(boolean isVisible) {
    if (mActionHint != null) {
      mActionHint.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
  }

  /**
   * Sets the action hint text.
   */
  public void setActionHintText(String text) {
    if (mActionHint != null) {
      mActionHint.setText(text);
    }
  }

  /**
   * Creates an image and adds it to a container layout found on the view of this card. This view
   * is later returned after creation and being added to the container layout. A style is also applied to the image as specified
   * by the imageStyleResourceId.
   *
   * @param context The current context.
   * @param rootView The view that contains the image container layout.
   * @param imageStyleResourceId The resource id for the style used for the {@link ImageView}.
   * @param imageContainerLayoutId The layout id for the {@link RelativeLayout} containing the {@link ImageView}.
   * @return A {@link View} for the card image. This {@link View} will be an {@link ImageView}.
   */
  public View createCardImageWithStyle(Context context, View rootView, int imageStyleResourceId, int imageContainerLayoutId) {
    View cardImage;
    final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, imageStyleResourceId);
    cardImage = new ImageView(contextThemeWrapper, null, imageStyleResourceId);

    RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
    cardImage.setLayoutParams(newParams);

    RelativeLayout imageContainerLayout = rootView.findViewById(imageContainerLayoutId);
    imageContainerLayout.addView(cardImage);
    return cardImage;
  }
}
