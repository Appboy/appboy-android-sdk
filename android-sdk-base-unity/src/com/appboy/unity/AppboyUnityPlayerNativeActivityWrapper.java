package com.appboy.unity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.unity.configuration.UnityConfigurationProvider;
import com.appboy.unity.utils.InAppMessageUtils;
import com.unity3d.player.UnityPlayerNativeActivity;

/**
 * This class allows UnityPlayerNativeActivity and subclasses instances to gain AppboyUnityPlayerNativeActivity
 * functionality by calling appropriate methods during each phase of the Android Activity lifecycle.
 */
public class AppboyUnityPlayerNativeActivityWrapper {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyUnityPlayerNativeActivityWrapper.class.getName());

  private UnityConfigurationProvider mUnityConfigurationProvider;

  /**
   * Call from {@link Activity#onCreate(Bundle)}
   * @param unityPlayerNativeActivity
   */
  public void onCreateCalled(UnityPlayerNativeActivity unityPlayerNativeActivity) {
    mUnityConfigurationProvider = new UnityConfigurationProvider(unityPlayerNativeActivity);
    Appboy.getInstance(unityPlayerNativeActivity).subscribeToNewInAppMessages(EventSubscriberFactory.createInAppMessageEventSubscriber(mUnityConfigurationProvider));
    Appboy.getInstance(unityPlayerNativeActivity).subscribeToFeedUpdates(EventSubscriberFactory.createFeedUpdatedEventSubscriber(mUnityConfigurationProvider));
    Log.d(TAG, String.format("%s finished onCreateCalled setup.", AppboyUnityPlayerNativeActivityWrapper.class.getSimpleName()));
  }

  /**
   * Call from {@link Activity#onStart()}
   * @param unityPlayerNativeActivity
   */
  public void onStartCalled(UnityPlayerNativeActivity unityPlayerNativeActivity) {
    Appboy.getInstance(unityPlayerNativeActivity).openSession(unityPlayerNativeActivity);
    if (!mUnityConfigurationProvider.getShowInAppMessagesAutomaticallyKey()) {
      AppboyUnityNativeInAppMessageManagerListener.getInstance().setShowInAppMessagesManually(true);
      Log.i(TAG, "In-app message display will be handled manually.");
    }
    AppboyInAppMessageManager.getInstance().setCustomInAppMessageManagerListener(AppboyUnityNativeInAppMessageManagerListener.getInstance());
    AppboyUnityNativeInAppMessageManagerListener.getInstance().registerContainerActivity(unityPlayerNativeActivity);
    Log.d(TAG, String.format("Starting %s.", AppboyUnityPlayerNativeActivityWrapper.class.getSimpleName()));
  }

  /**
   * Call from {@link Activity#onResume()}
   * @param unityPlayerNativeActivity
   */
  public void onResumeCalled(UnityPlayerNativeActivity unityPlayerNativeActivity) {
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(unityPlayerNativeActivity);
  }

  /**
   * Call from {@link Activity#onPause()}
   * @param unityPlayerNativeActivity
   */
  public void onPauseCalled(UnityPlayerNativeActivity unityPlayerNativeActivity) {
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(unityPlayerNativeActivity);
  }

  /**
   * Call from {@link Activity#onStop()}
   * @param unityPlayerNativeActivity
   */
  public void onStopCalled(UnityPlayerNativeActivity unityPlayerNativeActivity) {
    Appboy.getInstance(unityPlayerNativeActivity).closeSession(unityPlayerNativeActivity);
  }

  /**
   * Call from {@link Activity#onNewIntent(Intent)}
   * @param intent
   * @param unityPlayerNativeActivity
   */
  public void onNewIntentCalled(Intent intent, UnityPlayerNativeActivity unityPlayerNativeActivity) {
    // If the Activity is already open and we receive an intent to open the Activity again, we set
    // the new intent as the current one (which has the new intent extras).
    unityPlayerNativeActivity.setIntent(intent);
  }

  /**
   * See {@link AppboyUnityPlayerNativeActivity#logInAppMessageClick(String)}
   * @param messageJSONString
   * @param unityPlayerNativeActivity
   */
  public void logInAppMessageClick(String messageJSONString, UnityPlayerNativeActivity unityPlayerNativeActivity) {
    InAppMessageUtils.logInAppMessageClick(InAppMessageUtils.inAppMessageFromString(unityPlayerNativeActivity, messageJSONString));
  }

  /**
   * See {@link AppboyUnityPlayerNativeActivity#logInAppMessageButtonClick(String, int)}
   * @param messageJSONString
   * @param unityPlayerNativeActivity
   */
  public void logInAppMessageButtonClick(String messageJSONString, int buttonId, UnityPlayerNativeActivity unityPlayerNativeActivity) {
    InAppMessageUtils.logInAppMessageButtonClick(InAppMessageUtils.inAppMessageFromString(unityPlayerNativeActivity, messageJSONString), buttonId);
  }

  /**
   * See {@link AppboyUnityPlayerNativeActivity#logInAppMessageImpression(String)}
   * @param messageJSONString
   * @param unityPlayerNativeActivity
   */
  public void logInAppMessageImpression(String messageJSONString, UnityPlayerNativeActivity unityPlayerNativeActivity) {
    InAppMessageUtils.logInAppMessageImpression(InAppMessageUtils.inAppMessageFromString(unityPlayerNativeActivity, messageJSONString));
  }
}
