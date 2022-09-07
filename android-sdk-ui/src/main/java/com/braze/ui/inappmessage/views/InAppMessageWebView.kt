package com.braze.ui.inappmessage.views

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.webkit.WebView
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.braze.ui.inappmessage.utils.InAppMessageViewUtils.closeInAppMessageOnKeycodeBack

/**
 * WebView embedded in Braze html in-app messages.
 */
open class InAppMessageWebView(context: Context, attrs: AttributeSet?) : WebView(
    context, attrs
) {
    /**
     * If the back button is pressed while this WebView is in focus,
     * close the current in-app message.
     *
     * Note: When this WebView doesn't have focus, back button events on html in-app messages are
     * captured by [InAppMessageHtmlFullView.onKeyDown]
     *
     * @return If the button pressed was the back button, close the in-app message
     * and return true to indicate that the event was handled.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
            BrazeInAppMessageManager.getInstance().doesBackButtonDismissInAppMessageView
        ) {
            closeInAppMessageOnKeycodeBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * WebView-based messages can alternatively be closed by the back button.
     *
     * @return If the button pressed was the back button, close the in-app message
     * and return true to indicate that the event was handled.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!isInTouchMode && event.keyCode == KeyEvent.KEYCODE_BACK &&
            BrazeInAppMessageManager.getInstance().doesBackButtonDismissInAppMessageView
        ) {
            closeInAppMessageOnKeycodeBack()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
