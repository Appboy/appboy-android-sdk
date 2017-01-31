package com.appboy.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.appboy.models.cards.Card;
import com.appboy.support.StringUtils;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.feed.listeners.IFeedClickActionListener;

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
