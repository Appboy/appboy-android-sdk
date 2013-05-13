package com.appboy.sample;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.appboy.Appboy;
import com.appboy.AppboyGcmReceiver;
import com.appboy.IAppboy;
import com.appboy.ui.*;

/*
 * Appboy integration sample (using Appboy Fragments)
 *
 * To start tracking analytics using the Appboy Android SDK, you must make sure that you follow these steps
 * to integrate correctly.
 *
 * In all activities, call Appboy.openSession() and Appboy.closeSession() in the activity's onStart() and
 * onStop() respectively. In this sample, we put that code into DroidBoyActivity and PreferenceActivity.
 */
public class DroidBoyActivity extends FragmentActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, DroidBoyActivity.class.getName());

  private FragmentManager mFragmentManager;
  private DecisionFragment mDecisionFragment;
  private boolean mSlideupShouldBeRequested = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.droid_boy);
    activateStrictMode();

    mDecisionFragment = new DecisionFragment();
    mFragmentManager = getSupportFragmentManager();
    replaceCurrentFragment(mDecisionFragment);
    setTitle("DroidBoy");
  }

  @Override
  public void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session.
    mSlideupShouldBeRequested = Appboy.getInstance(this).openSession(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    // Registers for Appboy slideup messages.
    AppboySlideupManager.getInstance().registerSlideupUI(this);

    // Requests a slideup from the Appboy server only when a new session has started.
    if (mSlideupShouldBeRequested) {
      Appboy.getInstance(this).requestSlideupRefresh();
    }

    processIntent();
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
        replaceCurrentFragment(new AppboyFeedFragment());
        break;
      case R.id.feedback:
        replaceCurrentFragment(createFeedbackFragment());
        break;
      case R.id.settings:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      default:
        Log.e(TAG, String.format("The %s menu item was not found. Ignoring.", item.getTitle()));
    }
    return true;
  }

  @TargetApi(9)
  private void activateStrictMode() {
    // Set the activity to Strict mode so that we get LogCat warnings when code misbehaves on the main thread.
    if (BuildConfig.DEBUG
      && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }
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

  private void replaceCurrentFragment(Fragment fragment) {
    Fragment currentFragment = mFragmentManager.findFragmentById(R.id.root);
    if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
      Log.i(TAG, String.format("Fragment of type %s is already the active fragment. Ignoring request to replace " +
        "current fragment.", currentFragment.getClass()));
      return;
    }

    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
      android.R.anim.fade_in, android.R.anim.fade_out);
    fragmentTransaction.replace(R.id.root, fragment);
    if (currentFragment != null) {
      fragmentTransaction.addToBackStack(null);
    }
    fragmentTransaction.commit();
  }

  private void processIntent() {
    // Check to see if the Activity was opened by the NotificationActionReceiver. If it was, navigate to the
    // fragment identified by the APPBOY_TARGET_KEY.
    Bundle extras = getIntent().getExtras();
    if (extras != null && Constants.APPBOY.equals(extras.getString(NotificationActionReceiver.SOURCE_KEY))) {
      logPushNotificationOpened(extras);
      navigateToDestination(extras);
    }
  }

  private void logPushNotificationOpened(Bundle extras) {
    // Logs that the push notification was opened. These analytics will be sent to Appboy.
    String campaignId = extras.getString(AppboyGcmReceiver.CAMPAIGN_ID_KEY);
    if (campaignId != null) {
      Appboy.getInstance(this).logPushNotificationOpened(campaignId);
    }
  }

  private void navigateToDestination(Bundle extras) {
    // Navigate to the feed/feedback/home fragment based on the intent extras.
    String destination = extras.getString(NotificationActionReceiver.DESTINATION_VIEW);
    if (NotificationActionReceiver.FEED.equals(destination)) {
      replaceCurrentFragment(new AppboyFeedFragment());
    } else if (NotificationActionReceiver.FEEDBACK.equals(destination)) {
      replaceCurrentFragment(new AppboyFeedbackFragment());
    } else if (NotificationActionReceiver.HOME.equals(destination)) {
      replaceCurrentFragment(mDecisionFragment);
    }
  }

  private AppboyFeedbackFragment createFeedbackFragment() {
    AppboyFeedbackFragment appboyFeedbackFragment = new AppboyFeedbackFragment();
    appboyFeedbackFragment.setFeedbackFinishedListener(new AppboyFeedbackFragment.FeedbackFinishedListener() {
      @Override
      public void onFeedbackFinished() {
        mFragmentManager.popBackStack();
      }
    });
    return appboyFeedbackFragment;
  }
}
