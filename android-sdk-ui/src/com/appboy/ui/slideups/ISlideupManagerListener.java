package com.appboy.ui.slideups;

import com.appboy.models.Slideup;

public interface ISlideupManagerListener {
  boolean onSlideupReceived(Slideup slideup);
  SlideupOperation beforeSlideupDisplayed(Slideup slideup);
  boolean onSlideupClicked(Slideup slideup, SlideupCloser slideupCloser);
  void onSlideupDismissed(Slideup slideup);
}
