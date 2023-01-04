package com.braze.ui.contentcards

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.braze.ui.R
import com.braze.Braze
import com.braze.coroutine.BrazeCoroutineScope
import com.braze.events.ContentCardsUpdatedEvent
import com.braze.events.ContentCardsUpdatedEvent.Companion.emptyUpdate
import com.braze.events.IEventSubscriber
import com.braze.events.SdkDataWipeEvent
import com.braze.support.BrazeLogger.Priority.I
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.contentcards.adapters.ContentCardAdapter
import com.braze.ui.contentcards.adapters.EmptyContentCardsAdapter
import com.braze.ui.contentcards.handlers.DefaultContentCardsUpdateHandler
import com.braze.ui.contentcards.handlers.DefaultContentCardsViewBindingHandler
import com.braze.ui.contentcards.handlers.IContentCardsUpdateHandler
import com.braze.ui.contentcards.handlers.IContentCardsViewBindingHandler
import com.braze.ui.contentcards.recycler.ContentCardsDividerItemDecoration
import com.braze.ui.contentcards.recycler.SimpleItemTouchHelperCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A fragment to display Braze ContentCards.
 */
@Suppress("TooManyFunctions")
open class ContentCardsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    /**
     * A runnable to execute when the network is determined
     * to be unavailable.
     */
    protected var networkUnavailableJob: Job? = null

    /**
     * A [RecyclerView] associated with [ContentCardsFragment].
     * Note that this will be null until [Fragment.onCreateView] is called.
     */
    var contentCardsRecyclerView: RecyclerView? = null
        protected set

    @JvmField
    var cardAdapter: ContentCardAdapter? = null

    protected var defaultEmptyContentCardsAdapter: EmptyContentCardsAdapter = EmptyContentCardsAdapter()
    protected var contentCardsSwipeLayout: SwipeRefreshLayout? = null

    protected var contentCardsUpdatedSubscriber: IEventSubscriber<ContentCardsUpdatedEvent>? = null
    protected var sdkDataWipeEventSubscriber: IEventSubscriber<SdkDataWipeEvent>? = null

    protected val defaultContentCardUpdateHandler: IContentCardsUpdateHandler = DefaultContentCardsUpdateHandler()
    protected var customContentCardUpdateHandler: IContentCardsUpdateHandler? = null
    protected val defaultContentCardsViewBindingHandler: IContentCardsViewBindingHandler = DefaultContentCardsViewBindingHandler()
    protected var customContentCardsViewBindingHandler: IContentCardsViewBindingHandler? = null

    /**
     * An adapter to display when no cards are available for display.
     */
    protected val emptyCardsAdapter: RecyclerView.Adapter<*>
        get() = defaultEmptyContentCardsAdapter

    // Since the get always returns non-null, but we want to allow passing null into the setter to
    // clear the instance, we split this out into a custom getter and setter rather than using a var
    /**
     * @return the [IContentCardsUpdateHandler] for this [ContentCardsFragment].
     */
    fun getContentCardUpdateHandler() =
        customContentCardUpdateHandler ?: defaultContentCardUpdateHandler

    /**
     * Set the [IContentCardsUpdateHandler] for this [ContentCardsFragment]. This handler is for doing
     * any work on [Card]s before being rendered in the ContentCards.
     * If not set or set to null, the default handler will be used.
     */
    fun setContentCardUpdateHandler(value: IContentCardsUpdateHandler?) {
        customContentCardUpdateHandler = value
    }

    // Since the get always returns non-null, but we want to allow passing null into the setter to
    // clear the instance, we split this out into a custom getter and setter rather than using a var
    /**
     * @return the [IContentCardsViewBindingHandler] responsible for rendering each [Card] in the [RecyclerView].
     */
    fun getContentCardsViewBindingHandler() =
        customContentCardsViewBindingHandler ?: defaultContentCardsViewBindingHandler

    /**
     * Set the [IContentCardsViewBindingHandler] responsible for rendering each [Card] in the [RecyclerView].
     * Note that this should only be set before the [ContentCardsFragment] is first displayed or the
     * [ContentCardAdapter] will not update correctly.
     */
    fun setContentCardsViewBindingHandler(value: IContentCardsViewBindingHandler?) {
        customContentCardsViewBindingHandler = value
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.com_braze_content_cards, container, false)
        contentCardsRecyclerView = rootView.findViewById(R.id.com_braze_content_cards_recycler)
        contentCardsSwipeLayout = rootView.findViewById(R.id.braze_content_cards_swipe_container)
        contentCardsSwipeLayout?.setOnRefreshListener(this)
        contentCardsSwipeLayout?.setColorSchemeResources(
            R.color.com_braze_content_cards_swipe_refresh_color_1,
            R.color.com_braze_content_cards_swipe_refresh_color_2,
            R.color.com_braze_content_cards_swipe_refresh_color_3,
            R.color.com_braze_content_cards_swipe_refresh_color_4
        )
        return rootView
    }

    /**
     * Called when the user swipes down and requests a feed refresh.
     */
    override fun onRefresh() {
        Braze.getInstance(requireContext()).requestContentCardsRefresh(false)
        BrazeCoroutineScope.launchDelayed(AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS) { contentCardsSwipeLayout?.isRefreshing = false }
    }

    override fun onResume() {
        super.onResume()
        // Remove the previous subscriber before rebuilding a new one with our new activity.
        Braze.getInstance(requireContext()).removeSingleSubscription(contentCardsUpdatedSubscriber, ContentCardsUpdatedEvent::class.java)
        if (contentCardsUpdatedSubscriber == null) {
            contentCardsUpdatedSubscriber = IEventSubscriber { event: ContentCardsUpdatedEvent -> handleContentCardsUpdatedEvent(event) }
        }
        contentCardsUpdatedSubscriber?.let {
            Braze.getInstance(requireContext()).subscribeToContentCardsUpdates(it)
        }
        Braze.getInstance(requireContext()).requestContentCardsRefresh(true)
        Braze.getInstance(requireContext()).removeSingleSubscription(sdkDataWipeEventSubscriber, SdkDataWipeEvent::class.java)
        if (sdkDataWipeEventSubscriber == null) {
            // If the SDK data is wiped, then we want to clear any cached Content Cards
            sdkDataWipeEventSubscriber = IEventSubscriber { handleContentCardsUpdatedEvent(emptyUpdate) }
        }
        sdkDataWipeEventSubscriber?.let {
            Braze.getInstance(requireContext()).addSingleSynchronousSubscription(it, SdkDataWipeEvent::class.java)
        }
    }

    override fun onPause() {
        super.onPause()
        // If the view is going away, we don't care about updating it anymore. Remove the subscription immediately.
        Braze.getInstance(requireContext()).removeSingleSubscription(contentCardsUpdatedSubscriber, ContentCardsUpdatedEvent::class.java)
        Braze.getInstance(requireContext()).removeSingleSubscription(sdkDataWipeEventSubscriber, SdkDataWipeEvent::class.java)
        networkUnavailableJob?.cancel()
        networkUnavailableJob = null
        cardAdapter?.markOnScreenCardsAsRead()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        contentCardsRecyclerView?.layoutManager?.let {
            outState.putParcelable(LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY, it.onSaveInstanceState())
        }
        cardAdapter?.let {
            outState.putStringArrayList(KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY, ArrayList(it.impressedCardIds))
        }
        customContentCardsViewBindingHandler?.let {
            outState.putParcelable(VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY, it)
        }
        customContentCardUpdateHandler?.let {
            outState.putParcelable(UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY, it)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            val updateHandlerParcelable: IContentCardsUpdateHandler? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    savedInstanceState.getParcelable(UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY, IContentCardsUpdateHandler::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    savedInstanceState.getParcelable(UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY)
                }
            if (updateHandlerParcelable != null) {
                setContentCardUpdateHandler(updateHandlerParcelable)
            }
            val viewBindingHandlerParcelable: IContentCardsViewBindingHandler? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    savedInstanceState.getParcelable(VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY, IContentCardsViewBindingHandler::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    savedInstanceState.getParcelable(VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY)
                }
            if (viewBindingHandlerParcelable != null) {
                setContentCardsViewBindingHandler(viewBindingHandlerParcelable)
            }
            BrazeCoroutineScope.launch(Dispatchers.Main) {
                val layoutManagerState =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        savedInstanceState.getParcelable(LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY, Parcelable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        savedInstanceState.getParcelable(LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY)
                    }
                contentCardsRecyclerView?.let {
                    val layoutManager = it.layoutManager
                    if (layoutManagerState != null && layoutManager != null) {
                        layoutManager.onRestoreInstanceState(layoutManagerState)
                    }
                }
                cardAdapter?.let {
                    val savedCardIdImpressions: List<String>? = savedInstanceState.getStringArrayList(KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY)
                    if (savedCardIdImpressions != null) {
                        it.impressedCardIds = savedCardIdImpressions
                    }
                }
            }
        }
        initializeRecyclerView()
    }

    protected fun initializeRecyclerView() {
        val layoutManager = LinearLayoutManager(activity)
        cardAdapter = ContentCardAdapter(
            requireContext(),
            layoutManager,
            mutableListOf(),
            getContentCardsViewBindingHandler()
        )
        contentCardsRecyclerView?.adapter = cardAdapter
        contentCardsRecyclerView?.layoutManager = layoutManager
        attachSwipeHelperCallback()

        // Disable any animations when the items change to avoid any issues when the data changes
        // see https://stackoverflow.com/questions/29331075/recyclerview-blinking-after-notifydatasetchanged
        val animator = contentCardsRecyclerView?.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        // Add an item divider
        contentCardsRecyclerView?.addItemDecoration(ContentCardsDividerItemDecoration(requireContext()))
    }

    /**
     * Creates and attaches a [SimpleItemTouchHelperCallback] to handle swipe-to-dismiss functionality.
     */
    protected fun attachSwipeHelperCallback() {
        cardAdapter?.let {
            val itemTouchHelper = ItemTouchHelper(SimpleItemTouchHelperCallback(it))
            itemTouchHelper.attachToRecyclerView(contentCardsRecyclerView)
        }
    }

    /**
     * Handles the processing and rendering for a [ContentCardsUpdatedEvent] on the UI thread.
     */
    protected fun handleContentCardsUpdatedEvent(event: ContentCardsUpdatedEvent) {
        BrazeCoroutineScope.launch(Dispatchers.Main) { contentCardsUpdate(event) }
    }

    /**
     * A main thread runnable to handle [ContentCardsUpdatedEvent] on the main thread.
     */
    protected suspend fun contentCardsUpdate(event: ContentCardsUpdatedEvent) {
        brazelog(V) { "Updating Content Cards views in response to ContentCardsUpdatedEvent: $event" }
        // This list of cards could undergo filtering in the card update handler
        // and be a smaller list of cards compared to the original list
        // in the update event. Thus, any "empty feed" checks should be
        // performed on this filtered list and not the original list of cards.
        val cardsForRendering = getContentCardUpdateHandler().handleCardUpdate(event)
        cardAdapter?.replaceCards(cardsForRendering)
        networkUnavailableJob?.cancel()
        networkUnavailableJob = null

        // If the update came from storage and is stale, then request a refresh.
        if (event.isFromOfflineStorage && event.isTimestampOlderThan(MAX_CONTENT_CARDS_TTL_SECONDS.toLong())) {
            brazelog(I) {
                "ContentCards received was older than the max time to live of " +
                    "$MAX_CONTENT_CARDS_TTL_SECONDS seconds, displaying it for now, but " +
                    "requesting an updated view from the server."
            }
            Braze.getInstance(requireContext()).requestContentCardsRefresh(false)

            // If we don't have any cards to display, we put up the spinner while
            // we wait for the network to return.
            // Eventually displaying an error message if it doesn't.
            if (cardsForRendering.isEmpty()) {
                // Display a loading indicator
                contentCardsSwipeLayout?.isRefreshing = true
                brazelog {
                    "Old Content Cards was empty, putting up a network spinner and " +
                        "registering the network error message on a delay of " +
                        "$NETWORK_PROBLEM_WARNING_MS ms."
                }

                networkUnavailableJob?.cancel()
                networkUnavailableJob = BrazeCoroutineScope.launchDelayed(NETWORK_PROBLEM_WARNING_MS, Dispatchers.Main) {
                    networkUnavailable()
                }
                return
            }
        }

        // The cards are either fresh from the cache, or came directly from a
        // network request. An empty Content Cards should just display
        // an "empty ContentCards" message.
        if (cardsForRendering.isNotEmpty()) {
            // The Content Cards contains cards and should be displayed.
            // The card adapter shouldn't be null at this point
            cardAdapter?.let {
                swapRecyclerViewAdapter(it)
            }
        } else {
            // The Content Cards is empty and should display an "empty" message to the user.
            swapRecyclerViewAdapter(emptyCardsAdapter)
        }

        // Stop the refresh animation
        contentCardsSwipeLayout?.isRefreshing = false
    }

    /**
     * A main thread runnable to handle displaying network unavailable messages on the main thread.
     */
    protected suspend fun networkUnavailable() {
        brazelog(V) { "Displaying network unavailable toast." }
        context?.applicationContext?.let { applicationContext ->
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.com_braze_feed_connection_error_title),
                Toast.LENGTH_LONG
            ).show()
        }
        swapRecyclerViewAdapter(emptyCardsAdapter)
        contentCardsSwipeLayout?.isRefreshing = false
    }

    /**
     * Swaps the current [RecyclerView] [RecyclerView.Adapter] for a new one. If
     * the current adapter matches the new adapter, then this method does nothing.
     */
    protected fun swapRecyclerViewAdapter(newAdapter: RecyclerView.Adapter<*>) {
        val recyclerView = contentCardsRecyclerView
        if (recyclerView != null && recyclerView.adapter !== newAdapter) {
            recyclerView.adapter = newAdapter
        }
    }

    companion object {
        private const val MAX_CONTENT_CARDS_TTL_SECONDS = 60
        private const val NETWORK_PROBLEM_WARNING_MS = 5000L
        private const val AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS = 2500L
        private const val LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY = "LAYOUT_MANAGER_SAVED_INSTANCE_STATE_KEY"
        private const val KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY = "KNOWN_CARD_IMPRESSIONS_SAVED_INSTANCE_STATE_KEY"
        private const val VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY = "VIEW_BINDING_HANDLER_SAVED_INSTANCE_STATE_KEY"
        private const val UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY = "UPDATE_HANDLER_SAVED_INSTANCE_STATE_KEY"
    }
}
