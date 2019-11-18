package com.appboy.ui.inappmessage;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;

import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.models.IInAppMessage;
import com.appboy.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;

import java.util.List;

public interface IInAppMessageViewWrapperFactory {

  /**
   * Factory interface for non {@link com.appboy.models.IInAppMessageImmersive} view wrappers.
   * Implementations should add click listeners to the in-app message view and
   * also add swipe functionality to {@link com.appboy.models.InAppMessageSlideup} in-app messages.
   *
   * @param inAppMessageView                  In-app message top level view visible to the user.
   * @param inAppMessage                      In-app message model.
   * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
   * @param appboyConfigurationProvider       Configuration provider.
   * @param openingAnimation                  The {@link Animation} used when opening the {@link IInAppMessage}
   *                                          and becoming visible to the user.
   *                                          Should be called during {@link IInAppMessageViewWrapper#open(Activity)}.
   * @param closingAnimation                  The {@link Animation} used when closing the {@link IInAppMessage}.
   *                                          Should be called during {@link IInAppMessageViewWrapper#close()}.
   * @param clickableInAppMessageView         {@link View} for which click actions apply.
   */
  IInAppMessageViewWrapper createInAppMessageViewWrapper(View inAppMessageView,
                                                         IInAppMessage inAppMessage,
                                                         IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener,
                                                         AppboyConfigurationProvider appboyConfigurationProvider,
                                                         Animation openingAnimation,
                                                         Animation closingAnimation,
                                                         View clickableInAppMessageView);

  /**
   * Constructor for {@link com.appboy.models.IInAppMessageImmersive} in-app message view wrappers.
   * Implementations should add click listeners to the in-app message view and also
   * add listeners to an optional close button and message button views.
   *
   * @param inAppMessageView                  In-app message top level view visible to the user.
   * @param inAppMessage                      In-app message model.
   * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
   * @param appboyConfigurationProvider       Configuration provider.
   * @param openingAnimation                  The {@link Animation} used when opening the {@link IInAppMessage}
   *                                          and becoming visible to the user.
   *                                          Should be called during {@link IInAppMessageViewWrapper#open(Activity)}.
   * @param closingAnimation                  The {@link Animation} used when closing the {@link IInAppMessage}.
   *                                          Should be called during {@link IInAppMessageViewWrapper#close()}.
   * @param clickableInAppMessageView         {@link View} for which click actions apply.
   * @param buttons                           List of views corresponding to {@link com.appboy.models.MessageButton}
   *                                          objects stored in the in-app message model object.
   *                                          These views should map one to one with the MessageButton objects.
   * @param closeButton                       The {@link View} responsible for closing the in-app message.
   */
  IInAppMessageViewWrapper createInAppMessageViewWrapper(View inAppMessageView,
                                                         IInAppMessage inAppMessage,
                                                         IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener,
                                                         AppboyConfigurationProvider appboyConfigurationProvider,
                                                         Animation openingAnimation,
                                                         Animation closingAnimation,
                                                         View clickableInAppMessageView,
                                                         List<View> buttons,
                                                         View closeButton);
}
