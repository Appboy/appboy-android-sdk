package com.appboy.ui.contentcards.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.appboy.models.cards.BannerImageCard;
import com.appboy.ui.R;

public class BannerImageContentCardView extends BaseContentCardView<BannerImageCard> {
  // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
  // aspect ratio, then this value will be the aspect ratio of the card on render.
  private static final float DEFAULT_ASPECT_RATIO = 6f;

  public BannerImageContentCardView(Context context) {
    super(context);
  }

  private class ViewHolder extends ContentCardViewHolder {
    private final ImageView mCardImage;

    ViewHolder(View view) {
      super(view, isUnreadIndicatorEnabled());
      mCardImage = view.findViewById(R.id.com_appboy_content_cards_banner_image_card_image);
    }

    ImageView getImageView() {
      return mCardImage;
    }
  }

  @Override
  public ContentCardViewHolder createViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_appboy_banner_image_content_card, viewGroup, false);

    view.setBackground(getResources().getDrawable(R.drawable.com_appboy_content_card_background));
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, BannerImageCard card) {
    super.bindViewHolder(viewHolder, card);
    ViewHolder bannerImageViewHolder = (ViewHolder) viewHolder;
    setOptionalCardImage(bannerImageViewHolder.getImageView(),
        card.getAspectRatio(), card.getImageUrl(), DEFAULT_ASPECT_RATIO);
  }
}
