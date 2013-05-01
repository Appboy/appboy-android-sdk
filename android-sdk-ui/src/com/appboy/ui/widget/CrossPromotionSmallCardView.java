package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.actions.IAction;
import com.appboy.models.cards.CrossPromotionSmallCard;
import com.appboy.ui.R;

import java.text.NumberFormat;
import java.util.Locale;

public class CrossPromotionSmallCardView extends BaseCardView<CrossPromotionSmallCard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, CrossPromotionSmallCardView.class.getName());

  private final TextView mTitle;
  private final TextView mSubtitle;
  private final TextView mCaption;
  private final ImageView mImage;
  private final StarRatingView mStarRating;
  private final TextView mReviewCount;
  private final Button mPrice;
  private IAction mPriceAction;

  public CrossPromotionSmallCardView(Context context) {
    this(context, null);
  }

  public CrossPromotionSmallCardView(final Context context, CrossPromotionSmallCard card) {
    super(context);
    mTitle = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_title);
    mSubtitle = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_subtitle);
    mCaption = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_recommendation_tab);
    mImage = (ImageView) findViewById(R.id.com_appboy_cross_promotion_small_card_image);
    mStarRating = (StarRatingView) findViewById(R.id.com_appboy_cross_promotion_small_card_star_rating);
    mReviewCount = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_review_count);
    mPrice = (Button) findViewById(R.id.com_appboy_cross_promotion_small_card_price);

    if (card != null) {
      setCard(card);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_appboy_cross_promotion_small_card;
  }

  @Override
  public void setCard(final CrossPromotionSmallCard card) {
    mTitle.setText(card.getTitle());
    mSubtitle.setText(card.getSubtitle().toUpperCase(Locale.getDefault()));
    mCaption.setText(card.getCaption().toUpperCase(Locale.getDefault()));
    mStarRating.setRating((float) card.getRating());
    mReviewCount.setText(String.format("(%s)", NumberFormat.getInstance().format(card.getReviewCount())));
    mPrice.setText(getPriceString(card.getPrice()));
    mPriceAction = card.getClickAction();
    mPrice.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Appboy.getInstance(mContext).logFeedCardClick(card.getId());
        mPriceAction.execute(mContext, null);
      }
    });

    // card.getUrl() has the store action.
    setImageViewToUrl(mImage, card.getImageUrl());
  }

  private String getPriceString(double price) {
    if (price == 0.0) {
      return "Free";
    } else {
      return NumberFormat.getCurrencyInstance(Locale.US).format(price);
    }
  }
}