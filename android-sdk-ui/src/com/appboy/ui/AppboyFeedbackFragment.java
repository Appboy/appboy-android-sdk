package com.appboy.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.appboy.Appboy;
import com.appboy.IAppboy;

// TODO(martin) - Eclipse doesn't seem to like Honeycomb Fragments when building against a pre-Honeycomb target. Need
//                to import the project into Eclipse and straighten this out soon.
public class AppboyFeedbackFragment extends Fragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, AppboyFeedbackFragment.class.getName());

  private IAppboy mAppboy;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAppboy = Appboy.getInstance(getActivity());
  }

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    return FeedbackHelper.inflateFeedbackUI(getActivity());
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    FinishAction finishAction = new FinishAction() {
      @Override
      public void onFinish() {
        FragmentTransaction sendTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        sendTransaction.remove(AppboyFeedbackFragment.this);
        sendTransaction.commit();
      }
    };

    FeedbackHelper.wire(getView(), mAppboy, finishAction);
  }
}