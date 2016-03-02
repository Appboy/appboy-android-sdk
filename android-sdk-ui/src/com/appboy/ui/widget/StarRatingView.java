package com.appboy.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appboy.Constants;
import com.appboy.support.AppboyLogger;
import com.appboy.ui.R;

import java.util.ArrayList;
import java.util.List;

public class StarRatingView extends LinearLayout {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, StarRatingView.class.getName());

  private static final int MAX_NUMBER_OF_STARS = 5;
  private List<ImageView> mStarRating;
  private float mRating = 0.0f;

  public StarRatingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOrientation(HORIZONTAL);

    // Initialize the star views and layout parameters so they can be changed to match the rating of this specific card later.
    mStarRating = new ArrayList<ImageView>(MAX_NUMBER_OF_STARS);
    for (int i = 0; i < MAX_NUMBER_OF_STARS; i++) {
      ImageView star = new ImageView(context);
      star.setTag(0);
      ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      addView(star, layoutParams);
      mStarRating.add(star);
    }

    setRating(mRating);
  }

  public float getRating() {
    return mRating;
  }

  public boolean setRating(float rating) {
    if (rating < 0 || rating > MAX_NUMBER_OF_STARS) {
      AppboyLogger.e(TAG, String.format("Unable to set rating to %f. Rating must be between 0 and %d", rating, MAX_NUMBER_OF_STARS));
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

  List<ImageView> getImageViewList() {
    return mStarRating;
  }
}
