package com.braze.ui.inappmessage.listeners

import android.view.MotionEvent
import android.view.View

/**
 * Adds touch events to the SwipeDismissTouchListener.
 */
class TouchAwareSwipeDismissTouchListener(view: View, token: Any?, callbacks: DismissCallbacks?) :
    SwipeDismissTouchListener(view, token, callbacks) {
    private var touchListener: ITouchListener? = null

    interface ITouchListener {
        fun onTouchStartedOrContinued()
        fun onTouchEnded()
    }

    constructor(view: View, callbacks: DismissCallbacks?) : this(view, null, callbacks)

    fun setTouchListener(newTouchListener: ITouchListener) {
        touchListener = newTouchListener
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> touchListener?.onTouchStartedOrContinued()
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> touchListener?.onTouchEnded()
            else -> {}
        }
        return super.onTouch(view, motionEvent)
    }
}
