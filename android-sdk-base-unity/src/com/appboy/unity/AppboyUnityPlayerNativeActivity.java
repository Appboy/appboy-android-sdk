package com.appboy.unity;

import android.content.Intent;
import android.os.Bundle;

import com.unity3d.player.UnityPlayerNativeActivity;

/**
 * This is a wrapper subclass of the UnityPlayerNativeActivity class. It calls the necessary Appboy methods
 * to ensure that analytics are collected and that push notifications are properly forwarded to
 * the Unity application. The AppboyUnityPlayerNativeActivity will only work on devices running
 * Android OS's Gingerbread or newer and provides improvements for input handling.
 *
 * NOTE: This Activity is not compatible with Prime31 plugins. If you are using any Prime31 plugins, you
 * must use the AppboyUnityPlayerNativeActivity in the com.appboy.unity.prime31compatible package instead.
 */
public class AppboyUnityPlayerNativeActivity extends UnityPlayerNativeActivity {
  private AppboyUnityPlayerNativeActivityWrapper mAppboyUnityPlayerNativeActivityWrapper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAppboyUnityPlayerNativeActivityWrapper = new AppboyUnityPlayerNativeActivityWrapper();
    mAppboyUnityPlayerNativeActivityWrapper.onCreateCalled(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mAppboyUnityPlayerNativeActivityWrapper.onStartCalled(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mAppboyUnityPlayerNativeActivityWrapper.onResumeCalled(this);
  }

  @Override
  protected void onPause() {
    mAppboyUnityPlayerNativeActivityWrapper.onPauseCalled(this);
    super.onPause();
  }

  @Override
  protected void onStop() {
    mAppboyUnityPlayerNativeActivityWrapper.onStopCalled(this);
    super.onStop();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    mAppboyUnityPlayerNativeActivityWrapper.onNewIntentCalled(intent, this);
  }

  public void logInAppMessageClick(String messageJSONString) {
    mAppboyUnityPlayerNativeActivityWrapper.logInAppMessageClick(messageJSONString, this);
  }

  public void logInAppMessageButtonClick(String messageJSONString, int buttonId) {
    mAppboyUnityPlayerNativeActivityWrapper.logInAppMessageButtonClick(messageJSONString, buttonId, this);
  }

  public void logInAppMessageImpression(String messageJSONString) {
    mAppboyUnityPlayerNativeActivityWrapper.logInAppMessageImpression(messageJSONString, this);
  }
}
