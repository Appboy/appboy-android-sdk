package com.braze.ui.contentcards.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.models.cards.CaptionedImageCard;
import com.appboy.ui.R;
import com.braze.support.StringUtils;

public class CaptionedImageContentCardView extends BaseContentCardView<CaptionedImageCard> {
  public CaptionedImageContentCardView(Context context) {
    super(context);
  }

  private class ViewHolder extends ContentCardViewHolder {
    private final TextView mTitle;
    private final TextView mDescription;
    private final ImageView mCardImage;

    ViewHolder(View view) {
      super(view, isUnreadIndicatorEnabled());
      mCardImage = view.findViewById(R.id.com_braze_content_cards_captioned_image_card_image);
      mTitle = view.findViewById(R.id.com_braze_content_cards_captioned_image_title);
      mDescription = view.findViewById(R.id.com_braze_content_cards_captioned_image_description);
    }

    TextView getTitle() {
      return mTitle;
    }

    TextView getDescription() {
      return mDescription;
    }

    ImageView getImageView() {
      return mCardImage;
    }
  }

  @Override
  public ContentCardViewHolder createViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_braze_captioned_image_content_card, viewGroup, false);
    setViewBackground(view);
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, final CaptionedImageCard card) {
    super.bindViewHolder(viewHolder, card);
    ViewHolder captionedImageViewHolder = (ViewHolder) viewHolder;

    setOptionalTextView(captionedImageViewHolder.getTitle(), card.getTitle());
    setOptionalTextView(captionedImageViewHolder.getDescription(), card.getDescription());
    captionedImageViewHolder.setActionHintText(StringUtils.isNullOrBlank(card.getDomain()) ? card.getUrl() : card.getDomain());

    setOptionalCardImage(captionedImageViewHolder.getImageView(), card.getAspectRatio(), card.getImageUrl(), card);
    viewHolder.itemView.setContentDescription(card.getTitle() + " . " + card.getDescription());
  }
}
