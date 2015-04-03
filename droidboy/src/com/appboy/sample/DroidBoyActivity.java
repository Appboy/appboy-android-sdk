package com.appboy.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import com.appboy.Appboy;
import com.appboy.AppboyGcmReceiver;
import com.appboy.Constants;
import com.appboy.enums.CardCategory;
import com.appboy.ui.AppboyFeedFragment;
import com.appboy.ui.AppboyFeedbackFragment;
import com.crittercism.app.Crittercism;

import java.util.EnumSet;

public class DroidBoyActivity extends AppboyFragmentActivity implements FeedCategoriesFragment.NoticeDialogListener {
  public static final String CATEGORIES_STRING= "categories";
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DroidBoyActivity.class.getName());
  private int mBackStackEntryCount = 0;
  private AppboyFeedFragment mAppboyFeedFragment;
  private AppboyFeedbackFragment mAppboyFeedbackFragment;
  private EnumSet<CardCategory> mAppboyFeedCategories;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.droid_boy);
    setTitle("DroidBoy");

    final FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment currentFragment = fragmentManager.findFragmentById(R.id.root);
    Log.i(TAG, String.format("Creating DroidBoyActivity with current fragment: %s", currentFragment));

    if (currentFragment != null) {
      // We set the FeedbackFinishedListener in onCreate() so that it assigns the new listener on every
      // orientation change.
      if (currentFragment instanceof AppboyFeedbackFragment) {
        ((AppboyFeedbackFragment) currentFragment).setFeedbackFinishedListener(getFeedbackFinishedListener());
      }
    } else {
      fragmentManager.beginTransaction().add(R.id.root, new DecisionFragment()).commit();
    }

    fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        int newBackStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (newBackStackEntryCount <= mBackStackEntryCount) {
          Crittercism.leaveBreadcrumb("Popped the back stack");
        } else {
          FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(newBackStackEntryCount - 1);
          Crittercism.leaveBreadcrumb(backStackEntry.getName());
        }
        mBackStackEntryCount = newBackStackEntryCount;
      }
    });

    // Retain the existing AppboyFeedFragment and AppboyFeedbackFragment if they exists in the backstack.
    // If they're not in the backstack, we will lazily construct them in onOptionsItemSelected, which is
    // called for each click on the options menu item.
    if (mAppboyFeedFragment == null) {
      mAppboyFeedFragment = (AppboyFeedFragment) fragmentManager.findFragmentByTag(AppboyFeedFragment.class.toString());
    }
    if (mAppboyFeedbackFragment == null) {
      mAppboyFeedbackFragment = (AppboyFeedbackFragment) fragmentManager.findFragmentByTag(AppboyFeedbackFragment.class.toString());
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  public void onResume() {
    super.onResume();
    processIntent();
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
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.root);
        if (currentFragment != null && currentFragment instanceof AppboyFeedFragment) {
          mAppboyFeedCategories = ((AppboyFeedFragment) currentFragment).getCategories();
          DialogFragment newFragment = FeedCategoriesFragment.newInstance(mAppboyFeedCategories);
          newFragment.show(getSupportFragmentManager(), "categories");
        } else {
          if (mAppboyFeedFragment == null) {
            mAppboyFeedFragment = new AppboyFeedFragment();
          }
          mAppboyFeedFragment.setCategories(mAppboyFeedCategories);
          replaceCurrentFragment(mAppboyFeedFragment);
        }
        break;
      case R.id.feedback:
        if (mAppboyFeedbackFragment == null) {
          mAppboyFeedbackFragment = new AppboyFeedbackFragment();
        }
        replaceCurrentFragment(mAppboyFeedbackFragment);
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
        Log.e(TAG, String.format("The %s menu item was not found. Ignoring.", item.getTitle()));
    }
    return true;
  }

  private void replaceCurrentFragment(Fragment newFragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment currentFragment = fragmentManager.findFragmentById(R.id.root);

    if (currentFragment != null && currentFragment.getClass().equals(newFragment.getClass())) {
      Log.i(TAG, String.format("Fragment of type %s is already the active fragment. Ignoring request to replace " +
        "current fragment.", currentFragment.getClass()));
      return;
    }

    // Re-attach any listeners. We currently only have one for the feedback fragment.
    if (newFragment instanceof AppboyFeedbackFragment) {
      ((AppboyFeedbackFragment) newFragment).setFeedbackFinishedListener(getFeedbackFinishedListener());
    }

    hideSoftKeyboard();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
      android.R.anim.fade_in, android.R.anim.fade_out);
    fragmentTransaction.replace(R.id.root, newFragment, newFragment.getClass().toString());
    if (currentFragment != null) {
      fragmentTransaction.addToBackStack(newFragment.getClass().toString());
    } else {
      fragmentTransaction.addToBackStack(null);
    }
    fragmentTransaction.commit();
  }

  private void processIntent() {
    // Check to see if the Activity was opened by the AppboyBroadcastReceiver. If it was, navigate to the
    // correct fragment.
    Bundle extras = getIntent().getExtras();
    if (extras != null && Constants.APPBOY.equals(extras.getString(AppboyBroadcastReceiver.SOURCE_KEY))) {
      // Logs that the push notification was opened. These analytics will be sent to Appboy.
      String campaignId = extras.getString(AppboyGcmReceiver.CAMPAIGN_ID_KEY);
      if (campaignId != null) {
        Appboy.getInstance(this).logPushNotificationOpened(campaignId);
      }
      navigateToDestination(extras);
    }

    // Clear the intent so that screen rotations don't cause the intent to be re-executed on.
    setIntent(new Intent());
  }

  private void navigateToDestination(Bundle extras) {
    // DESTINATION_VIEW holds the name of the fragment we're trying to visit.
    String destination = extras.getString(AppboyBroadcastReceiver.DESTINATION_VIEW);
    if (AppboyBroadcastReceiver.FEED.equals(destination)) {
      if (mAppboyFeedFragment == null) {
        mAppboyFeedFragment = new AppboyFeedFragment();
      }
      mAppboyFeedFragment.setCategories(mAppboyFeedCategories);
      replaceCurrentFragment(mAppboyFeedFragment);
    } else if (AppboyBroadcastReceiver.FEEDBACK.equals(destination)) {
      if (mAppboyFeedbackFragment == null) {
        mAppboyFeedbackFragment = new AppboyFeedbackFragment();
      }
      replaceCurrentFragment(mAppboyFeedbackFragment);
    } else if (AppboyBroadcastReceiver.HOME.equals(destination)) {
      replaceCurrentFragment(new DecisionFragment());
    }
  }

  private void hideSoftKeyboard() {
    if (getCurrentFocus() != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
        InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
  }

  private AppboyFeedbackFragment.FeedbackFinishedListener getFeedbackFinishedListener() {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    return new AppboyFeedbackFragment.FeedbackFinishedListener() {
      @Override
      public void onFeedbackFinished() {
        fragmentManager.popBackStack();
      }
    };
  }

  // The dialog fragment receives a reference to this Activity through the
  // Fragment.onAttach() callback, which it uses to call the following methods
  // defined by the NoticeDialogFragment.NoticeDialogListener interface
  public void onDialogPositiveClick(FeedCategoriesFragment dialog) {
    if (mAppboyFeedFragment == null) {
      mAppboyFeedFragment = new AppboyFeedFragment();
    }
    mAppboyFeedCategories = EnumSet.copyOf(dialog.selectedCategories);
    mAppboyFeedFragment.setCategories(mAppboyFeedCategories);
  }
}
