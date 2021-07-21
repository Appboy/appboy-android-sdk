package com.appboy.ui.activities;

import android.app.Activity;

import com.braze.Braze;
import com.braze.BrazeActivityLifecycleCallbackListener;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;

/**
 * @deprecated Please use {@link BrazeActivityLifecycleCallbackListener} to
 * automatically register sessions and in-app messages.
 */
@Deprecated
public class AppboyBaseActivity extends Activity {
  @Override
  public void onStart() {
    super.onStart();
    // Opens (or reopens) an Braze session.
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
