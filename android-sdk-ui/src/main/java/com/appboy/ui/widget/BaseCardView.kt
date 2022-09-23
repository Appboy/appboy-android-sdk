package com.appboy.ui.widget

import android.content.Context
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.appboy.models.cards.Card
import com.appboy.ui.R
import com.appboy.ui.feed.AppboyImageSwitcher
import com.braze.Braze
import com.braze.configuration.BrazeConfigurationProvider
import com.braze.enums.BrazeViewBounds
import com.braze.support.BrazeLogger.Priority.V
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag
import com.braze.ui.BrazeDeeplinkHandler.Companion.getInstance
import com.braze.ui.actions.IAction
import com.braze.ui.actions.UriAction

/**
 * Base class for Braze feed card views.
 */
abstract class BaseCardView<T : Card>(context: Context) : RelativeLayout(context) {
    @JvmField
    protected val applicationContext: Context = context.applicationContext
    val classLogTag: String = getBrazeLogTag(this.javaClass)
    @JvmField
    protected var card: T? = null
    @JvmField
    var imageSwitcher: AppboyImageSwitcher? = null
    @JvmField
    protected var configurationProvider = BrazeConfigurationProvider(context)

    private var isUnreadCardVisualIndicatorEnabled: Boolean = configurationProvider.isNewsfeedVisualIndicatorOn

    val isUnreadIndicatorEnabled: Boolean
        get() = isUnreadCardVisualIndicatorEnabled

    /**
     * Applies the text to the [TextView]. If the text is null or blank,
     * the [TextView]'s visibility is changed to [android.view.View.GONE].
     */
    fun setOptionalTextView(view: TextView, text: String?) {
        if (!text.isNullOrBlank()) {
            view.text = text
            view.visibility = VISIBLE
        } else {
            view.text = ""
            view.visibility = GONE
        }
    }

    /**
     * Asynchronously fetches the image at the given imageUrl and displays the image in the ImageView. No image will be
     * displayed if the image cannot be downloaded or fetched from the cache.
     *
     * @param imageView the ImageView in which to display the image
     * @param imageUrl the URL of the image resource
     * @param placeholderAspectRatio a placeholder aspect ratio that will be used for sizing purposes.
     * The actual dimensions of the final image will dictate the final image aspect ratio.
     * @param card
     */
    fun setImageViewToUrl(
        imageView: ImageView,
        imageUrl: String,
        placeholderAspectRatio: Float,
        card: Card
    ) {
        if (imageUrl != imageView.getTag(R.string.com_braze_image_resize_tag_key)) {
            // If the campaign is using liquid, the aspect ratio could be unknown (0)
            if (placeholderAspectRatio != 0f) {
                // We need to set layout params on the imageView once its layout state is visible. To do this,
                // we obtain the imageView's observer and attach a listener on it for when the view's layout
                // occurs. At layout time, we set the imageView's size params based on the aspect ratio
                // for our card. Note that after the card's first layout, we don't want redundant resizing
                // so we remove our listener after the resizing.
                val viewTreeObserver = imageView.viewTreeObserver
                if (viewTreeObserver.isAlive) {
                    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            imageView.viewTreeObserver.removeOnPreDrawListener(this)
                            val width = imageView.width
                            imageView.layoutParams =
                                LayoutParams(width, (width / placeholderAspectRatio).toInt())
                            return true
                        }
                    })
                }
            }
            imageView.setImageResource(android.R.color.transparent)

            Braze.getInstance(context).imageLoader.renderUrlIntoCardView(
                context,
                card,
                imageUrl,
                imageView,
                BrazeViewBounds.BASE_CARD_VIEW
            )

            imageView.setTag(R.string.com_braze_image_resize_tag_key, imageUrl)
        }
    }

    /**
     * Checks to see if the card object is viewed and if so, sets the read/unread status
     * indicator image. If the card is null, does nothing.
     */
    fun setCardViewedIndicator(imageSwitcher: AppboyImageSwitcher?, card: Card) {
        if (imageSwitcher == null) {
            brazelog(W) { "imageSwitcher is null. Can't set card viewed indicator." }
            return
        }
        // Check the tag for the image switcher so we don't have to re-draw the same indicator unnecessarily
        var imageSwitcherTag =
            imageSwitcher.getTag(R.string.com_braze_image_is_read_tag_key)
        // If the tag is null, default to the empty string
        imageSwitcherTag = imageSwitcherTag ?: ""

        if (card.isIndicatorHighlighted) {
            if (imageSwitcherTag != ICON_READ_TAG) {
                if (imageSwitcher.readIcon != null) {
                    imageSwitcher.setImageDrawable(imageSwitcher.readIcon)
                } else {
                    imageSwitcher.setImageResource(R.drawable.com_braze_content_card_icon_read)
                }
                imageSwitcher.setTag(R.string.com_braze_image_is_read_tag_key, ICON_READ_TAG)
            }
        } else {
            if (imageSwitcherTag != ICON_UNREAD_TAG) {
                if (imageSwitcher.unReadIcon != null) {
                    imageSwitcher.setImageDrawable(imageSwitcher.unReadIcon)
                } else {
                    imageSwitcher.setImageResource(R.drawable.com_braze_content_card_icon_unread)
                }
                imageSwitcher.setTag(R.string.com_braze_image_is_read_tag_key, ICON_UNREAD_TAG)
            }
        }
    }

    protected fun handleCardClick(context: Context, card: Card, cardAction: IAction?) {
        brazelog(V) { "Handling card click for card: $card" }
        card.isIndicatorHighlighted = true
        if (!isClickHandled(context, card, cardAction)) {
            if (cardAction != null) {
                card.logClick()
                brazelog(V) { "Card action is non-null. Attempting to perform action on card: ${card.id}" }
                if (cardAction is UriAction) {
                    getInstance().gotoUri(context, cardAction)
                } else {
                    brazelog { "Executing non uri action for click on card: ${card.id}" }
                    cardAction.execute(context)
                }
            } else {
                brazelog(V) { "Card action is null. Not performing any click action on card: ${card.id}" }
            }
        } else {
            brazelog { "Card click was handled by custom listener on card: ${card.id}" }
            card.logClick()
        }
    }

    /**
     * Calls the corresponding card manager to see if the action listener has handled the click.
     */
    protected abstract fun isClickHandled(
        context: Context,
        card: Card,
        cardAction: IAction?
    ): Boolean

    companion object {
        private const val ICON_READ_TAG = "icon_read"
        private const val ICON_UNREAD_TAG = "icon_unread"

        @JvmStatic
        protected fun getUriActionForCard(card: Card): UriAction? {
            val extras = Bundle()
            for (key in card.extras.keys) {
                extras.putString(key, card.extras[key])
            }
            val url = card.url
            if (url == null) {
                brazelog(V) { "Card URL is null, returning null for getUriActionForCard" }
                return null
            }
            return getInstance().createUriActionFromUrlString(
                url,
                extras,
                card.openUriInWebView,
                card.channel
            )
        }
    }
}
