package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.facebook.drawee.view.SimpleDraweeView;

public class CaptionedImageCardView extends BaseCardView<CaptionedImageCard> {
  private ImageView mImage;
  private final TextView mTitle;
  private final TextView mDescription;
  private final TextView mDomain;
  private SimpleDraweeView mDrawee;
  private IAction mCardAction;
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, CaptionedImageCardView.class.getName());

  // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
  // aspect ratio, then this value will be the aspect ratio of the card on render.
  private float mAspectRatio = 4f / 3f;

  public CaptionedImageCardView(Context context) {
    this(context, null);
  }

  public CaptionedImageCardView(final Context context, CaptionedImageCard card) {
    super(context);
    if (canUseFresco()) {
      mDrawee = (SimpleDraweeView) getProperViewFromInflatedStub(R.id.com_appboy_captioned_image_card_drawee_stub);
    } else {
      mImage = (ImageView) getProperViewFromInflatedStub(R.id.com_appboy_captioned_image_card_imageview_stub);
      mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
      mImage.setAdjustViewBounds(true);
    }

    mTitle = (TextView) findViewById(R.id.com_appboy_captioned_image_title);
    mDescription = (TextView) findViewById(R.id.com_appboy_captioned_image_description);
    mDomain = (TextView) findViewById(R.id.com_appboy_captioned_image_card_domain);

    if (card != null) {
      setCard(card);
    }

    safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
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
    mCardAction = ActionFactory.createUriAction(getContext(), card.getUrl());
    boolean respectAspectRatio = false;
    if (card.getAspectRatio() != 0f) {
      mAspectRatio = card.getAspectRatio();
      respectAspectRatio = true;
    }

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        handleCardClick(mContext, card, mCardAction, TAG);
      }
    });

    if (canUseFresco()) {
      setSimpleDraweeToUrl(mDrawee, card.getImageUrl(), mAspectRatio, respectAspectRatio);
    } else {
      setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio, respectAspectRatio);
    }
  }
}
