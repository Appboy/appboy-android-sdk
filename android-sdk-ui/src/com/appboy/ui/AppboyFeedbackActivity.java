package com.appboy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.appboy.Appboy;
import com.appboy.IAppboy;

public class AppboyFeedbackActivity extends Activity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedbackActivity.class.getName());

  private IAppboy mAppboy;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View feedbackView = FeedbackHelper.inflateFeedbackUI(this);
    setContentView(feedbackView);
    mAppboy = Appboy.getInstance(this);

    FinishAction finishAction = new FinishAction() {
      @Override
      public void onFinish() {
        AppboyFeedbackActivity.this.finish();
      }
    };

    FeedbackHelper.wire(feedbackView, mAppboy, finishAction);
  }

  @Override
  public void onStart() {
    super.onStart();
    mAppboy.openSession();
  }

  @Override
  public void onStop() {
    mAppboy.closeSession();
    super.onStop();
  }
}
