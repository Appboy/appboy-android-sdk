package com.appboy.sample.activity;

import android.os.Bundle;

import com.appboy.sample.R;

public class FeedFragmentActivity extends AppboyFragmentActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_feed_activity);
    setTitle("DroidGirl");
  }
}
