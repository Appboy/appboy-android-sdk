package com.appboy.sample;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.appboy.models.Slideup;
import com.appboy.ui.slideups.ISlideupViewFactory;

public class CustomSlideupViewFactory implements ISlideupViewFactory {
  @Override
  public View createSlideupView(Activity activity, Slideup slideup) {
    View slideupView = activity.getLayoutInflater().inflate(R.layout.custom_slideup, null);
    TextView message = (TextView) slideupView.findViewById(R.id.slideup_message);
    message.setText(slideup.getMessage());
    return slideupView;
  }
}
