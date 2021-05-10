package com.appboy.ui.inappmessage.factories;

import android.view.View;
import android.view.animation.Animation;

import com.appboy.models.IInAppMessage;
import com.appboy.ui.inappmessage.DefaultInAppMessageViewWrapper;
import com.appboy.ui.inappmessage.IInAppMessageViewWrapper;
import com.appboy.ui.inappmessage.IInAppMessageViewWrapperFactory;
import com.appboy.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;
import com.braze.configuration.BrazeConfigurationProvider;

import java.util.List;

/**
 * The default {@link IInAppMessageViewWrapperFactory} that returns
 * an instance of {@link DefaultInAppMessageViewWrapper}.
 */
public class AppboyInAppMessageViewWrapperFactory implements IInAppMessageViewWrapperFactory {
  @Override
  public IInAppMessageViewWrapper createInAppMessageViewWrapper(View inAppMessageView,
                                                                IInAppMessage inAppMessage,
                                                                IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener,
                                                                BrazeConfigurationProvider configurationProvider,
                                                                Animation openingAnimation,
                                                                Animation closingAnimation,
                                                                View clickableInAppMessageView) {
    return new DefaultInAppMessageViewWrapper(inAppMessageView,
        inAppMessage,
        inAppMessageViewLifecycleListener,
        configurationProvider,
        openingAnimation,
        closingAnimation,
        clickableInAppMessageView);
  }

  @Override
  public IInAppMessageViewWrapper createInAppMessageViewWrapper(View inAppMessageView,
                                                                IInAppMessage inAppMessage,
                                                                IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener,
                                                                BrazeConfigurationProvider configurationProvider,
                                                                Animation openingAnimation,
                                                                Animation closingAnimation,
                                                                View clickableInAppMessageView,
                                                                List<View> buttons,
                                                                View closeButton) {
    return new DefaultInAppMessageViewWrapper(inAppMessageView,
        inAppMessage,
        inAppMessageViewLifecycleListener,
        configurationProvider,
        openingAnimation,
        closingAnimation,
        clickableInAppMessageView,
        buttons,
        closeButton);
  }
}
