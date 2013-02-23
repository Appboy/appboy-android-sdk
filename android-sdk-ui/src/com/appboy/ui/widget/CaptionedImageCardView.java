package com.appboy.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.appboy.Constants;
import com.appboy.ui.R;
import com.appboy.models.actions.IAction;
import com.appboy.models.actions.WebAction;
import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.ui.AppboyWebViewActivity;

public class CaptionedImageCardView extends BaseCardView<CaptionedImageCard> {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, CaptionedImageCardView.class.getName());

  private final ImageView mImage;
  private final TextView mTitle;
  private final TextView mDescription;
  private IAction mUrlAction;

  public CaptionedImageCardView(Context context) {
    this(context, null);
  }

  public CaptionedImageCardView(Context context, CaptionedImageCard card) {
    super(context);
    mImage = (ImageView) findViewById(R.id.image);
    mTitle = (TextView) findViewById(R.id.title);
    mDescription = (TextView) findViewById(R.id.description);

    if (card != null) {
      setCard(card);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.captioned_image_card;
  }

  @Override
  public void setCard(CaptionedImageCard card) {
    mTitle.setText(card.getTitle());
    mDescription.setText(card.getDescription());
    String url = card.getUrl();

    if (url != null) {
      mUrlAction = new WebAction(card.getUrl());
      this.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          mUrlAction.execute(CaptionedImageCardView.this.getContext(), AppboyWebViewActivity.class);
        }
      });
    } else {
      mUrlAction = null;
    }
  }
}