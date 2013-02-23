package com.appboy.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.appboy.Constants;
import com.appboy.ui.R;
import com.appboy.models.results.Slideup;

public class SlideupView extends LinearLayout {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY, SlideupView.class.getName());

  private final TextView mMessage;

  public SlideupView(Context context, Slideup slideup) {
    super(context);

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.slideup, this);

    mMessage = (TextView) findViewById(R.id.message);
    mMessage.setText(slideup.getMessage());
  }
}
