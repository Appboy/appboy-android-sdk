package com.appboy.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.appboy.Appboy;

public class AppboyFeedbackFragment extends Fragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedbackFragment.class.getName());

  private FeedbackCustomStyle mFeedbackCustomStyle;
  private FinishAction mFinishAction;

  public static AppboyFeedbackFragment newInstance(FeedbackCustomStyle feedbackCustomStyle, FinishAction finishAction) {
    AppboyFeedbackFragment appboyFeedbackFragment = new AppboyFeedbackFragment();
    Bundle bundle = new Bundle(2);
    bundle.putParcelable("custom_style", feedbackCustomStyle);
    bundle.putSerializable("finish_action", finishAction);
    appboyFeedbackFragment.setArguments(bundle);
    return appboyFeedbackFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle bundle = getArguments();
    mFeedbackCustomStyle = (FeedbackCustomStyle) bundle.get("custom_style");
    mFinishAction = (FinishAction) bundle.get("finish_action");
  }

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    return FeedbackHelper.inflateFeedbackUI(getActivity());
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    FeedbackHelper.wire(getView(), Appboy.getInstance(getActivity()), mFeedbackCustomStyle, mFinishAction);
  }
}