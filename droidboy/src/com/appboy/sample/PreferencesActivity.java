package com.appboy.sample;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.ListView;
import com.appboy.Appboy;
import com.appboy.enums.SocialNetwork;
import com.appboy.ui.AppboySlideupManager;
import com.appboy.ui.Constants;

public class PreferencesActivity extends PreferenceActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, PreferencesActivity.class.getName());

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    ListView listView = getListView();
    int mint = getResources().getColor(R.color.light_gray);
    listView.setBackgroundColor(mint);
    listView.setCacheColorHint(mint);

    Preference facebookSharePreference = findPreference("facebook_share");
    Preference twitterSharePreference = findPreference("twitter_share");

    facebookSharePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).logShare(SocialNetwork.FACEBOOK);
        return true;
      }
    });
    twitterSharePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).logShare(SocialNetwork.TWITTER);
        return true;
      }
    });
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session. You can now start logging custom events.
    Appboy.getInstance(this).openSession(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers for Appboy slideup messages.
    AppboySlideupManager.getInstance().registerSlideupUI(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    // Unregisters from Appboy slideup messages.
    AppboySlideupManager.getInstance().unregisterSlideupUI(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession(this);
  }
}
