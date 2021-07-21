package com.braze.ui.inappmessage;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;

import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.IInAppMessageImmersive;
import com.braze.models.inappmessage.InAppMessageSlideup;
import com.braze.models.inappmessage.MessageButton;
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;

import java.util.List;

public interface IInAppMessageViewWrapperFactory {

  /**
   * Factory interface for non {@link IInAppMessageImmersive} view wrappers.
   * Implementations should add click listeners to the in-app message view and
   * also add swipe functionality to {@link InAppMessageSlideup} in-app messages.
   *
   * @param inAppMessageView                  In-app message top level view visible to the user.
   * @param inAppMessage                      In-app message model.
   * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
   * @param configurationProvider       Configuration provider.
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
                                                         BrazeConfigurationProvider configurationProvider,
                                                         Animation openingAnimation,
                                                         Animation closingAnimation,
                                                         View clickableInAppMessageView);

  /**
   * Constructor for {@link IInAppMessageImmersive} in-app message view wrappers.
   * Implementations should add click listeners to the in-app message view and also
   * add listeners to an optional close button and message button views.
   *
   * @param inAppMessageView                  In-app message top level view visible to the user.
   * @param inAppMessage                      In-app message model.
   * @param inAppMessageViewLifecycleListener In-app message lifecycle listener.
   * @param configurationProvider       Configuration provider.
   * @param openingAnimation                  The {@link Animation} used when opening the {@link IInAppMessage}
   *                                          and becoming visible to the user.
   *                                          Should be called during {@link IInAppMessageViewWrapper#open(Activity)}.
   * @param closingAnimation                  The {@link Animation} used when closing the {@link IInAppMessage}.
   *                                          Should be called during {@link IInAppMessageViewWrapper#close()}.
   * @param clickableInAppMessageView         {@link View} for which click actions apply.
   * @param buttons                           List of views corresponding to {@link MessageButton}
   *                                          objects stored in the in-app message model object.
   *                                          These views should map one to one with the MessageButton objects.
   * @param closeButton                       The {@link View} responsible for closing the in-app message.
   */
  IInAppMessageViewWrapper createInAppMessageViewWrapper(View inAppMessageView,
                                                         IInAppMessage inAppMessage,
                                                         IInAppMessageViewLifecycleListener inAppMessageViewLifecycleListener,
                                                         BrazeConfigurationProvider configurationProvider,
                                                         Animation openingAnimation,
                                                         Animation closingAnimation,
                                                         View clickableInAppMessageView,
                                                         List<View> buttons,
                                                         View closeButton);
}
