package com.appboy.ui.contentcards.view;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.models.cards.ShortNewsCard;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;

public class ShortNewsContentCardView extends BaseContentCardView<ShortNewsCard> {
  // This value will be the aspect ratio of the card on render.
  private static final float DEFAULT_ASPECT_RATIO = 1f;

  public ShortNewsContentCardView(Context context) {
    super(context);
  }

  private class ViewHolder extends ContentCardViewHolder {
    private final TextView mTitle;
    private final TextView mDescription;
    private View mCardImage;

    ViewHolder(View view) {
      super(view, isUnreadIndicatorEnabled());

      mCardImage = createCardImageWithStyle(getContext(), view,
          R.style.Appboy_ContentCards_ShortNews_ImageContainer_Image, R.id.com_appboy_content_cards_short_news_card_image_container);

      mTitle = (TextView) view.findViewById(R.id.com_appboy_content_cards_short_news_card_title);
      mDescription = (TextView) view.findViewById(R.id.com_appboy_content_cards_short_news_card_description);
    }

    TextView getTitle() {
      return mTitle;
    }

    TextView getDescription() {
      return mDescription;
    }

    ImageView getImageView() {
      return mCardImage instanceof ImageView ? (ImageView) mCardImage : null;
    }
  }

  @Override
  public ContentCardViewHolder createViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_appboy_short_news_content_card, viewGroup, false);

    view.setBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, ShortNewsCard card) {
    super.bindViewHolder(viewHolder, card);
    ViewHolder shortNewsCardViewHolder = (ViewHolder) viewHolder;

    shortNewsCardViewHolder.getTitle().setText(card.getTitle());
    shortNewsCardViewHolder.getDescription().setText(card.getDescription());
    shortNewsCardViewHolder.setActionHintText(StringUtils.isNullOrBlank(card.getDomain()) ? card.getUrl() : card.getDomain());

    // Using the default aspect ratio here since the card doesn't specify an aspect ratio
    setOptionalCardImage(shortNewsCardViewHolder.getImageView(),
        DEFAULT_ASPECT_RATIO, card.getImageUrl(), DEFAULT_ASPECT_RATIO);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      safeSetClipToOutline(shortNewsCardViewHolder.getImageView());
    }
  }
}
