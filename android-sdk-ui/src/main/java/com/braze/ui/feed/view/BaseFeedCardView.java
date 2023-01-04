package com.braze.ui.feed.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;

import com.braze.models.cards.Card;
import com.braze.ui.R;
import com.braze.ui.feed.BrazeFeedManager;
import com.braze.ui.widget.BaseCardView;
import com.braze.support.BrazeLogger;
import com.braze.ui.actions.IAction;

/**
 * Base class for Braze feed card views
 */
public abstract class BaseFeedCardView<T extends Card> extends BaseCardView<T> {
  private static final String TAG = BrazeLogger.getBrazeLogTag(BaseCardView.class);

  public BaseFeedCardView(Context context) {
    super(context);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(getLayoutResource(), this);
    // All implementing views of BaseCardView must include this switcher view in order to have the
    // read/unread functionality. Views that don't have the indicator (like banner views) won't have the image switcher
    // in them and thus we do the null-check below.
    imageSwitcher = findViewById(R.id.com_braze_newsfeed_item_read_indicator_image_switcher);
    if (imageSwitcher != null) {
      imageSwitcher.setFactory(() -> new ImageView(applicationContext));
    }

    // If the visual indicator on cards shouldn't be on, due to the xml setting in braze.xml, then set the
    // imageSwitcher to GONE to hide the indicator UI.
    // If the setting is false, then hide the indicator.
    if (!isUnreadIndicatorEnabled()) {
      if (imageSwitcher != null) {
        imageSwitcher.setVisibility(GONE);
      }
    }
  }

  /**
   * Gets the view to display the correct card image. Note that since the Content Cards does not
   * use image view stubs any longer, this method is only used for Feed cards.
   *
   * @param stubLayoutId The resource Id of the stub for inflation as returned by findViewById.
   * @return the view to display the image.
   */
  public View getProperViewFromInflatedStub(int stubLayoutId) {
    ViewStub imageStub = findViewById(stubLayoutId);
    imageStub.inflate();
    return findViewById(R.id.com_braze_stubbed_feed_image_view);
  }

  /**
   * This method is called when the setRead() method is called on the internal Card object.
   */

  public void setCard(final T newCard) {
    card = newCard;
    onSetCard(card);
    card.setListener(() -> setCardViewedIndicator(imageSwitcher, getCard()));
    setCardViewedIndicator(imageSwitcher, getCard());
  }

  public Card getCard() {
    return card;
  }

  @Override
  protected boolean isClickHandled(Context context, Card card, IAction cardAction) {
    return BrazeFeedManager.getInstance().getFeedCardClickActionListener().onFeedCardClicked(context, card, cardAction);
  }

  protected abstract int getLayoutResource();

  protected abstract void onSetCard(T card);
}
