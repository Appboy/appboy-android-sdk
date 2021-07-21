package com.braze.ui.activities;

import android.os.Bundle;

import com.appboy.ui.R;
import com.braze.ui.contentcards.ContentCardsFragment;

/**
 * The {@link ContentCardsActivity} in an Activity class that displays the Braze Content Cards
 * Fragment. This class can be used to integrate Content Cards as an Activity.
 *
 * Note: To integrate Braze Content Cards as a Fragment instead of an Activity, use the
 * {@link ContentCardsFragment} class.
 */
public class ContentCardsActivity extends BrazeBaseFragmentActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_braze_content_cards_activity);
  }
}
