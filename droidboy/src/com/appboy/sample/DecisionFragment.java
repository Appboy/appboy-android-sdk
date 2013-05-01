package com.appboy.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import com.appboy.Appboy;
import com.appboy.ui.Constants;

import java.util.Random;

public class DecisionFragment extends Fragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, DecisionFragment.class.getName());

  private ImageView mSpinner;
  private Random mRandom;
  private Interpolator mInterpolator;
  private float mCurrentSpinnerInDegrees;
  private int mSpinnerDuration;
  private View mSelectedMode;
  private View mBisectMode;
  private View mQuadrisectMode;
  private float mSpinDirectionMultiplier;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    mRandom = new Random();
    mInterpolator = new AccelerateDecelerateInterpolator();
    mCurrentSpinnerInDegrees = 0f;
  }

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    View contentView = layoutInflater.inflate(R.layout.decision, container, false);

    mSpinner = (ImageView) contentView.findViewById(R.id.spinner);
    final Button spinButton = (Button) contentView.findViewById(R.id.spin);
    final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
      public void onAnimationStart(Animation animation) {
        spinButton.setEnabled(false);
      }
      public void onAnimationEnd(Animation animation) {
        spinButton.setEnabled(true);
      }
      public void onAnimationRepeat(Animation animation) { }
    };
    spinButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        float rotationDegrees = getRotationInDegrees();
        Animation animation = getRotationAnimation(rotationDegrees);
        animation.setAnimationListener(animationListener);
        spin(animation, rotationDegrees);
      }
    });

    mSelectedMode = contentView.findViewById(R.id.mode_placeholder);
    mBisectMode = getActivity().getLayoutInflater().inflate(R.layout.bisect, (ViewGroup) mSelectedMode.getParent(), false);
    mQuadrisectMode = getActivity().getLayoutInflater().inflate(R.layout.quadrisect, (ViewGroup) mSelectedMode.getParent(), false);
    switchBoardMode(mBisectMode);

    return contentView;
  }

  @Override
  public void onStart() {
    super.onStart();
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    int boardChoices = Integer.parseInt(sharedPreferences.getString("board.choices", getString(R.string.default_board_choices)));
    switch (boardChoices) {
      case 2:
        switchBoardMode(mBisectMode);
        break;
      case 4:
        switchBoardMode(mQuadrisectMode);
        break;
      default:
        Log.e(TAG, "Unknown board configuration! Defaulting to mode '2'");
        switchBoardMode(mBisectMode);
    }
    mSpinnerDuration = Integer.parseInt(sharedPreferences.getString("spinner.duration", getString(R.string.default_spinner_duration)));
    mSpinDirectionMultiplier = Float.parseFloat(sharedPreferences.getString("spin.direction", getString(R.string.default_spin_direction)));
    resetSpinner();
  }

  private float getRotationInDegrees() {
    float minRotationDegrees = mSpinnerDuration * 2 * 360f;
    return (minRotationDegrees + mRandom.nextInt(360)) * mSpinDirectionMultiplier;
  }

  private Animation getRotationAnimation(float rotationDegrees) {
    RotateAnimation rotationAnimation = new RotateAnimation(mCurrentSpinnerInDegrees, mCurrentSpinnerInDegrees + rotationDegrees,
      Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    rotationAnimation.setStartOffset(0l);
    rotationAnimation.setDuration(mSpinnerDuration * 1000l);
    rotationAnimation.setFillAfter(true);
    rotationAnimation.setInterpolator(mInterpolator);
    return rotationAnimation;
  }

  private void spin(Animation animation, float rotationDegrees) {
    Appboy.getInstance(getActivity()).logCustomEvent("spin");
    mSpinner.startAnimation(animation);
    mCurrentSpinnerInDegrees += rotationDegrees;
    mCurrentSpinnerInDegrees %= 360f;
  }

  private void switchBoardMode(View secondView) {
    ViewGroup parent = (ViewGroup) mSelectedMode.getParent();
    int index = parent.indexOfChild(mSelectedMode);
    parent.removeView(mSelectedMode);
    parent.addView(secondView, index);
    mSelectedMode = secondView;
  }

  private void resetSpinner() {
    mCurrentSpinnerInDegrees = 0;
    mSpinner.clearAnimation();
  }
}
