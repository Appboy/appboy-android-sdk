package com.appboy.ui.inappmessage.listeners;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SimpleSwipeDismissTouchListener implements View.OnTouchListener {

  private final GestureDetector mSwipeGestureListener;

  public SimpleSwipeDismissTouchListener(Context context) {
    mSwipeGestureListener = new GestureDetector(context, new SwipeGestureListener());
  }

  public void onSwipeLeft() {}

  public void onSwipeRight() {}

  public boolean onTouch(View view, MotionEvent event) {
    return mSwipeGestureListener.onTouchEvent(event);
  }

  private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

    /**
     * Swipe distance and speed threshold constants, in pixels.  These represent the speed and distance
     * a fling gesture must exceed to be interpreted as a slideup dismiss gesture.  Specific values
     * determined through user acceptance tests.
     */
    private static final int SWIPE_DISTANCE_THRESHOLD = 120;
    private static final int SWIPE_VELOCITY_THRESHOLD = 90;

    @Override
    public boolean onFling(MotionEvent downEvent, MotionEvent upEvent, float velocityX, float velocityY) {
      float distanceX = upEvent.getX() - downEvent.getX();
      float distanceY = upEvent.getY() - downEvent.getY();
      if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
        if (distanceX > 0) {
          onSwipeRight();
        } else {
          onSwipeLeft();
        }
        return true;
      }
      return false;
    }
  }
}
