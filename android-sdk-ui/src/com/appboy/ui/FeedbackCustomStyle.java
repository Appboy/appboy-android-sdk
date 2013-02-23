package com.appboy.ui;

/**
 * A model class that contains the custom styling for the Feedback form.
 *
 * To create a custom style, use the FeedbackCustomStyle.Builder class to override any properties with custom
 * style values. All custom style attributes are optional.
 */
// TODO(martin) - After seeing this approach in action, I realize that it's very restrictive. We should consider
//                allowing users define a custom theme instead.
public class FeedbackCustomStyle {
  private final Integer mFontColor;
  private final Integer mNavigationBarColor;
  private final Integer mNavigationButtonsBackgroundEnabledColor;
  private final Integer mNavigationButtonsBackgroundDisabledColor;
  private final Integer mNavigationButtonsFontColor;
  private final Integer mHintColor;

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

  public static class Builder {
    private Integer mHintColor = null;
    private Integer mFontColor = null;
    private Integer mNavigationBarColor = null;
    private Integer mNavigationButtonsBackgroundEnabledColor = null;
    private Integer mNavigationButtonsBackgroundDisabledColor = null;
    private Integer mNavigationButtonsFontColor = null;

    public Builder setHintColor(int hintColor) {
      mHintColor = hintColor;
      return this;
    }

    public Builder setFontColor(int fontColor) {
      mFontColor = fontColor;
      return this;
    }

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


