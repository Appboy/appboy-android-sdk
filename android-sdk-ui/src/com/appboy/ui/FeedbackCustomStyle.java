package com.appboy.ui;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A model class that contains the custom styling for the Feedback form.
 *
 * To create a custom style, use the FeedbackCustomStyle.Builder class to override any properties with custom
 * style values. All custom style attributes are optional.
 */
public class FeedbackCustomStyle implements Parcelable {
  private final Integer mFontColor;
  private final Integer mNavigationBarColor;
  private final Integer mNavigationButtonsBackgroundEnabledColor;
  private final Integer mNavigationButtonsBackgroundDisabledColor;
  private final Integer mNavigationButtonsFontColor;
  private final Integer mHintColor;

  public FeedbackCustomStyle(Parcel parcel) {
    ClassLoader classLoader = Integer.class.getClassLoader();
    mFontColor = (Integer) parcel.readValue(classLoader);
    mNavigationBarColor = (Integer) parcel.readValue(classLoader);
    mNavigationButtonsBackgroundEnabledColor = (Integer) parcel.readValue(classLoader);
    mNavigationButtonsBackgroundDisabledColor = (Integer) parcel.readValue(classLoader);
    mNavigationButtonsFontColor = (Integer) parcel.readValue(classLoader);
    mHintColor = (Integer) parcel.readValue(classLoader);
  }

  private FeedbackCustomStyle(Builder builder) {
    mHintColor = builder.mHintColor;
    mFontColor = builder.mFontColor;
    mNavigationBarColor = builder.mNavigationBarColor;
    mNavigationButtonsBackgroundEnabledColor = builder.mNavigationButtonsBackgroundEnabledColor;
    mNavigationButtonsBackgroundDisabledColor = builder.mNavigationButtonsBackgroundDisabledColor;
    mNavigationButtonsFontColor = builder.mNavigationButtonsFontColor;
  }

  public Integer getHintColor() {
    return mHintColor;
  }

  public Integer getFontColor() {
    return mFontColor;
  }

  public Integer getNavigationBarColor() {
    return mNavigationBarColor;
  }

  public Integer getNavigationButtonsBackgroundEnabledColor() {
    return mNavigationButtonsBackgroundEnabledColor;
  }

  public Integer getNavigationButtonsBackgroundDisabledColor() {
    return mNavigationButtonsBackgroundDisabledColor;
  }

  public Integer getNavigationButtonsFontColor() {
    return mNavigationButtonsFontColor;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int flags) {
    parcel.writeValue(mFontColor);
    parcel.writeValue(mNavigationBarColor);
    parcel.writeValue(mNavigationButtonsBackgroundEnabledColor);
    parcel.writeValue(mNavigationButtonsBackgroundDisabledColor);
    parcel.writeValue(mNavigationButtonsFontColor);
    parcel.writeValue(mHintColor);
  }

  public static class Builder {
    private Integer mHintColor = null;
    private Integer mFontColor = null;
    private Integer mNavigationBarColor = null;
    private Integer mNavigationButtonsBackgroundEnabledColor = null;
    private Integer mNavigationButtonsBackgroundDisabledColor = null;
    private Integer mNavigationButtonsFontColor = null;

    /**
     * Sets the color of the Feedback hint text
     */
    public Builder setHintColor(int hintColor) {
      mHintColor = hintColor;
      return this;
    }

    /**
     * Sets the color of the Feedback text
     */
    public Builder setFontColor(int fontColor) {
      mFontColor = fontColor;
      return this;
    }

    /**
     * Sets the color of the
     */
    public Builder setNavigationBarColor(int navigationBarColor) {
      mNavigationBarColor = navigationBarColor;
      return this;
    }

    public Builder setNavigationButtonsBackgroundColors(int enabled, int disabled) {
      mNavigationButtonsBackgroundEnabledColor = enabled;
      mNavigationButtonsBackgroundDisabledColor = disabled;
      return this;
    }

    public Builder setNavigationButtonsFontColor(int navigationButtonsFontColor) {
      mNavigationButtonsFontColor = navigationButtonsFontColor;
      return this;
    }

    public FeedbackCustomStyle build() {
      return new FeedbackCustomStyle(this);
    }
  }
}


