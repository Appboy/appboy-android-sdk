package com.braze.ui.contentcards.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.appboy.models.cards.Card
import com.appboy.ui.R
import com.appboy.ui.widget.BaseCardView
import com.braze.ui.actions.IAction
import com.braze.ui.contentcards.managers.BrazeContentCardsManager.Companion.instance

/**
 * Base class for ContentCard views
 */
abstract class BaseContentCardView<T : Card>(context: Context) : BaseCardView<T>(
    context
) {
    abstract fun createViewHolder(viewGroup: ViewGroup): ContentCardViewHolder

    open fun bindViewHolder(viewHolder: ContentCardViewHolder, card: Card) {
        viewHolder.setPinnedIconVisible(card.isPinned)
        viewHolder.setUnreadBarVisible(
            configurationProvider.isContentCardsUnreadVisualIndicatorEnabled
                && !card.isIndicatorHighlighted
        )
        val cardAction = getUriActionForCard(card)
        viewHolder.itemView.setOnClickListener {
            handleCardClick(
                applicationContext,
                card,
                cardAction
            )
        }

        // Only set the action hint to visible if there's a card action
        viewHolder.setActionHintVisible(cardAction != null)
    }

    /**
     * Sets the card's image to a given url. The view may be null.
     *
     * @param imageView          The ImageView
     * @param cardAspectRatio    The aspect ratio as set by the card itself
     * @param cardImageUrl       The image url
     * @param card               The card being rendered
     */
    fun setOptionalCardImage(
        imageView: ImageView?,
        cardAspectRatio: Float,
        cardImageUrl: String?,
        card: Card
    ) {
        if (imageView != null && cardImageUrl != null) {
            setImageViewToUrl(imageView, cardImageUrl, cardAspectRatio, card)
        }
    }

    override fun isClickHandled(context: Context, card: Card, cardAction: IAction?): Boolean =
        instance.contentCardsActionListener?.onContentCardClicked(context, card, cardAction) == true

    @Suppress("MagicNumber")
    @TargetApi(21)
    protected fun safeSetClipToOutline(imageView: ImageView?) {
        imageView?.clipToOutline = true
    }

    // getDrawable() is deprecated but the alternative is supported by 21+
    protected fun setViewBackground(view: View) {
        @Suppress("deprecation")
        view.background = resources.getDrawable(R.drawable.com_braze_content_card_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("deprecation")
            view.foreground = resources.getDrawable(R.drawable.com_braze_content_card_scrim)
        }
    }
}
