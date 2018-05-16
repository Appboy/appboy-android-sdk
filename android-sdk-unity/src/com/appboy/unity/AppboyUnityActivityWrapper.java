package com.appboy.unity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.unity.configuration.UnityConfigurationProvider;

/**
 * This class allows UnityPlayerNativeActivity and UnityPlayerActivity instances to
 * integrate Braze by calling appropriate methods during each phase of the Android {@link Activity} lifecycle.
 */
public class AppboyUnityActivityWrapper {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyUnityActivityWrapper.class);

  private UnityConfigurationProvider mUnityConfigurationProvider;

  /**
   * Call from {@link Activity#onCreate(Bundle)}
   */
  public void onCreateCalled(Activity activity) {
    mUnityConfigurationProvider = new UnityConfigurationProvider(activity);
    Appboy.getInstance(activity).subscribeToNewInAppMessages(EventSubscriberFactory.createInAppMessageEventSubscriber(mUnityConfigurationProvider));
    Appboy.getInstance(activity).subscribeToFeedUpdates(EventSubscriberFactory.createFeedUpdatedEventSubscriber(mUnityConfigurationProvider));
    Log.d(TAG, TAG + " finished onCreateCalled setup.");
  }

  /**
   * Call from {@link Activity#onStart()}
   */
  public void onStartCalled(Activity activity) {
    Appboy.getInstance(activity).openSession(activity);
    if (!mUnityConfigurationProvider.getShowInAppMessagesAutomaticallyKey()) {
      AppboyUnityNativeInAppMessageManagerListener.getInstance().setShowInAppMessagesManually(true);
      Log.i(TAG, "In-app message display will be handled manually.");
    }
    AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(AppboyUnityNativeInAppMessageManagerListener.getInstance());
    AppboyUnityNativeInAppMessageManagerListener.getInstance().registerContainerActivity(activity);
    Log.d(TAG, "Starting " + TAG + ".");
  }

  /**
   * Call from {@link Activity#onResume()}
   */
  public void onResumeCalled(Activity activity) {
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
  }

  /**
   * Call from {@link Activity#onPause()}
   */
  public void onPauseCalled(Activity activity) {
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
  }

  /**
   * Call from {@link Activity#onStop()}
   */
  public void onStopCalled(Activity activity) {
    Appboy.getInstance(activity).closeSession(activity);
  }

  /**
   * Call from {@link Activity#onNewIntent(Intent)}
   */
  public void onNewIntentCalled(Intent intent, Activity activity) {
    // If the Activity is already open and we receive an intent to open the Activity again, we set
    // the new intent as the current one (which has the new intent extras).
    activity.setIntent(intent);
  }
}
