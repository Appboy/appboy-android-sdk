package com.appboy.sample;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.appboy.Appboy;
import com.appboy.ui.AppboySlideupManager;
import com.appboy.ui.Constants;

/*
 * Appboy integration sample (using Appboy Activity classes)
 *
 * To start tracking analytics using the Appboy Android SDK, you must make sure that you follow these steps
 * to integrate correctly.
 *
 * Step 1: In all activities, call Appboy.openSession() and Appboy.closeSession() in the activity's onStart()
 *         and onStop() respectively. In this sample, we put that code into DroidBoyActivity and PreferenceActivity.
 *
 * Step 2 (Optional): To integrate the Feedback form, first, create a custom style by using FeedbackCustomStyle.Builder.
 *                    Second, define the navigation action that should be performed after the feedback has been sent by
 *                    creating a FinishAction. Finally, either use the FeedbackHelper directly to inflate/attach/wire
 *                    the form or use the AppboyFeedFragment class which will do it all for you.
 */
public class DroidGirlActivity extends FragmentActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, DroidBoyActivity.class.getName());

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.droid_girl);
    activateStrictMode();
    setTitle("DroidGirl");

    FragmentManager fragmentManager = getSupportFragmentManager();
    if (savedInstanceState == null) {
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.replace(R.id.root, new DecisionFragment());
      fragmentTransaction.commit();
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session.
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
    // Unregisters from Appboy slideup messages.
    AppboySlideupManager.getInstance().unregisterSlideupUI(this);
    super.onPause();
  }


  @Override
  public void onStop() {
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession(this);
    super.onStop();
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
        startActivity(new Intent(this, AppboyFeedActivity.class));
        break;
      case R.id.feedback:
        startActivity(new Intent(this, AppboyFeedbackActivity.class));
        break;
      case R.id.settings:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      default:
        Log.e(TAG, String.format("MenuItem not found: [%s]", item.getTitle()));
    }
    return true;
  }

  @TargetApi(11)
  private void setTitleOnActionBar(String title) {
    ActionBar actionBar = getActionBar();
    actionBar.setTitle(title);
  }

  private void setTitle(String title) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      setTitleOnActionBar(title);
    } else {
      super.setTitle(title);
    }
  }

  @TargetApi(9)
  private void activateStrictMode() {
    // Set the activity to Strict mode so that we get LogCat warnings when code misbehaves on the main thread.
    if (BuildConfig.DEBUG
      && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }
  }
}

