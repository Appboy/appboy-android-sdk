package com.appboy.sample.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.appboy.Appboy;
import com.appboy.Constants;
import com.appboy.enums.CardCategory;
import com.appboy.sample.FeedCategoriesFragment;
import com.appboy.sample.InAppMessageTesterFragment;
import com.appboy.sample.MainFragment;
import com.appboy.sample.PushTesterFragment;
import com.appboy.sample.R;
import com.appboy.sample.util.RuntimePermissionUtils;
import com.appboy.sample.util.ViewUtils;
import com.appboy.support.AppboyLogger;
import com.appboy.support.PermissionUtils;
import com.appboy.ui.AppboyContentCardsFragment;
import com.appboy.ui.AppboyFeedFragment;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class DroidBoyActivity extends AppboyFragmentActivity implements FeedCategoriesFragment.NoticeDialogListener {
  private static final String TAG = AppboyLogger.getAppboyLogTag(DroidBoyActivity.class);
  private EnumSet<CardCategory> mAppboyFeedCategories;
  protected Context mApplicationContext;
  protected DrawerLayout mDrawerLayout;
  private static boolean mRequestedLocationPermissions = false;
  private FloatingActionButton mFloatingActionButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    boolean shouldDisplayInCutout = defaultSharedPreferences.getBoolean("display_in_full_cutout_setting_key", false);
    if (shouldDisplayInCutout) {
      setTheme(R.style.DisplayInNotchTheme);
      ViewUtils.enableImmersiveMode(getWindow().getDecorView());
    }
    boolean shouldDisplayNoLimitsMode = defaultSharedPreferences.getBoolean("display_no_limits_setting_key", false);
    if (shouldDisplayNoLimitsMode) {
      ViewUtils.enableNoLimitsMode(getWindow());
    }
    setContentView(R.layout.landing_page);
    mApplicationContext = getApplicationContext();

    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.viewpager);
    Log.i(TAG, String.format("Creating DroidBoyActivity with current fragment: %s", currentFragment));

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(null);

    final ViewPager viewPager = findViewById(R.id.viewpager);
    if (viewPager != null) {
      setupViewPager(viewPager);
    }

    mFloatingActionButton = findViewById(R.id.floating_action_bar);
    mFloatingActionButton.setOnClickListener(view -> {
      final Context context = view.getContext();
      Appboy.getInstance(context).requestContentCardsRefresh(false);
      Appboy.getInstance(context).requestImmediateDataFlush();
      Toast.makeText(context, "Requested data flush and content card sync.", Toast.LENGTH_SHORT).show();
    });

    TabLayout tabLayout = findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);

    mDrawerLayout = findViewById(R.id.root);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mRequestedLocationPermissions) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        boolean hasFineLocationPermission = PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (!hasFineLocationPermission) {
          // Only request fine location
          requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION);
        } else {
          // Request background now that fine is set
          requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION);
        }
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        boolean hasAllPermissions = PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            && PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (!hasAllPermissions) {
          // Request both BACKGROUND and FINE location permissions
          requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
              RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION);
        }
      } else {
        if (!PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
          // Request only FINE location permission
          requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
              RuntimePermissionUtils.DROIDBOY_PERMISSION_LOCATION);
        }
      }
      mRequestedLocationPermissions = true;
    }

    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
    // We implement the listener this way so that it doesn't get garbage collected when we navigate to and from this activity
    SharedPreferences.OnSharedPreferenceChangeListener newsfeedSortListener = (prefs, key) -> {
      SharedPreferences sharedPref1 = getSharedPreferences(getString(R.string.feed), Context.MODE_PRIVATE);
      AppboyFeedFragment feedFragment = getFeedFragment();
      if (feedFragment != null) {
        feedFragment.setSortEnabled(sharedPref1.getBoolean(getString(R.string.sort_feed), false));
      }
    };
    sharedPref.registerOnSharedPreferenceChangeListener(newsfeedSortListener);

    Log.i(TAG, "Braze device id is " + Appboy.getInstance(getApplicationContext()).getDeviceId());
  }

  private void setupViewPager(final ViewPager viewPager) {
    Adapter adapter = new Adapter(getSupportFragmentManager());
    adapter.addFragment(new MainFragment(), "Events");
    adapter.addFragment(new InAppMessageTesterFragment(), getString(R.string.inappmessage_tester_tab_title));
    adapter.addFragment(new AppboyContentCardsFragment(), "Content Cards");
    adapter.addFragment(new PushTesterFragment(), "Push");
    adapter.addFragment(new AppboyFeedFragment(), "Feed");
    viewPager.setAdapter(adapter);

    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

      @Override
      public void onPageScrollStateChanged(int state) {}

      @Override
      public void onPageSelected(int position) {
        final boolean hideFlushButton = viewPager.getAdapter().getPageTitle(position).equals(getString(R.string.inappmessage_tester_tab_title));
        runOnUiThread(() -> {
          if (hideFlushButton) {
            mFloatingActionButton.hide();
          } else {
            mFloatingActionButton.show();
          }
        });
      }
    });
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
        startActivity(new Intent(this, SettingsPreferencesActivity.class));
        break;
      case R.id.geofences_map:
        mDrawerLayout.closeDrawers();
        startActivity(new Intent(mApplicationContext, GeofencesMapActivity.class));
        break;
      case R.id.iam_sandbox:
        startActivity(new Intent(mApplicationContext, InAppMessageSandboxActivity.class));
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
          Toast.makeText(this, "Feed fragment hasn't been instantiated yet.",
              Toast.LENGTH_SHORT).show();
        }
        break;
      default:
        Log.e(TAG, String.format("The %s options item was not found. Ignoring.", item.getTitle()));
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    RuntimePermissionUtils.handleOnRequestPermissionsResult(this, requestCode, grantResults);
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
    // Check to see if the Activity was opened by the Broadcast Receiver. If it was, navigate to the
    // correct fragment.
    Bundle extras = getIntent().getExtras();
    if (extras != null && Constants.APPBOY.equals(extras.getString(getResources().getString(R.string.source_key)))) {
      navigateToDestination(extras);
      String bundleLogString = convertBundleToAppboyLogString(extras);
      Toast.makeText(this, bundleLogString, Toast.LENGTH_LONG).show();
      Log.d(TAG, bundleLogString);
    }

    // Clear the intent so that screen rotations don't cause the intent to be re-executed on.
    setIntent(new Intent());
  }

  private void navigateToDestination(Bundle extras) {
    // DESTINATION_VIEW holds the name of the fragment we're trying to visit.
    String destination = extras.getString(getResources().getString(R.string.destination_view));
    if (getResources().getString(R.string.feed_key).equals(destination)) {
      AppboyFeedFragment appboyFeedFragment = new AppboyFeedFragment();
      appboyFeedFragment.setCategories(mAppboyFeedCategories);
      replaceCurrentFragment(appboyFeedFragment);
    } else if (getResources().getString(R.string.home).equals(destination)) {
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
      return "Received intent with null extras Bundle from Braze.";
    }
    String bundleString = "Received intent with extras Bundle of size " + bundle.size()
        + " from Braze containing [";
    for (String key : bundle.keySet()) {
      bundleString += " '" + key + "':'" + bundle.get(key) + "'";
    }
    bundleString += " ].";
    return bundleString;
  }
  
  @SuppressLint("RestrictedApi")
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
