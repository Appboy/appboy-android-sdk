package com.appboy.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.cards.Card;
import com.appboy.ui.support.StringUtils;

/**
 * Base class for Appboy feed card views
 */
public abstract class BaseCardView<T extends Card> extends RelativeLayout {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, BaseCardView.class.getName());

  protected final Context mContext;
  protected T mCard;

  public BaseCardView(Context context) {
    super(context);
    mContext = context;
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(getLayoutResource(), this);
  }

  protected abstract int getLayoutResource();

  public void setCard(final T card) {
    mCard = card;
    onSetCard(card);
  };

  protected abstract void onSetCard(T card);

  public Card getCard() {
    return mCard;
  }

  void setOptionalTextView(TextView view, String value) {
    if (value != null && !value.trim().equals(StringUtils.EMPTY_STRING)) {
      view.setText(value);
      view.setVisibility(VISIBLE);
    } else {
      view.setText(StringUtils.EMPTY_STRING);
      view.setVisibility(GONE);
    }
  }

  void safeSetBackground(Drawable background) {
    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      setBackgroundDrawable(background);
    } else {
      setBackgroundNew(background);
    }
  }

  @TargetApi(16)
  private void setBackgroundNew(Drawable background) {
    setBackground(background);
  }

  void setImageViewToUrl(final ImageView imageView, final String imageUrl) {
    setImageViewToUrl(imageView, imageUrl, 1f);
  }

  /**
   * Asynchronously fetches the image at the given imageUrl and displays the image in the ImageView. No image will be
   * displayed if the image cannot be downloaded or fetched from the cache.
   *
   * @param imageView the ImageView in which to display the image
   * @param imageUrl the URL of the image resource
   */
  void setImageViewToUrl(final ImageView imageView, final String imageUrl, final float aspectRatio) {
    if (imageUrl == null) {
      Log.w(TAG, String.format("The image url (%s) to render is null. Not setting the card image.", imageUrl));
    }

    if (aspectRatio != 1) {
      ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
      if (viewTreeObserver.isAlive()) {
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            int width = imageView.getWidth();
            imageView.setLayoutParams(new LayoutParams(width, (int) (width / aspectRatio)));
            safeRemoveOnGlobalLayoutListener(imageView.getViewTreeObserver(), this);
          }
        });
      }
    }

    if (!imageUrl.equals(imageView.getTag())) {
      imageView.setImageResource(android.R.color.transparent);
      Appboy.getInstance(getContext()).fetchAndRenderImage(imageUrl, imageView);
      imageView.setTag(imageUrl);
    }
  }

  private void safeRemoveOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver,
                                                ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
    if (android.os.Build.VERSION.SDK_INT < 16) {
      viewTreeObserver.removeGlobalOnLayoutListener(onGlobalLayoutListener);
    } else {
      viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }
  }
}
