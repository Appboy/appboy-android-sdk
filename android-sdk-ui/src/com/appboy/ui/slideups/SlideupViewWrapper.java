package com.appboy.ui.slideups;

import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.appboy.enums.Slideup.DismissType;
import com.appboy.enums.Slideup.SlideFrom;
import com.appboy.models.Slideup;
import com.appboy.ui.Constants;
import com.appboy.ui.support.ViewUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class SlideupViewWrapper {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, SlideupViewWrapper.class.getName());
  private static Interpolator sInterpolator = new DecelerateInterpolator();
  private static final long sAnimationDurationInMilliseconds = 400l;

  private final View mSlideupView;
  private final Slideup mSlideup;
  private final ISlideupViewLifecycleListener mSlideupViewLifecycleListener;
  private Runnable mDismissRunnable;
  private AtomicBoolean mClosedEventFired = new AtomicBoolean(false);

  public SlideupViewWrapper(View slideupView, Slideup slideup, ISlideupViewLifecycleListener slideupViewLifecycleListener) {
    mSlideupView = slideupView;
    mSlideup = slideup;
    mSlideupViewLifecycleListener = slideupViewLifecycleListener;

    // We only apply the swipe touch listener to the slideup View on devices running Android version
    // 12 or higher. Pre-12 devices will have to click to close the slideup.
    if (Build.VERSION.SDK_INT >= 12) {
      // Adds the swipe listener to the slideup View. All slideups should be dismissable via a swipe
      // (even auto close slideups).
      SwipeDismissTouchListener.DismissCallbacks dismissCallbacks = createDismissCallbacks();
      TouchAwareSwipeDismissTouchListener touchAwareSwipeListener = new TouchAwareSwipeDismissTouchListener(slideupView, null, dismissCallbacks);
      // We set a custom touch listener that cancel the auto close runnable when touched and adds
      // a new runnable when the touch ends.
      touchAwareSwipeListener.setTouchListener(createTouchAwareListener());
      mSlideupView.setOnTouchListener(touchAwareSwipeListener);
    }

    slideupView.setOnClickListener(createClickListener());
  }

  public void open(final FrameLayout root) {
    mSlideupViewLifecycleListener.beforeOpened(mSlideupView, mSlideup);
    addViewToLayout(root);
    display();
  }

  private void preClose() {
    mSlideupViewLifecycleListener.beforeClosed(mSlideupView, mSlideup);
  }

  private void performClose() {
    if (mSlideup.getAnimateOut()) {
      setAndStartAnimation(false);
    } else {
      ViewUtils.removeViewFromParent(mSlideupView);
      mSlideupViewLifecycleListener.afterClosed(mSlideup);
    }
  }

  public void close() {
    preClose();
    performClose();
  }

  public View getSlideupView() {
    return mSlideupView;
  }

  public Slideup getSlideup() {
    return mSlideup;
  }

  private View.OnClickListener createClickListener() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // The onClicked lifecycle method is called and it can be used to turn off the close animation.
        mSlideupViewLifecycleListener.onClicked(new SlideupCloser(SlideupViewWrapper.this), mSlideupView, mSlideup);
      }
    };
  }

  private void addViewToLayout(final FrameLayout root) {
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = mSlideup.getSlideFrom() == SlideFrom.TOP ? Gravity.TOP : Gravity.BOTTOM;
    root.addView(mSlideupView, layoutParams);
  }

  private void removeViewFromLayout() {
    ViewUtils.removeViewFromParent(mSlideupView);
  }

  private void display() {
    if (mSlideup.getAnimateIn()) {
      setAndStartAnimation(true);
      // The afterOpened lifecycle method gets called when the opening animation ends.
    } else {
      // There is no opening animation, so we call the afterOpened lifecycle method immediately.
      if (mSlideup.getDismissType() == DismissType.AUTO_DISMISS) {
        addDismissRunnable();
      }
      mSlideupViewLifecycleListener.afterOpened(mSlideupView, mSlideup);
    }
  }

  private void addDismissRunnable() {
    if (mDismissRunnable == null) {
      mDismissRunnable = new Runnable() {
        @Override
        public void run() {
          close();
        }
      };
      mSlideupView.postDelayed(mDismissRunnable, mSlideup.getDurationInMilliseconds());
    }
  }

  private SwipeDismissTouchListener.DismissCallbacks createDismissCallbacks() {
    return new SwipeDismissTouchListener.DismissCallbacks() {
      @Override
      public boolean canDismiss(Object token) {
        return true;
      }
      @Override
      public void onDismiss(View view, Object token) {
        mSlideupViewLifecycleListener.onDismissed(mSlideupView, mSlideup);
        mSlideup.setAnimateOut(false);
        close();
      }
    };
  }

  private TouchAwareSwipeDismissTouchListener.ITouchListener createTouchAwareListener() {
    return new TouchAwareSwipeDismissTouchListener.ITouchListener() {
      @Override
      public void onTouchStartedOrContinued() {
        mSlideupView.removeCallbacks(mDismissRunnable);
      }
      @Override
      public void onTouchEnded() {
        if (mSlideup.getDismissType() == DismissType.AUTO_DISMISS) {
          addDismissRunnable();
        }
      }
    };
  }

  private Animation createAnimation(float fromY, float toY, long duration) {
    TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f,
        Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_SELF, fromY,
        Animation.RELATIVE_TO_SELF, toY);
    animation.setDuration(duration);
    animation.setFillAfter(true);
    animation.setFillEnabled(true);
    animation.setInterpolator(sInterpolator);
    return animation;
  }

  private void setAndStartAnimation(boolean opening) {
    Animation animation;
    if (opening && mSlideup.getSlideFrom() == SlideFrom.TOP) {
      animation = createAnimation(-1, 0, sAnimationDurationInMilliseconds);
    } else if (opening && mSlideup.getSlideFrom() == SlideFrom.BOTTOM) {
      animation = createAnimation(1, 0, sAnimationDurationInMilliseconds);
    } else if (!opening && mSlideup.getSlideFrom() == SlideFrom.TOP) {
      animation = createAnimation(0, -1, sAnimationDurationInMilliseconds);
    } else {
      animation = createAnimation(0, 1, sAnimationDurationInMilliseconds);
    }
    animation.setAnimationListener(createAnimationListener(opening));
    mSlideupView.clearAnimation();
    mSlideupView.setAnimation(animation);
    animation.startNow();
    // We need to explicitly call invalidate on Froyo, otherwise the animation won't start :(
    mSlideupView.invalidate();
  }

  private Animation.AnimationListener createAnimationListener(boolean opening) {
    if (opening) {
      return new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
          mSlideupView.setClickable(false);
        }
        @Override
        public void onAnimationEnd(Animation animation) {
          mSlideupView.setVisibility(View.VISIBLE);
          mSlideupView.setClickable(true);
          if (mSlideup.getDismissType() == DismissType.AUTO_DISMISS) {
            addDismissRunnable();
          }
          mSlideupViewLifecycleListener.afterOpened(mSlideupView, mSlideup);
        }
        @Override
        public void onAnimationRepeat(Animation animation) {}
      };
    } else {
      return new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
          mSlideupView.setClickable(false);
        }
        @Override
        public void onAnimationEnd(Animation animation) {
          mSlideupView.clearAnimation();
          mSlideupView.setVisibility(View.GONE);
          mSlideupView.setClickable(true);
          removeViewFromLayout();
          mSlideupViewLifecycleListener.afterClosed(mSlideup);
        }
        @Override
        public void onAnimationRepeat(Animation animation) {}
      };
    }
  }
}
