package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import com.appboy.Constants;
import com.appboy.ui.R;
import com.appboy.models.actions.IAction;
import com.appboy.models.actions.WebAction;
import com.appboy.models.cards.BannerImageCard;
import com.appboy.ui.AppboyWebViewActivity;


public class BannerImageCardView extends BaseCardView<BannerImageCard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, BannerImageCardView.class.getName());

  // TODO(martin) - Not accessed?
  private final ImageView mImage;
  private IAction mUrlAction;

  public BannerImageCardView(Context context) {
    this(context, null);
  }

  public BannerImageCardView(Context context, BannerImageCard card) {
    super(context);
    mImage = (ImageView) findViewById(R.id.image);

    if (card != null) {
      setCard(card);
    }
  }

    @Override
  protected int getLayoutResource() {
    return R.layout.banner_image_card;
  }

  @Override
  public void setCard(BannerImageCard card) {
    if (card.getUrl() != null) {
      mUrlAction = new WebAction(card.getUrl());
      this.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          mUrlAction.execute(BannerImageCardView.this.getContext(), AppboyWebViewActivity.class);
        }
      });
    } else {
      mUrlAction = null;
    }
  }
}