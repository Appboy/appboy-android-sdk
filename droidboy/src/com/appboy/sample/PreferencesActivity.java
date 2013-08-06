package com.appboy.sample;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.ListView;
import android.widget.Toast;
import com.appboy.Appboy;
import com.appboy.enums.SocialNetwork;
import com.appboy.ui.AppboySlideupManager;
import com.appboy.ui.Constants;
import com.crittercism.app.Crittercism;

public class PreferencesActivity extends PreferenceActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, PreferencesActivity.class.getName());

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    Preference facebookSharePreference = findPreference("facebook_share");
    Preference twitterSharePreference = findPreference("twitter_share");
    Preference logPurchasePreference = findPreference("log_purchase");

    facebookSharePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).logShare(SocialNetwork.FACEBOOK);
        Toast.makeText(PreferencesActivity.this, getString(R.string.facebook_share_toast), Toast.LENGTH_LONG).show();
        return true;
      }
    });
    twitterSharePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).logShare(SocialNetwork.TWITTER);
        Toast.makeText(PreferencesActivity.this, getString(R.string.twitter_share_toast), Toast.LENGTH_LONG).show();
        return true;
      }
    });
    logPurchasePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Appboy.getInstance(PreferencesActivity.this).logPurchase("product_id", 99);
        Toast.makeText(PreferencesActivity.this, getString(R.string.log_purchase_toast), Toast.LENGTH_LONG).show();
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
    Crittercism.leaveBreadcrumb(PreferencesActivity.class.getName());
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
