package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.facebook.drawee.view.SimpleDraweeView;

public class ShortNewsCardView extends BaseCardView<ShortNewsCard> {
  private ImageView mImage;
  private SimpleDraweeView mDrawee;
  private final TextView mTitle;
  private final TextView mDescription;
  private final TextView mDomain;
  private IAction mCardAction;
  private final float mAspectRatio = 1f;
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, ShortNewsCardView.class.getName());


  public ShortNewsCardView(Context context) {
    this(context, null);
  }

  public ShortNewsCardView(final Context context, ShortNewsCard card) {
    super(context);
    mDescription = (TextView) findViewById(R.id.com_appboy_short_news_card_description);
    mTitle = (TextView) findViewById(R.id.com_appboy_short_news_card_title);
    mDomain = (TextView) findViewById(R.id.com_appboy_short_news_card_domain);

    if (canUseFresco()) {
      mDrawee = (SimpleDraweeView) getProperViewFromInflatedStub(R.id.com_appboy_short_news_card_drawee_stub);
    } else {
      mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_short_news_card_imageview_stub);
      mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
      mImage.setAdjustViewBounds(true);
    }

    if (card != null) {
      setCard(card);
    }

    safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_appboy_short_news_card;
  }

  @Override
  public void onSetCard(final ShortNewsCard card) {
    mDescription.setText(card.getDescription());
    setOptionalTextView(mTitle, card.getTitle());
    setOptionalTextView(mDomain, card.getDomain());
    mCardAction = ActionFactory.createUriAction(getContext(), card.getUrl());

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        handleCardClick(mContext, card, mCardAction, TAG);
      }
    });

    if (canUseFresco()) {
      setSimpleDraweeToUrl(mDrawee, card.getImageUrl(), mAspectRatio, true);
    } else {
      setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio);
    }
  }
}
