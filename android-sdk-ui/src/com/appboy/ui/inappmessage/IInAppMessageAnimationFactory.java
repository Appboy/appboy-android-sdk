package com.appboy.ui.inappmessage;

import android.view.View;//NOPMD
import android.view.animation.Animation;

import com.appboy.models.IInAppMessage;

public interface IInAppMessageAnimationFactory {

  /**
   * This method returns the animation that will be used to animate the message as it enters the screen.
   * @return animation that will be applied to the in-app message view using {@link View#setAnimation}
   */
  Animation getOpeningAnimation(IInAppMessage inAppMessage);

  /**
   * This method returns the animation that will be used to animate the message as it exits the screen.
   * @return animation that will be applied to the in-app message view using {@link View#setAnimation}
   */
  Animation getClosingAnimation(IInAppMessage inAppMessage);
}
