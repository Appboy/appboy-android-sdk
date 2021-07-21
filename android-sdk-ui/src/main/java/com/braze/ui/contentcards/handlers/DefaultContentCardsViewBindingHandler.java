package com.braze.ui.contentcards.handlers;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView;

import com.appboy.enums.CardType;
import com.appboy.models.cards.Card;
import com.braze.ui.contentcards.view.BannerImageContentCardView;
import com.braze.ui.contentcards.view.BaseContentCardView;
import com.braze.ui.contentcards.view.CaptionedImageContentCardView;
import com.braze.ui.contentcards.view.ContentCardViewHolder;
import com.braze.ui.contentcards.view.DefaultContentCardView;
import com.braze.ui.contentcards.view.ShortNewsContentCardView;
import com.braze.ui.contentcards.view.TextAnnouncementContentCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultContentCardsViewBindingHandler implements IContentCardsViewBindingHandler {

  // Interface that must be implemented and provided as a public CREATOR
  // field that generates instances of your Parcelable class from a Parcel.
  public static final Parcelable.Creator<DefaultContentCardsViewBindingHandler> CREATOR = new Parcelable.Creator<DefaultContentCardsViewBindingHandler>() {
    public DefaultContentCardsViewBindingHandler createFromParcel(Parcel in) {
      return new DefaultContentCardsViewBindingHandler();
    }

    public DefaultContentCardsViewBindingHandler[] newArray(int size) {
      return new DefaultContentCardsViewBindingHandler[size];
    }
  };

  /**
   * A cache for the views used in binding the items in the {@link RecyclerView}.
   */
  private final Map<CardType, BaseContentCardView> mContentCardViewCache = new HashMap<>();

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

  // Parcelable interface method
  @Override
  public int describeContents() {
    return 0;
  }

  // Parcelable interface method
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    // Retaining views across a transition could lead to a
    // resource leak so the parcel is left unmodified
  }
}
