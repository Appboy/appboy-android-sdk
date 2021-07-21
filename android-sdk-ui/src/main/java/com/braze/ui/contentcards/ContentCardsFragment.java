package com.braze.ui.contentcards;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appboy.events.IEventSubscriber;
import com.appboy.models.cards.Card;
import com.appboy.ui.R;
import com.braze.Braze;
import com.braze.events.ContentCardsUpdatedEvent;
import com.braze.events.SdkDataWipeEvent;
import com.braze.support.BrazeLogger;
import com.braze.ui.contentcards.adapters.ContentCardAdapter;
import com.braze.ui.contentcards.adapters.EmptyContentCardsAdapter;
import com.braze.ui.contentcards.handlers.DefaultContentCardsUpdateHandler;
import com.braze.ui.contentcards.handlers.DefaultContentCardsViewBindingHandler;
import com.braze.ui.contentcards.handlers.IContentCardsUpdateHandler;
import com.braze.ui.contentcards.handlers.IContentCardsViewBindingHandler;
import com.braze.ui.contentcards.recycler.ContentCardsDividerItemDecoration;
import com.braze.ui.contentcards.recycler.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment to display Braze ContentCards.
 */
public class ContentCardsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
  private static final String TAG = BrazeLogger.getBrazeLogTag(ContentCardsFragment.class);
  private static final int MAX_CONTENT_CARDS_TTL_SECONDS = 60;
  private static final long NETWORK_PROBLEM_WARNING_MS = 5000L;
  private static final long AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS = 2500L;
  private static final String LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY = "LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY";
  private static final String KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY = "KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY";
  private static final String VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY = "VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY";
  private static final String UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY = "UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY";

  private final Handler mMainThreadLooper = new Handler(Looper.getMainLooper());
  protected Runnable mDefaultNetworkUnavailableRunnable;

  @Nullable
  protected RecyclerView mRecyclerView;
  @Nullable
  protected ContentCardAdapter mCardAdapter;
  @Nullable
  protected EmptyContentCardsAdapter mDefaultEmptyContentCardsAdapter;
  @Nullable
  protected SwipeRefreshLayout mContentCardsSwipeLayout;

  protected IEventSubscriber<ContentCardsUpdatedEvent> mContentCardsUpdatedSubscriber;
  protected IEventSubscriber<SdkDataWipeEvent> mSdkDataWipeEventSubscriber;
  protected final IContentCardsUpdateHandler mDefaultContentCardUpdateHandler = new DefaultContentCardsUpdateHandler();
  protected IContentCardsUpdateHandler mCustomContentCardUpdateHandler;
  protected final IContentCardsViewBindingHandler mDefaultContentCardsViewBindingHandler = new DefaultContentCardsViewBindingHandler();
  protected IContentCardsViewBindingHandler mCustomContentCardsViewBindingHandler;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDefaultNetworkUnavailableRunnable = new NetworkUnavailableRunnable(getContext());
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.com_braze_content_cards, container, false);

    mRecyclerView = rootView.findViewById(R.id.com_braze_content_cards_recycler);
    mContentCardsSwipeLayout = rootView.findViewById(R.id.appboy_content_cards_swipe_container);
    mContentCardsSwipeLayout.setOnRefreshListener(this);
    mContentCardsSwipeLayout.setColorSchemeResources(R.color.com_braze_content_cards_swipe_refresh_color_1,
        R.color.com_braze_content_cards_swipe_refresh_color_2,
        R.color.com_braze_content_cards_swipe_refresh_color_3,
        R.color.com_braze_content_cards_swipe_refresh_color_4);
    return rootView;
  }

  /**
   * Called when the user swipes down and requests a feed refresh.
   */
  @Override
  public void onRefresh() {
    Braze.getInstance(getContext()).requestContentCardsRefresh(false);
    mMainThreadLooper.postDelayed(() -> mContentCardsSwipeLayout.setRefreshing(false), AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Remove the previous subscriber before rebuilding a new one with our new activity.
    Braze.getInstance(getContext()).removeSingleSubscription(mContentCardsUpdatedSubscriber, ContentCardsUpdatedEvent.class);
    if (mContentCardsUpdatedSubscriber == null) {
      mContentCardsUpdatedSubscriber = event -> handleContentCardsUpdatedEvent(event);
    }
    Braze.getInstance(getContext()).subscribeToContentCardsUpdates(mContentCardsUpdatedSubscriber);
    Braze.getInstance(getContext()).requestContentCardsRefresh(true);
    Braze.getInstance(getContext()).logContentCardsDisplayed();

    Braze.getInstance(getContext()).removeSingleSubscription(mSdkDataWipeEventSubscriber, SdkDataWipeEvent.class);
    if (mSdkDataWipeEventSubscriber == null) {
      // If the SDK data is wiped, then we want to clear any cached Content Cards
      mSdkDataWipeEventSubscriber = event -> handleContentCardsUpdatedEvent(ContentCardsUpdatedEvent.getEmptyUpdate());
    }
    Braze.getInstance(getContext()).addSingleSynchronousSubscription(mSdkDataWipeEventSubscriber, SdkDataWipeEvent.class);
  }

  @Override
  public void onPause() {
    super.onPause();
    // If the view is going away, we don't care about updating it anymore. Remove the subscription immediately.
    Braze.getInstance(getContext()).removeSingleSubscription(mContentCardsUpdatedSubscriber, ContentCardsUpdatedEvent.class);
    Braze.getInstance(getContext()).removeSingleSubscription(mSdkDataWipeEventSubscriber, SdkDataWipeEvent.class);
    mMainThreadLooper.removeCallbacks(mDefaultNetworkUnavailableRunnable);
    mCardAdapter.markOnScreenCardsAsRead();
  }

  /**
   * @return The custom card update handler if not null. Else returns the default handler.
   */
  public IContentCardsUpdateHandler getContentCardUpdateHandler() {
    return mCustomContentCardUpdateHandler != null ? mCustomContentCardUpdateHandler : mDefaultContentCardUpdateHandler;
  }

  /**
   * Sets an {@link IContentCardsUpdateHandler} for this {@link ContentCardsFragment}.
   *
   * @param contentCardUpdateHandler a handler for doing any work on {@link Card}s before being rendered in the ContentCards.
   */
  public void setContentCardUpdateHandler(@Nullable IContentCardsUpdateHandler contentCardUpdateHandler) {
    mCustomContentCardUpdateHandler = contentCardUpdateHandler;
  }

  /**
   * @return The custom view binding handler if not null. Else returns the default handler.
   */
  public IContentCardsViewBindingHandler getContentCardsViewBindingHandler() {
    return mCustomContentCardsViewBindingHandler != null ? mCustomContentCardsViewBindingHandler : mDefaultContentCardsViewBindingHandler;
  }

  /**
   * Sets the {@link IContentCardsViewBindingHandler}. Note that this method should
   * only be called before the {@link ContentCardsFragment} is first displayed
   * or the {@link ContentCardAdapter} will not update correctly.
   *
   * @param contentCardsViewBindingHandler The {@link IContentCardsViewBindingHandler}
   *                                       responsible for rendering each {@link Card} in the {@link RecyclerView}.
   */
  public void setContentCardsViewBindingHandler(@Nullable IContentCardsViewBindingHandler contentCardsViewBindingHandler) {
    mCustomContentCardsViewBindingHandler = contentCardsViewBindingHandler;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
      outState.putParcelable(LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }
    if (mCardAdapter != null) {
      outState.putStringArrayList(KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY, (ArrayList<String>) mCardAdapter.getImpressedCardIds());
    }
    if (mCustomContentCardsViewBindingHandler != null) {
      outState.putParcelable(VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY, mCustomContentCardsViewBindingHandler);
    }
    if (mCustomContentCardUpdateHandler != null) {
      outState.putParcelable(UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY, mCustomContentCardUpdateHandler);
    }
  }

  @Override
  public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if (savedInstanceState != null) {
      IContentCardsUpdateHandler updateHandlerParcelable = savedInstanceState.getParcelable(UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY);
      if (updateHandlerParcelable != null) {
        setContentCardUpdateHandler(updateHandlerParcelable);
      }
      IContentCardsViewBindingHandler viewBindingHandlerParcelable = savedInstanceState.getParcelable(VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY);
      if (viewBindingHandlerParcelable != null) {
        setContentCardsViewBindingHandler(viewBindingHandlerParcelable);
      }
      mMainThreadLooper.post(() -> {
        Parcelable layoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY);
        if (mRecyclerView != null) {
          RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
          if (layoutManagerState != null && layoutManager != null) {
            layoutManager.onRestoreInstanceState(layoutManagerState);
          }
        }
        if (mCardAdapter != null) {
          List<String> savedCardIdImpressions = savedInstanceState.getStringArrayList(KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY);
          if (savedCardIdImpressions != null) {
            mCardAdapter.setImpressedCardIds(savedCardIdImpressions);
          }
        }
      });
    }
    initializeRecyclerView();
  }

  /**
   * The {@link RecyclerView} used in this fragment. Note that this will be
   * null until {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)} is called.
   *
   * @return A {@link RecyclerView} associated with {@link ContentCardsFragment}.
   */
  @Nullable
  public RecyclerView getContentCardsRecyclerView() {
    return mRecyclerView;
  }

  protected void initializeRecyclerView() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    mCardAdapter = new ContentCardAdapter(getContext(), layoutManager, new ArrayList<>(), getContentCardsViewBindingHandler());
    mRecyclerView.setAdapter(mCardAdapter);
    mRecyclerView.setLayoutManager(layoutManager);

    attachSwipeHelperCallback();

    // Disable any animations when the items change to avoid any issues when the data changes
    // see https://stackoverflow.com/questions/29331075/recyclerview-blinking-after-notifydatasetchanged
    RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
    if (animator instanceof SimpleItemAnimator) {
      ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
    }

    // Add an item divider
    mRecyclerView.addItemDecoration(new ContentCardsDividerItemDecoration(getContext()));

    // Create the "empty" adapter
    mDefaultEmptyContentCardsAdapter = new EmptyContentCardsAdapter();
  }

  /**
   * Creates and attaches a {@link SimpleItemTouchHelperCallback} to handle swipe-to-dismiss functionality.
   */
  protected void attachSwipeHelperCallback() {
    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCardAdapter);
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
    itemTouchHelper.attachToRecyclerView(mRecyclerView);
  }

  /**
   * Handles the processing and rendering for a {@link ContentCardsUpdatedEvent} on the UI thread.
   */
  protected void handleContentCardsUpdatedEvent(ContentCardsUpdatedEvent event) {
    mMainThreadLooper.post(getContentCardUpdateRunnable(event));
  }

  /**
   * A main thread runnable to handle {@link ContentCardsUpdatedEvent} on the main thread.
   */
  protected class ContentCardsUpdateRunnable implements Runnable {
    private final ContentCardsUpdatedEvent mEvent;

    ContentCardsUpdateRunnable(ContentCardsUpdatedEvent event) {
      mEvent = event;
    }

    @Override
    public void run() {
      BrazeLogger.v(TAG, "Updating Content Cards views in response to ContentCardsUpdatedEvent: " + mEvent);
      // This list of cards could undergo filtering in the card update handler
      // and be a smaller list of cards compared to the original list
      // in the update event. Thus, any "empty feed" checks should be
      // performed on this filtered list and not the original list of cards.
      List<Card> cardsForRendering = getContentCardUpdateHandler().handleCardUpdate(mEvent);
      mCardAdapter.replaceCards(cardsForRendering);
      mMainThreadLooper.removeCallbacks(getNetworkUnavailableRunnable());

      // If the update came from storage and is stale, then request a refresh.
      if (mEvent.isFromOfflineStorage() && mEvent.isTimestampOlderThan(MAX_CONTENT_CARDS_TTL_SECONDS)) {
        BrazeLogger.i(TAG, "ContentCards received was older than the max time "
            + "to live of " + MAX_CONTENT_CARDS_TTL_SECONDS + " seconds, displaying it "
            + "for now, but requesting an updated view from the server.");
        Braze.getInstance(getContext()).requestContentCardsRefresh(false);

        // If we don't have any cards to display, we put up the spinner while
        // we wait for the network to return.
        // Eventually displaying an error message if it doesn't.
        if (cardsForRendering.isEmpty()) {
          // Display a loading indicator
          mContentCardsSwipeLayout.setRefreshing(true);

          BrazeLogger.d(TAG, "Old Content Cards was empty, putting up a "
              + "network spinner and registering the network "
              + "error message on a delay of " + NETWORK_PROBLEM_WARNING_MS + " ms.");
          mMainThreadLooper.postDelayed(getNetworkUnavailableRunnable(), NETWORK_PROBLEM_WARNING_MS);
          return;
        }
      }

      // The cards are either fresh from the cache, or came directly from a
      // network request. An empty Content Cards should just display
      // an "empty ContentCards" message.
      if (!cardsForRendering.isEmpty()) {
        // The Content Cards contains cards and should be displayed
        swapRecyclerViewAdapter(mCardAdapter);
      } else {
        // The Content Cards is empty and should display an "empty" message to the user.
        swapRecyclerViewAdapter(getEmptyCardsAdapter());
      }

      // Stop the refresh animation
      mContentCardsSwipeLayout.setRefreshing(false);
    }
  }

  /**
   * A main thread runnable to handle displaying network unavailable messages on the main thread.
   */
  protected class NetworkUnavailableRunnable implements Runnable {
    private final Context mApplicationContext;

    private NetworkUnavailableRunnable(Context applicationContext) {
      mApplicationContext = applicationContext;
    }

    @Override
    public void run() {
      BrazeLogger.v(TAG, "Displaying network unavailable toast.");
      Toast.makeText(mApplicationContext, mApplicationContext.getString(R.string.com_appboy_feed_connection_error_title), Toast.LENGTH_LONG).show();

      swapRecyclerViewAdapter(getEmptyCardsAdapter());
      mContentCardsSwipeLayout.setRefreshing(false);
    }
  }

  /**
   * Swaps the current {@link RecyclerView} {@link RecyclerView.Adapter} for a new one. If
   * the current adapter matches the new adapter, then this method does nothing.
   */
  protected void swapRecyclerViewAdapter(RecyclerView.Adapter<?> newAdapter) {
    if (mRecyclerView != null && mRecyclerView.getAdapter() != newAdapter) {
      mRecyclerView.setAdapter(newAdapter);
    }
  }

  /**
   * @return An adapter to display when no
   * cards are available for display.
   */
  protected RecyclerView.Adapter<?> getEmptyCardsAdapter() {
    return mDefaultEmptyContentCardsAdapter;
  }

  /**
   * @return A runnable to execute any UI updates
   * stemming from the {@link ContentCardsUpdatedEvent}/
   */
  protected Runnable getContentCardUpdateRunnable(ContentCardsUpdatedEvent event) {
    return new ContentCardsUpdateRunnable(event);
  }

  /**
   * @return A runnable to execute when the network is determined
   * to be unavailable.
   */
  protected Runnable getNetworkUnavailableRunnable() {
    return mDefaultNetworkUnavailableRunnable;
  }
}
