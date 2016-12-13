package com.appboy.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.appboy.Constants;
import com.appboy.models.cards.BannerImageCard;
import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.models.cards.Card;
import com.appboy.models.cards.CrossPromotionSmallCard;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.models.cards.TextAnnouncementCard;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.widget.BannerImageCardView;
import com.appboy.ui.widget.BaseCardView;
import com.appboy.ui.widget.CaptionedImageCardView;
import com.appboy.ui.widget.CrossPromotionSmallCardView;
import com.appboy.ui.widget.DefaultCardView;
import com.appboy.ui.widget.ShortNewsCardView;
import com.appboy.ui.widget.TextAnnouncementCardView;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default adapter used to display cards and log card impressions for the Appboy feed.
 * <p/>
 * This allows the stream to reuse cards when they go out of view.
 * <p/>
 * IMPORTANT - When you add a new card, be sure to add the new view type and update the view count here
 * <p/>
 * A card generates an impression once per viewing per open ListView. If a card is viewed more than once
 * in a particular ListView, it generates only one impression. If closed an reopened, a card will again
 * generate an impression. This also takes into account the case of a card being off-screen in the ListView.
 * The card only generates an impression when it actually scrolls onto the screen.
 * <p/>
 * IMPORTANT - You must call resetCardImpressionTracker() whenever the ListView is displayed. This will ensure
 * that cards that come into view will be tracked according to the description above.
 * <p/>
 * Adding and removing cards to and from the adapter should be done using the following synchronized
 * methods: {@link com.appboy.ui.adapters.AppboyListAdapter#add(Card)},
 * {@link com.appboy.ui.adapters.AppboyListAdapter#clear()}clear(),
 * {@link com.appboy.ui.adapters.AppboyListAdapter#replaceFeed(java.util.List)}
 */
public class AppboyListAdapter extends ArrayAdapter<Card> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyListAdapter.class.getName());

  private final Context mContext;
  private final Set<String> mCardIdImpressions;

  public AppboyListAdapter(Context context, int layoutResourceId, List<Card> cards) {
    super(context, layoutResourceId, cards);
    mContext = context;
    mCardIdImpressions = new HashSet<String>();
  }

  /**
   * Be sure to keep view count in sync with the number of card types in the stream.
   * It is used internally to return the correct card type and to cache views for reuse
   */
  @Override
  public int getViewTypeCount() {
    return 8;
  }

  @Override
  public int getItemViewType(int position) {
    Card card = getItem(position);
    if (card instanceof BannerImageCard) {
      return 1;
    } else if (card instanceof CaptionedImageCard) {
      return 2;
    } else if (card instanceof CrossPromotionSmallCard) {
      return 3;
    } else if (card instanceof ShortNewsCard) {
      return 4;
    } else if (card instanceof TextAnnouncementCard) {
      return 5;
    } else {
      return 0;
    }
  }

  /**
   * Always try to use a convert view if possible, otherwise create one from scratch. The convertView should always
   * be of the appropriate type, but it will be recycled, so you need to fully re-populate it with data from the card.
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    BaseCardView view;
    Card card = getItem(position);

    if (convertView == null) {
      if (card instanceof BannerImageCard) {
        view = new BannerImageCardView(mContext);
      } else if (card instanceof CaptionedImageCard) {
        view = new CaptionedImageCardView(mContext);
      } else if (card instanceof CrossPromotionSmallCard) {
        view = new CrossPromotionSmallCardView(mContext);
      } else if (card instanceof ShortNewsCard) {
        view = new ShortNewsCardView(mContext);
      } else if (card instanceof TextAnnouncementCard) {
        view = new TextAnnouncementCardView(mContext);
      } else {
        view = new DefaultCardView(mContext);
      }
    } else {
      AppboyLogger.d(TAG, "Reusing convertView for rendering of item " + position);
      view = (BaseCardView) convertView;
    }

    AppboyLogger.d(TAG, String.format("Using view of type: %s for card at position %d: %s", view.getClass().getName(),
        position, card.toString()));
    view.setCard(card);
    logCardImpression(card);
    return view;
  }

  @SuppressWarnings("checkstyle:localvariablename")
  public synchronized void replaceFeed(List<Card> cards) {
    setNotifyOnChange(false);

    if (cards == null) {
      clear();
      notifyDataSetChanged();
      return;
    }

    AppboyLogger.d(TAG, String.format("Replacing existing feed of %d cards with new feed containing %d cards.",
        getCount(), cards.size()));
    int i = 0;
    int j = 0;
    int newFeedSize = cards.size();
    Card existingCard;
    Card newCard;

    // Iterate over the entire existing feed, skipping items at the head of the list whenever they're the same as the
    // head of the new list and otherwise removing them.
    while (i < getCount()) {
      existingCard = getItem(i);
      newCard = null;

      // Only consider a new card if there are any left.
      if (j < newFeedSize) {
        newCard = cards.get(j);
      }

      // If there is still a card to add and it is the same as the next existing card in the feed, continue.
      if (newCard != null && newCard.isEqualToCard(existingCard)) {
        i++;
        j++;
      } else { // Otherwise, we need to get rid of the next card in the adapter, and continue checking.
        remove(existingCard);
      }
    }

    // Now we add the remainder of the feed.
    if (android.os.Build.VERSION.SDK_INT < 11) {
      while (j < newFeedSize) {
        add(cards.get(j));
        j++;
      }
    } else {
      addAllBatch(cards.subList(j, newFeedSize));
    }
    notifyDataSetChanged();
  }

  @Override
  public synchronized void add(Card card) {
    super.add(card);
  }

  @TargetApi(11)
  private synchronized void addAllBatch(Collection<Card> cards) {
    super.addAll(cards);
  }

  /**
   * Resets the list of viewed cards. This must be called every time the ListView is displayed and it will reset
   * the impressions.
   */
  public void resetCardImpressionTracker() {
    mCardIdImpressions.clear();
  }

  private void logCardImpression(Card card) {
    String cardId = card.getId();
    if (!mCardIdImpressions.contains(cardId)) {
      mCardIdImpressions.add(cardId);
      card.logImpression();
      AppboyLogger.d(TAG, String.format("Logged impression for card %s", cardId));
    } else {
      AppboyLogger.d(TAG, String.format("Already counted impression for card %s", cardId));
    }
    if (!card.getViewed()) {
      card.setViewed(true);
    }
  }

  /**
   * Helper method to batch set cards to visually read after either an up or down scroll of the feed.
   * Since scrolls can have multiple cards scrolled off screen at a time, this method can batch set those
   * cards to read.
   *
   * @param startIndex Where to start setting cards to viewed. The card at this index will
   *                   be set to viewed. Must be less than endIndex
   * @param endIndex   Where to end setting cards to viewed. The card at this index will be set to viewed.
   */
  public void batchSetCardsToRead(int startIndex, int endIndex) {
    if (getCount() == 0) {
      AppboyLogger.d(TAG, "mAdapter is empty in setting some cards to viewed.");
      return;
    }

    // Make sure the start and end are in bounds
    startIndex = Math.max(0, startIndex);
    endIndex = Math.min(getCount(), endIndex);

    for (int traversalIndex = startIndex; traversalIndex < endIndex; traversalIndex++) {
      // Get the card
      Card card = getItem(traversalIndex);
      if (card == null) {
        AppboyLogger.d(TAG, "Card was null in setting some cards to viewed.");
        break;
      }

      if (!card.isRead()) {
        card.setIsRead(true);
      }
    }
  }
}