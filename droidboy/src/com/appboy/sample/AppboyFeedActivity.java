package com.appboy.sample;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import com.appboy.Appboy;
import com.appboy.IAppboy;
import com.appboy.ui.AppboySlideupManager;
import com.appboy.ui.Constants;

public class AppboyFeedActivity extends FragmentActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedActivity.class.getName());

  private boolean mSlideupShouldBeRequested = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_feed_activity);
    setTitle("DroidGirl");
    activateStrictMode();
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
