@file:JvmName("ViewUtils")
@file:Suppress("TooManyFunctions")

package com.braze.ui.support

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import com.braze.enums.inappmessage.Orientation
import com.braze.support.BrazeLogger.Priority.D
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import com.braze.support.BrazeLogger.getBrazeLogTag
import kotlin.math.max

private val TAG = "ViewUtils".getBrazeLogTag()
private const val TABLET_SMALLEST_WIDTH_DP = 600

fun View?.removeViewFromParent() {
    if (this == null) {
        brazelog(TAG, D) { "View passed in is null. Not removing from parent." }
    }
    if (this?.parent is ViewGroup) {
        val parent = this.parent as ViewGroup
        parent.removeView(this)
        brazelog(TAG, D) { "Removed view: $this\nfrom parent: $parent" }
    }
}

fun View.setFocusableInTouchModeAndRequestFocus() {
    try {
        this.isFocusableInTouchMode = true
        this.requestFocus()
    } catch (e: Exception) {
        brazelog(TAG, E, e) {
            "Caught exception while setting view to focusable in touch mode and requesting focus."
        }
    }
}

fun convertDpToPixels(context: Context, valueInDp: Double): Double {
    val density = context.resources.displayMetrics.density.toDouble()
    return valueInDp * density
}

fun Activity.isRunningOnTablet(): Boolean {
    return (
        this.resources.configuration.smallestScreenWidthDp
            >= TABLET_SMALLEST_WIDTH_DP
        )
}

/**
 * Safely calls [Activity.setRequestedOrientation]
 */
fun Activity.setActivityRequestedOrientation(requestedOrientation: Int) {
    try {
        this.requestedOrientation = requestedOrientation
    } catch (e: Exception) {
        brazelog(TAG, E, e) {
            "Failed to set requested orientation $requestedOrientation for activity class: ${this.localClassName}"
        }
    }
}

fun setHeightOnViewLayoutParams(view: View, height: Int) {
    val layoutParams = view.layoutParams
    layoutParams.height = height
    view.layoutParams = layoutParams
}

/**
 * Checks if the device is in night mode. In Android 10+, this corresponds
 * to "Dark Theme" being enabled by the user.
 */
fun isDeviceInNightMode(context: Context): Boolean {
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}

/**
 * @return Whether the current screen orientation (e.g. [Configuration.ORIENTATION_LANDSCAPE])
 * matches the preferred orientation (e.g. [Orientation.LANDSCAPE].
 */
fun isCurrentOrientationValid(
    currentScreenOrientation: Int,
    preferredOrientation: Orientation
): Boolean {
    return if (currentScreenOrientation == Configuration.ORIENTATION_LANDSCAPE
        && preferredOrientation === Orientation.LANDSCAPE
    ) {
        brazelog(TAG, D) { "Current and preferred orientation are landscape." }
        true
    } else if (currentScreenOrientation == Configuration.ORIENTATION_PORTRAIT
        && preferredOrientation === Orientation.PORTRAIT
    ) {
        brazelog(TAG, D) { "Current and preferred orientation are portrait." }
        true
    } else {
        brazelog(TAG, D) {
            "Current orientation $currentScreenOrientation" +
                " and preferred orientation $preferredOrientation don't match"
        }
        false
    }
}

/**
 * @return The maximum of the display cutout left inset and the system window left inset.
 */
fun getMaxSafeLeftInset(windowInsets: WindowInsetsCompat): Int {
    return max(
        windowInsets.displayCutout?.safeInsetLeft ?: 0,
        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).left
    )
}

/**
 * @return The maximum of the display cutout right inset and the system window right inset.
 */
fun getMaxSafeRightInset(windowInsets: WindowInsetsCompat): Int {
    return max(
        windowInsets.displayCutout?.safeInsetRight ?: 0,
        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).right
    )
}

/**
 * @return The maximum of the display cutout top inset and the system window top inset.
 */
fun getMaxSafeTopInset(windowInsets: WindowInsetsCompat): Int {
    return max(
        windowInsets.displayCutout?.safeInsetTop ?: 0,
        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
    )
}

/**
 * @return The maximum of the display cutout bottom inset and the system window bottom inset.
 */
fun getMaxSafeBottomInset(windowInsets: WindowInsetsCompat): Int {
    return max(
        windowInsets.displayCutout?.safeInsetBottom ?: 0,
        windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
    )
}

/**
 * Detects if this device is currently in touch mode given a [View].
 */
fun isDeviceNotInTouchMode(view: View) =
    !view.isInTouchMode
