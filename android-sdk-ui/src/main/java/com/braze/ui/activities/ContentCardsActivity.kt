package com.braze.ui.activities

import android.os.Bundle
import com.braze.ui.R

/**
 * The [ContentCardsActivity] in an Activity class that displays the Braze Content Cards
 * Fragment. This class can be used to integrate Content Cards as an Activity.
 *
 * Note: To integrate Braze Content Cards as a Fragment instead of an Activity, use the
 * [ContentCardsFragment] class.
 */
open class ContentCardsActivity : BrazeBaseFragmentActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.com_braze_content_cards_activity)
    }
}
