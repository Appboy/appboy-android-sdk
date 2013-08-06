package com.appboy.sample;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.appboy.Appboy;
import com.appboy.ui.AppboySlideupManager;
import com.appboy.ui.Constants;
import com.crittercism.app.Crittercism;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Appboy integration sample (using Appboy Fragments)
 *
 * To start tracking analytics using the Appboy Android SDK, in all activities, you must call Appboy.openSession()
 * and Appboy.closeSession() in the activity's onStart() and onStop() respectively. You can see that in this
 * activity (inherited by most other activities) and com.appboy.sample.PreferencesActivity.
 */
public class AppboyFragmentActivity extends FragmentActivity {
  protected static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFragmentActivity.class.getName());
  private boolean mRefreshData;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Crittercism.init(getApplicationContext(), "51b731aa8b2e337d52000002", getCrittercismConfiguration());
    activateStrictMode();
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session. If we've opened a new session, request a refresh of the slideup.
    if (Appboy.getInstance(this).openSession(this)) {
      mRefreshData = true;
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    // Register for slideup messages and request a new one, if applicable.
    AppboySlideupManager.getInstance().registerSlideupUI(this);
    if (mRefreshData) {
      Appboy.getInstance(this).requestSlideupRefresh();
      mRefreshData = false;
    }

    Crittercism.leaveBreadcrumb(this.getClass().getName());
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

  @TargetApi(9)
  private void activateStrictMode() {
    // Set the activity to Strict mode so that we get LogCat warnings when code misbehaves on the main thread.
    if (BuildConfig.DEBUG
      && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }
  }

  protected void setTitle(String title) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      setTitleOnActionBar(title);
    } else {
      super.setTitle(title);
    }
  }

  @TargetApi(11)
  private void setTitleOnActionBar(String title) {
    ActionBar actionBar = getActionBar();
    actionBar.setTitle(title);
  }

  private JSONObject getCrittercismConfiguration() {
    JSONObject crittercismConfiguration = new JSONObject();
    String customVersionName = getCustomVersionName();
    try {
      crittercismConfiguration.put("includeVersionCode", true);
      crittercismConfiguration.put("shouldCollectLogcat", true);
      if (customVersionName != null) {
        crittercismConfiguration.put("customVersionName", customVersionName);
      }
    } catch (JSONException e) {
      Log.w(TAG, "Error while creating the Crittercism configuration. Using default configuration.");
    }
    return crittercismConfiguration;
  }

  private String getCustomVersionName() {
    String customVersionName = null;
    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      customVersionName = String.format("%s-%s", packageInfo.versionName, com.appboy.Constants.APPBOY_SDK_VERSION);
    } catch (PackageManager.NameNotFoundException e) {
      Log.w(TAG, "Unable to read the version name.");
    }
    return customVersionName;
  }
}
