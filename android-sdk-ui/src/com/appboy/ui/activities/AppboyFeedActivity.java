package com.appboy.ui.activities;

import android.os.Bundle;

import com.appboy.ui.R;

/**
 * The AppboyFeedActivity in an Activity class that displays the Appboy news feed fragment. This
 * class can be used to integrate the Appboy news feed as an Activity.
 *
 * Note: To integrate the Appboy news feed as a Fragment instead of an Activity, use the
 * {@link com.appboy.ui.AppboyFeedFragment} class.
 */
public class AppboyFeedActivity extends AppboyBaseFragmentActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_feed_activity);
  }
}
