package com.appboy.ui.feed;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;

import androidx.annotation.VisibleForTesting;

import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;

public class AppboyImageSwitcher extends ImageSwitcher {
  private static final String TAG = AppboyLogger.getBrazeLogTag(AppboyImageSwitcher.class);

  private Drawable mReadIcon;
  private Drawable mUnReadIcon;

  public AppboyImageSwitcher(Context context, AttributeSet attrs) {
    super(context, attrs);
    try {
      // Get the array of offset indices into the R value array defined for this view.
      // The R value array is at R.styleable.com_appboy_ui_feed_AppboyImageSwitcher.
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.com_appboy_ui_feed_AppboyImageSwitcher);

      // For all offsets defined on this view, if the offset is equal to the offset for the custom font file
      // defined at R.styleable.com_appboy_ui_feed_AppboyImageSwitcher_appboyFeedCustomReadIcon or
      // R.styleable.com_appboy_ui_feed_AppboyImageSwitcher_appboyFeedCustomUnReadIcon,
      // instruct the typed array to retrieve the data at that offset.
      for (int i = 0; i < typedArray.getIndexCount(); i++) {
        int offset = typedArray.getIndex(i);
        if (offset == R.styleable.com_appboy_ui_feed_AppboyImageSwitcher_appboyFeedCustomReadIcon) {
          Drawable drawable = typedArray.getDrawable(offset);
          if (drawable != null) {
            mReadIcon = drawable;
          }
        } else if (typedArray.getIndex(i) == R.styleable.com_appboy_ui_feed_AppboyImageSwitcher_appboyFeedCustomUnReadIcon) {
          Drawable drawable = typedArray.getDrawable(offset);
          if (drawable != null) {
            mUnReadIcon = drawable;
          }
        }
      }
      typedArray.recycle();
    } catch (Exception e) {
      AppboyLogger.w(TAG, "Error while checking for custom drawable.", e);
    }
  }

  public Drawable getUnReadIcon() {
    return mUnReadIcon;
  }

  public Drawable getReadIcon() {
    return mReadIcon;
  }

  @VisibleForTesting
  public void setUnReadIcon(Drawable unReadIcon) {
    mUnReadIcon = unReadIcon;
  }

  @VisibleForTesting
  public void setReadIcon(Drawable readIcon) {
    mReadIcon = readIcon;
  }
}
