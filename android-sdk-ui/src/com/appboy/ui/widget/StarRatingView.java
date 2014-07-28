package com.appboy.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.appboy.Constants;
import com.appboy.ui.R;

import java.util.ArrayList;
import java.util.List;

public class StarRatingView extends LinearLayout {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, StarRatingView.class.getName());

  private int mNumStars;
  private List<ImageView> mStarRating;
  private float mRating;

  public StarRatingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  @TargetApi(11)
  public StarRatingView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  public float getRating() {
    return mRating;
  }

  public boolean setRating(float rating) {
    if (rating < 0 || rating > mNumStars) {
      Log.e(TAG, String.format("Unable to set rating to %f. Rating must be between 0 and %d", rating, mNumStars));
      return false;
    }

    mRating = rating;
    int ratingRoundedDown = (int) Math.floor(mRating);
    int ratingRoundedUp = (int) Math.ceil(mRating);
    for (int starIndex = 0; starIndex < ratingRoundedDown; starIndex++) {
      ImageView star = mStarRating.get(starIndex);
      star.setTag(R.drawable.com_appboy_rating_full_star);
      star.setImageResource(R.drawable.com_appboy_rating_full_star);
    }
    for (int starIndex = ratingRoundedUp; starIndex < mStarRating.size(); starIndex++) {
      ImageView star = mStarRating.get(starIndex);
      star.setTag(R.drawable.com_appboy_rating_empty_star);
      star.setImageResource(R.drawable.com_appboy_rating_empty_star);
    }

    // Processing the remainder. A remainder between [0.25, 0.75) will be displayed as a half star.
    // Otherwise, it be rounded up/down to the nearest whole star.
    float remainder = rating - ratingRoundedDown;
    if (remainder > 0.0f) {
      ImageView star = mStarRating.get(ratingRoundedDown);
      if (remainder < 0.25f) {
        star.setTag(R.drawable.com_appboy_rating_empty_star);
        star.setImageResource(R.drawable.com_appboy_rating_empty_star);
      } else if (remainder < 0.75f) {
        star.setTag(R.drawable.com_appboy_rating_half_star);
        star.setImageResource(R.drawable.com_appboy_rating_half_star);
      } else {
        star.setTag(R.drawable.com_appboy_rating_full_star);
        star.setImageResource(R.drawable.com_appboy_rating_full_star);
      }
    }
    return true;
  }

  private void init(Context context, AttributeSet attrs, int defStyle) {
    setOrientation(HORIZONTAL);

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StarRatingView, defStyle, 0);
    mNumStars = typedArray.getInteger(R.styleable.StarRatingView_numStars, 5);
    mRating = typedArray.getFloat(R.styleable.StarRatingView_defaultRating, 0.0f);
    typedArray.recycle();

    mStarRating = new ArrayList<ImageView>(mNumStars);
    for (int i = 0; i < mNumStars; i++) {
      ImageView star = new ImageView(context);
      star.setTag(0);
      ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      addView(star, layoutParams);
      mStarRating.add(star);
    }

    setRating(mRating);
  }

  List<ImageView> getImageViewList() {
    return mStarRating;
  }
}
