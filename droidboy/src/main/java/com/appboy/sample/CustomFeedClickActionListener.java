package com.appboy.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.braze.models.cards.Card;
import com.braze.ui.feed.listeners.IFeedClickActionListener;
import com.braze.support.StringUtils;
import com.braze.ui.actions.IAction;

public class CustomFeedClickActionListener implements IFeedClickActionListener {
  @Override
  public boolean onFeedCardClicked(Context context, Card card, IAction cardAction) {
    if (!StringUtils.isNullOrBlank(card.getUrl()) && card.getUrl().matches(context.getString(R.string.youtube_regex))) {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(card.getUrl()));
      context.startActivity(intent);
      return true;
    } else {
      return false;
    }
  }
}
