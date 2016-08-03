package com.appboy.sample.logging;

import android.content.Context;
import android.util.AttributeSet;
import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;
import com.appboy.sample.R;

public class CustomEventDialog extends CustomLogger {

  public CustomEventDialog(Context context, AttributeSet attributeSet) {
    super(context, attributeSet, R.layout.custom_event);
  }

  @Override
  protected boolean customLog(String name, AppboyProperties properties) {
    return Appboy.getInstance(getContext()).logCustomEvent(name, properties);
  }
}
