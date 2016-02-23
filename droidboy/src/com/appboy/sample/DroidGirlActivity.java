package com.appboy.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.appboy.Constants;

/*
 * Appboy integration sample (using Appboy Activity classes)
 *
 * To start tracking analytics using the Appboy Android SDK, you must make sure that you follow these steps
 * to integrate correctly.
 *
 * In all activities, call Appboy.openSession() and Appboy.closeSession() in the activity's onStart() and
 * onStop() respectively. In this sample, we put that code into DroidBoyActivity and PreferenceActivity.
 */
public class DroidGirlActivity extends AppboyFragmentActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DroidGirlActivity.class.getName());

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.droid_girl);
    setTitle("DroidGirl");

    FragmentManager fragmentManager = getSupportFragmentManager();
    if (savedInstanceState == null) {
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.replace(R.id.root, new MainFragment());
      fragmentTransaction.commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.decision, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.feed:
        startActivity(new Intent(this, FeedFragmentActivity.class));
        break;
      case R.id.feedback:
        startActivity(new Intent(this, FeedbackFragmentActivity.class));
        break;
      case R.id.settings:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      case R.id.inappmessages:
        startActivity(new Intent(this, InAppMessageTesterActivity.class));
        break;
      case R.id.push_testing:
        startActivity(new Intent(this, PushTesterActivity.class));
        break;
      default:
        Log.e(TAG, String.format("MenuItem not found: [%s]", item.getTitle()));
    }
    return true;
  }
}

