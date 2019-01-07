package com.appboy;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appboy.push.AppboyNotificationRoutingActivity;
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
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyLifecycleCallbackListener.class);

  private final boolean mRegisterInAppMessageManager;
  private final boolean mSessionHandlingEnabled;
  @NonNull
  private Set<Class> mInAppMessagingRegistrationBlacklist;
  @NonNull
  private Set<Class> mSessionHandlingBlacklist;

  /**
   * A default constructor equivalent to calling AppboyLifecycleCallbackListener(true, true, Collections.<Class>emptySet(), Collections.<Class>emptySet())
   */
  public AppboyLifecycleCallbackListener() {
    this(true, true, Collections.<Class>emptySet(), Collections.<Class>emptySet());
  }

  /**
   * @param sessionHandlingEnabled When true, handles calling openSession and closeSession in {@link Application.ActivityLifecycleCallbacks#onActivityStarted(Activity)} (Activity)}
   *                               and {@link Application.ActivityLifecycleCallbacks#onActivityStopped(Activity)} (Activity)} respectively.
   * @param registerInAppMessageManager When true, registers and unregisters the InAppMessageManager in
   *                                    {@link Application.ActivityLifecycleCallbacks#onActivityResumed}
   *                                    and {@link Application.ActivityLifecycleCallbacks#onActivityPaused(Activity)} respectively.
   */
  public AppboyLifecycleCallbackListener(boolean sessionHandlingEnabled, boolean registerInAppMessageManager) {
    this(sessionHandlingEnabled, registerInAppMessageManager, Collections.<Class>emptySet(), Collections.<Class>emptySet());
  }

  /**
   * Constructor that sets a blacklist for session handling and {@link AppboyInAppMessageManager} registration while also enabling both features.
   *
   * @param inAppMessagingRegistrationBlacklist A set of {@link Activity}s for which in-app message
   *                                            registration will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   * @param sessionHandlingBlacklist A set of {@link Activity}s for which session handling
   *                                 will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   */
  public AppboyLifecycleCallbackListener(@Nullable Set<Class> inAppMessagingRegistrationBlacklist, @Nullable Set<Class> sessionHandlingBlacklist) {
    this(true, true, inAppMessagingRegistrationBlacklist, sessionHandlingBlacklist);
  }

  /**
   * Constructor that only sets a blacklist for {@link AppboyInAppMessageManager} registration and enables
   * {@link AppboyInAppMessageManager} registration. Session handling is enabled and has an empty blacklist.
   *
   * @param inAppMessagingRegistrationBlacklist A set of {@link Activity}s for which in-app message
   *                                            registration will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   *                                            If null, an empty set is used instead.
   */
  public AppboyLifecycleCallbackListener(@Nullable Set<Class> inAppMessagingRegistrationBlacklist) {
    this(true, true, inAppMessagingRegistrationBlacklist, Collections.<Class>emptySet());
  }

  /**
   * @param sessionHandlingEnabled When true, handles calling openSession and closeSession in onActivityStarted
   *                               and onActivityStopped respectively.
   * @param registerInAppMessageManager When true, registers and unregisters the {@link AppboyInAppMessageManager} in
   *                                    {@link Application.ActivityLifecycleCallbacks#onActivityResumed}
   *                                    and {@link Application.ActivityLifecycleCallbacks#onActivityPaused(Activity)} respectively.
   * @param inAppMessagingRegistrationBlacklist A set of {@link Activity}s for which in-app message
   *                                            registration will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   *                                            If null, an empty set is used instead.
   * @param sessionHandlingBlacklist A set of {@link Activity}s for which session handling
   *                                 will not occur. Each class should be retrieved via {@link Activity#getClass()}.
   *                                 If null, an empty set is used instead.
   */
  public AppboyLifecycleCallbackListener(boolean sessionHandlingEnabled, boolean registerInAppMessageManager,
                                         @Nullable Set<Class> inAppMessagingRegistrationBlacklist, @Nullable Set<Class> sessionHandlingBlacklist) {
    mRegisterInAppMessageManager = registerInAppMessageManager;
    mSessionHandlingEnabled = sessionHandlingEnabled;
    mInAppMessagingRegistrationBlacklist = inAppMessagingRegistrationBlacklist != null ? inAppMessagingRegistrationBlacklist : Collections.<Class>emptySet();
    mSessionHandlingBlacklist = sessionHandlingBlacklist != null ? sessionHandlingBlacklist : Collections.<Class>emptySet();

    AppboyLogger.v(TAG, "AppboyLifecycleCallbackListener using in-app messaging blacklist: " + mInAppMessagingRegistrationBlacklist);
    AppboyLogger.v(TAG, "AppboyLifecycleCallbackListener using session handling blacklist: " + mSessionHandlingBlacklist);
  }

  /**
   * Sets the {@link Activity#getClass()} blacklist for which in-app message registration will not occur.
   */
  public void setInAppMessagingRegistrationBlacklist(@NonNull Set<Class> blacklist) {
    AppboyLogger.v(TAG, "setInAppMessagingRegistrationBlacklist called with blacklist: " + blacklist);
    mInAppMessagingRegistrationBlacklist = blacklist;
  }

  /**
   * Sets the {@link Activity#getClass()} blacklist for which session handling will not occur.
   */
  public void setSessionHandlingBlacklist(@NonNull Set<Class> blacklist) {
    AppboyLogger.v(TAG, "setSessionHandlingBlacklist called with blacklist: " + blacklist);
    mSessionHandlingBlacklist = blacklist;
  }

  @Override
  public void onActivityStarted(Activity activity) {
    if (mSessionHandlingEnabled && shouldHandleLifecycleMethodsInActivity(activity, true)) {
      Appboy.getInstance(activity.getApplicationContext()).openSession(activity);
    }
  }

  @Override
  public void onActivityStopped(Activity activity) {
    if (mSessionHandlingEnabled && shouldHandleLifecycleMethodsInActivity(activity, true)) {
      Appboy.getInstance(activity.getApplicationContext()).closeSession(activity);
    }
  }

  @Override
  public void onActivityResumed(Activity activity) {
    if (mRegisterInAppMessageManager && shouldHandleLifecycleMethodsInActivity(activity, false)) {
      AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
    if (mRegisterInAppMessageManager && shouldHandleLifecycleMethodsInActivity(activity, false)) {
      AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
    }
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle bundle) {
    if (mRegisterInAppMessageManager && shouldHandleLifecycleMethodsInActivity(activity, false)) {
      AppboyInAppMessageManager.getInstance().ensureSubscribedToInAppMessageEvents(activity.getApplicationContext());
    }
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

  @Override
  public void onActivityDestroyed(Activity activity) {}

  /**
   * Determines if this {@link Activity} should be ignored for the purposes of session tracking or in-app message registration.
   */
  private boolean shouldHandleLifecycleMethodsInActivity(Activity activity, boolean forSessionHandling) {
    Class<? extends Activity> activityClass = activity.getClass();
    if (activityClass.equals(AppboyNotificationRoutingActivity.class)) {
      // Always ignore
      return false;
    }
    if (forSessionHandling) {
      return !mSessionHandlingBlacklist.contains(activityClass);
    } else {
      return !mInAppMessagingRegistrationBlacklist.contains(activityClass);
    }
  }
}
