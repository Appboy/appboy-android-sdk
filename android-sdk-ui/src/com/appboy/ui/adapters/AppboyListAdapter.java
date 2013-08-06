package com.appboy.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.appboy.Appboy;
import com.appboy.models.cards.Card;
import com.appboy.models.cards.CrossPromotionSmallCard;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.models.cards.TextAnnouncementCard;
import com.appboy.ui.Constants;
import com.appboy.ui.widget.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default adapter used to display cards and log card impressions for the Appboy feed.
 *
 * This allows the stream to reuse cards when they go out of view.
 *
 * IMPORTANT - When you add a new card, be sure to add the new view type and update the view count here
 *
 * A card generates an impression once per viewing per open ListView. If a card is viewed more than once
 * in a particular ListView, it generates only one impression. If closed an reopened, a card will again
 * generate an impression. This also takes into account the case of a card being off-screen in the ListView. 
 * The card only generates an impression when it actually scrolls onto the screen.
 *
 * IMPORTANT - You must call resetCardImpressionTracker() whenever the ListView is displayed. This will ensure
 *             that cards that come into view will be tracked according to the description above.
 */
public class AppboyListAdapter extends ArrayAdapter<Card> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyListAdapter.class.getName());

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
    return 4;
  }

  @Override
  public int getItemViewType(int position) {
    Card card = getItem(position);
    if (card instanceof CrossPromotionSmallCard) {
      return 1;
    } else if (card instanceof ShortNewsCard) {
      return 2;
    } else if (card instanceof TextAnnouncementCard) {
      return 3;
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
      if (card instanceof CrossPromotionSmallCard) {
        view = new CrossPromotionSmallCardView(mContext);
      } else if (card instanceof ShortNewsCard) {
        view = new ShortNewsCardView(mContext);
      } else if (card instanceof TextAnnouncementCard) {
        view = new TextAnnouncementCardView(mContext);
      } else {
        view = new DefaultCardView(mContext);
      }
    } else {
      Log.d(TAG, "Reusing convertView for rendering of item " + position);
      view = (BaseCardView) convertView;
    }

    Log.d(TAG, String.format("Using view of type: %s for card at position %d: %s", view.getClass().getName(),
        position, card.toString()));
    view.setCard(card);
    logCardImpression(card);
    return view;
  }

  public void replaceFeed(List<Card> cards) {
    setNotifyOnChange(false);

    if (cards == null) {
      clear();
      notifyDataSetChanged();
      return;
    }

    Log.d(TAG, String.format("Replacing existing feed of %d cards with new feed containing %d cards.",
      getCount(), cards.size()));
    int i = 0, j = 0, newFeedSize = cards.size();
    Card existingCard, newCard;

    // Iterate over the entire existing feed, skipping items at the head of the list whenever they're the same as the
    // head of the new list.
    while (i < getCount()) {
      existingCard = getItem(i);
      newCard = cards.get(j);
      // If the card we're trying to add is the same as the next existing card in the feed, continue.
      if (newCard.getId().equals(existingCard.getId()) && newCard.getUpdated() == existingCard.getUpdated()) {
        i++;
        j++;
      } else { // Otherwise, we need to get rid of the front card from the adapter, and continue checking.
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

  @TargetApi(11)
  private void addAllBatch(Collection<Card> cards) {
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
      Appboy.getInstance(mContext).logFeedCardImpression(cardId);
      Log.d(TAG, String.format("Logged impression for card %s", cardId));
    } else {
      Log.d(TAG, String.format("Already counted impression for card %s", cardId));
    }
  }

  boolean hasCardImpression(String cardId) {
    return mCardIdImpressions.contains(cardId);
  }
}