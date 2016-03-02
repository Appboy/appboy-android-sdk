package com.appboy.ui.widget;

import android.content.Context;

import com.appboy.Constants;
import com.appboy.models.cards.Card;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;

public class DefaultCardView extends BaseCardView<Card> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DefaultCardView.class.getName());

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
    return R.layout.com_appboy_default_card;
  }

  @Override public void onSetCard(Card card) {
    AppboyLogger.w(TAG, "onSetCard called for blank view with: " + card.toString());
  }
}
