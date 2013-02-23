package com.appboy.ui.widget;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import com.appboy.Constants;
import com.appboy.ui.R;
import com.appboy.models.cards.CrossPromotionCard;

public class CrossPromotionCardView extends BaseCardView<CrossPromotionCard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, CrossPromotionCardView.class.getName());

  private final ImageView mImage;
  private final TextView mTitle;
  private final TextView mSubtitle;
  private final TextView mRating;
  private final TextView mPrice;
  private final TextView mDescription;

  public CrossPromotionCardView(Context context) {
    this(context, null);
  }

  public CrossPromotionCardView(Context context, CrossPromotionCard card) {
    super(context);
    mImage = (ImageView) findViewById(R.id.image);
    mTitle = (TextView) findViewById(R.id.title);
    mSubtitle = (TextView) findViewById(R.id.subtitle);
    mRating = (TextView) findViewById(R.id.rating);
    mPrice = (TextView) findViewById(R.id.price);
    mDescription = (TextView) findViewById(R.id.description);

    if (card != null) {
      setCard(card);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.cross_promotion_card;
  }

  @Override
  public void setCard(CrossPromotionCard card) {
    mTitle.setText(card.getTitle());
    mSubtitle.setText(card.getSubtitle());
    mRating.setText(String.valueOf(card.getRating()));
    mDescription.setText(card.getDescription());
  }
}