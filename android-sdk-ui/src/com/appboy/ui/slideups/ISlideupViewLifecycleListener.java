package com.appboy.ui.slideups;

import android.view.View;

import com.appboy.models.Slideup;

public interface ISlideupViewLifecycleListener {
  /**
   * Lifecycle method that gets executed before the slideup View is added to the root layout.
   */
  void beforeOpened(View slideupView, Slideup slideup);

  /**
   * Lifecycle method that gets executed after the slideup View has been added to the root layout
   * (and the slide in animation has completed).
   */
  void afterOpened(View slideupView, Slideup slideup);

  /**
   * Lifecycle method that gets executed before the slideup View is removed (and before any closing
   * animation starts).
   */
  void beforeClosed(View slideupView, Slideup slideup);

  /**
   * Lifecycle method that gets executed after the slideup View has been removed from the root
   * layout (and the slide out animation has completed).
   */
  void afterClosed(Slideup slideup);

  /**
   * Method that gets executed when the slideup View is clicked.
   */
  void onClicked(SlideupCloser slideupCloser, View slideupView, Slideup slideup);

  /**
   * Method that gets executed when the slideup View is dismissed.
   */
  void onDismissed(View slideupView, Slideup slideup);
}
