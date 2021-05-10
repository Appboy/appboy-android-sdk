package com.appboy.unity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.unity.configuration.UnityConfigurationProvider;
import com.braze.Braze;
import com.braze.support.BrazeLogger;

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
      AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }
  }

  /**
   * Call from {@link Activity#onPause()}
   */
  public void onPauseCalled(Activity activity) {
    UnityConfigurationProvider unityConfigurationProvider = getUnityConfigurationProvider(activity);
    if (unityConfigurationProvider.getShowInAppMessagesAutomaticallyKey()) {
      AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
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

  private UnityConfigurationProvider getUnityConfigurationProvider(Activity activity) {
    if (mUnityConfigurationProvider == null) {
      mUnityConfigurationProvider = new UnityConfigurationProvider(activity.getApplicationContext());
    }
    return mUnityConfigurationProvider;
  }
}
