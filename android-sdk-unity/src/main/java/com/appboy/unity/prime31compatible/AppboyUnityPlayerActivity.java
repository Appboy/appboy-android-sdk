package com.appboy.unity.prime31compatible;

import android.content.Intent;
import android.os.Bundle;

import com.appboy.unity.BrazeUnityActivityWrapper;
import com.prime31.UnityPlayerActivity;

/**
 * Classes in the com.appboy.unity.prime31compatible package provide support for Prime31 plugins. If you
 * are using any Prime31 plugins, you must use the classes in this package INSTEAD of the classes used
 * in the com.appboy.unity package.
 *
 * This is a wrapper subclass of the {@link UnityPlayerActivity} class. It calls the necessary Braze methods
 * to ensure that analytics are collected and that push notifications are properly forwarded to
 * the Unity application.
 */
public class AppboyUnityPlayerActivity extends UnityPlayerActivity {
  private BrazeUnityActivityWrapper mBrazeUnityActivityWrapper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBrazeUnityActivityWrapper = new BrazeUnityActivityWrapper();
    mBrazeUnityActivityWrapper.onCreateCalled(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mBrazeUnityActivityWrapper.onStartCalled(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    mBrazeUnityActivityWrapper.onResumeCalled(this);
  }

  @Override
  public void onPause() {
    mBrazeUnityActivityWrapper.onPauseCalled(this);
    super.onPause();
  }

  @Override
  protected void onStop() {
    mBrazeUnityActivityWrapper.onStopCalled(this);
    super.onStop();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    mBrazeUnityActivityWrapper.onNewIntentCalled(intent, this);
  }

  public void onNewUnityInAppMessageManagerAction(int actionEnumValue) {
    mBrazeUnityActivityWrapper.onNewUnityInAppMessageManagerAction(actionEnumValue);
  }

  public void launchContentCardsActivity() {
    mBrazeUnityActivityWrapper.launchContentCardsActivity(this);
  }

  public void setInAppMessageListener() {
    mBrazeUnityActivityWrapper.setInAppMessageListener();
  }
}
