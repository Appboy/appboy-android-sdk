package com.braze.ui.inappmessage.factories;

import android.view.View;
import android.view.animation.Animation;

import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.ui.inappmessage.DefaultInAppMessageViewWrapper;
import com.braze.ui.inappmessage.IInAppMessageViewWrapper;
import com.braze.ui.inappmessage.IInAppMessageViewWrapperFactory;
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;

import java.util.List;

/**
 * The default {@link IInAppMessageViewWrapperFactory} that returns
 * an instance of {@link DefaultInAppMessageViewWrapper}.
 */
public class DefaultInAppMessageViewWrapperFactory implements IInAppMessageViewWrapperFactory {
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
