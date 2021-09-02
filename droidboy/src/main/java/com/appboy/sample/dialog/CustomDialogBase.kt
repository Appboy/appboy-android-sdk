package com.appboy.sample.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.appboy.sample.R

/**
 * Requires R.layout.dialog_footer_navigation in the dialog to work properly.
 */
abstract class CustomDialogBase : DialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.bDialogNegative).setOnClickListener { onExitButtonPressed(false) }
        view.findViewById<Button>(R.id.bDialogPositive).setOnClickListener { onExitButtonPressed(true) }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    abstract fun onExitButtonPressed(isPositive: Boolean)
}
