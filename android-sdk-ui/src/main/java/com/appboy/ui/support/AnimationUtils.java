package com.appboy.ui.support;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {
  private static Interpolator sAccelerateInterpolator = new AccelerateInterpolator();
  private static Interpolator sDecelerateInterpolator = new DecelerateInterpolator();

  /**
   * @param fromY Change in Y coordinate to apply at the start of the animation, represented as a percentage (where 1.0 is 100%).
   * @param toY Change in Y coordinate to apply at the end of the animation, represented as a percentage (where 1.0 is 100%).
   * @param duration Amount of time (in milliseconds) for the animation to run.
   * @param accelerate Whether to use the accelerate interpolator or the decelerate interpolator.
   * @return an Animation object with appropriate vertical transformation and duration
   */
  public static Animation createVerticalAnimation(float fromY, float toY, long duration, boolean accelerate) {
    TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_SELF, fromY,
        Animation.RELATIVE_TO_SELF, toY);
    return setAnimationParams(animation, duration, accelerate);
  }

  /**
   * @param fromX Change in X coordinate to apply at the start of the animation, represented as a percentage (where 1.0 is 100%).
   * @param toX Change in X coordinate to apply at the end of the animation, represented as a percentage (where 1.0 is 100%).
   * @param duration Amount of time (in milliseconds) for the animation to run.
   * @param accelerate Whether to use the accelerate interpolator or the decelerate interpolator.
   * @return an Animation object with appropriate horizontal transformation and duration
   */
  public static Animation createHorizontalAnimation(float fromX, float toX, long duration, boolean accelerate) {
    TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromX,
        Animation.RELATIVE_TO_SELF, toX, Animation.RELATIVE_TO_PARENT, 0.0f,
        Animation.RELATIVE_TO_PARENT, 0.0f);
    return setAnimationParams(animation, duration, accelerate);
  }

  /**
   * Sets duration and interpolator for the given Animation object.
   *
   * @param animation The Animation object to modify.
   * @param duration Amount of time (in milliseconds) for the animation to run.
   * @param accelerate Whether to use the accelerate interpolator or the decelerate interpolator.
   * @return the input Animation with duration and interpolator set
   */
  public static Animation setAnimationParams(Animation animation, long duration, boolean accelerate) {
    animation.setDuration(duration);
    if (accelerate) {
      animation.setInterpolator(sAccelerateInterpolator);
    } else {
      animation.setInterpolator(sDecelerateInterpolator);
    }
    return animation;
  }
}
