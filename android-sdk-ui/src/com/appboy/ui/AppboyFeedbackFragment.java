package com.appboy.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import com.appboy.Appboy;

public class AppboyFeedbackFragment extends Fragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedbackFragment.class.getName());

  private static FinishAction sFinishAction = new FinishAction() {
    @Override
    public void onFinish() {
      Log.w(TAG, "Executing default FinishAction. Call AppboyFeedbackFragment.configure override the default behavior");
    }
  };

  /**
   * Configures the feedback view with th finish action as a static member. Call this before passing the fragment
   * to the FragmentManager to ensure that every instance of the AppboyFeedbackFragment has the same functionality.
   *
   * @param finishAction The action to be executed on send/cancel (typically navigation code)
   */
  public static void configure(FinishAction finishAction) {
    sFinishAction = finishAction;
  }

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    return layoutInflater.inflate(R.layout.com_appboy_feedback, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // Makes the feedback fragment functional by wiring up the UI elements.
    FeedbackHelper.wire(getView(), Appboy.getInstance(getActivity()), sFinishAction);
  }

  @Override
  public void onResume() {
    super.onResume();
    Appboy.getInstance(getActivity()).logFeedbackDisplayed();
  }

  @Override
  public void onPause() {
    super.onPause();
    // Hide keyboard when paused.
    Activity activity = getActivity();
    View currentFocusView = activity.getCurrentFocus();
    if (currentFocusView != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
        InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
  }
}