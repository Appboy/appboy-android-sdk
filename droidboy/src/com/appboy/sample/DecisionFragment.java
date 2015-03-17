package com.appboy.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import com.appboy.Constants;

import java.util.Random;

public class DecisionFragment extends Fragment {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, DecisionFragment.class.getName());

  private ImageView mSpinner;
  private Random mRandom;
  private Interpolator mInterpolator;
  private float mCurrentSpinnerInDegrees;
  private final int mSpinnerDuration = 5;
  private Button mSpinButton;
  private Animation.AnimationListener mSpinAnimationListener;

  public DecisionFragment() {}

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
    mSpinButton = (Button) contentView.findViewById(R.id.spin);
    return contentView;
  }

  @Override
  public void onStart() {
    super.onStart();
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    resetSpinner();

    mSpinAnimationListener = new Animation.AnimationListener() {
      public void onAnimationStart(Animation animation) {
        mSpinButton.setEnabled(false);
      }
      public void onAnimationEnd(Animation animation) {
        mSpinButton.setEnabled(true);
      }
      public void onAnimationRepeat(Animation animation) { }
    };

    mSpinButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        performSpin();
      }
    });
  }

  public void performSpin() {
    float rotationDegrees = getRotationInDegrees();
    Animation animation = getRotationAnimation(rotationDegrees);
    animation.setAnimationListener(mSpinAnimationListener);
    spin(animation, rotationDegrees);
  }

  private float getRotationInDegrees() {
    float minRotationDegrees = mSpinnerDuration * 2 * 360f;
    return minRotationDegrees + mRandom.nextInt(360);
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

  private void resetSpinner() {
    mCurrentSpinnerInDegrees = 0;
    mSpinner.clearAnimation();
  }
}
