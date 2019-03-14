package com.appboy.ui.feed.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.appboy.models.cards.Card;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.feed.AppboyFeedManager;
import com.appboy.ui.feed.AppboyImageSwitcher;
import com.appboy.ui.widget.BaseCardView;

import java.util.Observable;
import java.util.Observer;

/**
 * Base class for Appboy feed card views
 */
public abstract class BaseFeedCardView<T extends Card> extends BaseCardView<T> implements Observer {
  private static final String TAG = AppboyLogger.getAppboyLogTag(BaseCardView.class);

  public BaseFeedCardView(Context context) {
    super(context);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(getLayoutResource(), this);
    // All implementing views of BaseCardView must include this switcher view in order to have the
    // read/unread functionality. Views that don't have the indicator (like banner views) won't have the image switcher
    // in them and thus we do the null-check below.
    mImageSwitcher = (AppboyImageSwitcher) findViewById(R.id.com_appboy_newsfeed_item_read_indicator_image_switcher);
    if (mImageSwitcher != null) {
      mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
        @Override
        public View makeView() {
          return new ImageView(mContext.getApplicationContext());
        }
      });
    }

    // If the visual indicator on cards shouldn't be on, due to the xml setting in appboy.xml, then set the
    // imageSwitcher to GONE to hide the indicator UI.
    // If the setting is false, then hide the indicator.
    if (!isUnreadIndicatorEnabled()) {
      if (mImageSwitcher != null) {
        mImageSwitcher.setVisibility(GONE);
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
    ViewStub imageStub = (ViewStub) findViewById(stubLayoutId);
    imageStub.inflate();
    return findViewById(R.id.com_appboy_stubbed_feed_image_view);
  }

  /**
   * This method is called when the setRead() method is called on the internal Card object.
   */
  @Override
  public void update(Observable observable, Object data) {
    setCardViewedIndicator(mImageSwitcher, getCard());
  }

  public void setCard(final T card) {
    mCard = card;
    onSetCard(card);
    // Register as an observer to the card class
    card.addObserver(this);
    setCardViewedIndicator(mImageSwitcher, getCard());
  }

  public Card getCard() {
    return mCard;
  }

  @Override
  protected boolean isClickHandled(Context context, Card card, IAction cardAction) {
    return AppboyFeedManager.getInstance().getFeedCardClickActionListener().onFeedCardClicked(context, card, cardAction);
  }

  protected abstract int getLayoutResource();

  protected abstract void onSetCard(T card);
}
