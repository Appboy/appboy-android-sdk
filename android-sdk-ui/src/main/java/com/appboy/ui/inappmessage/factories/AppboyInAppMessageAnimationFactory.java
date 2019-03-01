package com.appboy.ui.inappmessage.factories;

import android.content.res.Resources;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.appboy.enums.inappmessage.SlideFrom;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.ui.inappmessage.IInAppMessageAnimationFactory;
import com.appboy.ui.support.AnimationUtils;

public class AppboyInAppMessageAnimationFactory implements IInAppMessageAnimationFactory {
  private final int mShortAnimationDurationMillis = Resources.getSystem().getInteger(android.R.integer.config_shortAnimTime);

  @Override
  public Animation getOpeningAnimation(IInAppMessage inAppMessage) {
    if (inAppMessage instanceof InAppMessageSlideup) {
      InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
      if (inAppMessageSlideup.getSlideFrom() == SlideFrom.TOP) {
        return AnimationUtils.createVerticalAnimation(-1, 0, mShortAnimationDurationMillis, false);
      } else {
        return AnimationUtils.createVerticalAnimation(1, 0, mShortAnimationDurationMillis, false);
      }
    } else {
      return AnimationUtils.setAnimationParams(new AlphaAnimation(0, 1), mShortAnimationDurationMillis, true);
    }
  }

  @Override
  public Animation getClosingAnimation(IInAppMessage inAppMessage) {
    if (inAppMessage instanceof InAppMessageSlideup) {
      InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
      if (inAppMessageSlideup.getSlideFrom() == SlideFrom.TOP) {
        return AnimationUtils.createVerticalAnimation(0, -1, mShortAnimationDurationMillis, false);
      } else {
        return AnimationUtils.createVerticalAnimation(0, 1, mShortAnimationDurationMillis, false);
      }
    } else {
      return AnimationUtils.setAnimationParams(new AlphaAnimation(1, 0), mShortAnimationDurationMillis, false);
    }
  }
}