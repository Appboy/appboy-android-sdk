package com.braze.ui.inappmessage.views

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.TouchDelegate
import android.view.View
import android.widget.TextView
import com.braze.ui.R
import com.braze.enums.inappmessage.TextAlign
import com.braze.models.inappmessage.MessageButton
import com.braze.support.BrazeLogger.Priority.W
import com.braze.support.BrazeLogger.brazelog
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.utils.InAppMessageButtonViewUtils.setButtons
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.closeInAppMessageOnKeycodeBack
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.resetMessageMarginsIfNecessary
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setFrameColor
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setTextAlignment
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setTextViewColor
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.setViewBackgroundColorFilter
import com.braze.ui.support.removeViewFromParent

@Suppress("TooManyFunctions")
abstract class InAppMessageImmersiveBaseView(context: Context?, attrs: AttributeSet?) :
    InAppMessageBaseView(context, attrs), IInAppMessageImmersiveView {
    abstract val frameView: View?
    abstract override val messageTextView: TextView?
    abstract val messageHeaderTextView: TextView?

    override fun resetMessageMargins(imageRetrievalSuccessful: Boolean) {
        super.resetMessageMargins(imageRetrievalSuccessful)
        if (messageTextView?.text.toString().isBlank()) {
            messageTextView.removeViewFromParent()
        }
        if (messageHeaderTextView?.text.toString().isBlank()) {
            messageHeaderTextView.removeViewFromParent()
        }
        resetMessageMarginsIfNecessary(messageTextView, messageHeaderTextView)
    }

    @Suppress("LongMethod")
    override fun setupDirectionalNavigation(numButtons: Int) {
        // Buttons should focus to each other and the close button
        val messageButtonViews = getMessageButtonViews(numButtons)
        val closeButton = messageCloseButtonView
        val closeButtonId = closeButton?.id
        // If the user happens to leave touch mode while the IAM is already on-screen,
        // we need to specify what View will receive that initial focus.
        var defaultFocusId = closeButtonId
        var defaultFocusView: View? = closeButton
        val primaryButton: View?
        val secondaryButton: View?
        val primaryId: Int?
        val secondaryId: Int?
        if (closeButtonId == null) {
            brazelog(W) { "closeButtonId is null. Cannot continue setting up navigation." }
            return
        }
        when (numButtons) {
            2 -> {
                primaryButton = messageButtonViews[1]
                secondaryButton = messageButtonViews[0]
                primaryId = primaryButton.id
                secondaryId = secondaryButton.id
                defaultFocusId = primaryId
                defaultFocusView = primaryButton

                // Primary points to close and secondary button
                primaryButton.nextFocusLeftId = secondaryId
                primaryButton.nextFocusRightId = secondaryId
                primaryButton.nextFocusUpId = closeButtonId
                primaryButton.nextFocusDownId = closeButtonId

                // Secondary also points to close and secondary button
                secondaryButton.nextFocusLeftId = primaryId
                secondaryButton.nextFocusRightId = primaryId
                secondaryButton.nextFocusUpId = closeButtonId
                secondaryButton.nextFocusDownId = closeButtonId

                // Close button points to primary, then secondary
                closeButton.nextFocusUpId = primaryId
                closeButton.nextFocusDownId = primaryId
                closeButton.nextFocusRightId = primaryId
                closeButton.nextFocusLeftId = secondaryId
            }
            1 -> {
                primaryButton = messageButtonViews[0]
                primaryId = primaryButton.id
                defaultFocusId = primaryId
                defaultFocusView = primaryButton

                // Primary points to close
                primaryButton.nextFocusLeftId = closeButtonId
                primaryButton.nextFocusRightId = closeButtonId
                primaryButton.nextFocusUpId = closeButtonId
                primaryButton.nextFocusDownId = closeButtonId

                // Close button points to primary
                closeButton.nextFocusUpId = primaryId
                closeButton.nextFocusDownId = primaryId
                closeButton.nextFocusRightId = primaryId
                closeButton.nextFocusLeftId = primaryId
            }
            0 -> {
                // Have the close button wrap back to itself
                closeButton.nextFocusUpId = closeButtonId
                closeButton.nextFocusDownId = closeButtonId
                closeButton.nextFocusRightId = closeButtonId
                closeButton.nextFocusLeftId = closeButtonId
            }
            else -> brazelog(W) {
                "Cannot setup directional navigation. Got unsupported number of buttons: $numButtons"
            }
        }

        // The entire view should focus back to the close
        // button and not allow for backwards navigation.
        if (defaultFocusId != null) {
            this.nextFocusUpId = defaultFocusId
            this.nextFocusDownId = defaultFocusId
            this.nextFocusRightId = defaultFocusId
            this.nextFocusLeftId = defaultFocusId
        }

        // Request focus for the default view
        val finalDefaultFocusView = defaultFocusView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            finalDefaultFocusView?.isFocusedByDefault = true
        }
        finalDefaultFocusView?.post { finalDefaultFocusView.requestFocus() }
    }

    open fun setMessageButtons(messageButtons: List<MessageButton>) {
        setButtons(getMessageButtonViews(messageButtons.size), messageButtons)
    }

    open fun setMessageCloseButtonColor(color: Int) {
        messageCloseButtonView?.let { setViewBackgroundColorFilter(it, color) }
    }

    open fun setMessageHeaderTextColor(color: Int) {
        messageHeaderTextView?.let {
            setTextViewColor(it, color)
        }
    }

    open fun setMessageHeaderText(text: String) {
        messageHeaderTextView?.text = text
    }

    open fun setMessageHeaderTextAlignment(textAlign: TextAlign) {
        messageHeaderTextView?.let {
            setTextAlignment(it, textAlign)
        }
    }

    open fun setFrameColor(color: Int) {
        frameView?.let { setFrameColor(it, color) }
    }

    /**
     * Returns a list of all button views for this [IInAppMessageImmersiveView]. The default views
     * for in-app messages can contain multiple layouts depending on the number of buttons.
     *
     * @param numButtons The number of buttons used for this layout.
     */
    abstract override fun getMessageButtonViews(numButtons: Int): List<View>

    /**
     * Immersive messages can alternatively be closed by the back button.
     *
     * @return If the button pressed was the back button, close the in-app message
     * and return true to indicate that the event was handled.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && BrazeInAppMessageManager.getInstance().doesBackButtonDismissInAppMessageView) {
            closeInAppMessageOnKeycodeBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Immersive messages can alternatively be closed by the back button.
     *
     * @return If the button pressed was the back button, close the in-app message
     * and return true to indicate that the event was handled.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!isInTouchMode && event.keyCode == KeyEvent.KEYCODE_BACK && BrazeInAppMessageManager.getInstance().doesBackButtonDismissInAppMessageView) {
            closeInAppMessageOnKeycodeBack()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Sets a rectangular click area for the close button. This is necessary to provide a larger click
     * area than the close button drawable and to ensure that the click area is not a mask of the drawable
     * and is instead an easy to tap rectangle.
     *
     * @param closeButtonView The close button view.
     */
    open fun setLargerCloseButtonClickArea(closeButtonView: View?) {
        if (closeButtonView == null || closeButtonView.parent == null) {
            brazelog(W) { "Cannot increase click area for view if view and/or parent are null." }
            return
        }
        val parent = closeButtonView.parent
        if (parent is View) {
            parent.post {
                val delegateArea = Rect()

                // The hit rectangle for the ImageButton
                closeButtonView.getHitRect(delegateArea)

                // Extend the touch area of the ImageButton beyond its bounds
                val desiredCloseButtonClickAreaWidth =
                    context.resources.getDimensionPixelSize(R.dimen.com_braze_inappmessage_close_button_click_area_width)
                val desiredCloseButtonClickAreaHeight =
                    context.resources.getDimensionPixelSize(R.dimen.com_braze_inappmessage_close_button_click_area_height)
                val extraHorizontalPadding = (desiredCloseButtonClickAreaWidth - delegateArea.width()) / 2
                val extraVerticalPadding = (desiredCloseButtonClickAreaHeight - delegateArea.height()) / 2
                delegateArea.top -= extraVerticalPadding
                delegateArea.bottom += extraVerticalPadding
                delegateArea.left -= extraHorizontalPadding
                delegateArea.right += extraHorizontalPadding

                // Instantiate a TouchDelegate.
                // "delegateArea" is the bounds in local coordinates of
                // the containing view to be mapped to the delegate view.
                val touchDelegate = TouchDelegate(delegateArea, closeButtonView)

                // Sets the TouchDelegate on the parent view, such that touches
                // within the touch delegate bounds are routed to the child.
                parent.touchDelegate = touchDelegate
            }
        }
    }
}
