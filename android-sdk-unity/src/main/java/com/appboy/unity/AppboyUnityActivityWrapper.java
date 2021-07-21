package com.appboy.unity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.appboy.unity.configuration.UnityConfigurationProvider;
import com.appboy.unity.enums.UnityInAppMessageManagerAction;
import com.braze.Braze;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.support.BrazeLogger;
import com.braze.ui.activities.ContentCardsActivity;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.braze.ui.inappmessage.InAppMessageOperation;
import com.braze.ui.inappmessage.listeners.DefaultInAppMessageManagerListener;

/**
 * This class allows UnityPlayerNativeActivity and UnityPlayerActivity instances to
 * integrate Braze by calling appropriate methods during each phase of the Android {@link Activity} lifecycle.
 */
public class AppboyUnityActivityWrapper {
  private static final String TAG = BrazeLogger.getBrazeLogTag(AppboyUnityActivityWrapper.class);

  private UnityConfigurationProvider mUnityConfigurationProvider;

  /**
   * Call from {@link Activity#onCreate(Bundle)}
   */
  public void onCreateCalled(Activity activity) {
    UnityConfigurationProvider unityConfigurationProvider = getUnityConfigurationProvider(activity);
    Braze.getInstance(activity).subscribeToNewInAppMessages(EventSubscriberFactory.createInAppMessageEventSubscriber(unityConfigurationProvider));
    Braze.getInstance(activity).subscribeToFeedUpdates(EventSubscriberFactory.createFeedUpdatedEventSubscriber(unityConfigurationProvider));
    Braze.getInstance(activity).subscribeToContentCardsUpdates(EventSubscriberFactory.createContentCardsEventSubscriber(unityConfigurationProvider));
    BrazeLogger.d(TAG, TAG + " finished onCreateCalled setup.");
  }

  /**
   * Call from {@link Activity#onStart()}
   */
  public void onStartCalled(Activity activity) {
    Braze.getInstance(activity).openSession(activity);
  }

  /**
   * Call from {@link Activity#onResume()}
   */
  public void onResumeCalled(Activity activity) {
    UnityConfigurationProvider unityConfigurationProvider = getUnityConfigurationProvider(activity);
    if (unityConfigurationProvider.getShowInAppMessagesAutomaticallyKey()) {
      BrazeInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }
  }

  /**
   * Call from {@link Activity#onPause()}
   */
  public void onPauseCalled(Activity activity) {
    UnityConfigurationProvider unityConfigurationProvider = getUnityConfigurationProvider(activity);
    if (unityConfigurationProvider.getShowInAppMessagesAutomaticallyKey()) {
      BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
    }
  }

  /**
   * Call from {@link Activity#onStop()}
   */
  public void onStopCalled(Activity activity) {
    Braze.getInstance(activity).closeSession(activity);
  }

  /**
   * Call from {@link Activity#onNewIntent(Intent)}
   */
  public void onNewIntentCalled(Intent intent, Activity activity) {
    // If the Activity is already open and we receive an intent to open the Activity again, we set
    // the new intent as the current one (which has the new intent extras).
    activity.setIntent(intent);
  }

  public void onNewUnityInAppMessageManagerAction(int actionEnumValue) {
    UnityInAppMessageManagerAction action = UnityInAppMessageManagerAction.getTypeFromValue(actionEnumValue);

    switch (action) {
      case IAM_DISPLAY_NOW:
      case IAM_DISPLAY_LATER:
      case IAM_DISCARD:
        BrazeInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(new DefaultInAppMessageManagerListener() {
          @Override
          public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
            super.beforeInAppMessageDisplayed(inAppMessage);
            return action.getInAppMessageOperation();
          }
        });
        break;
      case REQUEST_IAM_DISPLAY:
        BrazeInAppMessageManager.getInstance().requestDisplayInAppMessage();
        break;
      case UNKNOWN:
      default:
        // Do nothing
    }
  }

  public void launchContentCardsActivity(Activity activity) {
    activity.startActivity(new Intent(activity, ContentCardsActivity.class));
  }

  private UnityConfigurationProvider getUnityConfigurationProvider(Activity activity) {
    if (mUnityConfigurationProvider == null) {
      mUnityConfigurationProvider = new UnityConfigurationProvider(activity.getApplicationContext());
    }
    return mUnityConfigurationProvider;
  }
}
