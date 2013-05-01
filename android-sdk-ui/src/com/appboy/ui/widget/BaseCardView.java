package com.appboy.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.models.cards.ICard;

/**
 * Base class for Appboy feed card views
 */
public abstract class BaseCardView<T extends ICard> extends RelativeLayout {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, BaseCardView.class.getName());

  protected final Context mContext;

  public BaseCardView(Context context) {
    super(context);
    mContext = context;
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(getLayoutResource(), this);
  }

  protected abstract int getLayoutResource();

  public abstract void setCard(T card);

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

  void setImageViewToUrl(final ImageView view, final String imageUrl) {
    Appboy.getInstance(getContext()).fetchAndRenderImage(imageUrl, view);
  }
}
