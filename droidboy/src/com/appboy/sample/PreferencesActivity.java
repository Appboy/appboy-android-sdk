package com.appboy.sample;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.appboy.Appboy;
import com.appboy.IAppboy;
import com.appboy.ui.AppboySlideupManager;
import com.appboy.ui.Constants;

public class PreferencesActivity extends PreferenceActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, PreferencesActivity.class.getName());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session. You can now start logging custom events.
    Appboy.getInstance(this).openSession();
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers for Appboy slideup messages.
    AppboySlideupManager.getInstance().registerSlideupUI(this);
  }

  @Override
  public void onPause() {
    // Unregisters from Appboy slideup messages.
    AppboySlideupManager.getInstance().unregisterSlideupUI(this);
    super.onPause();
  }

  @Override
  public void onStop() {
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession();
    super.onStop();
  }
}
