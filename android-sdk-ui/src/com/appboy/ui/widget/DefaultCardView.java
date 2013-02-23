package com.appboy.ui.widget;

import android.content.Context;
import com.appboy.Constants;
import com.appboy.ui.R;
import com.appboy.models.cards.ICard;

public class DefaultCardView extends BaseCardView<ICard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, DefaultCardView.class.getName());

  public DefaultCardView(Context context) {
    this(context, null);
  }

  public DefaultCardView(Context context, ICard card) {
    super(context);

    if (card != null) {
      setCard(card);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.default_card;
  }

  @Override public void setCard(ICard card) { }
}
