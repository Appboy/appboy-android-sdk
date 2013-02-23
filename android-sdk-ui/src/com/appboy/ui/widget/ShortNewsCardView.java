package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.appboy.Constants;
import com.appboy.ui.R;
import com.appboy.models.actions.IAction;
import com.appboy.models.actions.WebAction;
import com.appboy.models.cards.ShortNewsCard;
import com.appboy.ui.AppboyWebViewActivity;

public class ShortNewsCardView extends BaseCardView<ShortNewsCard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, ShortNewsCardView.class.getName());

  private final TextView mTitle;
  private final TextView mDescription;
//  private final ImageView mImage;
  private IAction mUrlAction;

  public ShortNewsCardView(Context context) {
    this(context, null);
  }

  public ShortNewsCardView(Context context, ShortNewsCard card) {
    super(context);
    mTitle = (TextView) findViewById(R.id.title);
    mDescription = (TextView) findViewById(R.id.description);

    if (card != null) {
      setCard(card);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.short_news_card;
  }

  @Override
  public void setCard(ShortNewsCard card) {
    mTitle.setText(card.getTitle());
    String description = card.getDescription();
    String url = card.getUrl();

    if (description != null) {
      mDescription.setText(card.getDescription());
      mDescription.setVisibility(View.VISIBLE);
    } else {
      mDescription.setVisibility(View.GONE);
    }

    if (url != null) {
      mUrlAction = new WebAction(card.getUrl());
      this.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          mUrlAction.execute(ShortNewsCardView.this.getContext(), AppboyWebViewActivity.class);
        }
      });
    } else {
      mUrlAction = null;
    }
  }
}