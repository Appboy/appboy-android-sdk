package com.appboy.sample.featureflag.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appboy.sample.R
import com.appboy.sample.featureflag.controller.FeatureFlagAdapter
import com.braze.Braze
import com.braze.coroutine.BrazeCoroutineScope
import com.braze.events.FeatureFlagsUpdatedEvent
import com.braze.events.IEventSubscriber
import com.braze.models.FeatureFlag

class FeatureFlagFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var dataAdapter: FeatureFlagAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val updateListener = IEventSubscriber<FeatureFlagsUpdatedEvent> {
        handleFeatureFlagUpdate(it.featureFlags)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.feature_flag_fragment, container, false)
        recyclerView = rootView.findViewById(R.id.flag_overview_recycler_view)
        recyclerView.setHasFixedSize(true)
        swipeRefreshLayout = rootView.findViewById(R.id.feature_flag_swipe_container)
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeColors(Color.CYAN, Color.GREEN)
        return rootView
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        dataAdapter = FeatureFlagAdapter(mutableListOf())
        recyclerView.adapter = dataAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val itemDecoration: RecyclerView.ItemDecoration =
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(itemDecoration)
        requestFeatureFlagUpdate()
    }

    private fun requestFeatureFlagUpdate() {
        // Listen for new Feature Flag updates after retrieving the current set
        Braze.getInstance(requireContext()).let {
            handleFeatureFlagUpdate(it.getAllFeatureFlags())
            it.subscribeToFeatureFlagsUpdates(updateListener)
            it.refreshFeatureFlags()
        }
    }

    private fun handleFeatureFlagUpdate(flags: List<FeatureFlag>) {
        if (this::dataAdapter.isInitialized) {
            recyclerView.post {
                dataAdapter.replaceFeatureFlags(flags)
            }
        }
    }

    override fun onRefresh() {
        requestFeatureFlagUpdate()
        BrazeCoroutineScope.launchDelayed(AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS) { swipeRefreshLayout.isRefreshing = false }
    }

    companion object {
        private const val AUTO_HIDE_REFRESH_INDICATOR_DELAY_MS = 1500L
    }
}
