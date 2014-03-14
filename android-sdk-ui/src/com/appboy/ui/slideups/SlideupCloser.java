package com.appboy.ui.slideups;

/**
 * A delegate method class used to close the currently displayed slideup.
 */
public class SlideupCloser {
  private SlideupViewWrapper mSlideupViewWrapper;

  public SlideupCloser(SlideupViewWrapper slideupViewWrapper) {
    mSlideupViewWrapper = slideupViewWrapper;
  }

  public void close(boolean animate) {
    if (animate) {
      mSlideupViewWrapper.getSlideup().setAnimateOut(true);
    } else {
      mSlideupViewWrapper.getSlideup().setAnimateOut(false);
    }
    mSlideupViewWrapper.close();
  }
}
