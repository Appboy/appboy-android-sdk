package com.appboy.ui.widget;

import android.content.Context;
import android.widget.TextView;
import com.appboy.ui.R;
import com.appboy.models.cards.AppStoreReviewCard;
import com.appboy.ui.Constants;


public class AppStoreReviewCardView extends BaseCardView<AppStoreReviewCard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppStoreReviewCardView.class.getName());

  private final TextView mUrl;

  public AppStoreReviewCardView(Context context) {
    this(context, null);
  }

  public AppStoreReviewCardView(Context context, AppStoreReviewCard card) {
    super(context);
    mUrl = (TextView) findViewById(R.id.url);

    if (card != null) {
      setCard(card);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.app_store_review_card;
  }

  @Override
  public void setCard(AppStoreReviewCard card) {
    mUrl.setText(card.getUrl());
  }
}