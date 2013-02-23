package com.appboy.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.appboy.Constants;
import com.appboy.models.cards.ICard;

/**
 * Base class for Appboy feed card views
 */
public abstract class BaseCardView<T extends ICard> extends LinearLayout {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, BaseCardView.class.getName());

  private final Context mContext;

  public BaseCardView(Context context) {
    this(context, null);
  }

  public BaseCardView(Context context, T card) {
    super(context);
    mContext = context;

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(getLayoutResource(), this);
  }

  protected abstract int getLayoutResource();

  public abstract void setCard(T card);
}
