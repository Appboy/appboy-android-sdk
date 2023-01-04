package com.appboy.sample.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.appboy.sample.R;

public class FeedFragmentActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_braze_feed_activity);
    setTitle("DroidGirl");
  }
}
