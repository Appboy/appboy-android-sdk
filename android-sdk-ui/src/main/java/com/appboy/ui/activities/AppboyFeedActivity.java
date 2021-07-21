package com.appboy.ui.activities;

import android.os.Bundle;

import com.appboy.ui.R;
import com.braze.ui.activities.BrazeBaseFragmentActivity;

/**
 * The AppboyFeedActivity in an Activity class that displays the Braze News Feed Fragment. This
 * class can be used to integrate the Braze News Feed as an Activity.
 *
 * Note: To integrate the Braze News Feed as a Fragment instead of an Activity, use the
 * {@link com.appboy.ui.AppboyFeedFragment} class.
 */
public class AppboyFeedActivity extends BrazeBaseFragmentActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_feed_activity);
  }
}
