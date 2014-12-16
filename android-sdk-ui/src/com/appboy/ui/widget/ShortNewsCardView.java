package com.appboy.ui.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.Constants;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;

public class ShortNewsCardView extends BaseCardView<ShortNewsCard> {
  private final ImageView mImage;
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
    mImage = (ImageView) findViewById(R.id.com_appboy_short_news_card_image);
    mTitle = (TextView) findViewById(R.id.com_appboy_short_news_card_title);
    mDomain = (TextView) findViewById(R.id.com_appboy_short_news_card_domain);

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
      public void onClick(View v) {
        card.setIsRead(true);
        if (mCardAction != null) {
          Log.d(TAG, String.format("Logged click for card %s", card.getId()));
          card.logClick();
          mCardAction.execute(mContext);
        }
      }
    });

    setImageViewToUrl(mImage, card.getImageUrl(), mAspectRatio);
  }
}
