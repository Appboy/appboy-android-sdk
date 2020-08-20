package com.appboy.ui.widget;

import android.content.Context;
import android.widget.ImageView;

import com.appboy.models.cards.BannerImageCard;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.feed.view.BaseFeedCardView;

public class BannerImageCardView extends BaseFeedCardView<BannerImageCard> {
  private static final String TAG = AppboyLogger.getAppboyLogTag(BannerImageCardView.class);
  private final ImageView mImage;
  private IAction mCardAction;

  // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
  // aspect ratio, then this value will be the aspect ratio of the card on render.
  private float mAspectRatio = 6f;

  public BannerImageCardView(Context context) {
    this(context, null);
  }

  @SuppressWarnings("deprecation") // getDrawable() until Build.VERSION_CODES.LOLLIPOP
  public BannerImageCardView(final Context context, BannerImageCard card) {
    super(context);
    mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_banner_image_card_imageview_stub);
    mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    mImage.setAdjustViewBounds(true);

    if (card != null) {
      setCard(card);
    }

    setBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_appboy_banner_image_card;
  }

  @Override
  public void onSetCard(final BannerImageCard card) {
    if (card.getAspectRatio() != 0f) {
      mAspectRatio = card.getAspectRatio();
    }
    setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio);

    mCardAction = getUriActionForCard(card);
    setOnClickListener(view -> handleCardClick(mContext, card, mCardAction, TAG));
  }
}
