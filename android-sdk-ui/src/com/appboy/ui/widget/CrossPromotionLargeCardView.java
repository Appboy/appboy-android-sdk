package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.Appboy;
import com.appboy.models.cards.CrossPromotionLargeCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.GooglePlayAppDetailsAction;
import com.appboy.ui.actions.IAction;

import java.text.NumberFormat;
import java.util.Locale;

public class CrossPromotionLargeCardView extends BaseCardView<CrossPromotionLargeCard> {

  private final TextView mTitle;
  private final TextView mSubtitle;
  private final ImageView mImage;
  private final StarRatingView mStarRating;
  private final TextView mReviewCount;
  private final Button mPrice;
  private final TextView mDescription;
  private IAction mPriceAction;

  public CrossPromotionLargeCardView(Context context) {
    this(context, null);
  }

  public CrossPromotionLargeCardView(final Context context, CrossPromotionLargeCard card) {
    super(context);
    mTitle = (TextView) findViewById(R.id.com_appboy_cross_promotion_large_card_title);
    mSubtitle = (TextView) findViewById(R.id.com_appboy_cross_promotion_large_card_subtitle);
    mImage = (ImageView) findViewById(R.id.com_appboy_cross_promotion_large_card_image);
    mStarRating = (StarRatingView) findViewById(R.id.com_appboy_cross_promotion_large_card_star_rating);
    mReviewCount = (TextView) findViewById(R.id.com_appboy_cross_promotion_large_card_review_count);
    mDescription = (TextView) findViewById(R.id.com_appboy_cross_promotion_large_description);
    mPrice = (Button) findViewById(R.id.com_appboy_cross_promotion_large_card_price);

    if (card != null) {
      setCard(card);
    }

    safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_appboy_cross_promotion_large_card;
  }

  @Override
  public void onSetCard(final CrossPromotionLargeCard card) {
    mTitle.setText(card.getTitle());
    mSubtitle.setText(card.getSubtitle().toUpperCase(Locale.getDefault()));
    mStarRating.setRating((float) card.getRating());
    mReviewCount.setText(String.format("(%s)", NumberFormat.getInstance().format(card.getReviewCount())));
    mDescription.setText(card.getDescription());
    mPrice.setText(getPriceString(card.getPrice()));
    mPriceAction = new GooglePlayAppDetailsAction(card.getPackage(), false);
    mPrice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Appboy.getInstance(mContext).logFeedCardClick(card.getId());
        mPriceAction.execute(mContext);
      }
    });

    setImageViewToUrl(mImage, card.getImageUrl(), 1.5f);
  }

  private String getPriceString(double price) {
    if (price == 0.0) {
      return mContext.getString(R.string.com_appboy_recommendation_free);
    } else {
      return NumberFormat.getCurrencyInstance(Locale.US).format(price);
    }
  }
}
