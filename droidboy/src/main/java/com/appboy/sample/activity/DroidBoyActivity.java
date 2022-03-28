package com.appboy.sample.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.appboy.Constants;
import com.appboy.enums.CardCategory;
import com.appboy.sample.FeedCategoriesFragment;
import com.appboy.sample.InAppMessageTesterFragment;
import com.appboy.sample.MainFragment;
import com.appboy.sample.PushTesterFragment;
import com.appboy.sample.R;
import com.appboy.sample.activity.settings.SettingsFragment;
import com.appboy.sample.util.EnvironmentUtils;
import com.appboy.sample.util.RuntimePermissionUtils;
import com.appboy.sample.util.ViewUtils;
import com.appboy.ui.AppboyFeedFragment;
import com.braze.Braze;
import com.braze.configuration.BrazeConfigurationProvider;
import com.braze.support.BrazeLogger;
import com.braze.support.PermissionUtils;
import com.braze.support.StringUtils;
import com.braze.ui.contentcards.ContentCardsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@SuppressWarnings("deprecation")
public class DroidBoyActivity extends AppCompatActivity implements FeedCategoriesFragment.NoticeDialogListener {
  private static final String TAG = BrazeLogger.getBrazeLogTag(DroidBoyActivity.class);
  private EnumSet<CardCategory> mAppboyFeedCategories;
  protected Context mApplicationContext;
  protected DrawerLayout mDrawerLayout;
  private static boolean mRequestedLocationPermissions = false;
  private FloatingActionButton mFloatingActionButton;
  private String mRequestedPermission = null;
  private ActivityResultLauncher<String> mRequestPermissionLauncher =
      registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        StringBuilder stringBuilder = new StringBuilder();
        switch (mRequestedPermission) {
          case Manifest.permission.ACCESS_FINE_LOCATION:
            stringBuilder.append("Location permission ");
            break;
          case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
            stringBuilder.append("Background location permission ");
            break;
          default:
            stringBuilder.append("Unknown permission ");
            break;
        }
        stringBuilder.append(result ? "granted" : "denied");
        Toast.makeText(getApplicationContext(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && Manifest.permission.ACCESS_FINE_LOCATION.equals(mRequestedPermission)
            && result) {
          makePermissionRequest(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
      });

  private ActivityResultLauncher<String[]> mRequestMultiplePermissionLauncher =
      registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        if (result.containsKey(Manifest.permission.ACCESS_FINE_LOCATION)
            && !result.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
          Toast.makeText(getApplicationContext(), "Location permissions denied.", Toast.LENGTH_SHORT).show();
        } else if (result.containsKey(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            && !result.get(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
          Toast.makeText(getApplicationContext(), "Background location permissions denied.", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getApplicationContext(), "All required location permissions granted.", Toast.LENGTH_SHORT).show();
        }
      });

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SharedPreferences defaultSharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
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
      Braze.getInstance(context).requestContentCardsRefresh(false);
      Braze.getInstance(context).requestImmediateDataFlush();
      Toast.makeText(context, "Requested data flush and content card sync.", Toast.LENGTH_SHORT).show();
    });

    TabLayout tabLayout = findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(viewPager);

    mDrawerLayout = findViewById(R.id.root);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !mRequestedLocationPermissions) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        boolean hasFineLocationPermission = PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (!hasFineLocationPermission) {
          makePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION);
        } else if (!PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
          // Request background now that fine is set
          makePermissionRequest(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        boolean hasAllPermissions = PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            && PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (!hasAllPermissions) {
          // Request both BACKGROUND and FINE location permissions
          RuntimePermissionUtils.requestLocationPermissions(this,
              new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
              mRequestMultiplePermissionLauncher);
        }
      } else {
        // From M to P, FINE gives us background access
        if (!PermissionUtils.hasPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
          // Request only FINE location permission
          makePermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION);
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
    Log.i(TAG, "Braze device id is " + Braze.getInstance(getApplicationContext()).getDeviceId());
  }

  private void makePermissionRequest(String permission) {
    // The callback for single permissions doesn't tell us what the request was, so we keep track ourselves
    mRequestedPermission = permission;
    RuntimePermissionUtils.requestLocationPermission(this,
        permission,
        mRequestPermissionLauncher);
  }

  private void setupViewPager(final ViewPager viewPager) {
    Adapter adapter = new Adapter(getSupportFragmentManager());
    adapter.addFragment(new MainFragment(), "Main");
    adapter.addFragment(new InAppMessageTesterFragment(), getString(R.string.inappmessage_tester_tab_title));
    adapter.addFragment(new ContentCardsFragment(), "Content Cards");
    adapter.addFragment(new PushTesterFragment(), "Push");
    adapter.addFragment(new SettingsFragment(), getString(R.string.settings_fragment_tab_title));
    viewPager.setAdapter(adapter);

    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        final CharSequence pageTitle = viewPager.getAdapter().getPageTitle(position);
        final boolean hideFlushButton = pageTitle.equals(getString(R.string.inappmessage_tester_tab_title))
            || pageTitle.equals(getString(R.string.settings_fragment_tab_title));
        runOnUiThread(() -> {
          if (hideFlushButton) {
            mFloatingActionButton.hide();
          } else {
            mFloatingActionButton.show();
          }
        });
      }

      @Override
      public void onPageScrollStateChanged(int state) {}

      @Override
      public void onPageSelected(int position) {}
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

    final BrazeConfigurationProvider configurationProvider = new BrazeConfigurationProvider(this);
    String endpoint = configurationProvider.getBaseUrlForRequests();
    if (!StringUtils.isNullOrEmpty(configurationProvider.getCustomEndpoint())) {
      endpoint = configurationProvider.getCustomEndpoint();
    }
    ((TextView) findViewById(R.id.toolbar_info_endpoint)).setText("endpoint: " + endpoint);

    final String configuredApiKey = Braze.getConfiguredApiKey(configurationProvider);
    ((TextView) findViewById(R.id.toolbar_info_api_key)).setText("current api key: " + configuredApiKey);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.actionbar_options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.feed_activity_launch:
        startActivity(new Intent(this, FeedFragmentActivity.class));
        break;
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
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
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SettingsFragment.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      Bitmap bitmap = (Bitmap) extras.get("data");
      EnvironmentUtils.analyzeBitmapForEnvironmentBarcode(this, bitmap);
    }
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
    StringBuilder bundleString = new StringBuilder().append("Received intent with extras Bundle of size ").append(bundle.size()).append(" from Braze containing [");
    for (String key : bundle.keySet()) {
      bundleString.append(" '").append(key).append("':'").append(bundle.get(key)).append('\'');
    }
    bundleString.append(" ].");
    return bundleString.toString();
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

  static class Adapter extends androidx.fragment.app.FragmentPagerAdapter {
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
