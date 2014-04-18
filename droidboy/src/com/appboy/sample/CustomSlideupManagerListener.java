package com.appboy.sample;

import android.app.Activity;
import android.widget.Toast;
import com.appboy.models.Slideup;
import com.appboy.ui.slideups.ISlideupManagerListener;
import com.appboy.ui.slideups.SlideupCloser;
import com.appboy.ui.slideups.SlideupOperation;

public class CustomSlideupManagerListener implements ISlideupManagerListener {
  private final Activity mActivity;

  public CustomSlideupManagerListener(Activity activity) {
    mActivity = activity;
  }

  @Override
  public boolean onSlideupReceived(Slideup slideup) {
    return false;
  }

  @Override
  public SlideupOperation beforeSlideupDisplayed(Slideup slideup) {
    return SlideupOperation.DISPLAY_NOW;
  }

  @Override
  public boolean onSlideupClicked(Slideup slideup, SlideupCloser slideupCloser) {
    Toast.makeText(mActivity, "The click was ignored.", Toast.LENGTH_LONG).show();
    slideupCloser.close(true);
    return true;
  }

  @Override
  public void onSlideupDismissed(Slideup slideup) {
    Toast.makeText(mActivity, "The slideup was dismissed.", Toast.LENGTH_LONG).show();
  }
}
