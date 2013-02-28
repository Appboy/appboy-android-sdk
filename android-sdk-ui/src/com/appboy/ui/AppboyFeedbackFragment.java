package com.appboy.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.appboy.Appboy;

public class AppboyFeedbackFragment extends Fragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedbackFragment.class.getName());

  private static FeedbackCustomStyle sFeedbackCustomStyle = new FeedbackCustomStyle.Builder().build();
  private static FinishAction sFinishAction = new FinishAction() {
    @Override
    public void onFinish() {
      Log.w(TAG, "Executing default FinishAction. Call AppboyFeedbackFragment.configure override the default behavior");
    }
  };

  /**
   * Configures the feedback view with the custom style and the finish action as static members. Call this
   * before passing the fragment to the FragmentManager to ensure that every instance of the AppboyFeedbackFragment
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
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflates the feedback view xml.
    return FeedbackHelper.inflateFeedbackUI(getActivity());
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // Makes the feedback fragment functional by wiring up the UI elements and applying the custom style.
    FeedbackHelper.wire(getView(), Appboy.getInstance(getActivity()), sFeedbackCustomStyle, sFinishAction);
  }
}