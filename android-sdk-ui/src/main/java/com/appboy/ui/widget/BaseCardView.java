package com.appboy.ui.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.appboy.Appboy;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.enums.AppboyViewBounds;
import com.appboy.enums.Channel;
import com.appboy.models.cards.Card;
import com.appboy.support.AppboyLogger;
import com.appboy.support.StringUtils;
import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.actions.UriAction;
import com.appboy.ui.feed.AppboyImageSwitcher;

/**
 * Base class for Braze feed card views
 */
@SuppressWarnings("PMD.AssignmentToNonFinalStatic")
public abstract class BaseCardView<T extends Card> extends RelativeLayout {
  private static final String TAG = AppboyLogger.getBrazeLogTag(BaseCardView.class);
  private static final float SQUARE_ASPECT_RATIO = 1f;
  private static final String ICON_READ_TAG = "icon_read";
  private static final String ICON_UNREAD_TAG = "icon_unread";

  protected final Context mContext;
  private final String mClassLogTag;

  private static Boolean sUnreadCardVisualIndicatorEnabled;
  @Nullable
  protected T mCard;
  protected AppboyImageSwitcher mImageSwitcher;
  protected AppboyConfigurationProvider mAppboyConfigurationProvider;

  public BaseCardView(Context context) {
    super(context);
    mContext = context.getApplicationContext();

    // Read the setting from the appboy.xml if we don't already have a value.
    if (mAppboyConfigurationProvider == null) {
      mAppboyConfigurationProvider = new AppboyConfigurationProvider(context);
    }
    if (sUnreadCardVisualIndicatorEnabled == null) {
      sUnreadCardVisualIndicatorEnabled = mAppboyConfigurationProvider.getIsNewsfeedVisualIndicatorOn();
    }

    mClassLogTag = AppboyLogger.getBrazeLogTag(this.getClass());
  }

  /**
   * Applies the text to the {@link TextView}. If the text is null or blank,
   * the {@link TextView}'s visibility is changed to {@link android.view.View#GONE}.
   */
  public void setOptionalTextView(TextView view, String text) {
    if (!StringUtils.isNullOrBlank(text)) {
      view.setText(text);
      view.setVisibility(VISIBLE);
    } else {
      view.setText("");
      view.setVisibility(GONE);
    }
  }

  /**
   * Asynchronously fetches the image at the given imageUrl and displays the image in the ImageView. No image will be
   * displayed if the image cannot be downloaded or fetched from the cache.
   *
   * @param imageView the ImageView in which to display the image
   * @param imageUrl the URL of the image resource
   * @param placeholderAspectRatio a placeholder aspect ratio that will be used for sizing purposes.
   *                               The actual dimensions of the final image will dictate the final image aspect ratio.
   * @param card
   */
  public void setImageViewToUrl(final ImageView imageView, final String imageUrl, final float placeholderAspectRatio, Card card) {
    if (imageUrl == null) {
      AppboyLogger.w(TAG, "The image url to render is null. Not setting the card image.");
      return;
    }

    if (placeholderAspectRatio == 0) {
      AppboyLogger.w(TAG, "The image aspect ratio is 0. Not setting the card image.");
      return;
    }

    if (!imageUrl.equals(imageView.getTag(R.string.com_appboy_image_resize_tag_key))) {
      if (placeholderAspectRatio != SQUARE_ASPECT_RATIO) {
        // We need to set layout params on the imageView once its layout state is visible. To do this,
        // we obtain the imageView's observer and attach a listener on it for when the view's layout
        // occurs. At layout time, we set the imageView's size params based on the aspect ratio
        // for our card. Note that after the card's first layout, we don't want redundant resizing
        // so we remove our listener after the resizing.
        ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
          viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              int width = imageView.getWidth();
              imageView.setLayoutParams(new LayoutParams(width, (int) (width / placeholderAspectRatio)));
              imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
          });
        }
      }

      imageView.setImageResource(android.R.color.transparent);
      Appboy.getInstance(getContext()).getAppboyImageLoader().renderUrlIntoCardView(getContext(), card, imageUrl, imageView, AppboyViewBounds.BASE_CARD_VIEW);
      imageView.setTag(R.string.com_appboy_image_resize_tag_key, imageUrl);
    }
  }

  /**
   * Checks to see if the card object is viewed and if so, sets the read/unread status
   * indicator image. If the card is null, does nothing.
   */
  public void setCardViewedIndicator(AppboyImageSwitcher imageSwitcher, Card card) {
    if (card == null) {
      AppboyLogger.d(getClassLogTag(), "The card is null. Not setting read/unread indicator.");
      return;
    }

    if (imageSwitcher == null) {
      return;
    }

    // Check the tag for the image switcher so we don't have to re-draw the same indicator unnecessarily
    String imageSwitcherTag = (String) imageSwitcher.getTag(R.string.com_appboy_image_is_read_tag_key);
    // If the tag is null, default to the empty string
    imageSwitcherTag = imageSwitcherTag != null ? imageSwitcherTag : "";

    if (card.isIndicatorHighlighted()) {
      if (!imageSwitcherTag.equals(ICON_READ_TAG)) {
        if (imageSwitcher.getReadIcon() != null) {
          imageSwitcher.setImageDrawable(imageSwitcher.getReadIcon());
        } else {
          imageSwitcher.setImageResource(R.drawable.icon_read);
        }
        imageSwitcher.setTag(R.string.com_appboy_image_is_read_tag_key, ICON_READ_TAG);
      }
    } else {
      if (!imageSwitcherTag.equals(ICON_UNREAD_TAG)) {
        if (imageSwitcher.getUnReadIcon() != null) {
          imageSwitcher.setImageDrawable(imageSwitcher.getUnReadIcon());
        } else {
          imageSwitcher.setImageResource(R.drawable.icon_unread);
        }
        imageSwitcher.setTag(R.string.com_appboy_image_is_read_tag_key, ICON_UNREAD_TAG);
      }
    }
  }

  public String getClassLogTag() {
    return mClassLogTag;
  }

  public boolean isUnreadIndicatorEnabled() {
    return sUnreadCardVisualIndicatorEnabled;
  }

  protected static UriAction getUriActionForCard(Card card) {
    Bundle extras = new Bundle();
    for (String key : card.getExtras().keySet()) {
      extras.putString(key, card.getExtras().get(key));
    }
    return ActionFactory.createUriActionFromUrlString(card.getUrl(), extras, card.getOpenUriInWebView(), Channel.NEWS_FEED);
  }

  protected void handleCardClick(Context context, Card card, IAction cardAction, String tag) {
    AppboyLogger.v(TAG, "Handling card click for card: " + card);
    card.setIndicatorHighlighted(true);
    if (!isClickHandled(context, card, cardAction)) {
      if (cardAction != null) {
        card.logClick();
        AppboyLogger.v(TAG, "Card action is non-null. Attempting to perform action on card: " + card.getId());
        if (cardAction instanceof UriAction) {
          AppboyNavigator.getAppboyNavigator().gotoUri(context, (UriAction) cardAction);
        } else {
          AppboyLogger.d(TAG, "Executing non uri action for click on card: " + card.getId());
          cardAction.execute(context);
        }
      } else {
        AppboyLogger.v(TAG, "Card action is null. Not performing any click action on card: " + card.getId());
      }
    } else {
      AppboyLogger.d(TAG, "Card click was handled by custom listener on card: " + card.getId());
      card.logClick();
    }
  }

  /**
   * Calls the corresponding card manager to see if the action listener has handled the click.
   */
  protected abstract boolean isClickHandled(Context context, Card card, IAction cardAction);
}
