package com.appboy.ui.inappmessage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;

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
import com.appboy.ui.inappmessage.factories.AppboyFullViewFactory;
import com.appboy.ui.inappmessage.factories.AppboyHtmlFullViewFactory;
import com.appboy.ui.inappmessage.factories.AppboyInAppMessageAnimationFactory;
import com.appboy.ui.inappmessage.factories.AppboyModalViewFactory;
import com.appboy.ui.inappmessage.factories.AppboySlideupViewFactory;
import com.appboy.ui.inappmessage.listeners.AppboyDefaultHtmlInAppMessageActionListener;
import com.appboy.ui.inappmessage.listeners.AppboyDefaultInAppMessageManagerListener;
import com.appboy.ui.inappmessage.listeners.AppboyInAppMessageViewLifecycleListener;
import com.appboy.ui.inappmessage.listeners.AppboyInAppMessageWebViewClientListener;
import com.appboy.ui.inappmessage.listeners.IHtmlInAppMessageActionListener;
import com.appboy.ui.inappmessage.listeners.IInAppMessageManagerListener;
import com.appboy.ui.inappmessage.listeners.IInAppMessageViewLifecycleListener;
import com.appboy.ui.inappmessage.listeners.IInAppMessageWebViewClientListener;
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
public final class AppboyInAppMessageManager {
  private static final String TAG = AppboyLogger.getAppboyLogTag(AppboyInAppMessageManager.class);
  private static volatile AppboyInAppMessageManager sInstance = null;

  private final Stack<IInAppMessage> mInAppMessageStack = new Stack<IInAppMessage>();
  private Activity mActivity;
  private IEventSubscriber<InAppMessageEvent> mInAppMessageEventSubscriber;
  private IInAppMessageViewFactory mCustomInAppMessageViewFactory;
  private IInAppMessageAnimationFactory mCustomInAppMessageAnimationFactory;
  private IInAppMessageViewWrapper mInAppMessageViewWrapper;
  private IInAppMessage mCarryoverInAppMessage;
  private IInAppMessage mUnRegisteredInAppMessage;
  private AtomicBoolean mDisplayingInAppMessage = new AtomicBoolean(false);
  private Context mApplicationContext;
  private Integer mOriginalOrientation;
  private AppboyConfigurationProvider mAppboyConfigurationProvider;
  private boolean mBackButtonDismissesInAppMessageView = true;

  // view listeners
  private final IInAppMessageWebViewClientListener mInAppMessageWebViewClientListener = new AppboyInAppMessageWebViewClientListener();
  private final IInAppMessageViewLifecycleListener mInAppMessageViewLifecycleListener = new AppboyInAppMessageViewLifecycleListener();

  // manager listeners
  private IInAppMessageManagerListener mDefaultInAppMessageManagerListener = new AppboyDefaultInAppMessageManagerListener();
  private IInAppMessageManagerListener mCustomInAppMessageManagerListener;
  /**
   * A custom listener to be fired for control in-app messages.
   * <p/>
   * see {@link IInAppMessage#isControl()}
   */
  private IInAppMessageManagerListener mCustomControlInAppMessageManagerListener;

  // html action listeners
  private IHtmlInAppMessageActionListener mDefaultHtmlInAppMessageActionListener = new AppboyDefaultHtmlInAppMessageActionListener();
  private IHtmlInAppMessageActionListener mCustomHtmlInAppMessageActionListener;

  // view factories
  private IInAppMessageViewFactory mInAppMessageSlideupViewFactory = new AppboySlideupViewFactory();
  private IInAppMessageViewFactory mInAppMessageModalViewFactory = new AppboyModalViewFactory();
  private IInAppMessageViewFactory mInAppMessageFullViewFactory = new AppboyFullViewFactory();
  private IInAppMessageViewFactory mInAppMessageHtmlFullViewFactory = new AppboyHtmlFullViewFactory(mInAppMessageWebViewClientListener);

  // animation factory
  private IInAppMessageAnimationFactory mInAppMessageAnimationFactory = new AppboyInAppMessageAnimationFactory();

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
    if (mInAppMessageEventSubscriber == null) {
      AppboyLogger.d(TAG, "Subscribing in-app message event subscriber");
      mInAppMessageEventSubscriber = createInAppMessageEventSubscriber();
      Appboy.getInstance(context).subscribeToNewInAppMessages(mInAppMessageEventSubscriber);
    }
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
    AppboyLogger.d(TAG, "registerInAppMessageManager called");
    // We need the current Activity so that we can inflate or programmatically create the in-app message
    // View for each Activity. We cannot share the View because doing so would create a memory leak.
    mActivity = activity;
    if (mActivity != null && mApplicationContext == null) {
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
    } else if (mUnRegisteredInAppMessage != null) {
      AppboyLogger.d(TAG, "Adding previously unregistered in-app message.");
      addInAppMessage(mUnRegisteredInAppMessage);
      mUnRegisteredInAppMessage = null;
    }

    ensureSubscribedToInAppMessageEvents(mApplicationContext);
  }

  /**
   * Unregisters the in-app message manager.
   *
   * @param activity The current Activity.
   */
  public void unregisterInAppMessageManager(Activity activity) {
    AppboyLogger.d(TAG, "unregisterInAppMessageManager called");

    // If there is an in-app message being displayed when the host app transitions to another Activity (or
    // requests an orientation change), we save it in memory so that we can redisplay it when the
    // operation is done.
    if (mInAppMessageViewWrapper != null) {
      ViewUtils.removeViewFromParent(mInAppMessageViewWrapper.getInAppMessageView());
      // Only continue if we're not animating a close
      if (mInAppMessageViewWrapper.getIsAnimatingClose()) {
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
   * Assigns a custom {@link IInAppMessageManagerListener} that will be used when displaying in-app messages. To revert
   * back to the default {@link IInAppMessageManagerListener}, call the
   * {@link AppboyInAppMessageManager#setCustomInAppMessageManagerListener(IInAppMessageManagerListener)}  method with null.
   * <p/>
   * see {@link IInAppMessage#isControl()}
   *
   * @param inAppMessageManagerListener A custom {@link IInAppMessageManagerListener} or null (to revert back to the
   *                                    default {@link IInAppMessageManagerListener}).
   */
  public void setCustomInAppMessageManagerListener(IInAppMessageManagerListener inAppMessageManagerListener) {
    AppboyLogger.d(TAG, "Custom InAppMessageManagerListener set");
    mCustomInAppMessageManagerListener = inAppMessageManagerListener;
  }

  /**
   * Assigns a custom {@link IInAppMessageManagerListener} that will be used when displaying control in-app messages. To revert
   * back to the default {@link IInAppMessageManagerListener}, call the
   * {@link AppboyInAppMessageManager#setCustomControlInAppMessageManagerListener(IInAppMessageManagerListener)} method with null.
   *
   * @param inAppMessageManagerListener A custom {@link IInAppMessageManagerListener} for control in-app messages or null (to revert back to the
   *                                    default {@link IInAppMessageManagerListener}).
   */
  public void setCustomControlInAppMessageManagerListener(IInAppMessageManagerListener inAppMessageManagerListener) {
    AppboyLogger.d(TAG, "Custom ControlInAppMessageManagerListener set. This listener will only be used for control in-app messages.");
    mCustomControlInAppMessageManagerListener = inAppMessageManagerListener;
  }

  /**
   * Assigns a custom IHtmlInAppMessageActionListener that will be used during the display of Html in-app messages.
   *
   * @param htmlInAppMessageActionListener A custom IHtmlInAppMessageActionListener or null (to revert back to the
   *                                       default IHtmlInAppMessageActionListener).
   */
  public void setCustomHtmlInAppMessageActionListener(IHtmlInAppMessageActionListener htmlInAppMessageActionListener) {
    AppboyLogger.d(TAG, "Custom htmlInAppMessageActionListener set");
    mCustomHtmlInAppMessageActionListener = htmlInAppMessageActionListener;
  }

  /**
   * Assigns a custom IInAppMessageAnimationFactory that will be used to animate the in-app message View. To revert
   * back to the default IInAppMessageAnimationFactory, call the setCustomInAppMessageAnimationFactory method with null.
   *
   * @param inAppMessageAnimationFactory A custom IInAppMessageAnimationFactory or null (to revert back to the default
   *                                     IInAppMessageAnimationFactory).
   */
  public void setCustomInAppMessageAnimationFactory(IInAppMessageAnimationFactory inAppMessageAnimationFactory) {
    AppboyLogger.d(TAG, "Custom InAppMessageAnimationFactory set");
    mCustomInAppMessageAnimationFactory = inAppMessageAnimationFactory;
  }

  /**
   * Assigns a custom IInAppMessageViewFactory that will be used to create the in-app message View. To revert
   * back to the default IInAppMessageViewFactory, call the setCustomInAppMessageViewFactory method with null.
   *
   * @param inAppMessageViewFactory A custom IInAppMessageViewFactory or null (to revert back to the default
   *                                IInAppMessageViewFactory).
   */
  public void setCustomInAppMessageViewFactory(IInAppMessageViewFactory inAppMessageViewFactory) {
    AppboyLogger.d(TAG, "Custom InAppMessageViewFactory set");
    mCustomInAppMessageViewFactory = inAppMessageViewFactory;
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
          AppboyLogger.w(TAG, "No activity is currently registered to receive in-app messages. Saving in-app message as unregistered "
              + "in-app message. It will automatically be displayed when the next activity registers to receive in-app messages.");
          mUnRegisteredInAppMessage = mInAppMessageStack.pop();
        } else {
          AppboyLogger.d(TAG, "No activity is currently registered to receive in-app messages and the in-app message stack is empty. Doing nothing.");
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
          AppboyLogger.e(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned null instead of a "
              + "InAppMessageOperation. Ignoring the in-app message. Please check the IInAppMessageStackBehaviour "
              + "implementation.");
          return false;
      }

      Handler mainLooperHandler = new Handler(mApplicationContext.getMainLooper());
      mainLooperHandler.post(new Runnable() {
        @Override
        public void run() {
          new AppboyAsyncInAppMessageDisplayer().execute(inAppMessage);
        }
      });
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

  public IInAppMessageManagerListener getInAppMessageManagerListener() {
    return mCustomInAppMessageManagerListener != null ? mCustomInAppMessageManagerListener : mDefaultInAppMessageManagerListener;
  }

  /**
   * A {@link IInAppMessageManagerListener} to be used only for control in-app messages.
   * <p/>
   * see {@link IInAppMessage#isControl()}
   */
  public IInAppMessageManagerListener getControlInAppMessageManagerListener() {
    return mCustomControlInAppMessageManagerListener != null ? mCustomControlInAppMessageManagerListener : mDefaultInAppMessageManagerListener;
  }

  public IHtmlInAppMessageActionListener getHtmlInAppMessageActionListener() {
    return mCustomHtmlInAppMessageActionListener != null ? mCustomHtmlInAppMessageActionListener : mDefaultHtmlInAppMessageActionListener;
  }

  public Activity getActivity() {
    return mActivity;
  }

  public Context getApplicationContext() {
    return mApplicationContext;
  }

  /**
   * Gets the default {@link IInAppMessageViewFactory} as returned by the {@link AppboyInAppMessageManager}
   * for the given {@link IInAppMessage}.
   *
   * @return The {@link IInAppMessageViewFactory} or null if the message type does not have a {@link IInAppMessageViewFactory}.
   */
  public IInAppMessageViewFactory getDefaultInAppMessageViewFactory(IInAppMessage inAppMessage) {
    switch (inAppMessage.getMessageType()) {
      case SLIDEUP:
        return mInAppMessageSlideupViewFactory;
      case MODAL:
        return mInAppMessageModalViewFactory;
      case FULL:
        return mInAppMessageFullViewFactory;
      case HTML_FULL:
        return mInAppMessageHtmlFullViewFactory;
      default:
        return null;
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
   * Sets whether the hardware back button dismisses in-app messages. Defaults to true.
   * Note that the hardware back button default behavior will be used instead (i.e. the host {@link Activity}'s
   * {@link Activity#onKeyDown(int, KeyEvent)} method will be called).
   */
  public void setBackButtonDismissesInAppMessageView(boolean backButtonDismissesInAppMessageView) {
    mBackButtonDismissesInAppMessageView = backButtonDismissesInAppMessageView;
  }

  public boolean getDoesBackButtonDismissInAppMessageView() {
    return mBackButtonDismissesInAppMessageView;
  }

  private IInAppMessageAnimationFactory getInAppMessageAnimationFactory() {
    return mCustomInAppMessageAnimationFactory != null ? mCustomInAppMessageAnimationFactory : mInAppMessageAnimationFactory;
  }

  private IInAppMessageViewFactory getInAppMessageViewFactory(IInAppMessage inAppMessage) {
    if (mCustomInAppMessageViewFactory != null) {
      return mCustomInAppMessageViewFactory;
    } else {
      return getDefaultInAppMessageViewFactory(inAppMessage);
    }
  }

  boolean displayInAppMessage(IInAppMessage inAppMessage, boolean isCarryOver) {
    // Note: for mDisplayingInAppMessage to be accurate it requires this method does not exit anywhere but the at the end
    // of this try/catch when we know whether we are successfully displaying the in-app message or not.
    if (!mDisplayingInAppMessage.compareAndSet(false, true)) {
      AppboyLogger.d(TAG, "A in-app message is currently being displayed. Adding in-app message back on the stack.");
      mInAppMessageStack.push(inAppMessage);
      return false;
    }

    try {
      if (mActivity == null) {
        mCarryoverInAppMessage = inAppMessage;
        throw new Exception("No activity is currently registered to receive in-app messages. Registering in-app message as carry-over "
            + "in-app message. It will automatically be displayed when the next activity registers to receive in-app messages.");
      }
      if (!isCarryOver) {
        long inAppMessageExpirationTimestamp = inAppMessage.getExpirationTimestamp();
        if (inAppMessageExpirationTimestamp > 0) {
          long currentTimeMillis = System.currentTimeMillis();
          if (currentTimeMillis > inAppMessageExpirationTimestamp) {
            throw new Exception("In-app message is expired. Doing nothing. Expiration: $" + inAppMessageExpirationTimestamp + ". Current time: " + currentTimeMillis);
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
        return true;
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

      if (inAppMessageView instanceof IInAppMessageImmersiveView) {
        AppboyLogger.d(TAG, "Creating view wrapper for immersive in-app message.");
        IInAppMessageImmersiveView inAppMessageViewImmersive = (IInAppMessageImmersiveView) inAppMessageView;
        InAppMessageImmersiveBase inAppMessageImmersiveBase = (InAppMessageImmersiveBase) inAppMessage;

        int numButtons = inAppMessageImmersiveBase.getMessageButtons() != null ? inAppMessageImmersiveBase.getMessageButtons().size() : 0;
        mInAppMessageViewWrapper = new InAppMessageViewWrapper(inAppMessageView, inAppMessage, mInAppMessageViewLifecycleListener,
            mAppboyConfigurationProvider, openingAnimation, closingAnimation, inAppMessageViewImmersive.getMessageClickableView(),
            inAppMessageViewImmersive.getMessageButtonViews(numButtons), inAppMessageViewImmersive.getMessageCloseButtonView());
      } else if (inAppMessageView instanceof IInAppMessageView) {
        AppboyLogger.d(TAG, "Creating view wrapper for base in-app message.");
        IInAppMessageView inAppMessageViewBase = (IInAppMessageView) inAppMessageView;
        mInAppMessageViewWrapper = new InAppMessageViewWrapper(inAppMessageView, inAppMessage, mInAppMessageViewLifecycleListener,
            mAppboyConfigurationProvider, openingAnimation, closingAnimation, inAppMessageViewBase.getMessageClickableView());
      } else {
        AppboyLogger.d(TAG, "Creating view wrapper for in-app message.");
        mInAppMessageViewWrapper = new InAppMessageViewWrapper(inAppMessageView, inAppMessage, mInAppMessageViewLifecycleListener,
            mAppboyConfigurationProvider, openingAnimation, closingAnimation, inAppMessageView);
      }
      mInAppMessageViewWrapper.open(mActivity);
      return true;
    } catch (Throwable e) {
      AppboyLogger.e(TAG, "Could not display in-app message with payload: " + JsonUtils.getPrettyPrintedString(inAppMessage.forJsonPut()), e);
      resetAfterInAppMessageClose();
      return false;
    }
  }

  /**
   *
   * For in-app messages that have a preferred orientation, locks the screen orientation and
   * returns true if the screen is currently in the preferred orientation. If the screen is not
   * currently in the preferred orientation, returns false.
   *
   * Always returns true for tablets, regardless of current orientation.
   *
   * Always returns true if the in-app message doesn't have a preferred orientation.
   *
   * @param inAppMessage
   * @return
   */
  @SuppressLint("InlinedApi")
  boolean verifyOrientationStatus(IInAppMessage inAppMessage) {
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
    if (currentOrientationIsValid(currentScreenOrientation, preferredOrientation)) {
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

  private boolean currentOrientationIsValid(int currentScreenOrientation, Orientation preferredOrientation) {
    if (currentScreenOrientation == Configuration.ORIENTATION_LANDSCAPE
        && preferredOrientation == Orientation.LANDSCAPE) {
      AppboyLogger.d(TAG, "Current and preferred orientation are landscape.");
      return true;
    } else if (currentScreenOrientation == Configuration.ORIENTATION_PORTRAIT
        && preferredOrientation == Orientation.PORTRAIT) {
      AppboyLogger.d(TAG, "Current and preferred orientation are portrait.");
      return true;
    } else {
      AppboyLogger.d(TAG, "Current orientation " + currentScreenOrientation + " and preferred orientation " + preferredOrientation + " don't match");
      return false;
    }
  }

  private IEventSubscriber<InAppMessageEvent> createInAppMessageEventSubscriber() {
    return new IEventSubscriber<InAppMessageEvent>() {
      @Override
      public void trigger(InAppMessageEvent event) {
        if (getInAppMessageManagerListener().onInAppMessageReceived(event.getInAppMessage())) {
          return;
        }
        addInAppMessage(event.getInAppMessage());
      }
    };
  }
}
