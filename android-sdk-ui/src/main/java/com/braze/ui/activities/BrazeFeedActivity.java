package com.braze.ui.activities;

import android.os.Bundle;

import com.braze.ui.R;

/**
 * The BrazeFeedActivity in an Activity class that displays the Braze News Feed Fragment. This
 * class can be used to integrate the Braze News Feed as an Activity.
 *
 * Note: To integrate the Braze News Feed as a Fragment instead of an Activity, use the
 * {@link BrazeFeedFragment} class.
 */
public class BrazeFeedActivity extends BrazeBaseFragmentActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_braze_feed_activity);
  }
}
