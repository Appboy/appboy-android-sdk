package com.appboy.ui;

import android.app.Activity;
import android.database.DataSetObserver;
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
import android.widget.TextView;
import com.appboy.Appboy;
import com.appboy.events.FeedUpdatedEvent;
import com.appboy.events.IEventSubscriber;
import com.appboy.models.cards.ICard;
import com.appboy.ui.adapters.AppboyListAdapter;

import java.util.ArrayList;

public class AppboyFeedFragment extends ListFragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedFragment.class.getName());
  private static int NETWORK_PROBLEM_WARNING_MS = 5000;

  private Appboy mAppboy;
  private IEventSubscriber<FeedUpdatedEvent> mFeedUpdatedSubscriber;
  private AppboyListAdapter mAdapter;
  private LinearLayout mNetworkErrorLayout;
  private LinearLayout mEmptyFeedLayout;
  private ProgressBar mLoadingSpinner;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    mAppboy = Appboy.getInstance(activity);
    mAdapter = new AppboyListAdapter(activity, R.id.tag, new ArrayList<ICard>());

    mFeedUpdatedSubscriber = new IEventSubscriber<FeedUpdatedEvent>() {
      @Override
      public void trigger(final FeedUpdatedEvent event) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (event.isFromOfflineStorage() && event.getFeedCards().isEmpty()) {
              Log.i(TAG, "Got result from offline storage, but it was empty, so waiting for result from web request.");
              return;
            }
            if (!mAdapter.isEmpty() && event.isFromOfflineStorage()) {
              Log.i(TAG, "Ignoring feed from offline storage because the feed was already non-empty.");
              return;
            }

            Log.i(TAG, String.format("Clearing existing feed and adding %d more cards.", event.getFeedCards().size()));
            mAdapter.clear();
            mAdapter.addCards(event.getFeedCards());
            mAdapter.notifyDataSetChanged();
          }
        });
      }
    };
    mAppboy.subscribeToFeedUpdates(mFeedUpdatedSubscriber);
    mAppboy.requestFeedRefresh();
    mAppboy.requestFeedRefreshFromCache();
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
    // Applying top and bottom padding like this allows for the top and bottom padding to be scrolled away, as opposed
    // to being a permanent frame around the feed.
    TextView padding = new TextView(getActivity());
    padding.setHeight(0);

    final ListView listView = getListView();
    listView.addHeaderView(padding);
    listView.addFooterView(padding);

    // We need to wait to call setAdapter until after we've added the header and foot views, otherwise Android gets mad.
    listView.setAdapter(mAdapter);

    mAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        super.onChanged();
        Log.d(TAG, "Feed list adapter changed. Updating visibility settings.");

        // Get rid of the loading spinner, if it's around.
        mLoadingSpinner.setVisibility(View.GONE);

        // Depending on the state of the adapter, we either show an empty feed message, or we display the list of cards.
        if (mAdapter.isEmpty()) {
          mEmptyFeedLayout.setVisibility(View.VISIBLE);
          listView.setVisibility(View.GONE);
        } else {
          mEmptyFeedLayout.setVisibility(View.GONE);
          listView.setVisibility(View.VISIBLE);
        }
      }
    });

    // If our request to refreshAll() has already returned, we've missed the chance for the adapter to notify the view
    // that it's data set changed, so we do it manually.
    if (mAdapter.hasReceivedFeed()) {
      mAdapter.notifyDataSetChanged();
    } else {
      final Handler handler = new Handler(Looper.getMainLooper());
      final Runnable showNetworkError = new Runnable() {
        @Override
        public void run() {
          mLoadingSpinner.setVisibility(View.GONE);
          mNetworkErrorLayout.setVisibility(View.VISIBLE);
        }
      };
      handler.postDelayed(showNetworkError, NETWORK_PROBLEM_WARNING_MS);

      // Setup a temporary observer on the list adapter which will remove the NetworkError runnable from the delay
      // queue (if it hasn't run yet) and will make sure that the error is not visible.
      DataSetObserver stopNetworkErrorRunnableObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
          super.onChanged();
          handler.removeCallbacks(showNetworkError);
          mNetworkErrorLayout.setVisibility(View.GONE);

          // Since this should only run once, we remove the data set observer right after executing.
          mAdapter.unregisterDataSetObserver(this);
        }
      };
      mAdapter.registerDataSetObserver(stopNetworkErrorRunnableObserver);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Appboy.getInstance(getActivity()).logFeedDisplayed();
    mAdapter.resetCardImpressionTracker();
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mAppboy.removeSingleSubscription(mFeedUpdatedSubscriber, FeedUpdatedEvent.class);
    setListAdapter(null);
  }
}
