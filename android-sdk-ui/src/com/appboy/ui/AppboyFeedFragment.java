package com.appboy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.appboy.Appboy;
import com.appboy.events.FeedUpdatedEvent;
import com.appboy.events.IEventSubscriber;
import com.appboy.models.cards.Card;
import com.appboy.ui.adapters.AppboyListAdapter;

import java.util.ArrayList;

public class AppboyFeedFragment extends ListFragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedFragment.class.getName());
  private static int NETWORK_PROBLEM_WARNING_MS = 5000;
  private static int MAX_FEED_TTL_SECONDS = 60;

  private final Handler mMainThreadLooper = new Handler(Looper.getMainLooper());
  // Shows the network error message. This should only be executed on the Main/UI thread.
  private final Runnable mShowNetworkError = new Runnable() {
    @Override
    public void run() {
      // null checks make sure that this only executes when the constituent views are valid references.
      if (mLoadingSpinner != null) {
        mLoadingSpinner.setVisibility(View.GONE);
      }
      if (mNetworkErrorLayout != null) {
        mNetworkErrorLayout.setVisibility(View.VISIBLE);
      }
    }
  };

  private Appboy mAppboy;
  private IEventSubscriber<FeedUpdatedEvent> mFeedUpdatedSubscriber;
  private AppboyListAdapter mAdapter;
  private LinearLayout mNetworkErrorLayout;
  private LinearLayout mEmptyFeedLayout;
  private ProgressBar mLoadingSpinner;
  private boolean mSkipCardImpressionsReset;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    mAppboy = Appboy.getInstance(activity);
    if (mAdapter == null) {
      mAdapter = new AppboyListAdapter(activity, R.id.tag, new ArrayList<Card>());
    }
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View view = layoutInflater.inflate(R.layout.com_appboy_feed, container, false);
    mNetworkErrorLayout = (LinearLayout) view.findViewById(R.id.com_appboy_feed_network_error);
    mLoadingSpinner = (ProgressBar) view.findViewById(R.id.com_appboy_feed_loading_spinner);
    mEmptyFeedLayout = (LinearLayout) view.findViewById(R.id.com_appboy_feed_empty_feed);
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (mSkipCardImpressionsReset) {
      mSkipCardImpressionsReset = false;
    } else {
      mAdapter.resetCardImpressionTracker();
      Log.d(TAG, "Resetting card impressions.");
    }

    // Applying top and bottom padding as header and footer views allows for the top and bottom padding to be scrolled
    // away, as opposed to being a permanent frame around the feed.
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    final ListView listView = getListView();
    listView.addHeaderView(inflater.inflate(R.layout.com_appboy_feed_header, null));
    listView.addFooterView(inflater.inflate(R.layout.com_appboy_feed_footer, null));

    // Remove the previous subscriber before rebuilding a new one with our new activity.
    mAppboy.removeSingleSubscription(mFeedUpdatedSubscriber, FeedUpdatedEvent.class);
    mFeedUpdatedSubscriber = new IEventSubscriber<FeedUpdatedEvent>() {
      @Override
      public void trigger(final FeedUpdatedEvent event) {
        Activity activity = getActivity();
        // Not strictly necessary, but being defensive in the face of a lot of inconsistent behavior with
        // fragment/activity lifecycles.
        if (activity == null) {
          return;
        }

        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Log.d(TAG, "Updating feed views in response to FeedUpdatedEvent: " + event);
            // If a FeedUpdatedEvent comes in, we make sure that the network error isn't visible. It could become
            // visible again later if we need to request a new feed and it doesn't return in time, but we display a
            // network spinner while we wait, instead of keeping the network error up.
            mMainThreadLooper.removeCallbacks(mShowNetworkError);
            mNetworkErrorLayout.setVisibility(View.GONE);

            // If there are no cards, regardless of what happens further down, we're not going to show the list view, so
            // clear the list view and change relevant visibility now.
            if (event.getCardCount() == 0) {
              listView.setVisibility(View.GONE);
              mAdapter.clear();
            } else {
              mEmptyFeedLayout.setVisibility(View.GONE);
              mLoadingSpinner.setVisibility(View.GONE);
            }

            // If we our feed from offline storage, and it was old, we asynchronously request a new one from the server,
            // putting up a spinner if the old feed was empty.
            if (event.isFromOfflineStorage() && (event.lastUpdatedInSecondsFromEpoch() + MAX_FEED_TTL_SECONDS) * 1000 < System.currentTimeMillis()) {
              Log.i(TAG, String.format("Feed received was older than the max time to live of %d seconds, displaying it " +
                  "for now, but requesting an updated view from the server.", MAX_FEED_TTL_SECONDS));
              mAppboy.requestFeedRefresh();
              // If we don't have any cards to display, we put up the spinner while we wait for the network to return.
              // Eventually displaying an error message if it doesn't.
              if (event.getCardCount() == 0) {
                Log.d(TAG, String.format("Old feed was empty, putting up a network spinner and registering the network error message on a delay of %dms.",
                    NETWORK_PROBLEM_WARNING_MS));
                mEmptyFeedLayout.setVisibility(View.GONE);
                mLoadingSpinner.setVisibility(View.VISIBLE);
                mMainThreadLooper.postDelayed(mShowNetworkError, NETWORK_PROBLEM_WARNING_MS);
                return;
              }
            }

            // If we get here, we know that our feed is either fresh from the cache, or came down directly from a
            // network request. Thus, an empty feed shouldn't have a network error, or a spinner, we should just
            // tell the user that the feed is empty.
            if (event.getCardCount() == 0) {
              mLoadingSpinner.setVisibility(View.GONE);
              mEmptyFeedLayout.setVisibility(View.VISIBLE);
            } else {
              mAdapter.replaceFeed(event.getFeedCards());
              listView.setVisibility(View.VISIBLE);
            }
          }
        });
      }
    };
    mAppboy.subscribeToFeedUpdates(mFeedUpdatedSubscriber);

    // Once the header and footer views are set and our event handlers are ready to go, we set the adapter and hit the
    // cache for an initial feed load.
    listView.setAdapter(mAdapter);
    mAppboy.requestFeedRefreshFromCache();
  }

  @Override
  public void onResume() {
    super.onResume();
    Appboy.getInstance(getActivity()).logFeedDisplayed();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    // If the view is destroyed, we don't care about updating it anymore. Remove the subscription immediately.
    mAppboy.removeSingleSubscription(mFeedUpdatedSubscriber, FeedUpdatedEvent.class);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    setListAdapter(null);
  }

  // The onSaveInstanceState method gets called before an orientation change when either the fragment is
  // the current fragment or exists in the fragment manager backstack.
  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // We set mSkipCardImpressionsReset to true only when onSaveInstanceState is called while the fragment
    // is visible on the screen. That happens when the fragment is being managed by the fragment manager and
    // it is not in the backstack. We do this to avoid setting the mSkipCardImpressionsReset flag when the
    // device undergoes an orientation change while the fragment is in the backstack.
    if (isVisible()) {
      mSkipCardImpressionsReset = true;
    }
  }
}