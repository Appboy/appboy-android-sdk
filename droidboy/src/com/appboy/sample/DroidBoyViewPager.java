package com.appboy.sample;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DroidBoyViewPager extends ViewPager {
  private boolean mPagingEnabled;

  public DroidBoyViewPager(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    mPagingEnabled = true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mPagingEnabled) {
      return super.onTouchEvent(event);
    }
    return false;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    if (mPagingEnabled) {
      return super.onInterceptTouchEvent(event);
    }
    return false;
  }

  public void setPagingEnabled(boolean enabled) {
    mPagingEnabled = enabled;
  }
}
