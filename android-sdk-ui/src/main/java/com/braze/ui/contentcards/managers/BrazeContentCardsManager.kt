package com.braze.ui.contentcards.managers

import com.braze.ui.contentcards.listeners.DefaultContentCardsActionListener
import com.braze.ui.contentcards.listeners.IContentCardsActionListener

open class BrazeContentCardsManager {
    /**
     * The current implementation of the [IContentCardsActionListener] interface.
     * Defaults to [DefaultContentCardsActionListener].
     * Will also be set to the default when set to null.
     */
    var contentCardsActionListener: IContentCardsActionListener? = DefaultContentCardsActionListener()
        set(value) {
            field = value ?: DefaultContentCardsActionListener()
        }

    companion object {
        @JvmStatic
        val instance by lazy {
            BrazeContentCardsManager()
        }
    }
}
