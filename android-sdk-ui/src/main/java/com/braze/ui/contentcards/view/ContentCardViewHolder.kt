package com.braze.ui.contentcards.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appboy.ui.R

open class ContentCardViewHolder(view: View, showUnreadIndicator: Boolean) :
    RecyclerView.ViewHolder(view) {
    private val unreadBar: View? = view.findViewById(R.id.com_braze_content_cards_unread_bar)
    private val pinnedIcon: ImageView? = view.findViewById(R.id.com_braze_content_cards_pinned_icon)
    private val actionHint: TextView? = view.findViewById(R.id.com_braze_content_cards_action_hint)

    init {
        if (showUnreadIndicator) {
            unreadBar?.visibility = View.VISIBLE
            // Round the edges at the bottom
            // getDrawable() is deprecated but the alternative is supported by 21+
            @Suppress("deprecation")
            unreadBar?.background =
                view.context.resources.getDrawable(R.drawable.com_braze_content_cards_unread_bar_background)
        } else {
            unreadBar?.visibility = View.GONE
        }
    }

    /**
     * Sets the pinned icon to [View.VISIBLE] when true, or [View.GONE] otherwise.
     *
     * @param isVisible Should the pinned icon be visible on the card.
     */
    fun setPinnedIconVisible(isVisible: Boolean) {
        pinnedIcon?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Sets the unread bar to [View.VISIBLE] when true, or [View.GONE] otherwise.
     *
     * @param isVisible Should the unread bar be visible on the card.
     */
    fun setUnreadBarVisible(isVisible: Boolean) {
        unreadBar?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Sets the action hint to [View.VISIBLE] when true, or [View.GONE] otherwise.
     *
     * @param isVisible Should the action hint be visible on the card.
     */
    fun setActionHintVisible(isVisible: Boolean) {
        actionHint?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Sets the action hint text.
     */
    fun setActionHintText(text: String) {
        actionHint?.text = text
    }
}
