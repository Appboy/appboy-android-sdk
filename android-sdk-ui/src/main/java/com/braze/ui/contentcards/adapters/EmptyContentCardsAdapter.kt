package com.braze.ui.contentcards.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.braze.ui.R

/**
 * This adapter displays a single, full width/height item. This item
 * should denote that the Content Cards contains no items and is empty.
 */
open class EmptyContentCardsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.com_braze_content_cards_empty, viewGroup, false)
        return NetworkUnavailableViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        // This method is here since all adapters require a bind implementation. This adapter is static, so no binding occurs.
    }

    override fun getItemCount() = 1

    internal class NetworkUnavailableViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
