package com.appboy.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.models.cards.Card;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.feed.AppboyFeedManager;
import com.appboy.ui.support.FrescoLibraryUtils;
import com.appboy.ui.support.ViewUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Observable;
import java.util.Observer;

/**
 * Base class for Appboy feed card views
 */
public abstract class BaseCardView<T extends Card> extends RelativeLayout implements Observer {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, BaseCardView.class.getName());
  private static Boolean unreadCardVisualIndicatorOn;
  private static final float SQUARE_ASPECT_RATIO = 1f;
  protected final Context mContext;
  protected T mCard;
  protected ImageSwitcher mImageSwitcher;
  private final boolean mCanUseFresco;

  public BaseCardView(Context context) {
    super(context);
    // Note: this must be called before we inflate any views.
    mCanUseFresco = FrescoLibraryUtils.canUseFresco(context);

    mContext = context;
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(getLayoutResource(), this);
    // All implementing views of BaseCardView must include this switcher view in order to have the
    // read/unread functionality. Views that don't have the indicator (like banner views) won't have the image switcher
    // in them and thus we do the null-check below.
    mImageSwitcher = (ImageSwitcher) findViewById(R.id.com_appboy_newsfeed_item_read_indicator_image_switcher);
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

    // Read the setting from the appboy.xml if we don't already have a value.
    if (unreadCardVisualIndicatorOn == null) {
      AppboyConfigurationProvider configurationProvider = new AppboyConfigurationProvider(context);
      unreadCardVisualIndicatorOn = configurationProvider.getIsNewsfeedVisualIndicatorOn();
    }

    // If the setting is false, then hide the indicator.
    if (!unreadCardVisualIndicatorOn) {
      if (mImageSwitcher != null) {
        mImageSwitcher.setVisibility(GONE);
      }
    }
  }

  /**
   * This method is called when the setRead() method is called on the internal Card object.
   */
  @Override
  public void update(Observable observable, Object data) {
    setCardViewedIndicator();
  }

  /**
   * Checks to see if the card object is viewed and if so, sets the read/unread status
   * indicator image. If the card is null, does nothing.
   */
  private void setCardViewedIndicator() {
    if (getCard() != null) {
      if (mImageSwitcher != null) {
        AppboyLogger.d(TAG, "Setting the read/unread indicator for the card.");
        int resourceId;
        if (getCard().isRead()) {
          resourceId = R.drawable.icon_read;
        } else {
          resourceId = R.drawable.icon_unread;
        }
        mImageSwitcher.setImageResource(resourceId);
        // Used to identify the current Drawable in the imageSwitcher
        mImageSwitcher.setTag(String.valueOf(resourceId));
      }
    } else {
      AppboyLogger.d(TAG, "The card is null.");
    }
  }

  protected abstract int getLayoutResource();

  public void setCard(final T card) {
    mCard = card;
    onSetCard(card);
    // Register as an observer to the card class
    card.addObserver(this);
    setCardViewedIndicator();
  }

  protected abstract void onSetCard(T card);

  public Card getCard() {
    return mCard;
  }

  void setOptionalTextView(TextView view, String value) {
    if (value != null && !value.trim().equals("")) {
      view.setText(value);
      view.setVisibility(VISIBLE);
    } else {
      view.setText("");
      view.setVisibility(GONE);
    }
  }

  void safeSetBackground(Drawable background) {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      setBackgroundDrawable(background);
    } else {
      setBackgroundNew(background);
    }
  }

  @TargetApi(16)
  private void setBackgroundNew(Drawable background) {
    setBackground(background);
  }

  /**
   * Calls setImageViewToUrl with aspect ratio set to 1f and respectAspectRatio set to false.
   * @see com.appboy.ui.widget.BaseCardView#setImageViewToUrl(android.widget.ImageView, String, float, boolean)
   */
  void setImageViewToUrl(final ImageView imageView, final String imageUrl) {
    setImageViewToUrl(imageView, imageUrl, 1f, false);
  }

  /**
   * Calls setImageViewToUrl with respectAspectRatio set to true.
   * @see com.appboy.ui.widget.BaseCardView#setImageViewToUrl(android.widget.ImageView, String, float, boolean)
   */
  void setImageViewToUrl(final ImageView imageView, final String imageUrl, final float aspectRatio) {
    setImageViewToUrl(imageView, imageUrl, aspectRatio, true);
  }

  /**
   * Asynchronously fetches the image at the given imageUrl and displays the image in the ImageView. No image will be
   * displayed if the image cannot be downloaded or fetched from the cache.
   *
   * @param imageView the ImageView in which to display the image
   * @param imageUrl the URL of the image resource
   * @param aspectRatio the desired aspect ratio of the image. This should match what's being sent down from the dashboard.
   * @param respectAspectRatio whether to use aspectRatio as the final aspect ratio of the imageView. When set to false,
   *                           the aspect ratio of the imageView will match that of the downloaded image. When set to true,
   *                           the provided aspect ratio will match aspectRatio, regardless of the actual dimensions of the
   *                           downloaded image.
   */
  void setImageViewToUrl(final ImageView imageView, final String imageUrl, final float aspectRatio, final boolean respectAspectRatio) {
    if (imageUrl == null) {
      AppboyLogger.w(TAG, "The image url to render is null. Not setting the card image.");
      return;
    }

    if (aspectRatio == 0) {
      AppboyLogger.w(TAG, "The image aspect ratio is 0. Not setting the card image.");
      return;
    }

    if (!imageUrl.equals(imageView.getTag())) {
      if (aspectRatio != SQUARE_ASPECT_RATIO) {
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
              imageView.setLayoutParams(new LayoutParams(width, (int) (width / aspectRatio)));
              ViewUtils.removeOnGlobalLayoutListenerSafe(imageView.getViewTreeObserver(), this);
            }
          });
        }
      }

      imageView.setImageResource(android.R.color.transparent);
      Appboy.getInstance(getContext()).fetchAndRenderImage(imageUrl, imageView, respectAspectRatio);
      imageView.setTag(imageUrl);
    }
  }

  /**
   * Loads an image via url for display in a SimpleDraweeView using the Facebook Fresco library.
   * By default, gif urls are set to autoplay and tap to retry is on for all images.
   * @param simpleDraweeView the fresco SimpleDraweeView in which to display the image
   * @param imageUrl the URL of the image resource
   */
  void setSimpleDraweeToUrl(final SimpleDraweeView simpleDraweeView, final String imageUrl, final float aspectRatio, final boolean respectAspectRatio) {
    if (imageUrl == null) {
      AppboyLogger.w(TAG, "The image url to render is null. Not setting the card image.");
      return;
    }

    FrescoLibraryUtils.setDraweeControllerHelper(simpleDraweeView, imageUrl, aspectRatio, respectAspectRatio);
  }

  /**
   * Returns whether we can use the Fresco Library for newsfeed cards.
   */
  boolean canUseFresco() {
    return mCanUseFresco;
  }

  protected static void handleCardClick(Context context, Card card, IAction cardAction, String tag) {
    handleCardClick(context, card, cardAction, tag, true);
  }

  /**
   * All card views should handle new feed card clicks through this method
   */
  protected static void handleCardClick(Context context, Card card, IAction cardAction, String tag, boolean markAsRead) {
    if (markAsRead) {
      card.setIsRead(true);
    }
    if (cardAction != null) {
      if (card.logClick()) {
        AppboyLogger.d(tag, String.format("Logged click for card %s", card.getId()));
      } else {
        AppboyLogger.d(tag, String.format("Logging click failed for card %s", card.getId()));
      }
      if (!AppboyFeedManager.getInstance().getFeedCardClickActionListener().onFeedCardClicked(context, card, cardAction)) {
        cardAction.execute(context);
      }
    }
  }

  /**
   * Gets the view to display the correct card image after checking if it can use Fresco.
   * @param stubLayoutId The resource Id of the stub for inflation as returned by findViewById.
   * @return the view to display the image. This will either be an ImageView or DraweeView
   */
  View getProperViewFromInflatedStub(int stubLayoutId) {
    ViewStub imageStub = (ViewStub) findViewById(stubLayoutId);
    imageStub.inflate();

    if (mCanUseFresco) {
      return findViewById(R.id.com_appboy_stubbed_feed_drawee_view);
    } else {
      return findViewById(R.id.com_appboy_stubbed_feed_image_view);
    }
  }
}
