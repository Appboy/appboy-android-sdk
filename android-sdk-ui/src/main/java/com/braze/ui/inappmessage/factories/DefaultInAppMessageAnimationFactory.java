package com.braze.ui.inappmessage.factories;

import android.content.res.Resources;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.braze.enums.inappmessage.SlideFrom;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.InAppMessageSlideup;
import com.braze.ui.inappmessage.IInAppMessageAnimationFactory;
import com.braze.ui.support.AnimationUtils;

public class DefaultInAppMessageAnimationFactory implements IInAppMessageAnimationFactory {
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
