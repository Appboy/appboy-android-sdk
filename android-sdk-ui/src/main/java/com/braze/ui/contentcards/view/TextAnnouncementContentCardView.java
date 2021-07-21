package com.braze.ui.contentcards.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appboy.models.cards.TextAnnouncementCard;
import com.appboy.ui.R;
import com.braze.support.StringUtils;

public class TextAnnouncementContentCardView extends BaseContentCardView<TextAnnouncementCard> {
  public TextAnnouncementContentCardView(Context context) {
    super(context);
  }

  private class ViewHolder extends ContentCardViewHolder {
    private final TextView mTitle;
    private final TextView mDescription;

    ViewHolder(View view) {
      super(view, isUnreadIndicatorEnabled());

      mTitle = view.findViewById(R.id.com_braze_content_cards_text_announcement_card_title);
      mDescription = view.findViewById(R.id.com_braze_content_cards_text_announcement_card_description);
    }

    TextView getTitle() {
      return mTitle;
    }

    TextView getDescription() {
      return mDescription;
    }
  }

  @Override
  public ContentCardViewHolder createViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_braze_text_announcement_content_card, viewGroup, false);
    setViewBackground(view);
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, final TextAnnouncementCard card) {
    super.bindViewHolder(viewHolder, card);
    ViewHolder textAnnouncementViewHolder = (ViewHolder) viewHolder;

    setOptionalTextView(textAnnouncementViewHolder.getTitle(), card.getTitle());
    setOptionalTextView(textAnnouncementViewHolder.getDescription(), card.getDescription());
    textAnnouncementViewHolder.setActionHintText(StringUtils.isNullOrBlank(card.getDomain()) ? card.getUrl() : card.getDomain());
    viewHolder.itemView.setContentDescription(card.getTitle() + " . " + card.getDescription());
  }
}
