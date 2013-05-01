package com.appboy.ui.widget;

import android.content.Context;
import android.util.Log;
import com.appboy.Constants;
import com.appboy.models.cards.ICard;
import com.appboy.ui.R;

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
    return R.layout.com_appboy_default_card;
  }

  @Override public void setCard(ICard card) {
    Log.w(TAG, "Set card called for blank view with: " + card.toString());
  }
}
