package com.appboy.sample;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.appboy.Constants;

public class SwipeGestureListener implements GestureDetector.OnGestureListener {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, DecisionFragment.class.getName());
  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 250;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private DecisionFragment mDecisionFragment;

  public SwipeGestureListener(DecisionFragment decisionFragment) {
    mDecisionFragment = decisionFragment;
  }

  @Override
  public boolean onDown(MotionEvent e) {
    return true;
  }

  @Override
  public void onShowPress(MotionEvent e) {}

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    return false;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {}

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    try {
      if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
        return false;
      } else if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        mDecisionFragment.performSpin(false);
      }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        mDecisionFragment.performSpin(true);
      }
    } catch (Exception e) {
      Log.e(TAG, "Unexpected exception occurred while detecting swipe gestures.", e);
    }
    return false;
  }
}