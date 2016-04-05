package com.appboy.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import com.appboy.ui.AppboyFeedbackFragment;

public class FeedbackFragmentActivity extends AppboyFragmentActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_feedback_activity);

    final FragmentManager fragmentManager = getSupportFragmentManager();
    AppboyFeedbackFragment appboyFeedbackFragment = (AppboyFeedbackFragment) fragmentManager.findFragmentById(R.id.com_appboy_feedback);
    appboyFeedbackFragment.setFeedbackFinishedListener(new AppboyFeedbackFragment.FeedbackFinishedListener() {
      @Override
      public void onFeedbackFinished(AppboyFeedbackFragment.FeedbackResult disposition) {
        finish();
      }

      @Override
      public String beforeFeedbackSubmitted(String message) {
        return message;
      }
    });
  }
}
