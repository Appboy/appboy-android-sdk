package com.braze.ui.widget;

import android.content.Context;

import com.braze.models.cards.Card;
import com.braze.ui.R;
import com.braze.ui.feed.view.BaseFeedCardView;
import com.braze.support.BrazeLogger;

public class DefaultCardView extends BaseFeedCardView<Card> {
  private static final String TAG = BrazeLogger.getBrazeLogTag(DefaultCardView.class);

  public DefaultCardView(Context context) {
    this(context, null);
  }

  public DefaultCardView(Context context, Card card) {
    super(context);

    if (card != null) {
      setCard(card);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_braze_default_card;
  }

  @Override public void onSetCard(Card card) {
    BrazeLogger.w(TAG, "onSetCard called for blank view with: " + card.toString());
  }
}
