package com.appboy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.appboy.Appboy;
import com.appboy.IAppboy;

public class AppboyFeedbackActivity extends Activity {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedbackActivity.class.getName());

  private static FeedbackCustomStyle sFeedbackCustomStyle = new FeedbackCustomStyle.Builder().build();
  private static FinishAction sFinishAction = new FinishAction() {
    @Override
    public void onFinish() {
      Log.w(TAG, "Executing default FinishAction. Call AppboyFeedbackActivity.configure override the default behavior");
    }
  };

  private IAppboy mAppboy;

  /**
   * Configures the feedback view with the custom style and the finish action as static members. Call this
   * before calling Activity.startActivity to ensure that every instance of the AppboyFeedbackActivity
   * has the same style and functionality.
   *
   * @param feedbackCustomStyle The custom style to be applied
   * @param finishAction The action to be executed on send/cancel (typically navigation code)
   */
  public static void configure(FeedbackCustomStyle feedbackCustomStyle, FinishAction finishAction) {
    sFeedbackCustomStyle = feedbackCustomStyle;
    sFinishAction = finishAction;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Inflates the feedback view xml.
    View feedbackView = FeedbackHelper.inflateFeedbackUI(this);
    setContentView(feedbackView);

    mAppboy = Appboy.getInstance(this);

    // Makes the feedback fragment functional by wiring up the UI elements and applying the custom style.
    FeedbackHelper.wire(feedbackView, mAppboy, sFeedbackCustomStyle, sFinishAction);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Opens a new Appboy session.
    mAppboy.openSession();
  }

  @Override
  public void onStop() {
    // Closes the Appboy session.
    mAppboy.closeSession();
    super.onStop();
  }
}
