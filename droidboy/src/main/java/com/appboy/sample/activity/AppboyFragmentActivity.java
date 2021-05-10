package com.appboy.sample.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.braze.support.BrazeLogger;

import io.branch.referral.Branch;

/*
 * Braze integration sample
 *
 * To start tracking analytics using the Braze Android SDK, in all activities, you must call Appboy.openSession()
 * and Appboy.closeSession() in the activity's onStart() and onStop() respectively.
 */
public class AppboyFragmentActivity extends AppCompatActivity {
  protected static final String TAG = BrazeLogger.getBrazeLogTag(AppboyFragmentActivity.class);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    Branch.getInstance(getApplicationContext()).initSession();
  }

  @Override
  public void onStop() {
    super.onStop();
    Branch.getInstance(getApplicationContext()).closeSession();
  }
}
