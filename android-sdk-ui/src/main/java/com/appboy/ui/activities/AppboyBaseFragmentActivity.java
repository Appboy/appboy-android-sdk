package com.appboy.ui.activities;

import androidx.fragment.app.FragmentActivity;

import com.appboy.Appboy;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

/**
 * The AppboyBaseFragmentActivity class is a base class that includes the necessary Braze method
 * calls for basic analytics and in-app message integration.
 */
public class AppboyBaseFragmentActivity extends FragmentActivity {
  @Override
  public void onStart() {
    super.onStart();
    // Opens (or reopens) a Braze session.
    // Note: This must be called in the onStart lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    Appboy.getInstance(this).openSession(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers the AppboyInAppMessageManager for the current Activity. This Activity will now listen for
    // in-app messages from Braze.
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    // Unregisters the AppboyInAppMessageManager.
    AppboyInAppMessageManager.getInstance().unregisterInAppMessageManager(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Closes the current Braze session.
    // Note: This must be called in the onStop lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    Appboy.getInstance(this).closeSession(this);
  }
}
