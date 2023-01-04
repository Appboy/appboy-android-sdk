package com.braze.ui.widget;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.braze.models.cards.ShortNewsCard;
import com.braze.ui.R;
import com.braze.ui.feed.view.BaseFeedCardView;
import com.braze.support.BrazeLogger;
import com.braze.ui.actions.IAction;

public class ShortNewsCardView extends BaseFeedCardView<ShortNewsCard> {
  private static final String TAG = BrazeLogger.getBrazeLogTag(ShortNewsCardView.class);
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
    mDescription = findViewById(R.id.com_braze_short_news_card_description);
    mTitle = findViewById(R.id.com_braze_short_news_card_title);
    mDomain = findViewById(R.id.com_braze_short_news_card_domain);

    mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_braze_short_news_card_imageview_stub);
    mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    mImage.setAdjustViewBounds(true);

    if (card != null) {
      setCard(card);
    }

    setBackground(getResources().getDrawable(R.drawable.com_braze_card_background));
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_braze_short_news_card;
  }

  @Override
  public void onSetCard(final ShortNewsCard card) {
    mDescription.setText(card.getDescription());
    setOptionalTextView(mTitle, card.getTitle());
    setOptionalTextView(mDomain, card.getDomain());
    mCardAction = getUriActionForCard(card);

    setOnClickListener(view -> handleCardClick(applicationContext, card, mCardAction));

    setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio, card);
  }
}
