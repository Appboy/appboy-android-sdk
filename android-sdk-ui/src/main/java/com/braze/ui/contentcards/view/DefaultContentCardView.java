package com.braze.ui.contentcards.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appboy.models.cards.Card;
import com.appboy.ui.R;

/**
 * A view for when the card type is unknown or otherwise can't be rendered.
 */
public class DefaultContentCardView extends BaseContentCardView<Card> {
  public DefaultContentCardView(Context context) {
    super(context);
  }

  @Override
  public ContentCardViewHolder createViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_braze_default_content_card, viewGroup, false);
    return new ContentCardViewHolder(view, false);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, Card card) {
    // Do nothing here since default cards are not meant to be displayed.
  }
}
