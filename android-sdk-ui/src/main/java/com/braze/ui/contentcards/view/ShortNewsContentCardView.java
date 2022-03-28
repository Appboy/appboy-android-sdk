package com.braze.ui.contentcards.view;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.models.cards.ShortNewsCard;
import com.appboy.ui.R;
import com.braze.support.StringUtils;

public class ShortNewsContentCardView extends BaseContentCardView<ShortNewsCard> {
  // This value will be the aspect ratio of the card on render.
  private static final float ASPECT_RATIO = 1f;

  public ShortNewsContentCardView(Context context) {
    super(context);
  }

  private class ViewHolder extends ContentCardViewHolder {
    private final TextView mTitle;
    private final TextView mDescription;
    private final ImageView mCardImage;

    ViewHolder(View view) {
      super(view, isUnreadIndicatorEnabled());

      mCardImage = view.findViewById(R.id.com_braze_content_cards_short_news_card_image);
      mTitle = view.findViewById(R.id.com_braze_content_cards_short_news_card_title);
      mDescription = view.findViewById(R.id.com_braze_content_cards_short_news_card_description);
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
        .inflate(R.layout.com_braze_short_news_content_card, viewGroup, false);
    setViewBackground(view);
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, ShortNewsCard card) {
    super.bindViewHolder(viewHolder, card);
    ViewHolder shortNewsCardViewHolder = (ViewHolder) viewHolder;

    setOptionalTextView(shortNewsCardViewHolder.getTitle(), card.getTitle());
    setOptionalTextView(shortNewsCardViewHolder.getDescription(), card.getDescription());
    shortNewsCardViewHolder.setActionHintText(StringUtils.isNullOrBlank(card.getDomain()) ? card.getUrl() : card.getDomain());

    setOptionalCardImage(shortNewsCardViewHolder.getImageView(), ASPECT_RATIO, card.getImageUrl(), card);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      safeSetClipToOutline(shortNewsCardViewHolder.getImageView());
    }
    viewHolder.itemView.setContentDescription(card.getTitle() + " . " + card.getDescription());
  }
}
