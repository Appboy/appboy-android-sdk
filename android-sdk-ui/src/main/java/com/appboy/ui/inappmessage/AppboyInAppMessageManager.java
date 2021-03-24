package com.appboy.ui.inappmessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.appboy.Appboy;
import com.appboy.configuration.AppboyConfigurationProvider;
import com.appboy.enums.inappmessage.InAppMessageFailureType;
import com.appboy.enums.inappmessage.Orientation;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.InAppMessageEvent;
import com.appboy.models.IInAppMessage;
import com.appboy.models.InAppMessageImmersiveBase;
import com.appboy.support.AppboyLogger;
import com.appboy.support.JsonUtils;
import com.appboy.ui.inappmessage.listeners.AppboyInAppMessageViewLifecycleListener;
import com.appboy.ui.inappmessage.listeners.IInAppMessageManagerListener;
import com.appboy.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;
import com.appboy.ui.inappmessage.views.AppboyInAppMessageHtmlBaseView;
import com.appboy.ui.support.ViewUtils;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The AppboyInAppMessageManager is used to display in-app messages that are either sent down from Appboy
 * or are created natively in the host app. It will only show one in-app message at a time and will
 * place all other in-app messages onto a stack. The AppboyInAppMessageManager will also keep track of in-app
 * impressions and clicks, which can be viewed on the dashboard.
 * <p/>
 * When an in-app message is received from Appboy, the
 * {@link IInAppMessageManagerListener#onInAppMessageReceived(com.appboy.models.IInAppMessage)}
 * method is called (if set). If this method returns true, that signals to the AppboyInAppMessageManager that
 * the in-app message will be handled by the host app and that it should not be displayed by the
 * AppboyInAppMessageManager. This method should be used if you choose to display the in-app message in a custom
 * way. If false is returned, the AppboyInAppMessageManager attempts to display the in-app message.
 * <p/>
 * If there is already an in-app message being displayed, the new in-app message will be put onto the top of the
 * stack and can be displayed at a later time. If there is no in-app message being displayed, then the
 * {@link IInAppMessageManagerListener#beforeInAppMessageDisplayed(com.appboy.models.IInAppMessage)}
 * will be called. The {@link InAppMessageOperation} return value can be used to
 * control when the in-app message should be displayed. A suggested usage of this method would be to delay
 * in-app message messages in certain parts of the app by returning {@link InAppMessageOperation#DISPLAY_LATER}
 * when in-app message would be distracting to the users app experience. If the method returns
 * {@link InAppMessageOperation#DISPLAY_NOW} then the in-app message will be displayed
 * immediately.
 * <p/>
 * The {@link IInAppMessageManagerListener#onInAppMessageClicked(com.appboy.models.IInAppMessage, InAppMessageCloser)}
 * and {@link IInAppMessageManagerListener#onInAppMessageDismissed(com.appboy.models.IInAppMessage)}
 * methods can be used to override the default click and dismiss behavior.
 * <p/>
 * By default, in-app messages fade in and out from view. The slideup type of in-app message slides in and out of view
 * can be dismissed by swiping the view horizontally. If the in-app message's DismissType is set to AUTO_DISMISS,
 * then the in-app message will animate out of view once the set duration time has elapsed.
 * <p/>
 * The default view used to display slideup, modal, and full in-app messages
 * is defined by res/layout/com_appboy_inappmessage_*.xml, where * is the message type. In
 * order to use a custom view, you must create a custom view factory using the
 * {@link AppboyInAppMessageManager#setCustomInAppMessageViewFactory(IInAppMessageViewFactory inAppMessageViewFactory)} method.
 * <p/>
 * A new in-app message {@link android.view.View} object is created when a in-app message is displayed and also
 * when the user navigates away to another {@link android.app.Activity}. This happens so that the
 * Activity can be garbage collected and does not create a memory leak. For that reason, the
 * {@link AppboyInAppMessageManager#registerInAppMessageManager(android.app.Activity)}
 * and {@link AppboyInAppMessageManager#unregisterInAppMessageManager(android.app.Activity)}
 * must be called in the {@link android.app.Activity#onResume()} and {@link android.app.Activity#onPause()}
 * methods of every Activity.
 */
// Static field leak doesn't apply to this singleton since the activity is nullified after the manager is unregistered.
@SuppressLint("StaticFieldLeak")
public final class AppboyInAppMessageManager extends AppboyInAppMessageManagerBase {
  private static final String TAG = AppboyLogger.getBrazeLogTag(AppboyInAppMessageManager.class);
  private static volatile AppboyInAppMessageManager sInstance = null;

  @NonNull
  private final Stack<IInAppMessage> mInAppMessageStack = new Stack<>();
  private final IInAppMessageViewLifecycleListener mInAppMessageViewLifecycleListener = new AppboyInAppMessageViewLifecycleListener();
  private final AtomicBoolean mDisplayingInAppMessage = new AtomicBoolean(false);

  @Nullable
  private IEventSubscriber<InAppMessageEvent> mInAppMessageEventSubscriber;
  @Nullable
  private IInAppMessage mCarryoverInAppMessage;
  @Nullable
  private IInAppMessage mUnregisteredInAppMessage;
  @Nullable
  private Integer mOriginalOrientation;
  @Nullable
  private AppboyConfigurationProvider mAppboyConfigurationProvider;
  @Nullable
  private IInAppMessageViewWrapper mInAppMessageViewWrapper;

  public static AppboyInAppMessageManager getInstance() {
    if (sInstance == null) {
      synchronized (AppboyInAppMessageManager.class) {
        if (sInstance == null) {
          sInstance = new AppboyInAppMessageManager();
        }
      }
    }
    return sInstance;
  }

  /**
   * Ensures the InAppMessageManager is subscribed in-app message events if not already subscribed.
   * Before this method gets called, the InAppMessageManager is not subscribed to in-app message events
   * and cannot display them. Every call to registerInAppMessageManager() calls this method.
   * <p/>
   * If events with triggers are logged before the first call to registerInAppMessageManager(), then the
   * corresponding in-app message won't display. Thus, if logging events with triggers before the first call
   * to registerInAppMessageManager(), then call this method to ensure that in-app message events
   * are correctly handled by the AppboyInAppMessageManager.
   * <p/>
   * For example, if logging custom events with triggers in your first activity's onCreate(), be sure
   * to call this method manually beforehand so that the in-app message will get displayed by the time
   * registerInAppMessageManager() gets called.
   *
   * @param context The application context
   */
  public void ensureSubscribedToInAppMessageEvents(Context context) {
    if (mInAppMessageEventSubscriber != null) {
      AppboyLogger.d(TAG, "Removing existing in-app message event subscriber before subscribing new one.");
      Appboy.getInstance(context).removeSingleSubscription(mInAppMessageEventSubscriber, InAppMessageEvent.class);
    }
    AppboyLogger.d(TAG, "Subscribing in-app message event subscriber");
    mInAppMessageEventSubscriber = createInAppMessageEventSubscriber();
    Appboy.getInstance(context).subscribeToNewInAppMessages(mInAppMessageEventSubscriber);
  }

  /**
   * Registers the in-app message manager, which will listen to and display incoming in-app messages. The
   * current Activity is required in order to properly inflate and display the in-app message view.
   * <p/>
   * Important note: Every Activity must call registerInAppMessageManager in the onResume lifecycle
   * method, otherwise in-app messages may be lost!
   * <p/>
   * This method also calls {@link AppboyInAppMessageManager#ensureSubscribedToInAppMessageEvents(Context)}.
   * To be sure that no in-app messages are lost, you should call {@link AppboyInAppMessageManager#ensureSubscribedToInAppMessageEvents(Context)} as early
   * as possible in your app, preferably in your {@link Application#onCreate()}.
   * @param activity The current Activity.
   */
  public void registerInAppMessageManager(Activity activity) {
    if (activity == null) {
      AppboyLogger.w(TAG, "Null Activity passed to registerInAppMessageManager. Doing nothing");
      return;
    } else {
      AppboyLogger.v(TAG, "Registering InAppMessageManager with activity: " + activity.getLocalClassName());
    }

    // We need the current Activity so that we can inflate or programmatically create the in-app message
    // View for each Activity. We cannot share the View because doing so would create a memory leak.
    mActivity = activity;
    if (mApplicationContext == null) {
      // Note, because this class is a singleton and doesn't have any dependencies passed in,
      // we cache the application context here because it's not available (as it normally would be
      // from Braze initialization).
      mApplicationContext = mActivity.getApplicationContext();
    }
    if (mAppboyConfigurationProvider == null) {
      mAppboyConfigurationProvider = new AppboyConfigurationProvider(mApplicationContext);
    }

    // We have a special check to see if the host app switched to a different Activity (or recreated
    // the same Activity during an orientation change) so that we can redisplay the in-app message.
    if (mCarryoverInAppMessage != null) {
      AppboyLogger.d(TAG, "Requesting display of carryover in-app message.");
      mCarryoverInAppMessage.setAnimateIn(false);
      displayInAppMessage(mCarryoverInAppMessage, true);
      mCarryoverInAppMessage = null;
    } else if (mUnregisteredInAppMessage != null) {
      AppboyLogger.d(TAG, "Adding previously unregistered in-app message.");
      addInAppMessage(mUnregisteredInAppMessage);
      mUnregisteredInAppMessage = null;
    }

    ensureSubscribedToInAppMessageEvents(mApplicationContext);
  }

  /**
   * Unregisters the in-app message manager.
   *
   * @param activity The current Activity.
   */
  public void unregisterInAppMessageManager(Activity activity) {
    if (activity == null) {
      // The activity is not needed to unregister so we can continue unregistration with it being null.
      AppboyLogger.w(TAG, "Null Activity passed to unregisterInAppMessageManager.");
    } else {
      AppboyLogger.v(TAG, "Unregistering InAppMessageManager from activity: " + activity.getLocalClassName());
    }

    // If there is an in-app message being displayed when the host app transitions to another Activity (or
    // requests an orientation change), we save it in memory so that we can redisplay it when the
    // operation is done.
    if (mInAppMessageViewWrapper != null) {
      final View inAppMessageView = mInAppMessageViewWrapper.getInAppMessageView();
      if (inAppMessageView instanceof AppboyInAppMessageHtmlBaseView) {
        AppboyLogger.d(TAG, "In-app message view includes HTML. Removing the page finished listener.");
        final AppboyInAppMessageHtmlBaseView appboyInAppMessageHtmlBaseView = (AppboyInAppMessageHtmlBaseView) inAppMessageView;
        appboyInAppMessageHtmlBaseView.setHtmlPageFinishedListener(null);
      }
      ViewUtils.removeViewFromParent(inAppMessageView);

      // Only continue if we're not animating a close
      if (mInAppMessageViewWrapper.getIsAnimatingClose()) {
        // Note that mInAppMessageViewWrapper may be null after this call
        mInAppMessageViewLifecycleListener.afterClosed(mInAppMessageViewWrapper.getInAppMessage());
        mCarryoverInAppMessage = null;
      } else {
        mCarryoverInAppMessage = mInAppMessageViewWrapper.getInAppMessage();
      }

      mInAppMessageViewWrapper = null;
    } else {
      mCarryoverInAppMessage = null;
    }

    mActivity = null;
    mDisplayingInAppMessage.set(false);
  }

  /**
   * Provides a in-app message that will then be handled by the in-app message manager. If no in-app message is being
   * displayed, it will attempt to display the in-app message immediately.
   *
   * @param inAppMessage The in-app message to add.
   */
  public void addInAppMessage(IInAppMessage inAppMessage) {
    mInAppMessageStack.push(inAppMessage);
    requestDisplayInAppMessage();
  }

  /**
   * Asks the InAppMessageManager to display the next in-app message if one is not currently being displayed.
   * If one is being displayed, this method will return false and will not display the next in-app message.
   *
   * @return A boolean value indicating whether an asynchronous task to display the in-app message display was executed.
   */
  public boolean requestDisplayInAppMessage() {
    try {
      if (mActivity == null) {
        if (!mInAppMessageStack.empty()) {
          AppboyLogger.w(TAG, "No activity is currently registered to receive in-app messages. "
              + "Saving in-app message as unregistered "
              + "in-app message. It will automatically be displayed when the next activity "
              + "registers to receive in-app messages.");
          mUnregisteredInAppMessage = mInAppMessageStack.pop();
        } else {
          AppboyLogger.d(TAG, "No activity is currently registered to receive in-app messages and the "
              + "in-app message stack is empty. Doing nothing.");
        }
        return false;
      }
      if (mDisplayingInAppMessage.get()) {
        AppboyLogger.d(TAG, "A in-app message is currently being displayed. Ignoring request to display in-app message.");
        return false;
      }
      if (mInAppMessageStack.isEmpty()) {
        AppboyLogger.d(TAG, "The in-app message stack is empty. No in-app message will be displayed.");
        return false;
      }

      final IInAppMessage inAppMessage = mInAppMessageStack.pop();
      InAppMessageOperation inAppMessageOperation;

      if (!inAppMessage.isControl()) {
        inAppMessageOperation = getInAppMessageManagerListener().beforeInAppMessageDisplayed(inAppMessage);
      } else {
        AppboyLogger.d(TAG, "Using the control in-app message manager listener.");
        inAppMessageOperation = getControlInAppMessageManagerListener().beforeInAppMessageDisplayed(inAppMessage);
      }

      switch (inAppMessageOperation) {
        case DISPLAY_NOW:
          AppboyLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_NOW. The "
              + "in-app message will be displayed.");
          break;
        case DISPLAY_LATER:
          AppboyLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_LATER. The "
              + "in-app message will be pushed back onto the stack.");
          mInAppMessageStack.push(inAppMessage);
          return false;
        case DISCARD:
          AppboyLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISCARD. The "
              + "in-app message will not be displayed and will not be put back on the stack.");
          return false;
        default:
          AppboyLogger.w(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned null instead of a "
              + "InAppMessageOperation. Ignoring the in-app message. Please check the IInAppMessageStackBehaviour "
              + "implementation.");
          return false;
      }

      Handler mainLooperHandler = new Handler(mActivity.getMainLooper());
      BackgroundInAppMessagePreparer.prepareInAppMessageForDisplay(mainLooperHandler, inAppMessage);
      return true;
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Error running requestDisplayInAppMessage", e);
      return false;
    }
  }

  /**
   * Hides any currently displaying in-app message. Note that in-app message animation
   * is configurable on the in-app message model itself and should be configured there.
   *
   * @param dismissed whether the message was dismissed by the user. If dismissed is true,
   *                  IInAppMessageViewLifecycleListener.onDismissed() will be called on the current
   *                  IInAppMessageViewLifecycleListener.
   */
  public void hideCurrentlyDisplayingInAppMessage(boolean dismissed) {
    IInAppMessageViewWrapper inAppMessageWrapperView = mInAppMessageViewWrapper;
    if (inAppMessageWrapperView != null) {
      if (dismissed) {
        mInAppMessageViewLifecycleListener.onDismissed(inAppMessageWrapperView.getInAppMessageView(),
            inAppMessageWrapperView.getInAppMessage());
      }
      inAppMessageWrapperView.close();
    }
  }

  /**
   * Resets the {@link AppboyInAppMessageManager} to its original state before the last in-app message
   * was displayed. This allows for a new in-app message to be displayed after calling this method.
   * {@link ViewUtils#setActivityRequestedOrientation(Activity, int)} is called with the original
   * orientation before the last in-app message was displayed.
   */
  public void resetAfterInAppMessageClose() {
    AppboyLogger.v(TAG, "Resetting after in-app message close.");
    mInAppMessageViewWrapper = null;
    mDisplayingInAppMessage.set(false);
    if (mActivity != null && mOriginalOrientation != null) {
      AppboyLogger.d(TAG, "Setting requested orientation to original orientation " + mOriginalOrientation);
      ViewUtils.setActivityRequestedOrientation(mActivity, mOriginalOrientation);
      mOriginalOrientation = null;
    }
  }

  /**
   * Gets whether an in-app message is currently displaying on the device.
   */
  public boolean getIsCurrentlyDisplayingInAppMessage() {
    return mDisplayingInAppMessage.get();
  }

  /**
   * The stack of In-App Messages waiting to be displayed.
   */
  @NonNull
  public Stack<IInAppMessage> getInAppMessageStack() {
    return mInAppMessageStack;
  }

  /**
   * An In-App Message being carried over during the
   * {@link #unregisterInAppMessageManager(Activity)}
   * {@link #registerInAppMessageManager(Activity)} transition.
   */
  @Nullable
  public IInAppMessage getCarryoverInAppMessage() {
    return mCarryoverInAppMessage;
  }

  /**
   * An In-App Message that could not display after a
   * call to {@link #requestDisplayInAppMessage()} due to no
   * {@link Activity} being registered via {@link #registerInAppMessageManager(Activity)}
   */
  @Nullable
  public IInAppMessage getUnregisteredInAppMessage() {
    return mUnregisteredInAppMessage;
  }

  /**
   * Attempts to display an {@link IInAppMessage} to the user.
   *
   * @param inAppMessage The {@link IInAppMessage}.
   * @param isCarryOver If this {@link IInAppMessage} is "carried over" from an {@link Activity} transition.
   */
  void displayInAppMessage(IInAppMessage inAppMessage, boolean isCarryOver) {
    AppboyLogger.v(TAG, "Attempting to display in-app message with payload: " + JsonUtils.getPrettyPrintedString(inAppMessage.forJsonPut()));

    // Note: for mDisplayingInAppMessage to be accurate it requires this method does not exit anywhere but the at the end
    // of this try/catch when we know whether we are successfully displaying the in-app message or not.
    if (!mDisplayingInAppMessage.compareAndSet(false, true)) {
      AppboyLogger.d(TAG, "A in-app message is currently being displayed. Adding in-app message back on the stack.");
      mInAppMessageStack.push(inAppMessage);
      return;
    }

    try {
      if (mActivity == null) {
        mCarryoverInAppMessage = inAppMessage;
        throw new Exception("No Activity is currently registered to receive in-app messages. Registering in-app message as carry-over "
            + "in-app message. It will automatically be displayed when the next Activity registers to receive in-app messages.");
      }
      if (!isCarryOver) {
        long inAppMessageExpirationTimestamp = inAppMessage.getExpirationTimestamp();
        if (inAppMessageExpirationTimestamp > 0) {
          long currentTimeMillis = System.currentTimeMillis();
          if (currentTimeMillis > inAppMessageExpirationTimestamp) {
            throw new Exception("In-app message is expired. Doing nothing. Expiration: $"
                + inAppMessageExpirationTimestamp + ". Current time: " + currentTimeMillis);
          }
        } else {
          AppboyLogger.d(TAG, "Expiration timestamp not defined. Continuing.");
        }
      } else {
        AppboyLogger.d(TAG, "Not checking expiration status for carry-over in-app message.");
      }
      if (!verifyOrientationStatus(inAppMessage)) {
        // No display failure gets logged here since control in-app messages would also be affected.
        throw new Exception("Current orientation did not match specified orientation for in-app message. Doing nothing.");
      }

      // At this point, the only factors that would inhibit in-app message display are view creation issues.
      // Since control in-app messages have no view, this is the end of execution for control in-app messages
      if (inAppMessage.isControl()) {
        AppboyLogger.d(TAG, "Not displaying control in-app message. Logging impression and ending display execution.");
        inAppMessage.logImpression();
        resetAfterInAppMessageClose();
        return;
      }

      IInAppMessageViewFactory inAppMessageViewFactory = getInAppMessageViewFactory(inAppMessage);
      if (inAppMessageViewFactory == null) {
        inAppMessage.logDisplayFailure(InAppMessageFailureType.DISPLAY_VIEW_GENERATION);
        throw new Exception("ViewFactory from getInAppMessageViewFactory was null.");
      }
      final View inAppMessageView = inAppMessageViewFactory.createInAppMessageView(mActivity, inAppMessage);

      if (inAppMessageView == null) {
        inAppMessage.logDisplayFailure(InAppMessageFailureType.DISPLAY_VIEW_GENERATION);
        throw new Exception("The in-app message view returned from the IInAppMessageViewFactory was null. The in-app message will "
            + "not be displayed and will not be put back on the stack.");
      }

      if (inAppMessageView.getParent() != null) {
        inAppMessage.logDisplayFailure(InAppMessageFailureType.DISPLAY_VIEW_GENERATION);
        throw new Exception("The in-app message view returned from the IInAppMessageViewFactory already has a parent. This "
            + "is a sign that the view is being reused. The IInAppMessageViewFactory method createInAppMessageView"
            + "must return a new view without a parent. The in-app message will not be displayed and will not "
            + "be put back on the stack.");
      }

      Animation openingAnimation = getInAppMessageAnimationFactory().getOpeningAnimation(inAppMessage);
      Animation closingAnimation = getInAppMessageAnimationFactory().getClosingAnimation(inAppMessage);
      IInAppMessageViewWrapperFactory viewWrapperFactory = getInAppMessageViewWrapperFactory();

      if (inAppMessageView instanceof IInAppMessageImmersiveView) {
        AppboyLogger.d(TAG, "Creating view wrapper for immersive in-app message.");
        IInAppMessageImmersiveView inAppMessageViewImmersive = (IInAppMessageImmersiveView) inAppMessageView;
        InAppMessageImmersiveBase inAppMessageImmersiveBase = (InAppMessageImmersiveBase) inAppMessage;

        int numButtons = inAppMessageImmersiveBase.getMessageButtons().size();
        mInAppMessageViewWrapper = viewWrapperFactory.createInAppMessageViewWrapper(inAppMessageView,
            inAppMessage,
            mInAppMessageViewLifecycleListener,
            mAppboyConfigurationProvider,
            openingAnimation,
            closingAnimation,
            inAppMessageViewImmersive.getMessageClickableView(),
            inAppMessageViewImmersive.getMessageButtonViews(numButtons),
            inAppMessageViewImmersive.getMessageCloseButtonView());
      } else if (inAppMessageView instanceof IInAppMessageView) {
        AppboyLogger.d(TAG, "Creating view wrapper for base in-app message.");
        IInAppMessageView inAppMessageViewBase = (IInAppMessageView) inAppMessageView;
        mInAppMessageViewWrapper = viewWrapperFactory.createInAppMessageViewWrapper(inAppMessageView,
            inAppMessage,
            mInAppMessageViewLifecycleListener,
            mAppboyConfigurationProvider,
            openingAnimation,
            closingAnimation,
            inAppMessageViewBase.getMessageClickableView());
      } else {
        AppboyLogger.d(TAG, "Creating view wrapper for in-app message.");
        mInAppMessageViewWrapper = viewWrapperFactory.createInAppMessageViewWrapper(inAppMessageView,
            inAppMessage,
            mInAppMessageViewLifecycleListener,
            mAppboyConfigurationProvider,
            openingAnimation,
            closingAnimation,
            inAppMessageView);
      }

      // If this message includes HTML, delay display until the content has finished loading
      if (inAppMessageView instanceof AppboyInAppMessageHtmlBaseView) {
        AppboyLogger.d(TAG, "In-app message view includes HTML. Delaying display until the content has finished loading.");
        final AppboyInAppMessageHtmlBaseView appboyInAppMessageHtmlBaseView = (AppboyInAppMessageHtmlBaseView) inAppMessageView;
        appboyInAppMessageHtmlBaseView.setHtmlPageFinishedListener(() -> {
          try {
            if (mInAppMessageViewWrapper != null && mActivity != null) {
              AppboyLogger.d(TAG, "Page has finished loading. Opening in-app message view wrapper.");
              mInAppMessageViewWrapper.open(mActivity);
            }
          } catch (Exception e) {
            AppboyLogger.e(TAG, "Failed to open view wrapper in page finished listener", e);
          }
        });
      } else {
        mInAppMessageViewWrapper.open(mActivity);
      }
    } catch (Throwable e) {
      AppboyLogger.e(TAG, "Could not display in-app message with payload: " + JsonUtils.getPrettyPrintedString(inAppMessage.forJsonPut()), e);
      resetAfterInAppMessageClose();
    }
  }

  @SuppressWarnings("deprecation") // https://jira.braze.com/browse/SDK-419
  private IEventSubscriber<InAppMessageEvent> createInAppMessageEventSubscriber() {
    return event -> {
      if (getInAppMessageManagerListener().onInAppMessageReceived(event.getInAppMessage())) {
        return;
      }
      addInAppMessage(event.getInAppMessage());
    };
  }

  /**
   * For in-app messages that have a preferred orientation, locks the screen orientation and
   * returns true if the screen is currently in the preferred orientation. If the screen is not
   * currently in the preferred orientation, returns false.
   *
   * Always returns true for tablets, regardless of current orientation.
   *
   * Always returns true if the in-app message doesn't have a preferred orientation.
   */
  @SuppressLint("InlinedApi")
  @VisibleForTesting
  boolean verifyOrientationStatus(IInAppMessage inAppMessage) {
    if (mActivity == null) {
      AppboyLogger.w(TAG, "Cannot verify orientation status with null Activity.");
      return true;
    }
    if (ViewUtils.isRunningOnTablet(mActivity)) {
      AppboyLogger.d(TAG, "Running on tablet. In-app message can be displayed in any orientation.");
      return true;
    }
    Orientation preferredOrientation = inAppMessage.getOrientation();
    if (preferredOrientation == null) {
      AppboyLogger.d(TAG, "No orientation specified. In-app message can be displayed in any orientation.");
      return true;
    }
    if (preferredOrientation == Orientation.ANY) {
      AppboyLogger.d(TAG, "Any orientation specified. In-app message can be displayed in any orientation.");
      return true;
    }
    int currentScreenOrientation = mActivity.getResources().getConfiguration().orientation;
    if (ViewUtils.isCurrentOrientationValid(currentScreenOrientation, preferredOrientation)) {
      if (mOriginalOrientation == null) {
        AppboyLogger.d(TAG, "Requesting orientation lock.");
        mOriginalOrientation = mActivity.getRequestedOrientation();
        // This constant was introduced in API 18, so for devices pre 18 this will be a no-op
        ViewUtils.setActivityRequestedOrientation(mActivity, ActivityInfo.SCREEN_ORIENTATION_LOCKED);
      }
      return true;
    }
    return false;
  }
}
