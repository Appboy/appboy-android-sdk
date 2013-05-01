package com.appboy.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.appboy.Appboy;

public class AppboyFeedbackActivity extends FragmentActivity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedbackActivity.class.getName());

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_appboy_feedback_container);

    FinishAction mFinishAction = new FinishAction() {
      @Override
      public void onFinish() {
        AppboyFeedbackActivity.this.finish();
      }
    };
    AppboyFeedbackFragment.configure(mFinishAction);
    AppboyFeedbackFragment appboyFeedbackFragment = new AppboyFeedbackFragment();

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(R.id.com_appboy_feedback_container_root, appboyFeedbackFragment);
    fragmentTransaction.commit();
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session.
    Appboy.getInstance(this).openSession();
  }

  @Override
  public void onStop() {
    // Closes the Appboy session.
    Appboy.getInstance(this).closeSession();
    super.onStop();
  }
}