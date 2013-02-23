package com.appboy.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.appboy.models.cards.*;
import com.appboy.ui.Constants;
import com.appboy.ui.widget.*;
import com.appboy.models.cards.*;

import java.util.List;

/**
 * Default adapter used to display cards for the Appboy stream.
 *
 * This allows the stream to reuse cards when they go out of view.
 * IMPORTANT - When you add a new card, be sure to add the new view type and update the view count here
 */
public class AppboyListAdapter extends ArrayAdapter<ICard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyListAdapter.class.getName());

  private final Context mContext;
  private final List<ICard> mCards;

  public AppboyListAdapter(Context context, int layoutResourceId, List<ICard> cards) {
    super(context, layoutResourceId, cards);
    mContext = context;
    mCards = cards;
  }

  /**
   * Be sure to keep view count in sync with the number of card types in the stream.
   * It is used internally to return the correct card type and to cache views for reuse
   */
  @Override
  public int getViewTypeCount() {
    return 6;
  }

  @Override
  public int getItemViewType(int position) {
    ICard card = mCards.get(position);
    if (card instanceof AppStoreReviewCard) {
      return 1;
    } else if (card instanceof BannerImageCard) {
      return 2;
    } else if (card instanceof CaptionedImageCard) {
      return 3;
    } else if (card instanceof CrossPromotionCard) {
      return 4;
    } else if (card instanceof ShortNewsCard) {
      return 5;
    } else {
      return 0;
    }
  }

  /**
   * Always try to use a convert view if possible, otherwise create on from scratch
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    BaseCardView view;
    ICard card = mCards.get(position);

    if (convertView == null) {
      if (card instanceof AppStoreReviewCard) {
        view = new AppStoreReviewCardView(mContext);
      } else if (card instanceof BannerImageCard) {
        view = new BannerImageCardView(mContext);
      } else if (card instanceof CaptionedImageCard) {
        view = new CaptionedImageCardView(mContext);
      } else if (card instanceof CrossPromotionCard) {
        view = new CrossPromotionCardView(mContext);
      } else if (card instanceof ShortNewsCard) {
        view = new ShortNewsCardView(mContext);
      } else {
        view = new DefaultCardView(mContext);
      }
    } else {
      Log.d(TAG, "Reused view");
      view = (BaseCardView) convertView;
    }

    view.setCard(card);
    return view;
  }
}