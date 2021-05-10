package com.appboy.ui.activities;

import android.app.Activity;

import com.appboy.ui.inappmessage.AppboyInAppMessageManager;
import com.braze.Braze;

/**
 * @deprecated Please use {@link com.appboy.AppboyLifecycleCallbackListener} to
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
    Braze.getInstance(this).closeSession(this);
  }
}
