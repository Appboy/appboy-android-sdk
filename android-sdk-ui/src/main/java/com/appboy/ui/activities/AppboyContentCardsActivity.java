package com.appboy.ui.activities;

import android.os.Bundle;

import com.appboy.ui.R;

/**
 * The AppboyContentCardsActivity in an Activity class that displays the Braze Content Cards
 * Fragment. This class can be used to integrate Content Cards as an Activity.
 *
 * Note: To integrate Braze Content Cards as a Fragment instead of an Activity, use the
 * {@link com.appboy.ui.AppboyContentCardsFragment} class.
 */
public class AppboyContentCardsActivity extends AppboyBaseFragmentActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_content_cards_activity);
  }
}
