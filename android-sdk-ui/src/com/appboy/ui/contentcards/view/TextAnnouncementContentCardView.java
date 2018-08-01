package com.appboy.ui.contentcards.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appboy.models.cards.TextAnnouncementCard;
import com.appboy.ui.R;

public class TextAnnouncementContentCardView extends BaseContentCardView<TextAnnouncementCard> {
  public TextAnnouncementContentCardView(Context context) {
    super(context);
  }

  private class ViewHolder extends ContentCardViewHolder {
    private final TextView mTitle;
    private final TextView mDescription;
    private final TextView mDomain;

    ViewHolder(View view) {
      super(view, isUnreadIndicatorEnabled());

      mTitle = (TextView) view.findViewById(R.id.com_appboy_content_cards_text_announcement_card_title);
      mDescription = (TextView) view.findViewById(R.id.com_appboy_content_cards_text_announcement_card_description);
      mDomain = (TextView) view.findViewById(R.id.com_appboy_content_cards_text_announcement_card_domain);
    }

    TextView getTitle() {
      return mTitle;
    }

    TextView getDescription() {
      return mDescription;
    }

    TextView getDomain() {
      return mDomain;
    }
  }

  @Override
  public ContentCardViewHolder createViewHolder(ViewGroup viewGroup) {
    View view = LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.com_appboy_text_announcement_content_card, viewGroup, false);

    view.setBackground(getResources().getDrawable(R.drawable.com_appboy_card_background));
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ContentCardViewHolder viewHolder, final TextAnnouncementCard card) {
    super.bindViewHolder(viewHolder, card);
    ViewHolder textAnnouncementViewHolder = (ViewHolder) viewHolder;

    textAnnouncementViewHolder.getTitle().setText(card.getTitle());
    textAnnouncementViewHolder.getDescription().setText(card.getDescription());
    setOptionalTextView(textAnnouncementViewHolder.getDomain(), card.getDomain());
  }
}
