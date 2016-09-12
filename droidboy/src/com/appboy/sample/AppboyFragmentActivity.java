package com.appboy.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.ui.inappmessage.AppboyInAppMessageManager;

/*
 * Appboy integration sample
 *
 * To start tracking analytics using the Appboy Android SDK, in all activities, you must call Appboy.openSession()
 * and Appboy.closeSession() in the activity's onStart() and onStop() respectively. You can see that in this
 * activity (inherited by most other activities) and com.appboy.sample.PreferencesActivity.
 */
public class AppboyFragmentActivity extends AppCompatActivity {
  protected static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, AppboyFragmentActivity.class.getName());
  private boolean mRefreshData;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens (or reopens) an Appboy session.
    // Note: This must be called in the onStart lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    if (Appboy.getInstance(this).openSession(this)) {
      mRefreshData = true;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers the AppboyInAppMessageManager for the current Activity. This Activity will now listen for
    // in-app messages from Appboy.
    AppboyInAppMessageManager.getInstance().registerInAppMessageManager(this);
    if (mRefreshData) {
      Appboy.getInstance(this).requestInAppMessageRefresh();
      mRefreshData = false;
    }
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
    // Closes the current Appboy session.
    // Note: This must be called in the onStop lifecycle method of EVERY Activity. Failure to do so
    // will result in incomplete and/or erroneous analytics.
    Appboy.getInstance(this).closeSession(this);
  }
}
