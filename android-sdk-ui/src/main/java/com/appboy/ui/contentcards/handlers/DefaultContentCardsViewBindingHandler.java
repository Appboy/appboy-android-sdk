package com.appboy.ui.contentcards.handlers;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.view.ViewGroup;

import com.appboy.enums.CardType;
import com.appboy.models.cards.Card;
import com.appboy.ui.contentcards.view.BannerImageContentCardView;
import com.appboy.ui.contentcards.view.BaseContentCardView;
import com.appboy.ui.contentcards.view.CaptionedImageContentCardView;
import com.appboy.ui.contentcards.view.ContentCardViewHolder;
import com.appboy.ui.contentcards.view.DefaultContentCardView;
import com.appboy.ui.contentcards.view.ShortNewsContentCardView;
import com.appboy.ui.contentcards.view.TextAnnouncementContentCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultContentCardsViewBindingHandler implements IContentCardsViewBindingHandler {
  /**
   * A cache for the views used in binding the items in the {@link android.support.v7.widget.RecyclerView}.
   */
  private final Map<CardType, BaseContentCardView> mContentCardViewCache = new HashMap<CardType, BaseContentCardView>();

  @Override
  public ContentCardViewHolder onCreateViewHolder(Context context, List<Card> cards, ViewGroup viewGroup, int viewType) {
    CardType cardType = CardType.fromValue(viewType);
    return getContentCardsViewFromCache(context, cardType).createViewHolder(viewGroup);
  }

  @Override
  public void onBindViewHolder(Context context, List<Card> cards, ContentCardViewHolder viewHolder, int adapterPosition) {
    if (adapterPosition < 0 || adapterPosition >= cards.size()) {
      return;
    }
    Card cardAtPosition = cards.get(adapterPosition);
    BaseContentCardView contentCardView = getContentCardsViewFromCache(context, cardAtPosition.getCardType());
    contentCardView.bindViewHolder(viewHolder, cardAtPosition);
  }

  @Override
  public int getItemViewType(Context context, List<Card> cards, int adapterPosition) {
    if (adapterPosition < 0 || adapterPosition >= cards.size()) {
      return -1;
    }
    Card card = cards.get(adapterPosition);
    return card.getCardType().getValue();
  }

  /**
   * Gets a cached instance of a {@link BaseContentCardView} for view creation/binding for a given {@link CardType}.
   * If the {@link CardType} is not found in the cache, then a view binding implementation for that {@link CardType}
   * is created and added to the cache.
   */
  @VisibleForTesting
  BaseContentCardView getContentCardsViewFromCache(Context context, CardType cardType) {
    if (!mContentCardViewCache.containsKey(cardType)) {
      // Create the view here
      BaseContentCardView contentCardView;
      switch (cardType) {
        case BANNER:
          contentCardView = new BannerImageContentCardView(context);
          break;
        case CAPTIONED_IMAGE:
          contentCardView = new CaptionedImageContentCardView(context);
          break;
        case SHORT_NEWS:
          contentCardView = new ShortNewsContentCardView(context);
          break;
        case TEXT_ANNOUNCEMENT:
          contentCardView = new TextAnnouncementContentCardView(context);
          break;
        default:
          contentCardView = new DefaultContentCardView(context);
          break;
      }
      mContentCardViewCache.put(cardType, contentCardView);
    }
    return mContentCardViewCache.get(cardType);
  }
}
