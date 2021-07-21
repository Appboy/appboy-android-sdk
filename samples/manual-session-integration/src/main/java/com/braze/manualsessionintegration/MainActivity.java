package com.braze.manualsessionintegration;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.braze.Braze;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

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
