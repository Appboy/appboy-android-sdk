package com.appboy.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.AppboyImageUtils;
import com.appboy.Constants;
import com.appboy.models.cards.CrossPromotionSmallCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.GooglePlayAppDetailsAction;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.support.StringUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.text.NumberFormat;
import java.util.Locale;

public class CrossPromotionSmallCardView extends BaseCardView<CrossPromotionSmallCard> {
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mReviewCount;
    private TextView mCaption;
    private StarRatingView mStarRating;
    private ImageView mImage;
    private SimpleDraweeView mDrawee;
    private Button mPrice;
    private IAction mPriceAction;
    private final float mAspectRatio = 1f;
    private static final String TAG = String.format("%s.%s", Constants.APPBOY, CrossPromotionSmallCardView.class.getName());

    public CrossPromotionSmallCardView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(null);
        AppboyImageUtils.setRoundingCorners(mDrawee, mContext, getRadius(), getRadius(), 0 ,0 );
    }

    private void init(final CrossPromotionSmallCard card) {
        mTitle = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_title);
        mSubtitle = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_subtitle);
        mReviewCount = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_review_count);
        mCaption = (TextView) findViewById(R.id.com_appboy_cross_promotion_small_card_recommendation_tab);
        mStarRating = (StarRatingView) findViewById(R.id.com_appboy_cross_promotion_small_card_star_rating);
        mPrice = (Button) findViewById(R.id.com_appboy_cross_promotion_small_card_price);

        if (canUseFresco()) {
            mDrawee = (SimpleDraweeView) getProperViewFromInflatedStub(R.id.com_appboy_cross_promotion_small_card_drawee_stub);
        } else {
            mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_cross_promotion_small_card_imageview_stub);
        }

        if (card != null) {
            setCard(card);
        }

        setTypeFace();

        backgroundCorners(((LayerDrawable) getBackground()));
    }

    private void setTypeFace() {
        String titleTypeFace = getTitleTypeFaceReference();
        String messageTypeFace = getMessageTypeFaceReference();
        if (!TextUtils.isEmpty(getTitleTypeFaceReference())) {
            titleTypeFace = ensureTypeFaceSuffix(titleTypeFace);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), titleTypeFace);
            mTitle.setTypeface(font);
        }

        if (!TextUtils.isEmpty(messageTypeFace)) {
            messageTypeFace = ensureTypeFaceSuffix(messageTypeFace);
            Typeface font = Typeface.createFromAsset(mContext.getAssets(), messageTypeFace);
            mSubtitle.setTypeface(font);
            mReviewCount.setTypeface(font);
            mCaption.setTypeface(font);
        }
    }


    public CrossPromotionSmallCardView(Context context) {
        super(context);
        init(null);
    }

    public CrossPromotionSmallCardView(final Context context, CrossPromotionSmallCard card) {
        super(context);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.com_appboy_cross_promotion_small_card;
    }

    @Override
    public void onSetCard(final CrossPromotionSmallCard card) {
        mTitle.setText(card.getTitle());
        if (card.getSubtitle() == null || card.getSubtitle().toUpperCase(Locale.getDefault()).equals("NULL")) {
            mSubtitle.setVisibility(View.GONE);
        } else {
            mSubtitle.setText(card.getSubtitle().toUpperCase(Locale.getDefault()));
        }
        mCaption.setText(card.getCaption().toUpperCase(Locale.getDefault()));
        // Kindle items do not have ratings, so they are set to 0
        if (card.getRating() <= 0) {
            mReviewCount.setVisibility(View.GONE);
            mStarRating.setVisibility(View.GONE);
        } else {
            mReviewCount.setText(String.format("(%s)", NumberFormat.getInstance().format(card.getReviewCount())));
            mStarRating.setRating((float) card.getRating());
        }
        // If the server sends down the display price, use that,
        if (!StringUtils.isNullOrBlank(card.getDisplayPrice())) {
            mPrice.setText(card.getDisplayPrice());
        } else {
            // else, format client-side.
            mPrice.setText(getPriceString(card.getPrice()));
        }
        mPriceAction = new GooglePlayAppDetailsAction(card.getPackage(), false, card.getAppStore(), card.getKindleId());
        mPrice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCardClick(mContext, card, mPriceAction, TAG);
            }
        });

        if (canUseFresco()) {
            setSimpleDraweeToUrl(mDrawee, card.getImageUrl(), mAspectRatio, true);
        } else {
            setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio);
        }
    }

    private String getPriceString(double price) {
        if (price == 0.0) {
            return mContext.getString(R.string.com_appboy_recommendation_free);
        } else {
            return NumberFormat.getCurrencyInstance(Locale.US).format(price);
        }
    }
}
