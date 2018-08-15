package com.appboy.ui.contentcards.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.support.StringUtils;
import com.appboy.ui.R;
import com.facebook.drawee.view.SimpleDraweeView;

public class CaptionedImageContentCardView extends BaseContentCardView<CaptionedImageCard> {
  // We set this card's aspect ratio here as a first guess. If the server doesn't send down an
  // aspect ratio, then this value will be the aspect ratio of the card on render.
  private static final float DEFAULT_ASPECT_RATIO = 4f / 3f;

  public CaptionedImageContentCardView(Context context) {
    super(context);
  }

  private class ViewHolder extends ContentCardViewHolder {
    private final TextView mTitle;
    private final TextView mDescription;
    /**
     * This will hold either a {@link SimpleDraweeView} image or an {@link ImageView}
     */
    private View mCardImage;

    ViewHolder(View view) {
      super(view, isUnreadIndicatorEnabled());

      mCardImage = createCardImageWithStyle(getContext(), view, canUseFresco(),
          R.style.Appboy_ContentCards_CaptionedImage_ImageContainer_Image, R.id.com_appboy_content_cards_captioned_image_card_image_container);

      mTitle = (TextView) view.findViewById(R.id.com_appboy_content_cards_captioned_image_title);
      mDescription = (TextView) view.findViewById(R.id.com_appboy_content_cards_captioned_image_description);
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

    SimpleDraweeView getSimpleDraweeView() {
      return mCardImage instanceof SimpleDraweeView ? (SimpleDraweeView) mCardImage : null;
    }
  }

  @Override
  public ContentCardViewHolder createViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_appboy_captioned_image_content_card, viewGroup, false);

    view.setBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, final CaptionedImageCard card) {
    super.bindViewHolder(viewHolder, card);
    ViewHolder captionedImageViewHolder = (ViewHolder) viewHolder;

    captionedImageViewHolder.getTitle().setText(card.getTitle());
    captionedImageViewHolder.getDescription().setText(card.getDescription());
    captionedImageViewHolder.setActionHintText(StringUtils.isNullOrBlank(card.getDomain()) ? card.getUrl() : card.getDomain());

    setOptionalCardImage(captionedImageViewHolder.getImageView(), captionedImageViewHolder.getSimpleDraweeView(),
        card.getAspectRatio(), card.getImageUrl(), DEFAULT_ASPECT_RATIO);
  }
}
