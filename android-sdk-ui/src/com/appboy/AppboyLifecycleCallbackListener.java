package com.appboy;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

/**
 * Can be used on API level 14 and above to automatically handle Appboy lifecycle methods.
 * Optionally, openSession() and closeSession() are called on onActivityStarted and onActivityStopped respectively.
 * The InAppMessageManager methods of registerInAppMessageManager() and unregisterInAppMessageManager() can
 * be optionally called here as well.
 */
@TargetApi(14)
public class AppboyLifecycleCallbackListener implements Application.ActivityLifecycleCallbacks {
  private final boolean mRegisterInAppMessageManager;
  private final boolean mSessionHandlingEnabled;


  /**
   * A default constructor equivalent to calling AppboyLifecycleCallbackListener(true, true)
   */
  public AppboyLifecycleCallbackListener() {
    this(true, true);
  }

  /**
   * @param sessionHandlingEnabled When true, handles calling openSession and closeSession in onActivityStarted
   *                               and onActivityStopped respectively.
   * @param registerInAppMessageManager When true, registers and unregisters the InAppMessageManager in
   *                                    onActivityResumed and onActivityPaused respectively.
   */
  public AppboyLifecycleCallbackListener(boolean sessionHandlingEnabled, boolean registerInAppMessageManager) {
    mRegisterInAppMessageManager = registerInAppMessageManager;
    mSessionHandlingEnabled = sessionHandlingEnabled;
  }

  @Override
  public void onActivityStarted(Activity activity) {
    if (mSessionHandlingEnabled) {
      Appboy.getInstance(activity.getApplicationContext()).openSession(activity);
    }
  }

  @Override
  public void onActivityStopped(Activity activity) {
    if (mSessionHandlingEnabled) {
      Appboy.getInstance(activity.getApplicationContext()).closeSession(activity);
    }
  }

  @Override
  public void onActivityResumed(Activity activity) {
    if (mRegisterInAppMessageManager) {
      AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
    if (mRegisterInAppMessageManager) {
      AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle bundle) {}

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

  @Override
  public void onActivityDestroyed(Activity activity) {}
}
