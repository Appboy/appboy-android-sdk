package com.braze.ui.activities;

import androidx.fragment.app.FragmentActivity;

import com.braze.Braze;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;

/**
 * The AppboyBaseFragmentActivity class is a base class that includes the necessary Braze method
 * calls for basic analytics and in-app message integration.
 */
public class BrazeBaseFragmentActivity extends FragmentActivity {
  @Override
  public void onStart() {
    super.onStart();
    // Opens (or reopens) a Braze session.
    // Note: This must be called in the onStart lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    Braze.getInstance(this).openSession(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers the BrazeInAppMessageManager for the current Activity. This Activity will now listen for
    // in-app messages from Braze.
    BrazeInAppMessageManager.getInstance().registerInAppMessageManager(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    // Unregisters the BrazeInAppMessageManager.
    BrazeInAppMessageManager.getInstance().unregisterInAppMessageManager(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Closes the current Braze session.
    // Note: This must be called in the onStop lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    Braze.getInstance(this).closeSession(this);
  }
}
