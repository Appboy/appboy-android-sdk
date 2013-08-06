package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.actions.WebAction;
import com.appboy.ui.support.StringUtils;

public class ShortNewsCardView extends BaseCardView<ShortNewsCard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, ShortNewsCardView.class.getName());

  private final ImageView mImage;
  private final TextView mTitle;
  private final TextView mDescription;
  private final TextView mDomain;
  private IAction mCardAction;

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
  public void setCard(final ShortNewsCard card) {
    mDescription.setText(card.getDescription());
    setOptionalTextView(mTitle, card.getTitle());
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

    setImageViewToUrl(mImage, card.getImageUrl());
  }
}