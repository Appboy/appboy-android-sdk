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
    TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.com_appboy_ui_inappmessage_views_AppboyInAppMessageBoundedLayout);
    mMaxDefinedWidthPixels = attributes.getDimensionPixelSize(R.styleable.com_appboy_ui_inappmessage_views_AppboyInAppMessageBoundedLayout_android_maxWidth, 0);
    mMinDefinedWidthPixels = attributes.getDimensionPixelSize(R.styleable.com_appboy_ui_inappmessage_views_AppboyInAppMessageBoundedLayout_android_minWidth, 0);
    mMaxDefinedHeightPixels = attributes.getDimensionPixelSize(R.styleable.com_appboy_ui_inappmessage_views_AppboyInAppMessageBoundedLayout_android_maxHeight, 0);
    mMinDefinedHeightPixels = attributes.getDimensionPixelSize(R.styleable.com_appboy_ui_inappmessage_views_AppboyInAppMessageBoundedLayout_android_minHeight, 0);
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
