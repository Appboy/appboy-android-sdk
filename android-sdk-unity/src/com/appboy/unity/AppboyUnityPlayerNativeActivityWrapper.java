package com.appboy.unity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.unity.configuration.UnityConfigurationProvider;
import com.appboy.unity.utils.InAppMessageUtils;

/**
 * This class allows UnityPlayerNativeActivity and UnityPlayerActivity instances to
 * integrate Braze by calling appropriate methods during each phase of the Android {@link Activity} lifecycle.
 */
public class AppboyUnityPlayerNativeActivityWrapper {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyUnityPlayerNativeActivityWrapper.class);

  private UnityConfigurationProvider mUnityConfigurationProvider;

  /**
   * Call from {@link Activity#onCreate(Bundle)}
   * @param activity
   */
  public void onCreateCalled(Activity activity) {
    mUnityConfigurationProvider = new UnityConfigurationProvider(activity);
    Appboy.getInstance(activity).subscribeToNewInAppMessages(EventSubscriberFactory.createInAppMessageEventSubscriber(mUnityConfigurationProvider));
    Appboy.getInstance(activity).subscribeToFeedUpdates(EventSubscriberFactory.createFeedUpdatedEventSubscriber(mUnityConfigurationProvider));
    Log.d(TAG, TAG + " finished onCreateCalled setup.");
  }

  /**
   * Call from {@link Activity#onStart()}
   * @param activity
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
   * @param activity
   */
  public void onResumeCalled(Activity activity) {
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
  }

  /**
   * Call from {@link Activity#onPause()}
   * @param activity
   */
  public void onPauseCalled(Activity activity) {
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
  }

  /**
   * Call from {@link Activity#onStop()}
   * @param activity
   */
  public void onStopCalled(Activity activity) {
    Appboy.getInstance(activity).closeSession(activity);
  }

  /**
   * Call from {@link Activity#onNewIntent(Intent)}
   * @param intent
   * @param activity
   */
  public void onNewIntentCalled(Intent intent, Activity activity) {
    // If the Activity is already open and we receive an intent to open the Activity again, we set
    // the new intent as the current one (which has the new intent extras).
    activity.setIntent(intent);
  }

  /**
   * See {@link AppboyUnityPlayerNativeActivity#logInAppMessageClick(String)}
   * @param messageJSONString
   * @param activity
   */
  public void logInAppMessageClick(String messageJSONString, Activity activity) {
    InAppMessageUtils.logInAppMessageClick(InAppMessageUtils.inAppMessageFromString(activity, messageJSONString));
  }

  /**
   * See {@link AppboyUnityPlayerNativeActivity#logInAppMessageButtonClick(String, int)}
   * @param messageJSONString
   * @param activity
   */
  public void logInAppMessageButtonClick(String messageJSONString, int buttonId, Activity activity) {
    InAppMessageUtils.logInAppMessageButtonClick(InAppMessageUtils.inAppMessageFromString(activity, messageJSONString), buttonId);
  }

  /**
   * See {@link AppboyUnityPlayerNativeActivity#logInAppMessageImpression(String)}
   * @param messageJSONString
   * @param activity
   */
  public void logInAppMessageImpression(String messageJSONString, Activity activity) {
    InAppMessageUtils.logInAppMessageImpression(InAppMessageUtils.inAppMessageFromString(activity, messageJSONString));
  }
}
