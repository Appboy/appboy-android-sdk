package com.appboy.ui.activities;

import android.support.v4.app.FragmentActivity;

import com.appboy.Appboy;
import com.appboy.ui.slideups.AppboySlideupManager;

/**
 * The AppboyBaseFragmentActivity class is a base class that includes the necessary Appboy method
 * calls for basic analytics and slideup integration. This class extends the Android support library
 * v4 FragmentActivity class.
 */
public class AppboyBaseFragmentActivity extends FragmentActivity {
  @Override
  public void onStart() {
    super.onStart();
    // Opens (or reopens) an Appboy session.
    // Note: This must be called in the onStart lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    Appboy.getInstance(this).openSession(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers the AppboySlideupManager for the current Activity. This Activity will now listen for
    // slideup messages from Appboy.
    AppboySlideupManager.getInstance().registerSlideupManager(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    // Unregisters the AppboySlideupManager.
    AppboySlideupManager.getInstance().unregisterSlideupManager(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Closes the current Appboy session.
    // Note: This must be called in the onStop lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    Appboy.getInstance(this).closeSession(this);
  }
}
