package com.appboy.ui.slideups;

import android.app.Activity;
import android.view.View;

import com.appboy.models.Slideup;

public interface ISlideupViewFactory {
  /**
   * This method should either inflate or programmatically create a new View that will be used
   * to display a slideup. A new View must be created on every call to createSlideupView. This
   * prevents the memory leak that would occur if the View was shared by multiple Activity
   * classes.
   *
   * Note: Do not add a click/touch listeners directly to the view. They will be ignored. Instead,
   * use an ISlideupManagerListener to perform custom logic.
   */
  View createSlideupView(Activity activity, Slideup slideup);
}
