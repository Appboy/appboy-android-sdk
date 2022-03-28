package com.appboy.ui.widget;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.ui.R;
import com.braze.ui.actions.IAction;
import com.appboy.ui.feed.view.BaseFeedCardView;
import com.braze.support.BrazeLogger;

public class CaptionedImageCardView extends BaseFeedCardView<CaptionedImageCard> {
  private static final String TAG = BrazeLogger.getBrazeLogTag(CaptionedImageCardView.class);
  private final ImageView mImage;
  private final TextView mTitle;
  private final TextView mDescription;
  private final TextView mDomain;
  private IAction mCardAction;

  // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
  // aspect ratio, then this value will be the aspect ratio of the card on render.
  private float mAspectRatio = 4f / 3f;

  public CaptionedImageCardView(Context context) {
    this(context, null);
  }

  @SuppressWarnings("deprecation") // getDrawable() until Build.VERSION_CODES.LOLLIPOP
  public CaptionedImageCardView(final Context context, CaptionedImageCard card) {
    super(context);
    mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_captioned_image_card_imageview_stub);
    mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
    mImage.setAdjustViewBounds(true);

    mTitle = findViewById(R.id.com_appboy_captioned_image_title);
    mDescription = findViewById(R.id.com_appboy_captioned_image_description);
    mDomain = findViewById(R.id.com_appboy_captioned_image_card_domain);

    if (card != null) {
      setCard(card);
    }

    setBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_appboy_captioned_image_card;
  }

  @Override
  public void onSetCard(final CaptionedImageCard card) {
    mTitle.setText(card.getTitle());
    mDescription.setText(card.getDescription());
    setOptionalTextView(mDomain, card.getDomain());
    mCardAction = getUriActionForCard(card);
    setOnClickListener(view -> handleCardClick(mContext, card, mCardAction, TAG));
    mAspectRatio = card.getAspectRatio();
    setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio, mCard);
  }
}
