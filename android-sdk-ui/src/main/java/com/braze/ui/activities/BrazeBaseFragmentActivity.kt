package com.braze.ui.activities

import androidx.fragment.app.FragmentActivity
import com.braze.Braze
import com.braze.ui.inappmessage.BrazeInAppMessageManager

/**
 * The BrazeBaseFragmentActivity class is a base class that includes the necessary Braze method
 * calls for basic analytics and in-app message integration.
 */
open class BrazeBaseFragmentActivity : FragmentActivity() {
    public override fun onStart() {
        super.onStart()
        // Opens (or reopens) a Braze session.
        // Note: This must be called in the onStart lifecycle method of EVERY Activity. Failure to do so
        // will result in incomplete and/or erroneous analytics.
        Braze.getInstance(this).openSession(this)
    }

    public override fun onResume() {
        super.onResume()
        // Registers the BrazeInAppMessageManager for the current Activity. This Activity will now listen for
        // in-app messages from Braze.
        BrazeInAppMessageManager.getInstance().registerInAppMessageManager(this)
    }

    public override fun onPause() {
        super.onPause()
        // Unregisters the BrazeInAppMessageManager.
        BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(this)
    }

    public override fun onStop() {
        super.onStop()
        // Closes the current Braze session.
        // Note: This must be called in the onStop lifecycle method of EVERY Activity. Failure to do so
        // will result in incomplete and/or erroneous analytics.
        Braze.getInstance(this).closeSession(this)
    }
}
