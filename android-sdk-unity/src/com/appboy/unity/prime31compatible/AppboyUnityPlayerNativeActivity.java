package com.appboy.unity.prime31compatible;

import android.content.Intent;
import android.os.Bundle;

import com.appboy.unity.AppboyUnityPlayerNativeActivityWrapper;
import com.prime31.UnityPlayerNativeActivity;

/**
 * Classes in the com.appboy.unity.prime31compatible package provide support for Prime31 plugins. If you
 * are using any Prime31 plugins, you must use the classes in this package INSTEAD of the classes used
 * in the com.appboy.unity package.
 *
 * This is a wrapper subclass of the UnityPlayerNativeActivity class. It calls the necessary Appboy methods
 * to ensure that analytics are collected and that push notifications are properly forwarded to
 * the Unity application. The AppboyUnityPlayerNativeActivity will only work on devices running
 * Android OS's Gingerbread or newer and provides improvements for input handling.
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
  public void onResume() {
    super.onResume();
    mAppboyUnityPlayerNativeActivityWrapper.onResumeCalled(this);
  }

  @Override
  public void onPause() {
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
