package com.appboy.unity;

import android.app.Activity;
import android.content.Intent;

import com.appboy.Constants;
import com.appboy.models.IInAppMessage;
import com.appboy.models.MessageButton;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.appboy.ui.inappmessage.InAppMessageCloser;
import com.appboy.ui.inappmessage.InAppMessageOperation;
import com.appboy.ui.inappmessage.listeners.IInAppMessageManagerListener;
import com.unity3d.player.UnityPlayerNativeActivity;

public class AppboyUnityNativeInAppMessageManagerListener implements IInAppMessageManagerListener {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyUnityNativeInAppMessageManagerListener.class.getName());
  private static volatile AppboyUnityNativeInAppMessageManagerListener sInstance;
  private UnityPlayerNativeActivity mContainerActivity;
  private AppboyOverlayActivity mOverlayActivity;
  private boolean mShowInAppMessagesManually = false;
  private IAppboyUnityInAppMessageListener mAppboyUnityInAppMessageListener;

  private AppboyUnityNativeInAppMessageManagerListener() {
  }

  public static AppboyUnityNativeInAppMessageManagerListener getInstance() {
    if (sInstance == null) {
      synchronized (AppboyUnityNativeInAppMessageManagerListener.class) {
        if (sInstance == null) {
          sInstance = new AppboyUnityNativeInAppMessageManagerListener();
        }
      }
    }
    return sInstance;
  }

  /**
   * Assigns a custom IAppboyUnityInAppMessageListener that will be used when displaying in-app messages.
   * To un-assign your listener, call this method again with a null listener.
   *
   * @param appboyUnityInAppMessageListener A custom IAppboyUnityInAppMessageListener or null (to un-assign
   *                                        your listener).
   */
  public void setUnityInAppMessageListener(IAppboyUnityInAppMessageListener appboyUnityInAppMessageListener) {
    mAppboyUnityInAppMessageListener = appboyUnityInAppMessageListener;
  }

  /**
   * Call to register a container activity (the game activity) that will be returned to
   * after the in-app message displays.
   *
   * @param containerActivity a UnityPlayerNativeActivity instance
   */
  public void registerContainerActivity(UnityPlayerNativeActivity containerActivity) {
    mContainerActivity = containerActivity;
  }

  /**
   * The overlay activity instance must register with AppboyUnityNativeInAppMessageManagerListener once
   * it is created.  This is because we need to have an {@link AppboyOverlayActivity} instance registered before
   * we request the display of an in-app message, to ensure the in-app message will use it.
   *
   * We then call {@link AppboyInAppMessageManager#requestDisplayInAppMessage()} to trigger display
   * of the in-app message from within the {@link AppboyOverlayActivity}
   *
   * See {@link #beforeInAppMessageDisplayed(IInAppMessage)}
   *
   * @param overlayActivity an AppboyOverlayActivity instance
   */
  public void registerOverlayActivityAndRequestDisplay(AppboyOverlayActivity overlayActivity) {
    if (mContainerActivity == null) {
      AppboyLogger.w(TAG, "No container activity is registered.  You must register a container activity "
          + "before registering or un-registering an overlay activity.");
      finishOverlayActivity();
      return;
    }
    mOverlayActivity = overlayActivity;
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(mOverlayActivity);
    AppboyInAppMessageManager.getInstance().requestDisplayInAppMessage();
  }

  /**
   * Unregister the overlay activity instance with AppboyUnityNativeInAppMessageManagerListener once
   * it is being destroyed.
   *
   * Re-register the container activity.
   */
  public void unregisterOverlayActivityAndReRegisterContainer() {
    if (mContainerActivity == null) {
      AppboyLogger.w(TAG, "No container activity is registered.  You must register a container activity "
          + "before registering or un-registering an overlay activity.");
      finishOverlayActivity();
      return;
    }
    AppboyInAppMessageManager.getInstance().hideCurrentlyDisplayingInAppMessage(false);
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(mOverlayActivity);
    if (mContainerActivity != null) {
      AppboyInAppMessageManager.getInstance().registerInAppMessageManager(mContainerActivity);
    }
    finishOverlayActivity();
  }

  /**
   * See {@link IInAppMessageManagerListener#onInAppMessageReceived(IInAppMessage)}
   */
  @Override
  public boolean onInAppMessageReceived(IInAppMessage inAppMessage) {
    if (mAppboyUnityInAppMessageListener != null) {
      return mAppboyUnityInAppMessageListener.onInAppMessageReceived(inAppMessage);
    }
    return mShowInAppMessagesManually;
  }

  /**
   * If mOverlayActivity is null, we create a new {@link AppboyOverlayActivity}. The new activity
   * will automatically register itself via {@link #registerOverlayActivityAndRequestDisplay(AppboyOverlayActivity)}.  At that
   * point, display will be re-requested.
   *
   * At this point, mOverlayActivity should no longer be null, and we display the in-app message
   * on mOverlayActivity.
   *
   * {@link IInAppMessage#setAnimateIn(boolean)} is set to false because activities already have
   * their own animation.
   *
   * See {@link IInAppMessageManagerListener#beforeInAppMessageDisplayed(IInAppMessage)}
   */
  @Override
  public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
    if (mContainerActivity == null) {
      AppboyLogger.w(TAG, "No container activity is registered.  You must register a container activity "
          + "before an in-app message can be displayed.");
      return InAppMessageOperation.DISPLAY_LATER;
    }
    if (mOverlayActivity != null) {
      inAppMessage.setAnimateIn(false);
      return InAppMessageOperation.DISPLAY_NOW;
    } else {
      startOverlayActivity();
      return InAppMessageOperation.DISPLAY_LATER;
    }
  }

  /**
   * See {@link IInAppMessageManagerListener#onInAppMessageClicked(IInAppMessage, InAppMessageCloser)}
   */
  @Override
  public boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
    inAppMessageCloser.close(true);
    finishOverlayActivity();
    if (mAppboyUnityInAppMessageListener != null) {
      return mAppboyUnityInAppMessageListener.onInAppMessageClicked(inAppMessage);
    }
    return false;
  }

  /**
   * See {@link IInAppMessageManagerListener#onInAppMessageButtonClicked(MessageButton, InAppMessageCloser)}
   */
  @Override
  public boolean onInAppMessageButtonClicked(MessageButton button, InAppMessageCloser inAppMessageCloser) {
    inAppMessageCloser.close(true);
    finishOverlayActivity();
    if (mAppboyUnityInAppMessageListener != null) {
      return mAppboyUnityInAppMessageListener.onInAppMessageButtonClicked(button);
    }
    return false;
  }

  /**
   * See {@link IInAppMessageManagerListener#onInAppMessageDismissed(IInAppMessage)}
   */
  @Override
  public void onInAppMessageDismissed(IInAppMessage inAppMessage) {
    finishOverlayActivity();
  }

  /**
   * Allows the client to specify that they will handle the display of in-app messages
   * themselves.  If set, Appboy will not handle the display of incoming in-app messages.
   *
   * @param showInAppMessagesManually whether the client will handle in-app messages
   *                                  themselves.
   */
  void setShowInAppMessagesManually(boolean showInAppMessagesManually) {
    mShowInAppMessagesManually = showInAppMessagesManually;
  }

  /**
   * Finish and null out mOverlayActivity if available.  This is called whenever an in-app message
   * is closed, ensuring that we don't leak mOverlayActivity.
   *
   * See {@link Activity#finish()}
   */
  private void finishOverlayActivity() {
    if (mOverlayActivity != null) {
      mOverlayActivity.finish();
      mOverlayActivity = null;
    }
  }

  /**
   * Start a new Overlay activity and assign it to mOverlayActivity.
   *
   * mContainerActivity may not be null.
   */
  private void startOverlayActivity() {
    Intent overlayIntent = new Intent(mContainerActivity, AppboyOverlayActivity.class);
    mContainerActivity.startActivity(overlayIntent);
  }
}
