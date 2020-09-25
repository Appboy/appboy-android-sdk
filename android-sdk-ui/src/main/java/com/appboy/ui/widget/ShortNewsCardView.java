package com.appboy.ui.widget;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.models.cards.ShortNewsCard;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.feed.view.BaseFeedCardView;

public class ShortNewsCardView extends BaseFeedCardView<ShortNewsCard> {
  private static final String TAG = AppboyLogger.getAppboyLogTag(ShortNewsCardView.class);
  private final ImageView mImage;
  private final TextView mTitle;
  private final TextView mDescription;
  private final TextView mDomain;
  private IAction mCardAction;
  private final float mAspectRatio = 1f;

  public ShortNewsCardView(Context context) {
    this(context, null);
  }

  @SuppressWarnings("deprecation") // getDrawable() until Build.VERSION_CODES.LOLLIPOP
  public ShortNewsCardView(final Context context, ShortNewsCard card) {
    super(context);
    mDescription = findViewById(R.id.com_appboy_short_news_card_description);
    mTitle = findViewById(R.id.com_appboy_short_news_card_title);
    mDomain = findViewById(R.id.com_appboy_short_news_card_domain);

    mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_short_news_card_imageview_stub);
    mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    mImage.setAdjustViewBounds(true);

    if (card != null) {
      setCard(card);
    }

    setBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
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
    mCardAction = getUriActionForCard(card);

    setOnClickListener(view -> handleCardClick(mContext, card, mCardAction, TAG));

    setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio, mCard);
  }
}
