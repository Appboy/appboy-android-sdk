package com.appboy.ui.slideups;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.appboy.Appboy;
import com.appboy.IAppboyNavigator;
import com.appboy.enums.Slideup.ClickAction;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.SlideupEvent;
import com.appboy.models.Slideup;
import com.appboy.support.BundleUtils;
import com.appboy.ui.AppboyNavigator;
import com.appboy.ui.Constants;
import com.appboy.ui.R;
import com.appboy.ui.support.ViewUtils;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The AppboySlideupManager is used to display slideup messages that are either sent down from Appboy
 * or are created navitely in the host app. It will only show one slideup message at a time and will
 * place all other slideups onto a stack. The AppboySlideupManager will also keep track of slideup
 * impressions and clicks, which can be viewed on the dashboard.
 *
 * When a slideup is received from Appboy, the
 * {@link com.appboy.ui.slideups.ISlideupManagerListener#onSlideupReceived(com.appboy.models.Slideup)}
 * method is called (if set). If this method returns true, that signals to the AppboySlideupManager that
 * the slideup will be handled by the host app and that it should not be displayed by the
 * AppboySlideupManager. This method should be used if you choose to display the slideup in a custom
 * way. If false is returned, the AppboySlideupManager attempts to display the slideup.
 *
 * If there is already a slideup being displayed, the new slideup will be put onto the top of the
 * stack and can be displayed at a later time. If there is no slideup being displayed, then the
 * {@link com.appboy.ui.slideups.ISlideupManagerListener#beforeSlideupDisplayed(com.appboy.models.Slideup)}
 * will be called. The {@link com.appboy.ui.slideups.SlideupOperation} return value can be used to
 * control when the slideup should be displayed. A suggested usage of this method would be to delay
 * slideup messages in certain parts of the app by returning {@link com.appboy.ui.slideups.SlideupOperation#DISPLAY_LATER}
 * when slideups would be distracting to the users app experience. If the method returns
 * {@link com.appboy.ui.slideups.SlideupOperation#DISPLAY_NOW} then the slideup will be displayed
 * immediately.
 *
 * The {@link com.appboy.ui.slideups.ISlideupManagerListener#onSlideupClicked(com.appboy.models.Slideup, com.appboy.ui.slideups.SlideupCloser)}
 * and {@link com.appboy.ui.slideups.ISlideupManagerListener#onSlideupDismissed(com.appboy.models.Slideup)}
 * methods can be used to override the default click and dismiss behavior.
 *
 * By default, slideups animate into view (from either the top or bottom of the screen). The slideup
 * can be dismissed by swiping the view horizontally. If the slideups DismissType is set to AUTO_DISMISS,
 * then the slideup will animate out of view once the set duration time has elapsed.
 *
 * The default view used to display slideups is defined by res/layout/com_appboy_slideup_view.xml. In
 * order to use a custom view, you must set the custom view factory using the
 * {@link AppboySlideupManager#setCustomSlideupViewFactory(ISlideupViewFactory slideupViewFactory)} method.
 *
 * A new slideup {@link android.view.View} object is created when a slideup is displayed and also
 * when the user navigates away to another {@link android.app.Activity}. This happens so that the
 * Activity can be garbage collected and does not create a memory leak. For that reason, the
 * {@link com.appboy.ui.slideups.AppboySlideupManager#registerSlideupManager(android.app.Activity)}
 * and {@link com.appboy.ui.slideups.AppboySlideupManager#unregisterSlideupManager(android.app.Activity)}
 * must be called in the {@link android.app.Activity#onResume()} and {@link android.app.Activity#onPause()}
 * methods of every Activity.
 */
public final class AppboySlideupManager {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboySlideupManager.class.getName());
  private static volatile AppboySlideupManager sInstance = null;

  private final Stack<Slideup> mSlideupStack = new Stack<Slideup>();
  private final IAppboyNavigator mDefaultAppboyNavigator = new AppboyNavigator();
  private Activity mActivity;
  private IEventSubscriber<SlideupEvent> mSlideupEventSubscriber;
  private ISlideupManagerListener mCustomSlideupManagerListener;
  private ISlideupViewFactory mCustomSlideupViewFactory;
  private SlideupViewWrapper mSlideupViewWrapper;
  private Slideup mCarryoverSlideup;
  private AtomicBoolean mDisplayingSlideup = new AtomicBoolean(false);

  public static AppboySlideupManager getInstance() {
    if (sInstance == null) {
      synchronized (AppboySlideupManager.class) {
        if (sInstance == null) {
          sInstance = new AppboySlideupManager();
        }
      }
    }
    return sInstance;
  }

  /**
   * Registers the slideup manager, which will listen to and display incoming slideup messages. The
   * current Activity is required in order to properly inflate and display the slideup view.
   *
   * Important note: Every Activity must call registerSlideupManager in the onResume lifecycle
   * method, otherwise slideup messages may be lost!
   *
   * @param activity The current Activity.
   */
  public void registerSlideupManager(Activity activity) {
    // We need the current Activity so that we can inflate or programmatically create the slideup
    // View for each Activity. We cannot share the View because doing so would create a memory leak.
    mActivity = activity;

    // We have a special check to see if the host app switched to a different Activity (or recreated
    // the same Activity during an orientation change) so that we can redisplay the slideup.
    if (mCarryoverSlideup != null) {
      mCarryoverSlideup.setAnimateIn(false);
      displaySlideup(mCarryoverSlideup);
      mCarryoverSlideup = null;
    }

    // Every time the AppboySlideupManager is registered to an Activity, we add a slideup subscriber
    // which listens to new slideups, adds it to the stack, and displays it if it can.
    mSlideupEventSubscriber = createSlideupEventSubscriber();
    Appboy.getInstance(activity).subscribeToNewSlideups(mSlideupEventSubscriber);
  }

  /**
   * Unregisters the slideup manager.
   *
   * @param activity The current Activity.
   */
  public void unregisterSlideupManager(Activity activity) {
    // If there is slideup being displayed when the host app transitions to another Activity (or
    // requests an orientation change), we save it in memory so that we can redisplay it when the
    // operation is done.
    if (mSlideupViewWrapper != null) {
      mCarryoverSlideup = mSlideupViewWrapper.getSlideup();
      ViewUtils.removeViewFromParent(mSlideupViewWrapper.getSlideupView());
      mSlideupViewWrapper = null;
    } else {
      mCarryoverSlideup = null;
    }

    // Slideup subscriptions are per Activity, so we must remove the subscriber when the host app
    // unregisters the slideup manager.
    Appboy.getInstance(activity).removeSingleSubscription(mSlideupEventSubscriber, SlideupEvent.class);
  }

  /**
   * Assigns a custom ISlideupManagerListener that will be used when displaying slideups. To revert
   * back to the default ISlideupManagerListener, call the setCustomSlideupManagerListener method with
   * null.
   *
   * @param slideupManagerListener A custom ISlideupManagerListener or null (to revert back to the
   *                               default ISlideupManagerListener).
   */
  public void setCustomSlideupManagerListener(ISlideupManagerListener slideupManagerListener) {
    mCustomSlideupManagerListener = slideupManagerListener;
  }

  /**
   * Assigns a custom ISlideupViewFactory that will be used to create the slideup View. To revert
   * back to the default ISlideupViewFactory, call the setCustomSlideupViewFactory method with null.
   *
   * @param slideupViewFactory A custom ISlideupViewFactory or null (to revert back to the default
   *                           ISlideupViewFactory).
   */
  public void setCustomSlideupViewFactory(ISlideupViewFactory slideupViewFactory) {
    mCustomSlideupViewFactory = slideupViewFactory;
  }

  /**
   * Provides a slideup that will then be handled by the slideup manager. If no slideup is being
   * displayed, it will attempt to display the slideup immediately.
   *
   * @param slideup The slideup to add.
   */
  public void addSlideup(Slideup slideup) {
    mSlideupStack.push(slideup);
    requestDisplaySlideup();
  }

  /**
   * Asks the SlideupManager to display the next slideup if one is not currently being displayed.
   * If one is being displayed, this method will return false and will not display the next slideup.
   *
   * @return A boolean value indicating whether a slideup was displayed.
   */
  public boolean requestDisplaySlideup() {
    if (!mDisplayingSlideup.compareAndSet(false, true)) {
      Log.d(TAG, "A slideup is currently being displayed. Ignoring request to display slideup.");
      return false;
    }
    if (mSlideupStack.isEmpty()) {
      Log.d(TAG, "The slideup stack is empty. No slideup will be displayed.");
      mDisplayingSlideup.set(false);
      return false;
    }

    final Slideup slideup = mSlideupStack.pop();
    SlideupOperation slideupOperation = getSlideupManagerListener().beforeSlideupDisplayed(slideup);

    switch (slideupOperation) {
      case DISPLAY_NOW:
        Log.d(TAG, "The ISlideupManagerListener method beforeSlideupDisplayed returned DISPLAY_NOW. The " +
            "slideup will be displayed.");
        break;
      case DISPLAY_LATER:
        Log.d(TAG, "The ISlideupManagerListener method beforeSlideupDisplayed returned DISPLAY_LATER. The " +
            "slideup will be pushed back onto the stack.");
        mSlideupStack.push(slideup);
        mDisplayingSlideup.set(false);
        return false;
      case DISCARD:
        Log.d(TAG, "The ISlideupManagerListener method beforeSlideupDisplayed returned DISCARD. The " +
            "slideup will not be displayed and will not be put back on the stack.");
        mDisplayingSlideup.set(false);
        return false;
      default:
        Log.e(TAG, "The ISlideupManagerListener method beforeSlideupDisplayed returned null instead of a " +
            "SlideupOperation. Ignoring the slideup. Please check the ISlideupStackBehaviour " +
            "implementation.");
        mDisplayingSlideup.set(false);
        return false;
    }

    Activity activity = mActivity;
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          displaySlideup(slideup);
        }
      });
      return true;
    } else {
      Log.e(TAG, "Cannot display the slideup because the Activity was null.");
      mDisplayingSlideup.set(false);
      return false;
    }
  }

  public void hideCurrentSlideup(boolean animate) {
    SlideupViewWrapper slideupWrapperView = mSlideupViewWrapper;
    if (slideupWrapperView != null) {
      Slideup slideup = slideupWrapperView.getSlideup();
      if (slideup != null) {
        slideup.setAnimateOut(animate);
      }
      slideupWrapperView.close();
    }
  }

  private ISlideupManagerListener getSlideupManagerListener() {
    return mCustomSlideupManagerListener != null ? mCustomSlideupManagerListener : mDefaultSlideupManagerListener;
  }

  private ISlideupViewFactory getSlideupViewFactory() {
    return mCustomSlideupViewFactory != null ? mCustomSlideupViewFactory : mDefaultSlideupViewFactory;
  }

  private boolean displaySlideup(Slideup slideup) {
    final View slideupView = getSlideupViewFactory().createSlideupView(mActivity, slideup);

    if (slideupView == null) {
      Log.e(TAG, "The slideup view returned from the ISlideupViewFactory was null. The slideup will " +
          "not be displayed and will not be put back on the stack.");
      mDisplayingSlideup.set(false);
      return false;
    }

    if (slideupView.getParent() != null) {
      Log.e(TAG, "The slideup view returned from the ISlideupViewFactory already has a parent. This " +
          "is a sign that the view is being reused. The ISlideupViewFactory method createSlideupView" +
          "must return a new view without a parent. The slideup will not be displayed and will not " +
          "be put back on the stack.");
      mDisplayingSlideup.set(false);
      return false;
    }

    mSlideupViewWrapper = new SlideupViewWrapper(slideupView, slideup, mSlideupViewLifecycleListener);
    FrameLayout root = (FrameLayout) mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
    mSlideupViewWrapper.open(root);
    return true;
  }

  private IEventSubscriber<SlideupEvent> createSlideupEventSubscriber() {
    return new IEventSubscriber<SlideupEvent>() {
      @Override
      public void trigger(SlideupEvent event) {
        if (getSlideupManagerListener().onSlideupReceived(event.getSlideup())) {
          return;
        }
        addSlideup(event.getSlideup());
      }
    };
  }

  private ISlideupManagerListener mDefaultSlideupManagerListener = new ISlideupManagerListener() {
    @Override
    public boolean onSlideupReceived(Slideup slideup) {
      return false;
    }

    @Override
    public SlideupOperation beforeSlideupDisplayed(Slideup slideup) {
      return SlideupOperation.DISPLAY_NOW;
    }

    @Override
    public boolean onSlideupClicked(Slideup slideup, SlideupCloser slideupCloser) {
      return false;
    }

    @Override
    public void onSlideupDismissed(Slideup slideup) {
    }
  };

  private ISlideupViewFactory mDefaultSlideupViewFactory = new ISlideupViewFactory() {
    @Override
    public View createSlideupView(Activity activity, Slideup slideup) {
      View slideupView = activity.getLayoutInflater().inflate(R.layout.com_appboy_slideup_view, null);
      TextView message = (TextView) slideupView.findViewById(R.id.com_appboy_slideup_message);
      message.setText(slideup.getMessage());
      if (slideup.getClickAction() == ClickAction.NONE) {
        ImageView chevron = (ImageView) slideupView.findViewById(R.id.com_appboy_slideup_chevron);
        chevron.setVisibility(View.GONE);
      }
      return slideupView;
    }
  };

  private final ISlideupViewLifecycleListener mSlideupViewLifecycleListener = new ISlideupViewLifecycleListener() {
    @Override
    public void beforeOpened(View slideupView, Slideup slideup) {
      Log.d(TAG, "SlideupViewWrapper.ISlideupViewLifecycleListener.beforeOpened called.");
      slideup.logImpression();
    }

    @Override
    public void afterOpened(View slideupView, Slideup slideup) {
      Log.d(TAG, "SlideupViewWrapper.ISlideupViewLifecycleListener.afterOpened called.");
    }

    @Override
    public void beforeClosed(View slideupView, Slideup slideup) {
      Log.d(TAG, "SlideupViewWrapper.ISlideupViewLifecycleListener.beforeClosed called.");
    }

    @Override
    public void afterClosed(Slideup slideup) {
      mSlideupViewWrapper = null;
      Log.d(TAG, "SlideupViewWrapper.ISlideupViewLifecycleListener.afterClosed called.");
      mDisplayingSlideup.set(false);
    }

    @Override
    public void onClicked(SlideupCloser slideupCloser, View slideupView, Slideup slideup) {
      Log.d(TAG, "SlideupViewWrapper.ISlideupViewLifecycleListener.onClicked called.");
      slideup.logClick();

      // Perform the slideup clicked listener action from the host application first. This give
      // the app the option to override the values that are sent from the server and handle the
      // slideup differently depending on where the user is in the app.
      //
      // To modify the default slideup clicked behavior, mutate the necessary slideup members. As
      // an example, if the slideup were to navigate to the news feed when it was clicked, the
      // behavior can be cancelled by setting the click action to NONE.
      boolean handled = getSlideupManagerListener().onSlideupClicked(slideup, slideupCloser);

      if (!handled) {
        // Perform the default (or modified) slideup clicked behavior.
        performSlideupClicked(slideup, slideupCloser);
      }
    }

    @Override
    public void onDismissed(View slideupView, Slideup slideup) {
      getSlideupManagerListener().onSlideupDismissed(slideup);
    }

    private void performSlideupClicked(Slideup slideup, SlideupCloser slideupCloser) {
      switch(slideup.getClickAction()) {
        case NEWS_FEED:
          slideup.setAnimateOut(false);
          slideupCloser.close(false);
          getAppboyNavigator().gotoNewsFeed(mActivity, BundleUtils.mapToBundle(slideup.getExtras()));
          break;
        case URI:
          slideup.setAnimateOut(false);
          slideupCloser.close(false);
          getAppboyNavigator().gotoURI(mActivity, slideup.getUri(), BundleUtils.mapToBundle(slideup.getExtras()));
          break;
        case NONE:
          slideupCloser.close(true);
          break;
        default:
          slideupCloser.close(false);
          break;
      }
    }
  };

  private IAppboyNavigator getAppboyNavigator() {
    IAppboyNavigator customAppboyNavigator = Appboy.getInstance(mActivity).getAppboyNavigator();
    return customAppboyNavigator != null ? customAppboyNavigator : mDefaultAppboyNavigator;
  }
}
