package com.appboy.ui;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;
import com.appboy.Appboy;
import com.appboy.events.IEventSubscriber;
import com.appboy.events.SlideupEvent;
import com.appboy.models.Slideup;
import com.appboy.ui.widget.SlideupDrawerView;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The AppboySlideupManager provides support for listening to and displaying slideup messages.
 *
 * The AppboySlideupManager consists of a single SlideupDrawerView which is attached to the current Activity's view
 * in registerSlideupUI(Activity) and detached in unregisterSlideupUI(Activity). To show a slideup in any Activity,
 * EVERY Activity should call registerSlideupUI(Activity) and registerSlideupUI(Activity) in the Activity's onResume()
 * and onPause() lifecycle method respectively. Forgetting to include these method calls can result in the SlideupDrawerView
 * not being attached to the current Activity's view and can also result in loss of slideup messages.
 *
 * This class contains a list of incoming slideup messages which are quesed and and displayed sequentially with a one
 * second interval between messages.
 *
 * This class is not thread-safe and should only be used from the UI thread.
 */
// TODO(martin) - Add support for queued slideups.
public final class AppboySlideupManager {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboySlideupManager.class.getName());
  private static final long TIME_INTERVAL_BETWEEN_SLIDEUPS_MS = 1000;
  private static volatile AppboySlideupManager sInstance = null;

  private final Queue<Slideup> mSlideupQueue;
  private IEventSubscriber<SlideupEvent> mSlideupEventSubscriber;
  private final SlideupDrawerView.OnSlideupDrawerCloseListener mSlideupDrawerCloseListener;
  private final Runnable mAnimateClosedRunnable;
  // Single slideup drawer that gets attached/detached from the the current Activity's view.
  private SlideupDrawerView mSlideupDrawerView;
  private boolean mShowingQueuedSlideups = false;

  /**
   * Static method that returns the AppboySlideupManager singleton. All activities should register to listen
   * to slideup events using this instance.
   *
   * @return the singleton AppboySlideupManager instance
   */
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

  private AppboySlideupManager() {
    mSlideupQueue = new LinkedList<Slideup>();

    mSlideupDrawerCloseListener = new SlideupDrawerView.OnSlideupDrawerCloseListener() {
      @Override
      public void onDrawerClosed() {
        Runnable checkQueueAndDisplayRunnable = new Runnable() {
          @Override
          public void run() {
            removeCurrentSlideupFromQueue();
            checkQueueAndDisplay();
          }
        };
        mSlideupDrawerView.postDelayed(checkQueueAndDisplayRunnable, TIME_INTERVAL_BETWEEN_SLIDEUPS_MS);
      }
    };
    mAnimateClosedRunnable = new Runnable() {
      @Override
      public void run() {
        mSlideupDrawerView.animateClose();
      }
    };
  }

  /**
   * Registers the AppboySlideupManager to listen to and display Appboy slideups for the current Activity.
   * This should be called in the onResume() method of the current Activity.
   *
   * @param activity The current activity
   */
  public boolean registerSlideupUI(final Activity activity) {
    if (mSlideupEventSubscriber != null) {
      Log.w(TAG, "Received a call to registerSlideupUI, but the event subscriber was still set. This usually means " +
          "that the call to unregisterSlideupUI was not made appropriately. Cleaning up the subscription and continuing...");
      Appboy.getInstance(activity).removeSingleSubscription(mSlideupEventSubscriber, SlideupEvent.class);
    }

    mSlideupEventSubscriber = new IEventSubscriber<SlideupEvent>() {
      @Override
      public void trigger(final SlideupEvent event) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Slideup slideup = event.getSlideup();
            if (mSlideupQueue.isEmpty()) {
              mSlideupQueue.add(slideup);
              checkQueueAndDisplay();
            } else {
              mSlideupQueue.add(slideup);
            }
      }});
    }};

    Appboy.getInstance(activity).subscribeToNewSlideups(mSlideupEventSubscriber);
    inflateSlideupDrawerAndInitialize(activity);
    return attachSlideupDrawer(activity);
  }

  /**
   * Unregisters the AppboySlideupManager. After a call to this method, the Activty will no longer receive
   * and display Appboy slideups. This should be called in the onPause() method of the current Activity.
   *
   * @param activity The current activity
   */
  public boolean unregisterSlideupUI(Activity activity) {
    if (mSlideupEventSubscriber != null) {
      Appboy.getInstance(activity).removeSingleSubscription(mSlideupEventSubscriber, SlideupEvent.class);
      mSlideupEventSubscriber = null;
    }
    if (mSlideupDrawerView != null) {
      forceCloseSlideupDrawer();
      // Removing the current slideup that's being displayed so that we don't show it again when we switch activities.
      removeCurrentSlideupFromQueue();
    }
    return detachSlideupDrawer(activity);
  }

  private void inflateSlideupDrawerAndInitialize(Activity activity) {
    if (mSlideupDrawerView == null) {
      mSlideupDrawerView = (SlideupDrawerView) activity.getLayoutInflater().inflate(R.layout.com_appboy_slideup_drawer, null);
      mSlideupDrawerView.setOnDrawerCloseListener(mSlideupDrawerCloseListener);
    }
  }

  /**
   * Attaches the slideup drawer to the android.R.id.content ViewGroup (which is a FrameLayout), if not already attached.
   */
  private boolean attachSlideupDrawer(Activity activity) {
    FrameLayout root = (FrameLayout) activity.findViewById(android.R.id.content);
    if (root.indexOfChild(mSlideupDrawerView) == -1) {
      root.addView(mSlideupDrawerView);
      return true;
    } else {
      Log.e(TAG, "SlideupDrawerView has already been attached.");
      return false;
    }
  }

  /**
   * Detaches the slideup drawer from the android.R.id.content ViewGroup (which is a FrameLayout), if attached.
   */
  private boolean detachSlideupDrawer(Activity activity) {
    FrameLayout root = (FrameLayout) activity.findViewById(android.R.id.content);
    if (root.indexOfChild(mSlideupDrawerView) == -1) {
      Log.e(TAG, "There is no SlideupDrawerView in the current activity's view hierarchy.");
      return false;
    } else {
      root.removeView(mSlideupDrawerView);
      return true;
    }
  }

  private void removeCurrentSlideupFromQueue() {
    Slideup slideup = mSlideupDrawerView.getSlideup();
    mSlideupQueue.remove(slideup);
  }

  private void checkQueueAndDisplay() {
    Slideup slideup = mSlideupQueue.peek();
    if (slideup != null) {
      displaySlideup(slideup);
    } else {
      mShowingQueuedSlideups = false;
    }
  }

  private void displaySlideup(Slideup slideup) {
    mSlideupDrawerView.setSlideup(slideup);
    mSlideupDrawerView.animateOpen();
    mSlideupDrawerView.postDelayed(mAnimateClosedRunnable, slideup.getDurationInMilliseconds());
    Appboy.getInstance(mSlideupDrawerView.getContext()).logSlideupShown();
  }

  private void forceCloseSlideupDrawer() {
    mSlideupDrawerView.removeCallbacks(mAnimateClosedRunnable);
    mSlideupDrawerView.close();
  }
}
