package com.appboy.sample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.enums.CardCategory;
import com.appboy.sample.util.RuntimePermissionUtils;
import com.appboy.support.PermissionUtils;
import com.appboy.ui.AppboyFeedFragment;
import com.appboy.ui.AppboyFeedbackFragment;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class DroidBoyActivity extends AppboyFragmentActivity implements FeedCategoriesFragment.NoticeDialogListener {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DroidBoyActivity.class.getName());
  private EnumSet<CardCategory> mAppboyFeedCategories;
  protected Context mApplicationContext;
  protected DrawerLayout mDrawerLayout;
  private Adapter mAdapter;
  private static boolean mRequestedLocationPermissions = false;
  private SharedPreferences.OnSharedPreferenceChangeListener mNewsfeedSortListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.droid_boy);
    mApplicationContext = getApplicationContext();

    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.viewpager);
    Log.i(TAG, String.format("Creating DroidBoyActivity with current fragment: %s", currentFragment));

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(null);
    toolbar.setLogo(R.drawable.ic_appboy_logo_white);

    final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
    if (viewPager != null) {
      setupViewPager(viewPager);
    }

    FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_bar);
    floatingActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Appboy.getInstance(view.getContext()).requestImmediateDataFlush();
        Toast.makeText(view.getContext(), "Requested data flush.", Toast.LENGTH_SHORT).show();
      }
    });

    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);

    mDrawerLayout = (DrawerLayout) findViewById(R.id.root);
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    if (navigationView != null) {
      setupDrawerContent(navigationView);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && !mRequestedLocationPermissions
        && !PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
      requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION);
      mRequestedLocationPermissions = true;
    }

    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
    // We implement the listener this way so that it doesn't get garbage collected when we navigate to and from this activity
    mNewsfeedSortListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
        AppboyFeedFragment feedFragment = getFeedFragment();
        if (feedFragment != null) {
          feedFragment.setSortEnabled(sharedPref.getBoolean(getString(R.string.sort_feed), false));
        }
      }
    };
    sharedPref.registerOnSharedPreferenceChangeListener(mNewsfeedSortListener);
  }

  private void setupViewPager(ViewPager viewPager) {
    mAdapter = new Adapter(getSupportFragmentManager());
    mAdapter.addFragment(new MainFragment(), "Events");
    mAdapter.addFragment(new PushTesterFragment(), "Push");
    mAdapter.addFragment(new InAppMessageTesterFragment(), "IAMs");
    mAdapter.addFragment(new AppboyFeedFragment(), "Feed");
    viewPager.setAdapter(mAdapter);
  }

  private void setupDrawerContent(NavigationView navigationView) {
    navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(MenuItem item) {
            return getNavigationItem(item);
          }
        });
  }

  public boolean getNavigationItem(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.feedback:
        mDrawerLayout.closeDrawers();
        startActivity(new Intent(mApplicationContext, FeedbackFragmentActivity.class));
        break;
      default:
        Log.e(TAG, String.format("The %s menu item was not found. Ignoring.", item.getTitle()));
    }
    return true;
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
    getMenuInflater().inflate(R.menu.actionbar_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      case R.id.feed_categories:
        AppboyFeedFragment feedFragment = getFeedFragment();
        if (feedFragment != null) {
          mAppboyFeedCategories = feedFragment.getCategories();
          if (mAppboyFeedCategories != null) {
            DialogFragment newFragment = FeedCategoriesFragment.newInstance(mAppboyFeedCategories);
            newFragment.show(getSupportFragmentManager(), "categories");
          } else {
            DialogFragment newFragment = FeedCategoriesFragment
                .newInstance(CardCategory.getAllCategories());
            newFragment.show(getSupportFragmentManager(), "categories");
          }
        } else {
          Toast.makeText(DroidBoyActivity.this, "Feed fragment hasn't been instantiated yet.",
              Toast.LENGTH_SHORT).show();
        }
        break;
      default:
        Log.e(TAG, String.format("The %s menu item was not found. Ignoring.", item.getTitle()));
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    RuntimePermissionUtils.handleOnRequestPermissionsResult(DroidBoyActivity.this, requestCode, grantResults);
  }

  private void replaceCurrentFragment(Fragment newFragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment currentFragment = fragmentManager.findFragmentById(R.id.root);

    if (currentFragment != null && currentFragment.getClass().equals(newFragment.getClass())) {
      Log.i(TAG, String.format("Fragment of type %s is already the active fragment. Ignoring request to replace "
          + "current fragment.", currentFragment.getClass()));
      return;
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
      navigateToDestination(extras);
      String bundleLogString = convertBundleToAppboyLogString(extras);
      Toast.makeText(DroidBoyActivity.this, bundleLogString, Toast.LENGTH_LONG).show();
      Log.d(TAG, bundleLogString);
    }

    // Clear the intent so that screen rotations don't cause the intent to be re-executed on.
    setIntent(new Intent());
  }

  private void navigateToDestination(Bundle extras) {
    // DESTINATION_VIEW holds the name of the fragment we're trying to visit.
    String destination = extras.getString(AppboyBroadcastReceiver.DESTINATION_VIEW);
    if (AppboyBroadcastReceiver.FEED.equals(destination)) {
      AppboyFeedFragment appboyFeedFragment = new AppboyFeedFragment();
      appboyFeedFragment.setCategories(mAppboyFeedCategories);
      replaceCurrentFragment(appboyFeedFragment);
    } else if (AppboyBroadcastReceiver.FEEDBACK.equals(destination)) {
      AppboyFeedbackFragment appboyFeedbackFragment = new AppboyFeedbackFragment();
      replaceCurrentFragment(appboyFeedbackFragment);
    } else if (AppboyBroadcastReceiver.HOME.equals(destination)) {
      replaceCurrentFragment(new MainFragment());
    }
  }

  private void hideSoftKeyboard() {
    if (getCurrentFocus() != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
          InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
  }

  public void onDialogPositiveClick(FeedCategoriesFragment dialog) {
    AppboyFeedFragment feedFragment = getFeedFragment();
    if (feedFragment != null) {
      mAppboyFeedCategories = EnumSet.copyOf(dialog.selectedCategories);
      feedFragment.setCategories(mAppboyFeedCategories);
    }
  }

  public static String convertBundleToAppboyLogString(Bundle bundle) {
    if (bundle == null) {
      return "Received intent with null extras Bundle from Appboy.";
    }
    String bundleString = "Received intent with extras Bundle of size " + bundle.size()
        + " from Appboy containing [";
    for (String key : bundle.keySet()) {
      bundleString += " '" + key + "':'" + bundle.get(key) + "'";
    }
    bundleString += " ].";
    return bundleString;
  }

  private AppboyFeedFragment getFeedFragment() {
    List<Fragment> fragments = getSupportFragmentManager().getFragments();
    for (int i = 0; i < fragments.size(); i++) {
      if (fragments.get(i) instanceof AppboyFeedFragment) {
        return (AppboyFeedFragment) fragments.get(i);
      }
    }
    return null;
  }

  static class Adapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    public Adapter(FragmentManager fm) {
      super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
      mFragments.add(fragment);
      mFragmentTitles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
      return mFragments.get(position);
    }

    @Override
    public int getCount() {
      return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return mFragmentTitles.get(position);
    }
  }
}
