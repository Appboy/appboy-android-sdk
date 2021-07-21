package com.appboy.unity;

import android.content.Intent;
import android.os.Bundle;

import com.unity3d.player.UnityPlayerActivity;

/**
 * This is a wrapper subclass of the {@link com.unity3d.player.UnityPlayerActivity} class. It calls the necessary Braze methods
 * to ensure that analytics are collected and that push notifications are properly forwarded to
 * the Unity application.
 *
 * NOTE: This Activity is not compatible with Prime31 plugins. If you are using any Prime31 plugins, you
 * must use the {@link AppboyUnityPlayerActivity} in the com.appboy.unity.prime31compatible package instead.
 */
public class AppboyUnityPlayerActivity extends UnityPlayerActivity {
  private AppboyUnityActivityWrapper mAppboyUnityActivityWrapper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAppboyUnityActivityWrapper = new AppboyUnityActivityWrapper();
    mAppboyUnityActivityWrapper.onCreateCalled(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mAppboyUnityActivityWrapper.onStartCalled(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    mAppboyUnityActivityWrapper.onResumeCalled(this);
  }

  @Override
  public void onPause() {
    mAppboyUnityActivityWrapper.onPauseCalled(this);
    super.onPause();
  }

  @Override
  protected void onStop() {
    mAppboyUnityActivityWrapper.onStopCalled(this);
    super.onStop();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    mAppboyUnityActivityWrapper.onNewIntentCalled(intent, this);
  }

  public void onNewUnityInAppMessageManagerAction(int actionEnumValue) {
    mAppboyUnityActivityWrapper.onNewUnityInAppMessageManagerAction(actionEnumValue);
  }

  public void launchContentCardsActivity() {
    mAppboyUnityActivityWrapper.launchContentCardsActivity(this);
  }
}
