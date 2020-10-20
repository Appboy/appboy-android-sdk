package com.appboy.ui.contentcards.handlers;

import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.appboy.models.cards.Card;
import com.appboy.ui.AppboyContentCardsFragment;
import com.appboy.ui.contentcards.view.ContentCardViewHolder;

import java.util.List;

/**
 * An interface to define how the cards display in the {@link AppboyContentCardsFragment}. The methods here
 * closely mirror those of {@link RecyclerView.Adapter} and are called as part of those methods in
 * the {@link com.appboy.ui.contentcards.AppboyCardAdapter}.
 */
public interface IContentCardsViewBindingHandler extends Parcelable {
  /**
   * Creates an {@link ContentCardViewHolder} of the given type to represent an item in the ContentCards. You can create
   * a new View manually or inflate it from an XML layout file.
   *
   * The new {@link ContentCardViewHolder} will be used to display adapter items
   * using {@link IContentCardsViewBindingHandler#onBindViewHolder(Context, List, ContentCardViewHolder, int)}.
   *
   * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
   * @param context The application context
   * @param cards The collection of card items in the adapter. Should not be modified.
   * @param viewGroup The {@link ViewGroup} into which the new View will be added after it is bound to an adapter position.
   * @param viewType The view type of the new View.
   * @return A new {@link ContentCardViewHolder} that holds a View of the given view type.
   */
  ContentCardViewHolder onCreateViewHolder(Context context, List<Card> cards, ViewGroup viewGroup, int viewType);

  /**
   * Called to display the data at the specified adapter position. This method should update the contents of the
   * {@link ContentCardViewHolder#itemView} to reflect the item at the given adapter position.
   *
   * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
   * @param context The application context.
   * @param cards The collection of card items in the adapter. Should not be modified.
   * @param viewHolder The {@link ContentCardViewHolder} which should be updated to represent the contents
   *                   of the item at the given adapter position.
   * @param adapterPosition The position of the item within the adapter's card items.
   */
  void onBindViewHolder(Context context, List<Card> cards, ContentCardViewHolder viewHolder, int adapterPosition);

  /**
   * Returns the view type of the item at the given position for the purposes of view recycling purposes.
   *
   * @param context The application context.
   * @param cards The collection of card items in the adapter. Should not be modified.
   * @param adapterPosition The position of the item within the adapter's card items.
   * @return A value identifying the type of the view needed to represent the item at position. Type values need not be contiguous.
   */
  int getItemViewType(Context context, List<Card> cards, int adapterPosition);
}
