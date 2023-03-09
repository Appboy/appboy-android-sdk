package com.appboy.sample.featureflag.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appboy.sample.R
import com.braze.models.FeatureFlag
import com.braze.support.getPrettyPrintedString

class FeatureFlagAdapter(
    private val featureFlags: MutableList<FeatureFlag>
) : RecyclerView.Adapter<FeatureFlagAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var areDetailsVisible = false
        val tvIdentifier: TextView
        val tvNumProperties: TextView
        val tvEnabled: TextView
        val tvPropertyDetails: TextView

        init {
            // Define click listener for the ViewHolder's View
            tvIdentifier = view.findViewById(R.id.tvFlagId)
            tvNumProperties = view.findViewById(R.id.tvFlagNumProperties)
            tvEnabled = view.findViewById(R.id.tvFlagEnabled)
            tvPropertyDetails = view.findViewById(R.id.tvFlagProperties)

            itemView.setOnClickListener {
                areDetailsVisible = !areDetailsVisible
                tvPropertyDetails.visibility = if (areDetailsVisible) View.VISIBLE else View.GONE
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.feature_flag_overview_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val featureFlag = featureFlags[position]
        viewHolder.run {
            tvIdentifier.text = featureFlag.id
            tvEnabled.text = if (featureFlag.enabled) "ON" else "OFF"
            tvNumProperties.text = featureFlag.properties.length().toString()
            tvPropertyDetails.text = featureFlag.properties.getPrettyPrintedString()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = featureFlags.size

    fun replaceFeatureFlags(newFlagData: List<FeatureFlag>) {
        val diffCallback = DiffCallback(featureFlags, newFlagData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        featureFlags.clear()
        featureFlags.addAll(newFlagData)
        diffResult.dispatchUpdatesTo(this)
    }

    private class DiffCallback(
        private val oldFlags: List<FeatureFlag>,
        private val newFlags: List<FeatureFlag>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() =
            oldFlags.size

        override fun getNewListSize() =
            newFlags.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            doItemsShareIds(oldItemPosition, newItemPosition)

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldFlags[oldItemPosition].forJsonPut() == newFlags[newItemPosition].forJsonPut()

        private fun doItemsShareIds(oldItemPosition: Int, newItemPosition: Int) =
            oldFlags[oldItemPosition].id == newFlags[newItemPosition].id
    }
}
