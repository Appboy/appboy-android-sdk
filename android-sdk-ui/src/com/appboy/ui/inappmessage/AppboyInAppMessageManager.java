package com.appboy.ui.inappmessage;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import com.appboy.support.AppboyLogger;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.appboy.Appboy;
import com.appboy.AppboyImageUtils;
import com.appboy.Constants;
import com.appboy.IAppboyNavigator;
import com.appboy.enums.inappmessage.ClickAction;
import com.appboy.enums.inappmessage.SlideFrom;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.InAppMessageEvent;
import com.appboy.models.IInAppMessage;
import com.appboy.models.IInAppMessageImmersive;
import com.appboy.models.InAppMessageFull;
import com.appboy.models.InAppMessageModal;
import com.appboy.models.InAppMessageSlideup;
import com.appboy.models.MessageButton;
import com.appboy.support.BundleUtils;
import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.R;
import com.appboy.ui.actions.ActionFactory;
import com.appboy.ui.actions.IAction;
import com.appboy.ui.support.AnimationUtils;
import com.appboy.ui.support.StringUtils;
import com.appboy.ui.support.ViewUtils;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The AppboyInAppMessageManager is used to display in-app messages that are either sent down from Appboy
 * or are created natively in the host app. It will only show one in-app message at a time and will
 * place all other in-app messages onto a stack. The AppboyInAppMessageManager will also keep track of in-app
 * impressions and clicks, which can be viewed on the dashboard.
 *
 * When an in-app message is received from Appboy, the
 * {@link IInAppMessageManagerListener#onInAppMessageReceived(com.appboy.models.IInAppMessage)}
 * method is called (if set). If this method returns true, that signals to the AppboyInAppMessageManager that
 * the in-app message will be handled by the host app and that it should not be displayed by the
 * AppboyInAppMessageManager. This method should be used if you choose to display the in-app message in a custom
 * way. If false is returned, the AppboyInAppMessageManager attempts to display the in-app message.
 *
 * If there is already an in-app message being displayed, the new in-app message will be put onto the top of the
 * stack and can be displayed at a later time. If there is no in-app message being displayed, then the
 * {@link IInAppMessageManagerListener#beforeInAppMessageDisplayed(com.appboy.models.IInAppMessage)}
 * will be called. The {@link InAppMessageOperation} return value can be used to
 * control when the in-app message should be displayed. A suggested usage of this method would be to delay
 * in-app message messages in certain parts of the app by returning {@link InAppMessageOperation#DISPLAY_LATER}
 * when in-app message would be distracting to the users app experience. If the method returns
 * {@link InAppMessageOperation#DISPLAY_NOW} then the in-app message will be displayed
 * immediately.
 *
 * The {@link IInAppMessageManagerListener#onInAppMessageClicked(com.appboy.models.IInAppMessage, InAppMessageCloser)}
 * and {@link IInAppMessageManagerListener#onInAppMessageDismissed(com.appboy.models.IInAppMessage)}
 * methods can be used to override the default click and dismiss behavior.
 *
 * By default, in-app messages fade in and out from view. The slideup type of in-app message slides in and out of view
 * can be dismissed by swiping the view horizontally. If the in-app message's DismissType is set to AUTO_DISMISS,
 * then the in-app message will animate out of view once the set duration time has elapsed.
 *
 * The default view used to display slideup, modal, and full in-app messages
 * is defined by res/layout/com_appboy_inappmessage_*.xml, where * is the message type. In
 * order to use a custom view, you must create a custom view factory using the
 * {@link AppboyInAppMessageManager#setCustomInAppMessageViewFactory(IInAppMessageViewFactory inAppMessageViewFactory)} method.
 *
 * A new in-app message {@link android.view.View} object is created when a in-app message is displayed and also
 * when the user navigates away to another {@link android.app.Activity}. This happens so that the
 * Activity can be garbage collected and does not create a memory leak. For that reason, the
 * {@link AppboyInAppMessageManager#registerInAppMessageManager(android.app.Activity)}
 * and {@link AppboyInAppMessageManager#unregisterInAppMessageManager(android.app.Activity)}
 * must be called in the {@link android.app.Activity#onResume()} and {@link android.app.Activity#onPause()}
 * methods of every Activity.
 */
public final class AppboyInAppMessageManager {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyInAppMessageManager.class.getName());
  private static volatile AppboyInAppMessageManager sInstance = null;

  private final Stack<IInAppMessage> mInAppMessageBaseStack = new Stack<IInAppMessage>();
  private final IAppboyNavigator mDefaultAppboyNavigator = new AppboyNavigator();
  private Activity mActivity;
  private IEventSubscriber<InAppMessageEvent> mInAppMessageEventSubscriber;
  private IInAppMessageManagerListener mCustomInAppMessageManagerListener;
  private IInAppMessageViewFactory mCustomInAppMessageViewFactory;
  private IInAppMessageAnimationFactory mCustomInAppMessageAnimationFactory;
  private InAppMessageViewWrapper mInAppMessageViewWrapper;
  private IInAppMessage mCarryoverInAppMessageBase;
  private AtomicBoolean mDisplayingInAppMessage = new AtomicBoolean(false);

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
   * Registers the in-app message manager, which will listen to and display incoming in-app messages. The
   * current Activity is required in order to properly inflate and display the in-app message view.
   *
   * Important note: Every Activity must call registerInAppMessageManager in the onResume lifecycle
   * method, otherwise in-app messages may be lost!
   *
   * @param activity The current Activity.
   */
  public void registerInAppMessageManager(Activity activity) {
    // We need the current Activity so that we can inflate or programmatically create the in-app message
    // View for each Activity. We cannot share the View because doing so would create a memory leak.
    mActivity = activity;

    // We have a special check to see if the host app switched to a different Activity (or recreated
    // the same Activity during an orientation change) so that we can redisplay the in-app message.
    if (mCarryoverInAppMessageBase != null) {
      AppboyLogger.d(TAG, "Displaying carryover in-app message.");
      mCarryoverInAppMessageBase.setAnimateIn(false);
      displayInAppMessage(mCarryoverInAppMessageBase);
      mCarryoverInAppMessageBase = null;
    }

    // Every time the AppboyInAppMessageManager is registered to an Activity, we add a in-app message subscriber
    // which listens to new in-app messages, adds it to the stack, and displays it if it can.
    mInAppMessageEventSubscriber = createInAppMessageEventSubscriber();
    Appboy.getInstance(activity).subscribeToNewInAppMessages(mInAppMessageEventSubscriber);
  }

  /**
   * Unregisters the in-app message manager.
   *
   * @param activity The current Activity.
   */
  public void unregisterInAppMessageManager(Activity activity) {
    // If there is an in-app message being displayed when the host app transitions to another Activity (or
    // requests an orientation change), we save it in memory so that we can redisplay it when the
    // operation is done.
    if (mInAppMessageViewWrapper != null) {

      ViewUtils.removeViewFromParent(mInAppMessageViewWrapper.getInAppMessageView());
      // Only continue if we're not animating a close
      if (mInAppMessageViewWrapper.getIsAnimatingClose()) {
        mInAppMessageViewWrapper.callAfterClosed();
        mCarryoverInAppMessageBase = null;
      } else {
        mCarryoverInAppMessageBase = mInAppMessageViewWrapper.getInAppMessage();
      }

      mInAppMessageViewWrapper = null;
    } else {
      mCarryoverInAppMessageBase = null;
    }

    // In-app message subscriptions are per Activity, so we must remove the subscriber when the host app
    // unregisters the in-app message manager.
    Appboy.getInstance(activity).removeSingleSubscription(mInAppMessageEventSubscriber, InAppMessageEvent.class);
  }

  /**
   * Assigns a custom IInAppMessageManagerListener that will be used when displaying in-app messages. To revert
   * back to the default IInAppMessageManagerListener, call the setCustomInAppMessageManagerListener method with
   * null.
   *
   * @param inAppMessageManagerListener A custom IInAppMessageManagerListener or null (to revert back to the
   *                               default IInAppMessageManagerListener).
   */
  public void setCustomInAppMessageManagerListener(IInAppMessageManagerListener inAppMessageManagerListener) {
    mCustomInAppMessageManagerListener = inAppMessageManagerListener;
  }

  /**
   * Assigns a custom IInAppMessageAnimationFactory that will be used to animate the in-app message View. To revert
   * back to the default IInAppMessageAnimationFactory, call the setCustomInAppMessageAnimationFactory method with null.
   *
   * @param inAppMessageAnimationFactory A custom IInAppMessageAnimationFactory or null (to revert back to the default
   *                           IInAppMessageAnimationFactory).
   */
  public void setCustomInAppMessageAnimationFactory(IInAppMessageAnimationFactory inAppMessageAnimationFactory) {
    mCustomInAppMessageAnimationFactory = inAppMessageAnimationFactory;
  }

  /**
   * Assigns a custom IInAppMessageViewFactory that will be used to create the in-app message View. To revert
   * back to the default IInAppMessageViewFactory, call the setCustomInAppMessageViewFactory method with null.
   *
   * @param inAppMessageViewFactory A custom IInAppMessageViewFactory or null (to revert back to the default
   *                           IInAppMessageViewFactory).
   */
  public void setCustomInAppMessageViewFactory(IInAppMessageViewFactory inAppMessageViewFactory) {
    mCustomInAppMessageViewFactory = inAppMessageViewFactory;
  }

  /**
   * Provides a in-app message that will then be handled by the in-app message manager. If no in-app message is being
   * displayed, it will attempt to display the in-app message immediately.
   *
   * @param inAppMessage The in-app message to add.
   */
  public void addInAppMessage(IInAppMessage inAppMessage) {
    mInAppMessageBaseStack.push(inAppMessage);
    requestDisplayInAppMessage();
  }

  /**
   * Asks the InAppMessageManager to display the next in-app message if one is not currently being displayed.
   * If one is being displayed, this method will return false and will not display the next in-app message.
   *
   * @return A boolean value indicating whether an asychronous task to display the in-app message display was executed.
   */
  public boolean requestDisplayInAppMessage() {
    if (!mDisplayingInAppMessage.compareAndSet(false, true)) {
      AppboyLogger.d(TAG, "A in-app message is currently being displayed. Ignoring request to display in-app message.");
      return false;
    }
    if (mInAppMessageBaseStack.isEmpty()) {
      AppboyLogger.d(TAG, "The in-app message stack is empty. No in-app message will be displayed.");
      mDisplayingInAppMessage.set(false);
      return false;
    }

    final IInAppMessage inAppMessage = mInAppMessageBaseStack.pop();
    InAppMessageOperation inAppMessageOperation = getInAppMessageManagerListener().beforeInAppMessageDisplayed(inAppMessage);

    switch (inAppMessageOperation) {
      case DISPLAY_NOW:
        AppboyLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_NOW. The " +
            "in-app message will be displayed.");
        break;
      case DISPLAY_LATER:
        AppboyLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISPLAY_LATER. The " +
            "in-app message will be pushed back onto the stack.");
        mInAppMessageBaseStack.push(inAppMessage);
        mDisplayingInAppMessage.set(false);
        return false;
      case DISCARD:
        AppboyLogger.d(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned DISCARD. The " +
            "in-app message will not be displayed and will not be put back on the stack.");
        mDisplayingInAppMessage.set(false);
        return false;
      default:
        AppboyLogger.e(TAG, "The IInAppMessageManagerListener method beforeInAppMessageDisplayed returned null instead of a " +
            "InAppMessageOperation. Ignoring the in-app message. Please check the IInAppMessageStackBehaviour " +
            "implementation.");
        mDisplayingInAppMessage.set(false);
        return false;
    }

    // Asynchronously display the in-app message.
    mActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        new AsyncInAppMessageDisplayer().execute(inAppMessage);
      }
    });
    return true;
  }

  private class AsyncInAppMessageDisplayer extends AsyncTask<IInAppMessage, Integer, IInAppMessage> {

    @Override
    protected IInAppMessage doInBackground(IInAppMessage... inAppMessages) {
      AppboyLogger.d(TAG, "Starting asynchronous in-app message preparation.");
      IInAppMessage inAppMessage = inAppMessages[0];
      String imageUrl = inAppMessage.getImageUrl();
      if (!StringUtils.isNullOrBlank(imageUrl) && inAppMessage.getBitmap() == null) {
        inAppMessage.setBitmap(AppboyImageUtils.downloadImageBitmap(inAppMessage.getImageUrl()));
      } else {
        AppboyLogger.d(TAG, "In-app message already contains image bitmap or has no image URL.  Not downloading image from URL.");
      }
      return inAppMessage;
    }

    @Override
    protected void onPostExecute(final IInAppMessage inAppMessage) {
      AppboyLogger.d(TAG, "Finished asycnhronous in-app message preparation. Attempting to display in-app message.");

      if (inAppMessage != null) {
        mActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            AppboyLogger.d(TAG, "Displaying in-app message.");
            displayInAppMessage(inAppMessage);
          }
        });
      } else {
        AppboyLogger.e(TAG, "Cannot display the in-app message because the in-app message was null.");
        mDisplayingInAppMessage.set(false);
      }
    }
  }

  public void hideCurrentInAppMessage(boolean animate) {
    InAppMessageViewWrapper inAppMessageWrapperView = mInAppMessageViewWrapper;
    if (inAppMessageWrapperView != null) {
      IInAppMessage inAppMessage = inAppMessageWrapperView.getInAppMessage();
      if (inAppMessage != null) {
        inAppMessage.setAnimateOut(animate);
      }
      inAppMessageWrapperView.close();
    }
  }

  private IInAppMessageManagerListener getInAppMessageManagerListener() {
    return mCustomInAppMessageManagerListener != null ? mCustomInAppMessageManagerListener : mDefaultInAppMessageManagerListener;
  }

  private IInAppMessageViewFactory getInAppMessageViewFactory(IInAppMessage inAppMessage) {
    if (mCustomInAppMessageViewFactory != null) {
      return mCustomInAppMessageViewFactory;
    } else if (inAppMessage instanceof InAppMessageSlideup) {
      return mInAppMessageSlideupViewFactory;
    } else if (inAppMessage instanceof InAppMessageModal) {
      return mInAppMessageModalViewFactory;
    } else if (inAppMessage instanceof InAppMessageFull) {
      return mInAppMessageFullViewFactory;
    } else {
      return null;
    }
  }

  private boolean displayInAppMessage(IInAppMessage inAppMessage) {
    final View inAppMessageView = getInAppMessageViewFactory(inAppMessage).createInAppMessageView(mActivity, inAppMessage);

    if (inAppMessageView == null) {
      AppboyLogger.e(TAG, "The in-app message view returned from the IInAppMessageViewFactory was null. The in-app message will " +
          "not be displayed and will not be put back on the stack.");
      mDisplayingInAppMessage.set(false);
      return false;
    }

    if (inAppMessageView.getParent() != null) {
      AppboyLogger.e(TAG, "The in-app message view returned from the IInAppMessageViewFactory already has a parent. This " +
          "is a sign that the view is being reused. The IInAppMessageViewFactory method createInAppMessageView" +
          "must return a new view without a parent. The in-app message will not be displayed and will not " +
          "be put back on the stack.");
      mDisplayingInAppMessage.set(false);
      return false;
    }

    Animation openingAnimation = getInAppMessageAnimationFactory(inAppMessage).getOpeningAnimation(inAppMessage);
    Animation closingAnimation = getInAppMessageAnimationFactory(inAppMessage).getClosingAnimation(inAppMessage);

    if (inAppMessageView instanceof IInAppMessageImmersiveView) {
      AppboyLogger.d(TAG, "Creating view wrapper for immersive in-app message.");
      IInAppMessageImmersiveView inAppMessageViewImmersive = (IInAppMessageImmersiveView) inAppMessageView;
      mInAppMessageViewWrapper = new InAppMessageViewWrapper(inAppMessageView, inAppMessage, mInAppMessageViewLifecycleListener,
          openingAnimation, closingAnimation, inAppMessageViewImmersive.getMessageClickableView(), inAppMessageViewImmersive.getMessageButtonViews(),
          inAppMessageViewImmersive.getMessageCloseButtonView());
    } else if (inAppMessageView instanceof IInAppMessageView) {
      AppboyLogger.d(TAG, "Creating view wrapper for base in-app message.");
      IInAppMessageView inAppMessageViewBase = (IInAppMessageView) inAppMessageView;
      mInAppMessageViewWrapper = new InAppMessageViewWrapper(inAppMessageView,
          inAppMessage, mInAppMessageViewLifecycleListener, openingAnimation, closingAnimation, inAppMessageViewBase.getMessageClickableView());
    } else {
      AppboyLogger.d(TAG, "Creating view wrapper for in-app message.");
      mInAppMessageViewWrapper = new InAppMessageViewWrapper(inAppMessageView, inAppMessage, mInAppMessageViewLifecycleListener, openingAnimation, closingAnimation, inAppMessageView);
    }

    FrameLayout root = (FrameLayout) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
    mInAppMessageViewWrapper.open(root);
    return true;
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

  private IInAppMessageManagerListener mDefaultInAppMessageManagerListener = new IInAppMessageManagerListener() {
    @Override
    public boolean onInAppMessageReceived(IInAppMessage inAppMessage) {
      return false;
    }

    @Override
    public InAppMessageOperation beforeInAppMessageDisplayed(IInAppMessage inAppMessage) {
      return InAppMessageOperation.DISPLAY_NOW;
    }

    @Override
    public boolean onInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
      return false;
    }

    @Override
    public boolean onInAppMessageButtonClicked(MessageButton button, InAppMessageCloser inAppMessageCloser) {
      return false;
    }

    @Override
    public void onInAppMessageDismissed(IInAppMessage inAppMessage) {
    }
  };

  private IInAppMessageViewFactory mInAppMessageSlideupViewFactory = new IInAppMessageViewFactory() {
    @Override
    public View createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
      InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
      AppboyInAppMessageSlideupView slideupView = (AppboyInAppMessageSlideupView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_slideup, null);

      slideupView.setMessageBackgroundColor(inAppMessageSlideup.getBackgroundColor());
      slideupView.setMessage(inAppMessageSlideup.getMessage());
      slideupView.setMessageTextColor(inAppMessageSlideup.getMessageTextColor());
      slideupView.setMessageIcon(inAppMessageSlideup.getIcon(), inAppMessageSlideup.getIconColor(), inAppMessageSlideup.getIconBackgroundColor());
      slideupView.setMessageImage(inAppMessageSlideup.getBitmap());
      slideupView.setMessageChevron(inAppMessageSlideup.getChevronColor(), inAppMessageSlideup.getClickAction());
      slideupView.resetMessageMargins();

      return slideupView;
    }
  };

  private IInAppMessageViewFactory mInAppMessageModalViewFactory = new IInAppMessageViewFactory() {
    @Override
    public View createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
      InAppMessageModal inAppMessageModal = (InAppMessageModal) inAppMessage;
      AppboyInAppMessageModalView modalView = (AppboyInAppMessageModalView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_modal, null);

      modalView.setMessageBackgroundColor(inAppMessage.getBackgroundColor());
      modalView.setMessage(inAppMessage.getMessage());
      modalView.setMessageTextColor(inAppMessage.getMessageTextColor());
      modalView.setMessageHeaderText(inAppMessageModal.getHeader());
      modalView.setMessageHeaderTextColor(inAppMessageModal.getHeaderTextColor());
      modalView.setMessageIcon(inAppMessage.getIcon(), inAppMessage.getIconColor(), inAppMessage.getIconBackgroundColor());
      modalView.setMessageImage(inAppMessage.getBitmap());
      modalView.setMessageButtons(inAppMessageModal.getMessageButtons());
      modalView.setMessageCloseButtonColor(inAppMessageModal.getCloseButtonColor());
      modalView.resetMessageMargins();

      return modalView;
    }
  };

  private IInAppMessageViewFactory mInAppMessageFullViewFactory = new IInAppMessageViewFactory() {
    @Override
    public View createInAppMessageView(Activity activity, IInAppMessage inAppMessage) {
      InAppMessageFull inAppMessageFull = (InAppMessageFull) inAppMessage;
      AppboyInAppMessageFullView fullView = (AppboyInAppMessageFullView) activity.getLayoutInflater().inflate(R.layout.com_appboy_inappmessage_full, null);

      fullView.setMessageBackgroundColor(inAppMessageFull.getBackgroundColor());
      fullView.setMessage(inAppMessageFull.getMessage());
      fullView.setMessageTextColor(inAppMessageFull.getMessageTextColor());
      fullView.setMessageHeaderText(inAppMessageFull.getHeader());
      fullView.setMessageHeaderTextColor(inAppMessageFull.getHeaderTextColor());
      fullView.setMessageImage(inAppMessageFull.getBitmap());
      fullView.setMessageButtons(inAppMessageFull.getMessageButtons());
      fullView.setMessageCloseButtonColor(inAppMessageFull.getCloseButtonColor());
      fullView.resetMessageMargins();

      return fullView;
    }
  };

  private IInAppMessageAnimationFactory getInAppMessageAnimationFactory(IInAppMessage inAppMessage) {
    if (mCustomInAppMessageAnimationFactory != null) {
      return mCustomInAppMessageAnimationFactory;
    } else {
      return mInAppMessageAnimationFactory;
    }
  }

  private IInAppMessageAnimationFactory mInAppMessageAnimationFactory = new IInAppMessageAnimationFactory() {
    private final long sSlideupAnimationDurationMillis = 400l;
    private final int mShortAnimationDurationMillis = Resources.getSystem().getInteger(android.R.integer.config_shortAnimTime);

    @Override
    public Animation getOpeningAnimation(IInAppMessage inAppMessage) {
      Animation animation;
      if (inAppMessage instanceof InAppMessageSlideup) {
        InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
        if (inAppMessageSlideup.getSlideFrom() == SlideFrom.TOP) {
          animation = AnimationUtils.createVerticalAnimation(-1, 0, sSlideupAnimationDurationMillis, false);
        } else {
          animation = AnimationUtils.createVerticalAnimation(1, 0, sSlideupAnimationDurationMillis, false);
        }
      } else {
        animation = new AlphaAnimation(0, 1);
      }
      return AnimationUtils.setAnimationParams(animation, mShortAnimationDurationMillis, true);
    }

    @Override
    public Animation getClosingAnimation(IInAppMessage inAppMessage) {
      Animation animation;
      if (inAppMessage instanceof InAppMessageSlideup) {
        InAppMessageSlideup inAppMessageSlideup = (InAppMessageSlideup) inAppMessage;
        if (inAppMessageSlideup.getSlideFrom() == SlideFrom.TOP) {
          animation = AnimationUtils.createVerticalAnimation(0, -1, sSlideupAnimationDurationMillis, false);
        } else {
          animation = AnimationUtils.createVerticalAnimation(0, 1, sSlideupAnimationDurationMillis, false);
        }
      } else {
        animation = new AlphaAnimation(1, 0);
      }
      return AnimationUtils.setAnimationParams(animation, mShortAnimationDurationMillis, false);
    }
  };

  private final IInAppMessageViewLifecycleListener mInAppMessageViewLifecycleListener = new IInAppMessageViewLifecycleListener() {
    @Override
    public void beforeOpened(View inAppMessageView, IInAppMessage inAppMessage) {
      AppboyLogger.d(TAG, "InAppMessageViewWrapper.IInAppMessageViewLifecycleListener.beforeOpened called.");
      inAppMessage.logImpression();
    }

    @Override
    public void afterOpened(View inAppMessageView, IInAppMessage inAppMessage) {
      AppboyLogger.d(TAG, "InAppMessageViewWrapper.IInAppMessageViewLifecycleListener.afterOpened called.");
    }

    @Override
    public void beforeClosed(View inAppMessageView, IInAppMessage inAppMessage) {
      AppboyLogger.d(TAG, "InAppMessageViewWrapper.IInAppMessageViewLifecycleListener.beforeClosed called.");
    }

    @Override
    public void afterClosed(IInAppMessage inAppMessage) {
      mInAppMessageViewWrapper = null;
      AppboyLogger.d(TAG, "InAppMessageViewWrapper.IInAppMessageViewLifecycleListener.afterClosed called.");
      mDisplayingInAppMessage.set(false);
    }

    @Override
    public void onClicked(InAppMessageCloser inAppMessageCloser, View inAppMessageView, IInAppMessage inAppMessage) {
      AppboyLogger.d(TAG, "InAppMessageViewWrapper.IInAppMessageViewLifecycleListener.onClicked called.");
      if (inAppMessage.getClickAction() != ClickAction.NONE) {
        inAppMessage.logClick();
      }

      // Perform the in-app message clicked listener action from the host application first. This give
      // the app the option to override the values that are sent from the server and handle the
      // in-app message differently depending on where the user is in the app.
      //
      // To modify the default in-app message clicked behavior, mutate the necessary in-app message members. As
      // an example, if the in-app message were to navigate to the news feed when it was clicked, the
      // behavior can be cancelled by setting the click action to NONE.
      boolean handled = getInAppMessageManagerListener().onInAppMessageClicked(inAppMessage, inAppMessageCloser);

      if (!handled) {
        // Perform the default (or modified) in-app message clicked behavior.
        performInAppMessageClicked(inAppMessage, inAppMessageCloser);
      }
    }

    @Override
    public void onButtonClicked(InAppMessageCloser inAppMessageCloser, MessageButton messageButton, IInAppMessageImmersive inAppMessageImmersive) {
      AppboyLogger.d(TAG, "InAppMessageViewWrapper.IInAppMessageViewLifecycleListener.onButtonClicked called.");
      if (messageButton.getClickAction() != ClickAction.NONE) {
        inAppMessageImmersive.logButtonClick(messageButton);
      }

      boolean handled = getInAppMessageManagerListener().onInAppMessageButtonClicked(messageButton, inAppMessageCloser);

      if (!handled) {
        // Perform the default (or modified) in-app message button clicked behavior.
        performInAppMessageButtonClicked(messageButton, inAppMessageImmersive, inAppMessageCloser);
      }
    }

    @Override
    public void onDismissed(View inAppMessageView, IInAppMessage inAppMessage) {
      getInAppMessageManagerListener().onInAppMessageDismissed(inAppMessage);
    }

    private void performInAppMessageButtonClicked(MessageButton messageButton, IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
      performClickAction(messageButton.getClickAction(), inAppMessage, inAppMessageCloser, messageButton.getUri());
    }

    private void performInAppMessageClicked(IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser) {
      performClickAction(inAppMessage.getClickAction(), inAppMessage, inAppMessageCloser, inAppMessage.getUri());
    }

    private void performClickAction(ClickAction clickAction, IInAppMessage inAppMessage, InAppMessageCloser inAppMessageCloser, Uri clickUri) {
      switch(clickAction) {
        case NEWS_FEED:
          inAppMessage.setAnimateOut(false);
          inAppMessageCloser.close(false);
          getAppboyNavigator().gotoNewsFeed(mActivity, BundleUtils.mapToBundle(inAppMessage.getExtras()));
          break;
        case URI:
          inAppMessage.setAnimateOut(false);
          inAppMessageCloser.close(false);
          IAction action = ActionFactory.createUriAction(mActivity, clickUri.toString());
          action.execute(mActivity);
          break;
        case NONE:
          inAppMessageCloser.close(true);
          break;
        default:
          inAppMessageCloser.close(false);
          break;
      }
    }
  };

  private IAppboyNavigator getAppboyNavigator() {
    IAppboyNavigator customAppboyNavigator = Appboy.getInstance(mActivity).getAppboyNavigator();
    return customAppboyNavigator != null ? customAppboyNavigator : mDefaultAppboyNavigator;
  }

  /**
   * @deprecated use {@link AppboyInAppMessageManager#registerInAppMessageManager(android.app.Activity)} instead.
   */
  @Deprecated
  public void registerSlideupManager(Activity activity) {
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(activity);
  }

  /**
   * @deprecated use {@link AppboyInAppMessageManager#unregisterInAppMessageManager(android.app.Activity)} instead.
   */
  @Deprecated
  public void unregisterSlideupManager(Activity activity) {
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(activity);
  }
}
