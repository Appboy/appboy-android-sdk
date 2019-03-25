package com.appboy.ui.inappmessage.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.appboy.ui.R;

/**
 * A {@link RelativeLayout} that respects maximum/minimum dimension bounds.
 */
public class AppboyInAppMessageBoundedLayout extends RelativeLayout {
  private int mMaxDefinedWidthPixels;
  private int mMinDefinedWidthPixels;
  private int mMaxDefinedHeightPixels;
  private int mMinDefinedHeightPixels;

  public AppboyInAppMessageBoundedLayout(Context context) {
    super(context);
  }

  public AppboyInAppMessageBoundedLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AppboyInAppMessageBoundedLayout);
    mMaxDefinedWidthPixels = attributes.getDimensionPixelSize(R.styleable.AppboyInAppMessageBoundedLayout_appboyBoundedLayoutMaxWidth, 0);
    mMinDefinedWidthPixels = attributes.getDimensionPixelSize(R.styleable.AppboyInAppMessageBoundedLayout_appboyBoundedLayoutMinWidth, 0);
    mMaxDefinedHeightPixels = attributes.getDimensionPixelSize(R.styleable.AppboyInAppMessageBoundedLayout_appboyBoundedLayoutMaxHeight, 0);
    mMinDefinedHeightPixels = attributes.getDimensionPixelSize(R.styleable.AppboyInAppMessageBoundedLayout_appboyBoundedLayoutMinHeight, 0);
    attributes.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
    if (mMinDefinedWidthPixels > 0 && measuredWidth < mMinDefinedWidthPixels) {
      int measureMode = MeasureSpec.getMode(widthMeasureSpec);
      widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMinDefinedWidthPixels, measureMode);
    } else if (mMaxDefinedWidthPixels > 0 && measuredWidth > mMaxDefinedWidthPixels) {
      int measureMode = MeasureSpec.getMode(widthMeasureSpec);
      widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxDefinedWidthPixels, measureMode);
    }

    int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
    if (mMinDefinedHeightPixels > 0 && measuredHeight < mMinDefinedHeightPixels) {
      int measureMode = MeasureSpec.getMode(heightMeasureSpec);
      heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMinDefinedHeightPixels, measureMode);
    } else if (mMaxDefinedHeightPixels > 0 && measuredHeight > mMaxDefinedHeightPixels) {
      int measureMode = MeasureSpec.getMode(heightMeasureSpec);
      heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxDefinedHeightPixels, measureMode);
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
