package com.appboy;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appboy.push.NotificationTrampolineActivity;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

import java.util.Collections;
import java.util.Set;

/**
 * Can be used to automatically handle Braze lifecycle methods.
 * Optionally, openSession() and closeSession() are called on onActivityStarted and onActivityStopped respectively.
 * The InAppMessageManager methods of registerInAppMessageManager() and unregisterInAppMessageManager() can
 * be optionally called here as well.
 */
public class AppboyLifecycleCallbackListener implements Application.ActivityLifecycleCallbacks {
  private static final String TAG = AppboyLogger.getBrazeLogTag(AppboyLifecycleCallbackListener.class);

  private final boolean mRegisterInAppMessageManager;
  private final boolean mSessionHandlingEnabled;
  @NonNull
  private Set<Class<?>> mInAppMessagingRegistrationBlocklist;
  @NonNull
  private Set<Class<?>> mSessionHandlingBlocklist;

  /**
   * A default constructor equivalent to calling AppboyLifecycleCallbackListener(true, true, Collections.<Class>emptySet(), Collections.<Class>emptySet())
   */
  public AppboyLifecycleCallbackListener() {
    this(true, true, Collections.emptySet(), Collections.emptySet());
  }

  /**
   * @param sessionHandlingEnabled When true, handles calling openSession and closeSession in {@link Application.ActivityLifecycleCallbacks#onActivityStarted(Activity)} (Activity)}
   *                               and {@link Application.ActivityLifecycleCallbacks#onActivityStopped(Activity)} (Activity)} respectively.
   * @param registerInAppMessageManager When true, registers and unregisters the InAppMessageManager in
   *                                    {@link Application.ActivityLifecycleCallbacks#onActivityResumed}
   *                                    and {@link Application.ActivityLifecycleCallbacks#onActivityPaused(Activity)} respectively.
   */
  public AppboyLifecycleCallbackListener(boolean sessionHandlingEnabled, boolean registerInAppMessageManager) {
    this(sessionHandlingEnabled, registerInAppMessageManager, Collections.emptySet(), Collections.emptySet());
  }

  /**
   * Constructor that sets a blocklist for session handling and {@link AppboyInAppMessageManager} registration while also enabling both features.
   *
   * @param inAppMessagingRegistrationBlocklist A set of {@link Activity}s for which in-app message
   *                                            registration will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   * @param sessionHandlingBlocklist A set of {@link Activity}s for which session handling
   *                                 will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   */
  public AppboyLifecycleCallbackListener(@Nullable Set<Class<?>> inAppMessagingRegistrationBlocklist, @Nullable Set<Class<?>> sessionHandlingBlocklist) {
    this(true, true, inAppMessagingRegistrationBlocklist, sessionHandlingBlocklist);
  }

  /**
   * Constructor that only sets a blocklist for {@link AppboyInAppMessageManager} registration and enables
   * {@link AppboyInAppMessageManager} registration. Session handling is enabled and has an empty blocklist.
   *
   * @param inAppMessagingRegistrationBlocklist A set of {@link Activity}s for which in-app message
   *                                            registration will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   *                                            If null, an empty set is used instead.
   */
  public AppboyLifecycleCallbackListener(@Nullable Set<Class<?>> inAppMessagingRegistrationBlocklist) {
    this(true, true, inAppMessagingRegistrationBlocklist, Collections.emptySet());
  }

  /**
   * @param sessionHandlingEnabled When true, handles calling openSession and closeSession in onActivityStarted
   *                               and onActivityStopped respectively.
   * @param registerInAppMessageManager When true, registers and unregisters the {@link AppboyInAppMessageManager} in
   *                                    {@link Application.ActivityLifecycleCallbacks#onActivityResumed}
   *                                    and {@link Application.ActivityLifecycleCallbacks#onActivityPaused(Activity)} respectively.
   * @param inAppMessagingRegistrationBlocklist A set of {@link Activity}s for which in-app message
   *                                            registration will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   *                                            If null, an empty set is used instead.
   * @param sessionHandlingBlocklist A set of {@link Activity}s for which session handling
   *                                 will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   *                                 If null, an empty set is used instead.
   */
  public AppboyLifecycleCallbackListener(boolean sessionHandlingEnabled,
                                         boolean registerInAppMessageManager,
                                         @Nullable Set<Class<?>> inAppMessagingRegistrationBlocklist,
                                         @Nullable Set<Class<?>> sessionHandlingBlocklist) {
    mRegisterInAppMessageManager = registerInAppMessageManager;
    mSessionHandlingEnabled = sessionHandlingEnabled;
    mInAppMessagingRegistrationBlocklist = inAppMessagingRegistrationBlocklist != null ? inAppMessagingRegistrationBlocklist : Collections.emptySet();
    mSessionHandlingBlocklist = sessionHandlingBlocklist != null ? sessionHandlingBlocklist : Collections.emptySet();

    AppboyLogger.v(TAG, "AppboyLifecycleCallbackListener using in-app messaging blocklist: " + mInAppMessagingRegistrationBlocklist);
    AppboyLogger.v(TAG, "AppboyLifecycleCallbackListener using session handling blocklist: " + mSessionHandlingBlocklist);
  }

  /**
   * @deprecated Please use {@link #setInAppMessagingRegistrationBlocklist(Set)}
   */
  @Deprecated
  public void setInAppMessagingRegistrationBlacklist(@NonNull Set<Class<?>> blocklist) {
    setInAppMessagingRegistrationBlocklist(blocklist);
  }

  /**
   * Sets the {@link Activity#getClass()} blocklist for which in-app message registration will not occur.
   */
  public void setInAppMessagingRegistrationBlocklist(@NonNull Set<Class<?>> blocklist) {
    AppboyLogger.v(TAG, "setInAppMessagingRegistrationBlocklist called with blocklist: " + blocklist);
    mInAppMessagingRegistrationBlocklist = blocklist;
  }

  /**
   * @deprecated Please use {@link #setSessionHandlingBlocklist(Set)}
   */
  @Deprecated
  public void setSessionHandlingBlacklist(@NonNull Set<Class<?>> blocklist) {
    setSessionHandlingBlocklist(blocklist);
  }

  /**
   * Sets the {@link Activity#getClass()} blocklist for which session handling will not occur.
   */
  public void setSessionHandlingBlocklist(@NonNull Set<Class<?>> blocklist) {
    AppboyLogger.v(TAG, "setSessionHandlingBlocklist called with blocklist: " + blocklist);
    mSessionHandlingBlocklist = blocklist;
  }

  @Override
  public void onActivityStarted(@NonNull Activity activity) {
    if (mSessionHandlingEnabled && shouldHandleLifecycleMethodsInActivity(activity, true)) {
      AppboyLogger.v(TAG, "Automatically calling lifecycle method: openSession");
      Appboy.getInstance(activity.getApplicationContext()).openSession(activity);
    }
  }

  @Override
  public void onActivityStopped(@NonNull Activity activity) {
    if (mSessionHandlingEnabled && shouldHandleLifecycleMethodsInActivity(activity, true)) {
      AppboyLogger.v(TAG, "Automatically calling lifecycle method: closeSession");
      Appboy.getInstance(activity.getApplicationContext()).closeSession(activity);
    }
  }

  @Override
  public void onActivityResumed(@NonNull Activity activity) {
    if (mRegisterInAppMessageManager && shouldHandleLifecycleMethodsInActivity(activity, false)) {
      AppboyLogger.v(TAG, "Automatically calling lifecycle method: registerInAppMessageManager");
      AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityPaused(@NonNull Activity activity) {
    if (mRegisterInAppMessageManager && shouldHandleLifecycleMethodsInActivity(activity, false)) {
      AppboyLogger.v(TAG, "Automatically calling lifecycle method: unregisterInAppMessageManager");
      AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityCreated(@NonNull Activity activity, Bundle bundle) {
    if (mRegisterInAppMessageManager && shouldHandleLifecycleMethodsInActivity(activity, false)) {
      AppboyLogger.v(TAG, "Automatically calling lifecycle method: ensureSubscribedToInAppMessageEvents");
      AppboyInAppMessageManager.getInstance().ensureSubscribedToInAppMessageEvents(activity.getApplicationContext());
    }
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {}

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {}

  /**
   * Determines if this {@link Activity} should be ignored for the purposes of session tracking or in-app message registration.
   */
  private boolean shouldHandleLifecycleMethodsInActivity(Activity activity, boolean forSessionHandling) {
    Class<? extends Activity> activityClass = activity.getClass();
    if (activityClass.equals(NotificationTrampolineActivity.class)) {
      AppboyLogger.v(TAG, "Skipping all automatic registration of notification trampoline activity class");
      // Always ignore
      return false;
    }
    if (forSessionHandling) {
      return !mSessionHandlingBlocklist.contains(activityClass);
    } else {
      return !mInAppMessagingRegistrationBlocklist.contains(activityClass);
    }
  }
}
