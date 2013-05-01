package com.appboy.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.appboy.Appboy;
import com.appboy.models.cards.CrossPromotionSmallCard;
import com.appboy.models.cards.ICard;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.models.cards.TextAnnouncementCard;
import com.appboy.ui.Constants;
import com.appboy.ui.widget.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default adapter used to display cards and log card impressions for the Appboy stream.
 *
 * This allows the stream to reuse cards when they go out of view.
 *
 * IMPORTANT - When you add a new card, be sure to add the new view type and update the view count here
 *
 * Note that this class will not automatically trigger change notifications. You must manually tell the adapter that
 * a change has been made.
 *
 * A card generates an impression once per viewing per open ListView. If a card is viewed more than once
 * in a particular ListView, it generates only one impression. If closed an reopened, a card will again
 * generate an impression. This also takes into account the case of a card being off-screen in the ListView. 
 * The card only generates an impression when it actually scrolls onto the screen.
 *
 * IMPORTANT - You must call resetCardImpressionTracker() whenever the ListView is displayed. This will ensure
 *             that cards that come into view will be tracked according to the description above.
 */
public class AppboyListAdapter extends ArrayAdapter<ICard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyListAdapter.class.getName());

  private final Context mContext;
  private final Set<String> mCardIdImpressions;
  private boolean mHasReceivedFeed = false;

  public AppboyListAdapter(Context context, int layoutResourceId, List<ICard> cards) {
    super(context, layoutResourceId, cards);
    mContext = context;
    mCardIdImpressions = new HashSet<String>();
    // Tell the adapter that we're going to manually notify it of changes.
    setNotifyOnChange(false);
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
    ICard card = getItem(position);
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
    ICard card = getItem(position);

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

  public void addCards(List<ICard> cards) {
    mHasReceivedFeed = true;
    if (cards == null) {
      clear();
      return;
    }
    if (android.os.Build.VERSION.SDK_INT < 11) {
      for (ICard card: cards) {
        add(card);
      }
    } else {
      addAllBatch(cards);
    }
  }

  /**
   * Resets the list of viewed cards. This must be called every time the ListView is displayed and it will reset
   * the impressions.
   */
  public void resetCardImpressionTracker() {
    mCardIdImpressions.clear();
  }

  public boolean hasReceivedFeed() {
    return mHasReceivedFeed;
  }

  @TargetApi(11)
  private void addAllBatch(List<ICard> cards) {
    addAll(cards);
  }

  private void logCardImpression(ICard card) {
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