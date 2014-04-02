package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.Appboy;
import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.actions.WebAction;
import com.appboy.ui.support.StringUtils;

public class CaptionedImageCardView  extends BaseCardView<CaptionedImageCard> {
  private final ImageView mImage;
  private final TextView mTitle;
  private final TextView mDescription;
  private final TextView mDomain;
  private IAction mCardAction;

  public CaptionedImageCardView(Context context) {
    this(context, null);
  }

  public CaptionedImageCardView(final Context context, CaptionedImageCard card) {
    super(context);
    mImage = (ImageView) findViewById(R.id.com_appboy_captioned_image_card_image);
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

    if (!StringUtils.isNullOrBlank(card.getUrl())) {
      mCardAction = new WebAction(card.getUrl());
    } else {
      mCardAction = null;
    }

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mCardAction != null) {
          Appboy.getInstance(mContext).logFeedCardClick(card.getId());
          mCardAction.execute(mContext);
        }
      }
    });

    setImageViewToUrl(mImage, card.getImageUrl(), 1.5f);
  }
}
