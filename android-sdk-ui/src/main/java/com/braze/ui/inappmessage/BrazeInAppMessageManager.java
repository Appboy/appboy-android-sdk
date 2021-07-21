package com.braze.ui.inappmessage;

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

import com.appboy.events.IEventSubscriber;
import com.braze.Braze;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.enums.inappmessage.InAppMessageFailureType;
import com.braze.enums.inappmessage.Orientation;
import com.braze.events.InAppMessageEvent;
import com.braze.events.SdkDataWipeEvent;
import com.braze.models.inappmessage.IInAppMessage;
import com.braze.models.inappmessage.InAppMessageImmersiveBase;
import com.braze.support.BrazeLogger;
import com.braze.support.JsonUtils;
import com.braze.ui.inappmessage.listeners.DefaultInAppMessageViewLifecycleListener;
import com.braze.ui.inappmessage.listeners.IInAppMessageManagerListener;
import com.braze.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;
import com.braze.ui.inappmessage.utils.BackgroundInAppMessagePreparer;
import com.braze.ui.inappmessage.views.IInAppMessageImmersiveView;
import com.braze.ui.inappmessage.views.IInAppMessageView;
import com.braze.ui.inappmessage.views.InAppMessageHtmlBaseView;
import com.braze.ui.support.ViewUtils;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to display in-app messages that are either sent from Braze
 * or are created natively in the host app. It will only show one in-app message at a time and will
 * place all other in-app messages onto a stack. The {@link BrazeInAppMessageManager} will also keep track of in-app
 * impressions and clicks, which can be viewed on the dashboard.
 * <p/>
 * When an in-app message is received from Braze, the
 * {@link IInAppMessageManagerListener#onInAppMessageReceived(IInAppMessage)}
 * method is called (if set). If this method returns true, that signals to the BrazeInAppMessageManager that
 * the in-app message will be handled by the host app and that it should not be displayed by the
 * BrazeInAppMessageManager. This method should be used if you choose to display the in-app message in a custom
 * way. If false is returned, the {@link BrazeInAppMessageManager} attempts to display the in-app message.
 * <p/>
 * If there is already an in-app message being displayed, the new in-app message will be put onto the top of the
 * stack and can be displayed at a later time. If there is no in-app message being displayed, then the
 * {@link IInAppMessageManagerListener#beforeInAppMessageDisplayed(IInAppMessage)}
 * will be called. The {@link InAppMessageOperation} return value can be used to
 * control when the in-app message should be displayed. A suggested usage of this method would be to delay
 * in-app message messages in certain parts of the app by returning {@link InAppMessageOperation#DISPLAY_LATER}
 * when in-app message would be distracting to the users app experience. If the method returns
 * {@link InAppMessageOperation#DISPLAY_NOW} then the in-app message will be displayed
 * immediately.
 * <p/>
 * The {@link IInAppMessageManagerListener#onInAppMessageClicked(IInAppMessage, InAppMessageCloser)}
 * and {@link IInAppMessageManagerListener#onInAppMessageDismissed(IInAppMessage)}
 * methods can be used to override the default click and dismiss behavior.
 * <p/>
 * By default, in-app messages fade in and out from view. The slideup type of in-app message slides in and out of view
 * can be dismissed by swiping the view horizontally. If the in-app message's DismissType is set to AUTO_DISMISS,
 * then the in-app message will animate out of view once the set duration time has elapsed.
 * <p/>
 * In order to use a custom view, you must create a custom view factory using the
 * {@link BrazeInAppMessageManager#setCustomInAppMessageViewFactory(IInAppMessageViewFactory inAppMessageViewFactory)} method.
 * <p/>
 * A new in-app message {@link android.view.View} object is created when a in-app message is displayed and also
 * when the user navigates away to another {@link android.app.Activity}. This happens so that the
 * Activity can be garbage collected and does not create a memory leak. For that reason, the
 * {@link BrazeInAppMessageManager#registerInAppMessageManager(android.app.Activity)}
 * and {@link BrazeInAppMessageManager#unregisterInAppMessageManager(android.app.Activity)}
 * must be called in the {@link android.app.Activity#onResume()} and {@link android.app.Activity#onPause()}
 * methods of every Activity.
 */
// Static field leak doesn't apply to this singleton since the activity is nullified after the manager is unregistered.
@SuppressLint("StaticFieldLeak")
public class BrazeInAppMessageManager extends InAppMessageManagerBase {
  private static final String TAG = BrazeLogger.getBrazeLogTag(BrazeInAppMessageManager.class);
  private static volatile BrazeInAppMessageManager sInstance = null;

  private final IInAppMessageViewLifecycleListener mInAppMessageViewLifecycleListener = new DefaultInAppMessageViewLifecycleListener();
  private final AtomicBoolean mDisplayingInAppMessage = new AtomicBoolean(false);
  @VisibleForTesting
  @NonNull
  final Stack<IInAppMessage> mInAppMessageStack = new Stack<>();

  @Nullable
  private IEventSubscriber<InAppMessageEvent> mInAppMessageEventSubscriber;
  @Nullable
  private IEventSubscriber<SdkDataWipeEvent> mSdkDataWipeEventSubscriber;
  @Nullable
  private Integer mOriginalOrientation;
  @Nullable
  private BrazeConfigurationProvider mConfigurationProvider;
  @Nullable
  private IInAppMessageViewWrapper mInAppMessageViewWrapper;
  @VisibleForTesting
  @Nullable
  IInAppMessage mCarryoverInAppMessage;
  @VisibleForTesting
  @Nullable
  IInAppMessage mUnregisteredInAppMessage;

  public static BrazeInAppMessageManager getInstance() {
    if (sInstance == null) {
      synchronized (BrazeInAppMessageManager.class) {
        if (sInstance == null) {
          sInstance = new BrazeInAppMessageManager();
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
   * are correctly handled by the BrazeInAppMessageManager.
   * <p/>
   * For example, if logging custom events with triggers in your first activity's onCreate(), be sure
   * to call this method manually beforehand so that the in-app message will get displayed by the time
   * registerInAppMessageManager() gets called.
   *
   * @param context The application context
   */
  public void ensureSubscribedToInAppMessageEvents(Context context) {
    if (mInAppMessageEventSubscriber != null) {
      BrazeLogger.d(TAG, "Removing existing in-app message event subscriber before subscribing a new one.");
      Braze.getInstance(context).removeSingleSubscription(mInAppMessageEventSubscriber, InAppMessageEvent.class);
    }
    BrazeLogger.d(TAG, "Subscribing in-app message event subscriber");
    mInAppMessageEventSubscriber = createInAppMessageEventSubscriber();
    Braze.getInstance(context).subscribeToNewInAppMessages(mInAppMessageEventSubscriber);

    if (mSdkDataWipeEventSubscriber != null) {
      BrazeLogger.v(TAG, "Removing existing sdk data wipe event subscriber before subscribing a new one.");
      Braze.getInstance(context).removeSingleSubscription(mSdkDataWipeEventSubscriber, SdkDataWipeEvent.class);
    }
    BrazeLogger.v(TAG, "Subscribing sdk data wipe subscriber");
    mSdkDataWipeEventSubscriber = message -> {
      mInAppMessageStack.clear();
      mCarryoverInAppMessage = null;
      mUnregisteredInAppMessage = null;
    };
    Braze.getInstance(context).addSingleSynchronousSubscription(mSdkDataWipeEventSubscriber, SdkDataWipeEvent.class);
  }

  /**
   * Registers the in-app message manager, which will listen to and display incoming in-app messages. The
   * current Activity is required in order to properly inflate and display the in-app message view.
   * <p/>
   * Important note: Every Activity must call registerInAppMessageManager in the onResume lifecycle
   * method, otherwise in-app messages may be lost!
   * <p/>
   * This method also calls {@link BrazeInAppMessageManager#ensureSubscribedToInAppMessageEvents(Context)}.
   * To be sure that no in-app messages are lost, you should call {@link BrazeInAppMessageManager#ensureSubscribedToInAppMessageEvents(Context)} as early
   * as possible in your app, preferably in your {@link Application#onCreate()}.
   * @param activity The current Activity.
   */
  public void registerInAppMessageManager(Activity activity) {
    if (activity == null) {
      BrazeLogger.w(TAG, "Null Activity passed to registerInAppMessageManager. Doing nothing");
      return;
    } else {
      BrazeLogger.v(TAG, "Registering InAppMessageManager with activity: " + activity.getLocalClassName());
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
    if (mConfigurationProvider == null) {
      mConfigurationProvider = new BrazeConfigurationProvider(mApplicationContext);
    }

    // We have a special check to see if the host app switched to a different Activity (or recreated
    // the same Activity during an orientation change) so that we can redisplay the in-app message.
    if (mCarryoverInAppMessage != null) {
      BrazeLogger.d(TAG, "Requesting display of carryover in-app message.");
      mCarryoverInAppMessage.setAnimateIn(false);
      displayInAppMessage(mCarryoverInAppMessage, true);
      mCarryoverInAppMessage = null;
    } else if (mUnregisteredInAppMessage != null) {
      BrazeLogger.d(TAG, "Adding previously unregistered in-app message.");
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
      BrazeLogger.w(TAG, "Null Activity passed to unregisterInAppMessageManager.");
    } else {
      BrazeLogger.v(TAG, "Unregistering InAppMessageManager from activity: " + activity.getLocalClassName());
    }

    // If there is an in-app message being displayed when the host app transitions to another Activity (or
    // requests an orientation change), we save it in memory so that we can redisplay it when the
    // operation is done.
    if (mInAppMessageViewWrapper != null) {
      final View inAppMessageView = mInAppMessageViewWrapper.getInAppMessageView();
      if (inAppMessageView instanceof InAppMessageHtmlBaseView) {
        BrazeLogger.d(TAG, "In-app message view includes HTML. Removing the page finished listener.");
        final InAppMessageHtmlBaseView inAppMessageHtmlBaseView = (InAppMessageHtmlBaseView) inAppMessageView;
        inAppMessageHtmlBaseView.setHtmlPageFinishedListener(null);
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
          BrazeLogger.w(TAG, "No activity is currently registered to receive in-app messages. "
              + "Saving in-app message as unregistered "
              + "in-app message. It will automatically be displayed when the next activity "
              + "registers to receive in-app messages.");
          mUnregisteredInAppMessage = mInAppMessageStack.pop();
        } else {
          BrazeLogger.d(TAG, "No activity is currently registered to receive in-app messages and the "
              + "in-app message stack is empty. Doing nothing.");
        }
        return false;
      }
      if (mDisplayingInAppMessage.get()) {
        BrazeLogger.d(TAG, "A in-app message is currently being displayed. Ignoring request to display in-app message.");
        return false;
      }
      if (mInAppMessageStack.isEmpty()) {
        BrazeLogger.d(TAG, "The in-app message stack is empty. No in-app message will be displayed.");
        return false;
      }

      final IInAppMessage inAppMessage = mInAppMessageStack.pop();
      InAppMessageOperation inAppMessageOperation;

      if (!inAppMessage.isControl()) {
        inAppMessageOperation = getInAppMessageManagerListener().beforeInAppMessageDisplayed(inAppMessage);
      } else {
        BrazeLogger.d(TAG, "Using the control in-app message manager listener.");
        inAppMessageOperation = getControlInAppMessageManagerListener().beforeInAppMessageDisplayed(inAppMessage);
      }

      switch (inAppMessageOperation) {
        case DISPLAY_NOW:
          BrazeLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_NOW. The "
              + "in-app message will be displayed.");
          break;
        case DISPLAY_LATER:
          BrazeLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_LATER. The "
              + "in-app message will be pushed back onto the stack.");
          mInAppMessageStack.push(inAppMessage);
          return false;
        case DISCARD:
          BrazeLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISCARD. The "
              + "in-app message will not be displayed and will not be put back on the stack.");
          return false;
        default:
          BrazeLogger.w(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned null instead of a "
              + "InAppMessageOperation. Ignoring the in-app message. Please check the IInAppMessageStackBehaviour "
              + "implementation.");
          return false;
      }

      Handler mainLooperHandler = new Handler(mActivity.getMainLooper());
      BackgroundInAppMessagePreparer.prepareInAppMessageForDisplay(mainLooperHandler, inAppMessage);
      return true;
    } catch (Exception e) {
      BrazeLogger.e(TAG, "Error running requestDisplayInAppMessage", e);
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
   * Resets the {@link BrazeInAppMessageManager} to its original state before the last in-app message
   * was displayed. This allows for a new in-app message to be displayed after calling this method.
   * {@link ViewUtils#setActivityRequestedOrientation(Activity, int)} is called with the original
   * orientation before the last in-app message was displayed.
   */
  public void resetAfterInAppMessageClose() {
    BrazeLogger.v(TAG, "Resetting after in-app message close.");
    mInAppMessageViewWrapper = null;
    mDisplayingInAppMessage.set(false);
    if (mActivity != null && mOriginalOrientation != null) {
      BrazeLogger.d(TAG, "Setting requested orientation to original orientation " + mOriginalOrientation);
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
   * Internal method, do not call as part of an integration!
   * <br>
   * <br>
   * Attempts to display an {@link IInAppMessage} to the user.
   *
   * @param inAppMessage The {@link IInAppMessage}.
   * @param isCarryOver If this {@link IInAppMessage} is "carried over" from an {@link Activity} transition.
   */
  public void displayInAppMessage(IInAppMessage inAppMessage, boolean isCarryOver) {
    BrazeLogger.v(TAG, "Attempting to display in-app message with payload: " + JsonUtils.getPrettyPrintedString(inAppMessage.forJsonPut()));

    // Note: for mDisplayingInAppMessage to be accurate it requires this method does not exit anywhere but the at the end
    // of this try/catch when we know whether we are successfully displaying the in-app message or not.
    if (!mDisplayingInAppMessage.compareAndSet(false, true)) {
      BrazeLogger.d(TAG, "A in-app message is currently being displayed. Adding in-app message back on the stack.");
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
          BrazeLogger.d(TAG, "Expiration timestamp not defined. Continuing.");
        }
      } else {
        BrazeLogger.d(TAG, "Not checking expiration status for carry-over in-app message.");
      }
      if (!verifyOrientationStatus(inAppMessage)) {
        // No display failure gets logged here since control in-app messages would also be affected.
        throw new Exception("Current orientation did not match specified orientation for in-app message. Doing nothing.");
      }

      // At this point, the only factors that would inhibit in-app message display are view creation issues.
      // Since control in-app messages have no view, this is the end of execution for control in-app messages
      if (inAppMessage.isControl()) {
        BrazeLogger.d(TAG, "Not displaying control in-app message. Logging impression and ending display execution.");
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
        BrazeLogger.d(TAG, "Creating view wrapper for immersive in-app message.");
        IInAppMessageImmersiveView inAppMessageViewImmersive = (IInAppMessageImmersiveView) inAppMessageView;
        InAppMessageImmersiveBase inAppMessageImmersiveBase = (InAppMessageImmersiveBase) inAppMessage;

        int numButtons = inAppMessageImmersiveBase.getMessageButtons().size();
        mInAppMessageViewWrapper = viewWrapperFactory.createInAppMessageViewWrapper(inAppMessageView,
            inAppMessage,
            mInAppMessageViewLifecycleListener,
            mConfigurationProvider,
            openingAnimation,
            closingAnimation,
            inAppMessageViewImmersive.getMessageClickableView(),
            inAppMessageViewImmersive.getMessageButtonViews(numButtons),
            inAppMessageViewImmersive.getMessageCloseButtonView());
      } else if (inAppMessageView instanceof IInAppMessageView) {
        BrazeLogger.d(TAG, "Creating view wrapper for base in-app message.");
        IInAppMessageView inAppMessageViewBase = (IInAppMessageView) inAppMessageView;
        mInAppMessageViewWrapper = viewWrapperFactory.createInAppMessageViewWrapper(inAppMessageView,
            inAppMessage,
            mInAppMessageViewLifecycleListener,
            mConfigurationProvider,
            openingAnimation,
            closingAnimation,
            inAppMessageViewBase.getMessageClickableView());
      } else {
        BrazeLogger.d(TAG, "Creating view wrapper for in-app message.");
        mInAppMessageViewWrapper = viewWrapperFactory.createInAppMessageViewWrapper(inAppMessageView,
            inAppMessage,
            mInAppMessageViewLifecycleListener,
            mConfigurationProvider,
            openingAnimation,
            closingAnimation,
            inAppMessageView);
      }

      // If this message includes HTML, delay display until the content has finished loading
      if (inAppMessageView instanceof InAppMessageHtmlBaseView) {
        BrazeLogger.d(TAG, "In-app message view includes HTML. Delaying display until the content has finished loading.");
        final InAppMessageHtmlBaseView inAppMessageHtmlBaseView = (InAppMessageHtmlBaseView) inAppMessageView;
        inAppMessageHtmlBaseView.setHtmlPageFinishedListener(() -> {
          try {
            if (mInAppMessageViewWrapper != null && mActivity != null) {
              BrazeLogger.d(TAG, "Page has finished loading. Opening in-app message view wrapper.");
              mInAppMessageViewWrapper.open(mActivity);
            }
          } catch (Exception e) {
            BrazeLogger.e(TAG, "Failed to open view wrapper in page finished listener", e);
          }
        });
      } else {
        mInAppMessageViewWrapper.open(mActivity);
      }
    } catch (Throwable e) {
      BrazeLogger.e(TAG, "Could not display in-app message with payload: " + JsonUtils.getPrettyPrintedString(inAppMessage.forJsonPut()), e);
      resetAfterInAppMessageClose();
    }
  }

  private IEventSubscriber<InAppMessageEvent> createInAppMessageEventSubscriber() {
    return event -> addInAppMessage(event.getInAppMessage());
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
      BrazeLogger.w(TAG, "Cannot verify orientation status with null Activity.");
      return true;
    }
    if (ViewUtils.isRunningOnTablet(mActivity)) {
      BrazeLogger.d(TAG, "Running on tablet. In-app message can be displayed in any orientation.");
      return true;
    }
    Orientation preferredOrientation = inAppMessage.getOrientation();
    if (preferredOrientation == null) {
      BrazeLogger.d(TAG, "No orientation specified. In-app message can be displayed in any orientation.");
      return true;
    }
    if (preferredOrientation == Orientation.ANY) {
      BrazeLogger.d(TAG, "Any orientation specified. In-app message can be displayed in any orientation.");
      return true;
    }
    int currentScreenOrientation = mActivity.getResources().getConfiguration().orientation;
    if (ViewUtils.isCurrentOrientationValid(currentScreenOrientation, preferredOrientation)) {
      if (mOriginalOrientation == null) {
        BrazeLogger.d(TAG, "Requesting orientation lock.");
        mOriginalOrientation = mActivity.getRequestedOrientation();
        // This constant was introduced in API 18, so for devices pre 18 this will be a no-op
        ViewUtils.setActivityRequestedOrientation(mActivity, ActivityInfo.SCREEN_ORIENTATION_LOCKED);
      }
      return true;
    }
    return false;
  }
}
