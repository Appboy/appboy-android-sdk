package com.appboy.ui.widget;

import android.content.Context;
import com.appboy.support.AppboyLogger;
import android.view.View;
import android.widget.ImageView;
import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.cards.BannerImageCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;

public class BannerImageCardView  extends BaseCardView<BannerImageCard> {
  private final ImageView mImage;
  private IAction mCardAction;
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, BannerImageCardView.class.getName());

  // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
  // aspect ratio, then this value will be the aspect ratio of the card on render.
  private float mAspectRatio = 6f;

  public BannerImageCardView(Context context) {
    this(context, null);
  }

  public BannerImageCardView(final Context context, BannerImageCard card) {
    super(context);
    mImage = (ImageView) findViewById(R.id.com_appboy_banner_image_card_image);

    if (card != null) {
      setCard(card);
    }

    safeSetBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.com_appboy_banner_image_card;
  }

  @Override
  public void onSetCard(final BannerImageCard card) {
    boolean respectAspectRatio = false;
    if (card.getAspectRatio() != 0f){
      mAspectRatio = card.getAspectRatio();
      respectAspectRatio = true;
    }
    setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio, respectAspectRatio);
    mCardAction = ActionFactory.createUriAction(getContext(), card.getUrl());

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // We don't set isRead here (like we do in other card views)
        // because Banner Cards don't have read/unread indicators.  They are all images, so there's
        // no free space to put the indicator.
        if (mCardAction != null) {
          AppboyLogger.d(TAG, String.format("Logged click for card %s", card.getId()));
          card.logClick();
          mCardAction.execute(mContext);
        }
      }
    });
  }
}
